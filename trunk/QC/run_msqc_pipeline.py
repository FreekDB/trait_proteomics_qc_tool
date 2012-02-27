"""
TODO Module to.. 
"""

from os import makedirs
from os.path import normpath, splitext, isdir
from parse_metrics import create_metrics
from shutil import move, copy # move
from string import Template
from subprocess import Popen, PIPE
from time import gmtime, strftime, time
import logging
import os.path
import tempfile

# Globals
_R_GRAPHICS = 'r_ms_graphics.R'

# Paths (These should be adapted for the system they run on)
_WEB_DIR = normpath('C:/Program Files (x86)/Apache Software Foundation/Apache2.2/htdocs/ctmm')
_NIST = normpath('C:/ctmm/NISTMSQCv1_2_0_CTMM')
_PROGRES_LOG = 'qc_status.log'
_QC_HOME = normpath('C:/ctmm/')


def qc_pipeline(indir, out_dir, copy_log):
    """Checks input directory for new RAW files to analyze, keeping track
    of all processed files. Once a new RAW file has been placed in this directory
    a report will be generated with this file as input."""

    print 'Version 0.0.6f'

    files = _read_logfile(_PROGRES_LOG)
    files = _parse_robocopy_log(copy_log, files)

    if not files:
        print "No files to process"
        return

    for rawfile, status in files.iteritems():
        if status != 'new':
            continue

        print "-----------\nProcessing:\n\t", rawfile, "\n-----------\n"
        ## For calculating runtimes
        t_start = time()
        # Set for current file and move file to output directory
        basename = splitext(rawfile)[0]
        working_dir = tempfile.mkdtemp(suffix='_QC', prefix=basename, dir=out_dir)

        # TODO instead of moving the file, find out if NIST accepts a (single) file as input
        # instead of a directory containing *.RAW files.
        original_path = normpath('{0}/{1}'.format(indir, rawfile))
        abs_rawfile_path = normpath('{0}/{1}'.format(working_dir, rawfile))
        copy(original_path, abs_rawfile_path)

        #Create folder to contain html report
        webdir = _manage_paths(basename)

        files[rawfile] = 'processing'

        #TODO Change all print statements to use logging
        # Run QC workflow
        print "Running RAW file conversion.."
        _raw_format_conversions(abs_rawfile_path, working_dir)
        print "Running NIST.."
        _run_nist(abs_rawfile_path, working_dir)
        print "Creating Graphics.."
        _run_r_script(working_dir, webdir, basename)
        print "Creating metrics.."
        metrics = create_metrics(abs_rawfile_path, t_start)
        print "Creating report.."
        _create_report(webdir, basename, metrics)

        # Once completed, update status and logfile
        # TODO Test threading using multiple robocopy instances to test if this is necessary
        files[rawfile] = 'completed'

        # Update logfile showing completed analysis
        _log_progress(_PROGRES_LOG, rawfile)


def _read_logfile(logfile):
    """
    Parse our own logfile of previously processed files. Return dictionary mapping filename to status.
    @param logfile:
    """
    # Logfile layout:
    # Filename    Status
    files = dict()
    with open(logfile, 'r') as logfile:
        for line in logfile:
            #Split line
            splitted = line.split('\t')
            if len(splitted) != 2:
                continue

            #Get filename and status
            name, status = splitted
            files[name] = status.strip()

    return files


def _parse_robocopy_log(copy_log, files):
    """ Check Robocopy logfile for new files copied """
    with open(copy_log, 'r') as logfile:
        loglines = logfile.readlines()

    #Premise: Look between Started and Monitor lines for new files
    #  Started : Thu Jan 19 11:08:10 2012
    #
    #                       6    F:\Backup\BRS\BRS2011P09\Data\E2\
    #        New File           211.2 m    110215_13.RAW
    #
    #  Monitor : Waiting for 1 minutes and 1 changes...

    # Parsing a file 'block'
    # TODOs:
    #     - use regex
    #    - test with other valid file names
    for lnr_start, logline in enumerate(loglines):
        if 'Started' not in logline:
            continue

        #We found the start of a new robocopy log
        for lnr_block, logline in enumerate(loglines[lnr_start:]):
            if 'Monitor' not in logline:
                continue

            #All files completed between lnr_start & lnr_start + lnr_block finished copying
            for logline in loglines[lnr_start:lnr_block + lnr_start]:
                if 'New File' not in logline:
                    continue

                #Handle new files
                newfile = logline.split('\t')[-1].strip()
                # If a new file has been found, set status to 'new'
                if newfile not in files:
                    files[newfile] = 'new'
            break
    return files


def _log_progress(logfile, rawfile):
    """
    Keeps track of processed RAW files, this logfile is used to create a
    simple status report through a webserver.
    TODO add information (realtime update, completion date, etc.)
    """
    log = '{0}\t{1}'.format(rawfile, 'completed')
    with open(logfile, 'a') as status_log:
        status_log.writeln(log)


def _manage_paths(basename):
    """
    Create directory on webserver part for storing images and the report.
    @param basename: name of the .RAW file without extension
    """
    ctime = gmtime()
    tree = normpath('{root}/{year}/{month}/{base}'.format(root=_WEB_DIR,
                                                                year=strftime("%Y", ctime),
                                                                month=strftime("%b", ctime),
                                                                base=basename))
    if not isdir(tree):
        makedirs(tree)
    return tree


