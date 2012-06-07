"""
Main CTMM QC Module that runs the NIST metrics pipeline as well as generating
graphics using R and combining all gathered data into an HTML report.
"""

from os import makedirs
from os.path import normpath, splitext, isdir
from parse_metrics import create_metrics, export_metrics_json
from pkg_resources import resource_filename # @UnresolvedImport
from shutil import move, copy
from string import Template
from subprocess import check_call
from time import gmtime, strftime, time
from datetime import datetime
import logging as log
import os.path
import shutil
import tempfile

__author__ = "Marcel Kempenaar"
__contact__ = "brs@nbic.nl"
__copyright__ = "Copyright, 2012, Netherlands Bioinformatics Centre"
__license__ = "MIT"

# Globals
_R_GRAPHICS = resource_filename(__name__, 'r_ms_graphics.R')

# Paths (These should be adapted for the system they run on)
_WEB_DIR = normpath('C:/Program Files (x86)/Apache Software Foundation/Apache2.2/htdocs/ctmm')
_NIST = normpath('NISTMSQCv1_2_0_CTMM')
_PROGRES_LOG = normpath('{0}/{1}'.format(_WEB_DIR, 'qc_status.log'))
_QC_HOME = normpath('C:/QC-pipeline')


def qc_pipeline(indir, out_dir, copy_log):
    """Checks input directory for new RAW files to analyze, keeping track
    of all processed files. Once a new RAW file has been placed in this directory
    a report will be generated with this file as input."""

    log.info('Version 0.1.0')

    # Check status of all previously processed files to determine
    # if any new files are present to process
    files = _read_logfile(_PROGRES_LOG)
    files = _parse_robocopy_log(copy_log, files)

    if not files:
        log.warning('No files to process')
        return

    for rawfile, status in files.iteritems():
        if status != 'new':
            continue

        log.info("-----------\nProcessing:\n\t", rawfile, "\n-----------\n")
        ## For calculating runtimes
        t_start = time()
        _log_progress(_PROGRES_LOG, rawfile, 'running')

        # Set for current file and move file to output directory
        basename = splitext(rawfile)[0]
        working_dir = tempfile.mkdtemp(suffix='_QC', prefix=basename, dir=out_dir)

        original_path = normpath('{0}/{1}'.format(indir, rawfile))
        abs_rawfile_path = original_path
        #abs_rawfile_path = normpath('{0}/{1}'.format(working_dir, rawfile))
        #copy(original_path, abs_rawfile_path)

        #Create folder to contain html report
        webdir = _manage_paths(basename)
        # Run QC workflow
        _raw_format_conversions(abs_rawfile_path, working_dir)
        #_run_nist(abs_rawfile_path, working_dir)
        _run_r_script(working_dir, webdir, basename)
        metrics = create_metrics(working_dir, abs_rawfile_path, t_start)
        _create_report(webdir, basename, metrics)

        # Update log-file showing completed analysis
        _log_progress(_PROGRES_LOG, rawfile, 'completed')

        # Cleanup (remove everything in working directory)
        _cleanup(working_dir)


def _read_logfile(logfile):
    """
    Parse our own log-file of previously processed files. Return dictionary mapping filename to status.
    @param logfile: QC log file listing current status and previously processed RAW files
    """
    # Log file layout:
    # Date    Filename    Status
    files = dict()
    with open(logfile, 'r') as logfile:
        # Discard header
        _ = logfile.next()
        for line in logfile:
            #Split line
            splitted = line.split('\t')
            if len(splitted) != 3:
                continue

            #Get filename and status
            timestamp, name, status = splitted
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
    for lnr_start, logline in enumerate(loglines):
        if 'Started' not in logline:
            continue
        # We found the start of a new robocopy log
        for lnr_block, logline in enumerate(loglines[lnr_start:]):
            if 'Monitor' not in logline:
                continue
            # All files completed between lnr_start & lnr_start + lnr_block finished copying
            for logline in loglines[lnr_start:lnr_block + lnr_start]:
                if 'New File' not in logline:
                    continue
                # Handle new files
                newfile = logline.split('\t')[-1].strip()
                # If a new file has been found, set status to 'new'
                if newfile not in files:
                    files[newfile] = 'new'
            break
    return files


def _log_progress(logfile, rawfile, status):
    '''
    Keeps track of processed RAW files, this log file is used to create a
    simple status report through a web server.
    @param logfile: log file used to log status of processed files
    @param rawfile: path to the RAW file
    '''
    log = '{0}\t{1}\t{2}\n'.format(str(datetime.now()), rawfile, status)
    with open(logfile, 'a') as status_log:
        status_log.write(log)


def _manage_paths(basename):
    '''
    Create directory on web server part for storing images and the report.
    @param basename: name of the .RAW file without extension
    '''
    ctime = gmtime()
    tree = normpath('{root}/{year}/{month}/{base}'.format(root=_WEB_DIR,
                                                                year=strftime("%Y", ctime),
                                                                month=strftime("%b", ctime),
                                                                base=basename))
    if not isdir(tree):
        makedirs(tree)
    return tree


