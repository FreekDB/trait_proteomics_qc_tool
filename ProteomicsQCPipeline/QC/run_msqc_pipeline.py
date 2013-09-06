"""
Main CTMM QC Module that runs the NIST metrics pipeline as well as generating
graphics using R and combining all gathered data into an HTML report.
"""

from os import makedirs
from os.path import normpath, splitext, isdir, split, join
from parse_metrics import create_metrics, export_metrics_json
from pkg_resources import resource_filename # @UnresolvedImport
from shutil import move, copy
from string import Template
from subprocess import check_call
from subprocess import check_output
from time import gmtime, strftime, time
from datetime import datetime
import logging as log
import shutil
import os
import tempfile
import glob
import subprocess

__author__ = "Marcel Kempenaar"
__contact__ = "brs@nbic.nl"
__copyright__ = "Copyright, 2012, Netherlands Bioinformatics Centre"
__license__ = "MIT"

# Globals
_R_GRAPHICS = resource_filename(__name__, 'r_ms_graphics.R')

# Paths (These should be adapted for the system they run on)
# Location of the 'ctmm' subfolder in the Apache 'htdocs' folder
_REPORT_DIR = normpath('C:/qc-data/QCArchive27Feb/archive/htdocs/ctmm')
# QC progress log used to log all processed RAW files (by default located within the _REPORT_DIR)
_PROGRES_LOG = normpath('{0}/{1}'.format(_REPORT_DIR, 'qc_status.log'))

# Root folder for the QC pipeline
_QC_HOME = normpath('C:/qc-data/QCArchive27Feb/archive/QC')
# NIST pipeline folder (by default located within _QC_HOME)
# _NIST = normpath('NISTMSQCv1_2_0_CTMM')
_NIST = normpath('NISTMSQCv1_5_0')
# QuaMeter installation folder (by default located within _QC_HOME)
_QUAMETER = normpath('quameter-bin-windows-x86-vc100-release-1_1_91')

def qc_pipeline(indir, outdir, copylog):
    """Checks input directory for new RAW files to analyze, keeping track
    of all processed files. Once a new RAW file has been placed in this directory
    a report will be generated with this file as input.
    
    If copylog is None, get a listing of all RAW files in indir and process
    them separately
    @param indir: directory containing RAW files to process
    @param outdir: directory used to store temporary files
    @param copylog: Robocopy log file location or None
    """

    log.info('Version 0.2.0.qc-lite')

    # Get a list of all RAW files to process
    files = _get_filelist(indir, copylog)
    for rawfile, status in files.iteritems():
        if status != 'new':
            continue

        log.info("-----------\nProcessing:\n\t", rawfile, "\n-----------\n")
        ## For calculating runtimes
        t_start = time()
        _log_progress(_PROGRES_LOG, rawfile, 'running')

        # Set for current file and move file to output directory
        basename = splitext(rawfile)[0]
        working_dir = tempfile.mkdtemp(suffix='_QC', prefix=basename, dir=outdir)
        # The following lines set the path to the RAW file to its original location
        original_path = normpath('{0}/{1}'.format(indir, rawfile))
        abs_rawfile_path = original_path
        # Use the following lines to copy the RAW files to a different path before processing
        #abs_rawfile_path = normpath('{0}/{1}'.format(working_dir, rawfile))
        #copy(original_path, abs_rawfile_path)
        #Copy rawfile to inputfile
        abs_inputfile_path = working_dir + '\\' + rawfile
        print("Indir is ", indir)
        print("Outdir is ", outdir)
        print("Working_dir is ", working_dir)
        try:
            time1 = datetime.now()
            print("@Stage 1: Preparation: Copying rawfile to newinputfilename ", abs_rawfile_path, abs_inputfile_path, time1)   
            shutil.copy(abs_rawfile_path, abs_inputfile_path)
            print("Input file is ", abs_inputfile_path)
            time2 = datetime.now()
            print("@Completed Stage 1 copying ", time2 - time1)  
            output = ("MSQC pipeline processing successfully completed for file ", abs_rawfile_path)
            time3 = datetime.now()
            print("@Stage 2: Preparation: Creating folder to write QC report in html form", time3)   
            # Create folder to contain html report
            webdir = _manage_paths(basename) 
            print("Report folder is ", webdir) 
            time4 = datetime.now()
            print("@Completed Stage 2 MakingReportFolder ", time4 - time3)
            # Run QC workflow - for performance improvement, use abs_inputfile_path instead of abs_rawfile_path
            time5 = datetime.now()
            print("@Stage 3: Running NIST pipeline for QC-Full", time5) 
            _run_nist(working_dir, abs_inputfile_path, working_dir)
            time6 = datetime.now()
            print("@Completed Stage 3 NISTPipeline ", time6 - time5) 
            time7 = datetime.now()
            print("@Stage 4: Running OPLReader program on mzXML file for TIC analysis", time7)             
            _run_r_script(working_dir, webdir, basename)
            time8 = datetime.now()
            print("@Stage 4 OPLReader program completed. ", time8 - time7)          
            time9 = datetime.now()
            print("@Stage 5: Running QuaMeter IDFree mode on the RAW file", time9) 
            _run_quameter_idfree(abs_inputfile_path, working_dir)
            time10 = datetime.now()
            print("@Completed Stage 5 QuaMeterIDFree", time10 - time9) 
            time11 = datetime.now()
            print("@Stage 6: Calculating QC metrics values", time11)  
            metrics = create_metrics(working_dir, abs_inputfile_path, t_start) 
            time12 = datetime.now()
            print("@Completed Stage 6 MetricsCalculation ", time12 - time11) 
            time13 = datetime.now()
            print("@Stage 7: Creating metrics report in ", webdir, time13)  
            _create_report(webdir, basename, metrics)  
            time14 = datetime.now()
            print("@Completed Stage 7 MetricsReport ", time14 - time13) 
            #TODO: Selectively cleanup mzXML and other files
            time15 = datetime.now()
            print("@Stage 8: Cleanup copied RAW data file from ", abs_inputfile_path, time15)    
            #Cleanup the abs_inputfile_path 
            os.remove(abs_inputfile_path) 
            # Cleanup (remove everything in working directory)
            _cleanup(working_dir)
            time16 = datetime.now()
            print("@Completed Stage 8 Cleanup ", time16 - time15)
            print("@Total processing time (days seconds microseconds)", time16 - time1)
        except subprocess.CalledProcessError, e:
            print("@error : ", e.output, datetime.now())
            output = e.output
        except IOError as e:
            print ("I/O error({0}): {1}".format(e.errno, e.strerror), datetime.now())
            output = e.strerror
        print("@output : ", output)
        # Update log-file showing completed analysis
        _log_progress(_PROGRES_LOG, rawfile, 'completed')

