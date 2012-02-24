from argparse import ArgumentParser
from os import makedirs
from os.path import normpath, splitext, isdir
from parse_metrics import create_metrics
from shutil import copy  # move
from string import Template
from subprocess import Popen, PIPE
from time import gmtime, strftime, time
import random

# Globals
_R_GRAPHICS = 'r_ms_graphics.R'


# Paths (These should be adapted for the system they run on)
_WEB_DIR = normpath('C:/Program Files (x86)/Apache Software Foundation/Apache2.2/htdocs/ctmm')
_NIST = normpath('C:/ctmm/NISTMSQCv1_2_0_CTMM')
_PROGRES_LOG = 'qc_status.log'
_QC_HOME = normpath('C:/ctmm/')


def qc_pipeline(indir, out_dir, copy_log):
    print 'Version 0.0.6f'

    """Checks input directory for new RAW files to analyze, keeping track
    of all processed files. Once a new RAW file has been placed in this directory
    a report will be generated with this file as input."""
    files = _read_logfile(_PROGRES_LOG)
    files = _parse_robocopy_log(copy_log, files)

    if not files:
        print "No files to process"
        return

    for f, status in files.iteritems():
        if status != 'new':
            continue

        print "-----------\nProcessing:\n\t", f, "\n-----------\n"
        ## For calculating runtimes
        t_start = time()
        # Set for current file and move file to output directory
        basename = splitext(f)[0]
        dirname = '{0}_{1}_QC'.format(basename, int(random.random() * 10000))
        outdir = normpath('{0}/{1}'.format(out_dir, dirname))

        # Create folders for storing temp output as well as web output
        if not isdir(outdir):
            makedirs(outdir)

        # TODO: instead of moving the file, find out if NIST accepts a (single) file as input
        # instead of a directory containing *.RAW files.
        copy(normpath('{0}/{1}'.format(indir, f)), outdir)

        raw_file = normpath('{0}/{1}'.format(outdir, f))
        webdir = _manage_paths(basename, outdir)

        files[f] = 'processing'

        # Run QC workflow
        print "Running NIST.."
        _run_NIST(raw_file, outdir)
        print "Creating Graphics.."
        _run_R_script(outdir, webdir, basename)
        print "Creating metrics.."
        metrics = create_metrics(raw_file, dirname, t_start)
        print "Creating report.."
        _create_report(raw_file, webdir, basename, metrics)

        # Once completed, update status and logfile
        files[f] = 'completed'

        # Update logfile showing completed analysis
        _log_progress(_PROGRES_LOG, f)


def _read_logfile(logfile):
    # Logfile layout:
    # Filename    Status
    files = dict()
    with open(logfile, 'r') as logfile:
        for line in logfile:
            try:
                name, status = line.split('\t')[0:2]
                files[name] = status.strip()
            except:
                continue
    return files


def _parse_robocopy_log(copy_log, files):
    """ Check Robocopy logfile for new files copied """
    with open(copy_log, 'r') as logfile:
        log = logfile.readlines()

    # Parsing a file 'block'
    # TODOs:
    #     - use regex
    #    - test with other valid file names
    for i, j in enumerate(log):
        if 'Started' in j:
            for k, l in enumerate(log[i:]):
                if 'Monitor' in l:
                    for n in log[i:k + i]:
                        if 'New File' in n:
                            f = n.split("\t")[-1].split(' ')[-1].strip()
                            # If a new file has been found, set status to 'new'
                            if f not in files:
                                files[f] = 'new'
                    break
    return files


def _log_progress(logfile, rawfile):
    """Keeps track of processed RAW files, this logfile is used to create a
    simple status report through a webserver.
    TODO:
    - add information (realtime update, completion date, etc.)
    """
    log = "{0}\t{1}\n".format(rawfile, 'completed')
    with open(logfile, 'a') as f:
        f.write(log)
    pass


def _manage_paths(basename, outdir):
    # Create directory on webserver part for storing images and the report
    time = gmtime()
    tree = normpath('{root}/{year}/{month}/{base}'.format(root=_WEB_DIR,
                                                                year=strftime("%Y", time),
                                                                month=strftime("%b", time),
                                                                base=basename))
    if not isdir(tree):
        makedirs(tree)
    return tree