def _run_nist(rawfile, outdir):
    '''
    Starts the NIST metrics workflow using a RAW file as input. Reads parameters
    from a configuration file to pass as arguments to the workflow.
    @param rawfile: path to the RAW file
    @param outdir: folder used for storing intermediate results
    '''
    log.info("Running NIST..")

    # NIST settings
    nist_library = 'hsa'
    search_engine = 'MSPepSearch' #'SpectraST'
    mode = 'lite'
    fasta = normpath('{0}/libs/{1}.fasta'.format(_NIST, nist_library))
    instrument = 'LTQ'

    # Run NIST pipeline
    # TODO error handling
    # TODO 'nistms_metrics.exe' tends to hang if something is wrong, process should resume after
    # a set amount of time (also, it can produce an empty metrics file)
    nist_exe = normpath('{0}/scripts/run_NISTMSQC_pipeline.pl'.format(_NIST))
    nist_cmd = [nist_exe,
                '--in_dir', rawfile,
                '--out_dir', outdir,
                '--library', nist_library,
                '--instrument_type', instrument,
                '--search_engine', search_engine,
                '--fasta', fasta,
                '--overwrite_searches',
                '--pro_ms',
                '--log_file',
                '--mode', mode]
    check_call(nist_cmd, shell=True)

    # Rename .msqc metrics file here, as it assumes the name of the containing folder,
    # which is now some random file path
    foldername = os.path.split(outdir)[1]
    rawfilename = os.path.split(os.path.splitext(rawfile)[0])[1]
    msqc_original = os.path.join(outdir, foldername + '_report.msqc')
    msqc_destination = os.path.join(outdir, rawfilename + '.msqc')
    move(msqc_original, msqc_destination)


def _run_r_script(outdir, webdir, basename):
    '''
    After running the NIST metrics workflow, the mzXML file created can be read in R
    and processed further (some graphics and basic metrics)
    @param outdir: directory in which the input mzXML file is located
    @param webdir: output directory for the graphics
    @param basename: RAW file name (without ext) used to identify mzXML file
    '''
    log.info("Creating Graphics..")

    # Execute Rscript
    rcmd = ['Rscript',
            _R_GRAPHICS,
            outdir,
            basename,
            webdir,
            '1']  # MS level (1 or 2)

    check_call(rcmd, shell=True)


def _create_report(webdir, basename, metrics):
    '''
    Uses the report template in combination with the metrics to construct a report
    @param webdir: location in which the report (index.html) should be placed
    @param basename: RAW filename (without ext) to identify currently processed file
    @param metrics: dictionary holding all metrics (generic)
    '''
    log.info("Creating report..")

    with open(normpath('templates/report.html'), 'r') as report_template:
        template = report_template.readlines()

    report_template = Template(''.join(template))
    report_updated = report_template.safe_substitute(raw_file='{0}.RAW'.format(basename),
                                                     date=metrics['generic']['date'],
                                                     runtime=metrics['generic']['runtime'],
                                                     # Figures
                                                     heatmap_img='{0}_heatmap.png'.format(basename),
                                                     ions_img='{0}_ions.png'.format(basename),
                                                     heatmap_pdf='{0}_heatmap.pdf'.format(basename),
                                                     ions_pdf='{0}_ions.pdf'.format(basename))

    # Write report file to directory holding all reports for this month
    with open(normpath('{0}/index.html'.format(webdir)), 'w') as report_html:
        report_html.writelines(report_updated)

    # Store NIST metrics together with the report
    export_metrics_json(metrics, webdir)


def _raw_format_conversions(raw_file, outdir):
    '''
    Converts the .RAW file both to .mzXMl and to .MGF for compatibility with other tools.
    @param raw_file: the RAW file to process
    @param outdir: directory in which to place output mzXML and MGF files
    '''
    log.info("Running RAW file conversion..")

    msconvert = normpath('{0}/{1}/converter/msconvert.exe'.format(_QC_HOME, _NIST))
    # Both mzXML and MGF files need to be created
    """
    mzxml_cmd = [msconvert,
                 raw_file,
                 '-o', outdir,
                 '--mzXML',
                 '-e', '.RAW.mzXML',
                 '--filter',
                 '"peakPicking true 1"']
    """
    mzxml_cmd = '{0} {1} -o {2} --mzXML -e .RAW.mzXML --filter "peakPicking true 1"'.format(msconvert, raw_file, outdir)
    mgf_cmd = [msconvert,
               raw_file,
               '-o', outdir,
               '--mgf',
               '-e', '.RAW.MGF']

    # Execute msconvert for both output files
    log.info('\tConverting to mzXML..')
    check_call(mzxml_cmd)
    log.info('\tConverting to MGF..')
    check_call(mgf_cmd)


def _cleanup(outdir):
    '''
    Cleans up temporary data (NIST output etc) after a successful run
    @param outdir: working directory to remove in which all intermediate files are stored
    '''
    shutil.rmtree(outdir)