def _get_filelist(indir, copylog):
    """
    Check status of all previously processed files to determine
    if any new files are present to process
    @param indir: directory containing the RAW file(s)
    @param copylog: Robocopy log file, None if running in offline mode
    """
    # Read list of already processed RAW files
    files = _read_logfile(_PROGRES_LOG)
    if copylog == None:
        # Check indir for any new files
        new_files = glob.glob('{}/*.[Rr][Aa][Ww]'.format(indir))
        files = dict((split(rawfile)[1], 'new') for rawfile in new_files if split(rawfile)[1] not in files)
    else:
        files = _parse_robocopy_log(copylog, files)

    if not files:
        log.warning('No files to process')
        return

    return files


def _read_logfile(logfile):
    """
    Parse our own log-file of previously processed files. Return dictionary mapping filename to status.
    @param logfile: QC log file listing current status and previously processed RAW files
    """
    # QC Log file layout:
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


def _parse_robocopy_log(copylog, files):
    """ 
    Check Robocopy logfile for new files copied
    @param copylog: Robocopy log file
    @param files: dictionary with already detected and processed RAW files
    """
    with open(copylog, 'r') as logfile:
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
    @param status: current status of the file (i.e. 'running', 'finished')
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
    print _REPORT_DIR
    tree = normpath('{root}/{year}/{month}/{base}'.format(root=_REPORT_DIR,
                                                          year=strftime("%Y", ctime),
                                                          month=strftime("%b", ctime),
                                                          base=basename))
    if not isdir(tree):
        makedirs(tree)
    return tree