def _run_nist(rawfile, outdir):
    """Starts the NIST metrics workflow using a RAW file as input. Reads parameters
    from a configuration file to pass as arguments to the workflow."""
    # -WORKAROUND-
    # ReAd4W2Mascot is not working on the VM, using 'msconvert' for the conversion
    # of RAW to mzXML, which needs to be done manually as well as fixing the mzXML header

    logging.info("Running NIST pipeline..")
    nist_library = 'hsa'
    fasta = normpath('{0}/libs/{1}.fasta'.format(_NIST, nist_library))
    instrument = 'LTQ'

    # Run NIST pipeline
    # TODO: validate parameters, check if in- and out-dir can be the same
    nist_exe = normpath('perl {0}/scripts/run_NISTMSQC_pipeline.pl'.format(_NIST))
    nist_cmd = '{0} --in_dir {1} --out_dir {2} --library {3} --instrument_type {4} --fasta {5} {6} {7} {8} {9}'\
        .format(nist_exe,
                outdir,
                outdir,
                nist_library,
                instrument,
                fasta,
                '--overwrite_searches',
                '--pro_ms',
                '--log_file',
                '--mode lite')

    # TODO error handling
    # TODO 'nistms_metrics.exe' tends to hang if something is wrong, process should resume after a set amount of time
    _run_command(nist_cmd)

    #Rename .msqc file here, as it assumes the name of the containing folder, which is now some random file path
    foldername = os.path.split(outdir)[1]
    rawfilename = os.path.split(os.path.splitext(rawfile)[0])[1]
    msqc_original = os.path.join(outdir, foldername + '_report.msqc')
    msqc_destination = os.path.join(outdir, rawfilename + '.msqc')
    move(msqc_original, msqc_destination)


def _run_r_script(outdir, webdir, basename):
    '''
    After running the NIST metrics workflow, the mzXML file created can be read in R
    and processed further (graphics and basic metrics)
    @param outdir: directory in which the input mzXML file is located
    @param webdir: output directory for the graphics
    @param basename: RAW file name (without ext) used to identify mzXML file
    '''
    # Execute Rscript
    rcmd = 'Rscript {0} {1} {2} {3} {4}'.format(_R_GRAPHICS, outdir, basename, webdir, 1)
    _run_command(rcmd)


def _create_report(webdir, basename, metrics):
    '''
    Uses the report template in combination with the metrics to construct a report
    @param webdir: location in which the report (index.html) should be placed
    @param basename: RAW filename (without ext) to identify currently processed file
    @param metrics: dictionary holding all metrics (from NIST and generic)
    '''
    with open(normpath('templates/report.html'), 'r') as report_template:
        template = report_template.readlines()

    report_template = Template(''.join(template))
    report_updated = report_template.safe_substitute(# General
                                                     raw_file='{0}.RAW'.format(basename),
                                                     date=metrics['date'],
                                                     runtime=metrics['runtime'],
                                                     # Metrics
                                                     m_fs=metrics['f_size'],
                                                     m_ms1_scans=metrics['ms1_spectra'],
                                                     m_ms2_scans=metrics['ms2_spectra'],
                                                     m_f_ms1_rt=metrics['f_ms1_rt'],
                                                     m_l_ms1_rt=metrics['l_ms1_rt'],
                                                     m_m_p_w=metrics['m_p_w'],
                                                     m_i_i_t_ms1=metrics['i_i_t_ms1'],
                                                     m_i_i_t_ms2=metrics['i_i_t_ms2'],
                                                     m_p_c_pep=metrics['p_c_pep'],
                                                     m_p_c_ion=metrics['p_c_ion'],
                                                     m_p_c_ids=metrics['p_c_ids'],
                                                     # Figures
                                                     heatmap_img='{0}_heatmap.png'.format(basename),
                                                     ions_img='{0}_ions.png'.format(basename),
                                                     heatmap_pdf='{0}_heatmap.pdf'.format(basename),
                                                     ions_pdf='{0}_ions.pdf'.format(basename))

    # Write report file to directory holding all reports
    with open(normpath('{0}/index.html'.format(webdir)), 'w') as report_html:
        report_html.writelines(report_updated)


# --- Temporary (workaround) functions ---
def _raw_format_conversions(raw_file, outdir):
    '''
    Converts the .RAW file both to .mzXMl and to .MGF for compatibility with other tools.
    @param raw_file: the RAW file to process
    @param outdir: directory in which to place output mzXML and MGF files
    '''
    msconvert = normpath('{0}/converter/msconvert.exe'.format(_NIST))
    # Both mzXML and MGF files need to be created
    mzxml_cmd = '{0} {1} -o {2} --mzXML -e .RAW.mzXML'.format(msconvert, raw_file, outdir)
    mgf_cmd = '{0} {1} -o {2} --mgf -e .RAW.MGF'.format(msconvert, raw_file, outdir)

    # Execute msconvert for both output files
    print "\tConverting to mzXML.."
    _run_command(mzxml_cmd)
    print "\tConverting to MGF.."
    _run_command(mgf_cmd)


def _run_command(cmd):
    #TODO Explain to me (Tim) what this method has to offer over the standard check_call method?
    # Runs a single command, no output is returned
    run = Popen(cmd, stdout=PIPE, stderr=PIPE)
    err = run.communicate()[1]
    if run.returncode != 0:
        raise IOError(err)


def _cleanup():
    """ Cleans up temporary data (NIST output etc) after a successful run"""
    pass  # TODO implement clean up of working directory