def _run_NIST(rawfile, outdir):
    """Starts the NIST metrics workflow using a RAW file as input. Reads parameters
    from a configuration file to pass as arguments to the workflow."""
    # -WORKAROUND-
    # ReAd4W2Mascot is not working on the VM, using 'msconvert' for the conversion
    # of RAW to mzXML, which needs to be done manually as well as fixing the mzXML header
    _run_msconvert(rawfile, outdir)  # DONE

    print "\tRunning NIST pipeline.."
    nist_library = 'hsa'
    fasta = normpath('{0}/libs/{1}.fasta'.format(_NIST, nist_library))
    instrument = 'LTQ'

    # Run NIST pipeline
    # TODO: validate parameters, check if in- and out-dir can be the same
    NIST_exe = normpath('perl {0}/scripts/run_NISTMSQC_pipeline.pl'.format(_NIST))
    NIST_cmd = '{0} --in_dir {1} --out_dir {2} --library {3} --instrument_type {4} --fasta {5} {6} {7} {8} {9}'.format(NIST_exe,
                outdir, outdir, nist_library, instrument, fasta, '--overwrite_searches', '--pro_ms', '--log_file', '--mode lite')
    #sys.exit("NIST_cmd: \n----------\n\n{0}\n\n".format(NIST_cmd))

    # TODOs:
    #     - error handling
    #     - 'nistms_metrics.exe' has a tendency to hang if something is wrong, process should
    #       resume after a set amount of time
    _run_command(NIST_cmd)


def _run_R_script(outdir, webdir, basename):
    """After running the NIST metrics workflow, the mzXML file created can be read in R
    and processed further (graphics and basic metrics)"""
    # Execute Rscript
    Rcmd = normpath('Rscript {0} "{1}/{2}.RAW.mzXML" "{3}" {4} "{5}"'.format(_R_GRAPHICS,
                                            # input mzXML
                                            outdir,
                                            basename,
                                            # output image filename prefix
                                            normpath('{0}/{1}'.format(webdir, basename)),
                                            # MSlevel
                                            1,
                                            # Logfile
                                            normpath('{0}/{1}.RLOG'.format(outdir, basename))))
    #print "R Command:\n\t", Rcmd
    _run_command(Rcmd)


def _create_report(rawfile, webdir, basename, metrics):
    # Place values and graphics in template HTML file
    with open(normpath('templates/report.html'), 'r') as f:
        template = f.readlines()

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
    with open(normpath('{0}/index.html'.format(webdir)), 'w') as f:
        f.writelines(report_updated)


# --- Temporary (workaround) functions ---
def _run_msconvert(raw_file, outdir):
    msconvert = normpath('{0}/converter/msconvert.exe'.format(_NIST))
    # Both mzXML and MGF files need to be created
    mzXML_cmd = '{0} {1} -o {2} --mzXML -e .RAW.mzXML'.format(msconvert, raw_file, outdir)
    MGF_cmd = '{0} {1} -o {2} --mgf -e .RAW.MGF'.format(msconvert, raw_file, outdir)

    # Execute msconvert for both output files
    print "\tConverting to mzXML.."
    _run_command(mzXML_cmd)
    print "\tConverting to MGF.."
    _run_command(MGF_cmd)


def _run_command(cmd):
    # Runs a single command, no output is returned
    run = Popen(cmd, stdout=PIPE, stderr=PIPE)
    err = run.communicate()[1]
    if run.returncode != 0:
        raise IOError(err)


def _cleanup():
    pass  # TODO implement clean up of working directory


if __name__ == "__main__":
    # Create and parse commandline arguments
    parser = ArgumentParser(description="QC-workflow monitor for MS data using NIST metrics")
    parser.add_argument('in_folder', type=str, help='Input folder containing (Thermo) RAW files outputted by a mass-spectrometer')
    parser.add_argument('out_folder', type=str, help='Folder in which output (report) PDF files will be written')
    parser.add_argument('copy_log', type=str, help='Logfile (local) that Robocopy uses to write status')

    args = parser.parse_args()
    qc_pipeline(args.in_folder, args.out_folder, args.copy_log)

""" *NOTES*

Robocopy command:
Mirror, 10 retries, waiting 30sec, monitor every 1min, display output, no progress, no job header, no job summary
-robocopy "source_path" "\\destination_path" / Mir / R:10 / W:30 / Log + :"\\log_path\logfile.txt" / mot:1 / tee / np / njh / njs
- Note: replace \mir with \e if removing files from source but need to be kept in destination

"""