def _run_nist(indir, rawfile, outdir):
    '''
    Starts the NIST metrics workflow using a RAW file as input. Reads parameters
    from a configuration file to pass as arguments to the workflow.
    @param rawfile: path to the RAW file
    @param outdir: folder used for storing intermediate results
    '''
    log.info("Running NIST..")

    # NIST settings
    #nist_library = 'jurkat'
    #nist_library = 'humanqtof'
    #nist_library = 'jurkat28'
    #nist_library = 'jurkathuman01'
    #nist_library = 'Jurkat28new095'
    #nist_library = 'Speclib0_5'
    #nist_library = 'Speclib0_6'
    nist_library = 'Jurkat28new0.999'
    #nist_library = 'Jurkat28_0.8m'
    search_engine = 'SpectraST'
    mode = 'full'
    fasta = normpath('{0}/libs/{1}.fasta'.format(_NIST, nist_library))
    instrument = 'Orbi_HCD'

    # Run NIST pipeline
    # TODO error handling
    # TODO 'nistms_metrics.exe' tends to hang if something is wrong, process should resume after
    # a set amount of time (also, it can produce an empty metrics file)
    nist_exe = normpath('{0}/scripts/run_NISTMSQC_pipeline.pl'.format(_NIST))
    nist_cmd = [nist_exe,
                '--in_dir', indir,
                '--out_dir', outdir,
                '--library', nist_library,
                '--instrument_type', instrument,
                '--search_engine', search_engine,
                '--fasta', fasta,
                '--overwrite_searches',
                '--pro_ms',
                '--log_file',
                '--mode', mode,
                '--updated_converter']
    print nist_cmd
    check_call(nist_cmd, shell=True)

    # Rename .msqc metrics file here, as it assumes the name of the containing folder,
    # which is now some random file path
    foldername = split(outdir)[1]
    rawfilename = split(splitext(rawfile)[0])[1]
    msqc_original = join(outdir, foldername + '_report.msqc')
    msqc_destination = join(outdir, rawfilename + '.msqc')
    print ("Renaming original report file ", msqc_original, " to desired report file name ", msqc_destination, "\n")
    move(msqc_original, msqc_destination)

def _run_quameter_idfree(rawfile, outdir):
    '''
    Starts the QuaMeter IDFree mode using a RAW file as input. 
    http://forge.fenchurch.mc.vanderbilt.edu/scm/viewvc.php/*checkout*/trunk/doc/Manual.pdf?root=quameter
    @param rawfile: path to the RAW file
    @param outdir: folder used for storing intermediate QuaMeter IDFree results
    '''
    log.info("Running QuaMeter IDFree..")

    # QuaMeter settings
    mode = 'idfree'
    # QuaMeter output file
    rawfilename = split(splitext(rawfile)[0])[1]
    quameter_output_file = join(outdir, rawfilename + '_quametermetrics.tsv')

    # Run QuaMeter IDFree mode
    # TODO error handling
    quameter_exe = normpath('{0}/quameter.exe'.format(_QUAMETER))
    quameter_idfree_cmd = [quameter_exe,
                rawfile,
                '-MetricsType', mode,
                '-OutputFilepath', quameter_output_file,
                '-cpus', '1']
    print quameter_idfree_cmd
    check_call(quameter_idfree_cmd, shell=True)

def _run_r_script(outdir, webdir, basename):
    '''
    After running the NIST metrics workflow, the mzXML file created can be read in R
    and processed further (some graphics and basic metrics)
    @param outdir: directory in which the input mzXML file is located
    @param webdir: output directory for the graphics
    @param basename: RAW file name (without ext) used to identify mzXML file
    '''
    log.info("Running oplReader..")
    oplreaderjar = normpath('{0}/oplreader.jar'.format(_QC_HOME))
    # Execute oplreader
    rcmd = ['java',
            '-jar',
            oplreaderjar,
            outdir,
            basename,
            webdir]  
    print rcmd
    check_call(rcmd, shell=True)

def _create_report(webdir, basename, metrics):
    '''
    Writes the metrics values to a json file. 
    @param webdir: location in which the report (index.html) should be placed
    @param basename: RAW filename (without ext) to identify currently processed file
    @param metrics: dictionary holding all metrics (generic)
    '''
    log.info("Exporting metrics to a json file..")
    # Store NIST and QuaMeter metrics together with the report
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
    #mzxml_cmd = '{0} {1} -o {2} --mzXML -e .RAW.mzXML --filter "peakPicking true 1"'.format(msconvert, raw_file, outdir)
    #Experimenting with 32 bit msconvert option
    mzxml_cmd = '{0} {1} -o {2} --mzXML -e .RAW.mzXML --32 --filter "peakPicking true 1"'.format(msconvert, raw_file, outdir)
    mgf_cmd = [msconvert,
               raw_file,
               '-o', outdir,
               '--mgf',
               '-e', '.RAW.MGF']

    # Execute msconvert for both output files
    log.info('\tConverting to mzXML..')
    #check_call(mzxml_cmd)
    #Experimenting with check_output
    check_output(mzxml_cmd)
    #log.info('\tConverting to MGF..')
    #check_call(mgf_cmd)

def _cleanup(outdir):
    '''
    Cleans up temporary data (NIST output etc) after a successful run
    @param outdir: working directory to remove in which all intermediate files are stored
    '''
    shutil.rmtree(outdir)
