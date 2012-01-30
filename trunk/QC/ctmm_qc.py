from argparse import ArgumentParser
from time import gmtime, strftime
from string import Template
from subprocess import Popen, PIPE
from os.path import normpath, splitext, isdir
from os import makedirs
from shutil import move

#from watchdog.observers import Observer
#from watchdog.events import LoggingEventHandler
import random
import sys
import os
import re

# Globals
_R_GRAPHICS = 'r_ms_graphics.R'

# Compiles all metrics to extract from the NIST metrics output
# Each value is a list of 3 holding the line to match (to get the line number),
# the offset from that line that holds the value of interest and the regex to
# retrieve this value.
_METRICS = {
    # Experiment details
    #'ms1_scans': ['', 0, re.compile('')],
    #'ms2_scans': ['', 0, re.compile('')],
    'f_ms1_rt': ['First and Last MS1 RT', 1, re.compile('First MS1\s+([0-9\.]+)')],
    'l_ms1_rt': ['First and Last MS1 RT', 2, re.compile('Last MS1\s+([0-9\.]+)')],
    #'m_p_w': ['', 0, re.compile('')],
    # Ion details
    'i_i_t_ms1': ['Ion Injection Times for IDs', 1, re.compile('MS1 Median\s+([0-9\.]+)')],
    'i_i_t_ms2': ['Ion Injection Times for IDs', 3, re.compile('MS2 Median\s+([0-9\.]+)')],
    # Peptide details
    #'p_c_pep': ['', 0, re.compile('')],
    #'p_c_ion': ['', 0, re.compile('')],
    #'p_c_ids': ['', 0, re.compile('')]
}

# Paths (These should be adapted for the system they run on)
_WEB_DIR = normpath('C:/Program Files (x86)/Apache Software Foundation/Apache2.2/htdocs/CTMM')
_NIST = normpath('C:/Users/nbic/Documents/NISTMSQCv1_2_0')
_PROGRES_LOG = 'qc_status.log'
_QC_HOME = normpath('E:/QC')

# Command line arguments
_IN_DIR = ''
_OUT_DIR = ''
_COPY_LOG = ''


def monitor_input():
    print 'Version 0.0.3'
    """Checks input directory for new RAW files to analyze, keeping track
    of all processed files. Once a new RAW file has been placed in this directory
    a report will be generated with this file as input."""
    files = _read_logfile(_PROGRES_LOG)
    files = _parse_robocopy_log(files)

    # The following code should (preferably) be threaded, one thread to monitor for new files
    # and another thread for each file to be processed

    #sys.exit('Files: {0}'.format(files))
    if not files:
        sys.exit('No files to process..')

    for f, status in files.iteritems():
        if status != 'new':
            continue

        print "-----------\nProcessing:\n\t", f, "\n-----------\n"
        # Set for current file and move file to output directory

        basename = splitext(f)[0]
        dirname = '{0}_{1}_QC'.format(basename, int(random.random()*10000))
        outdir = normpath('{0}/{1}'.format(_OUT_DIR, dirname))
        metrics = _METRICS
        
        # Create folders for storing temp output as well as web output
        if not isdir(outdir):
            makedirs(outdir)
        
        # TODO: instead of moving the file, find out if NIST accepts a file as input
        # instead of a directory containing *.RAW files.
        move(normpath('{0}/{1}'.format(_IN_DIR, f)), outdir)

        raw_file = normpath('{0}/{1}'.format(outdir, f))
        webdir = _manage_paths(basename, outdir)

        files[f] = 'processing'

        # Run QC workflow
        print "Running NIST.."
        _run_NIST(raw_file, outdir)
        print "Creating Graphics.."
        _run_R_script(outdir, webdir, basename)
        print "Creating metrics.."
        metrics = _create_metrics(raw_file, outdir, metrics, dirname, basename)
        print "Creating report.."
        _create_report(raw_file, webdir, basename, metrics)

        # Once completed, update status and logfile
        files[f] = 'completed'

        # Update logfile showing completed analysis
        _log_progress()


def _read_logfile(logfile):
    # Logfile layout:
    # Filename    Status    Report_path
    files = dict()
    with open(logfile, 'r') as logfile:
        for line in logfile:
            name, status = line.split('\t')[0:2]
            files[name] = status
    return files


def _parse_robocopy_log(files):
    """ Check Robocopy logfile for new files copied """
    with open(_COPY_LOG, 'r') as logfile:
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


def _log_progress():
    """Keeps track of processed RAW files, this logfile is used to create a
    simple status report through a webserver."""
    pass


def _manage_paths(basename, outdir):
    # Create directory on webserver part for storing images and the report
    time = gmtime()
    tree = normpath('{root}/{year}/{month}/{day}/{base}'.format(root=_WEB_DIR,
                                                                year=strftime("%Y", time),
                                                                month=strftime("%b", time),
                                                                day=strftime("%d", time),
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
    nist_library = 'human_2011_05_26_it'
    fasta = normpath('{0}/libs/{1}.fasta'.format(_NIST, nist_library))
    instrument = 'LTQ'

    # Run NIST pipeline
    # TODO: validate parameters, check if in- and out-dir can be the same
    NIST_exe = normpath('perl {0}/scripts/run_NISTMSQC_pipeline.pl'.format(_NIST))
    NIST_cmd = '{0} --in_dir {1} --out_dir {2} --library {3} --instrument_type {4} --fasta {5} {6} {7} {8} {9}'.format(NIST_exe,
                #FIXME Separate runs of NIST should perhaps not use the same out dir
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
    print "R Command:\n\t", Rcmd
    _run_command(Rcmd)


def _create_metrics(rawfile, outdir, metrics, dirname, basename):
    """ Parses NIST metrics output file extracting relevant metrics subset """
    metrics_file = normpath('{0}/{1}_report.msqc'.format(outdir, dirname))
    print "\n### Metrics File: ", metrics_file

    # Extracting metrics from NIST report file
    with open(metrics_file, 'r') as f:
        nist_metrics = f.readlines()

    for metric in metrics.keys():
        index = next((num for num, line in enumerate(nist_metrics) if metrics[metric][0] in line), None)
        if index != None:
            result = metrics[metric][-1].search(nist_metrics[index + metrics[metric][1]])
            metrics[metric] = result.group(1)
    
    # Extracting metrics (MS1, MS2 scans) from R log file
    Rlogfile = normpath('{0}/{1}.RLOG'.format(outdir, basename))
    with open(Rlogfile, 'r') as f:
        Rlog = ''.join(f.readlines())

    # TODO: error handling
    metrics['ms1_spectra'] = '{0} ({1})'.format(re.search('Number of MS1 scans: ([0-9]+)', Rlog).group(1),
	                                            re.search('MS1 scans containing peaks: ([0-9]+)', Rlog).group(1))
    metrics['ms2_spectra'] = '{0} ({1})'.format(re.search('Number of MS2 scans: ([0-9]+)', Rlog).group(1),
	                                            re.search('MS2 scans containing peaks: ([0-9]+)', Rlog).group(1))

    # Other generic metrics
    metrics['f_size'] = "%0.1f" % (os.stat(rawfile).st_size / (1024 * 1024.0))

    return metrics


def _create_report(rawfile, webdir, basename, metrics):
    # Graphics to include
    heatmap = normpath('{0}/{1}_heatmap.pdf'.format(webdir, basename))
    ion_cnt = '{0}_ions.pdf'.format(basename)

    # Place values and graphics in template HTML file
    with open('../web/templates/report.html', 'r') as f:
        template = f.readlines()

    report_template = Template(template)
    report_updated = report_template.safe_substitute(# General
                                                     raw_file=rawfile,
                                                     date='',
                                                     time='',
                                                     runtime='',
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
                                                     heatmap_img=heatmap,
                                                     ions_img=ion_cnt)

    # Write report file to directory holding all reports
    with open(normpath('{0}/{1}_report.html'.format(webdir, basename)), 'w') as f:
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

    """ NO NEED, adapted the R package reading in the mzXML file (does not check for version anymore)
    # Change mzXML schema from 3.1 to 2
    # TODO: copies the complete file, need to do an inplace replace!
    print "\tChanging mzXML header to v2.0.."
    mzXMLFile = '{0}{1}.RAW.mzXML'.format(_OUT_DIR, _BASE_NAME)
    for line in fileinput.FileInput(mzXMLFile, inplace=1):
        if 'http://sashimi.sourceforge.net/schema_revision/mzXML_3.1' in line:
            print line.replace('3.0', '2.0'),
        else:
            print line,
    """


def _run_command(cmd):
    # Runs a single command, no output is returned
    run = Popen(cmd, stdout=PIPE, stderr=PIPE)
    err = run.communicate()[1]
    if run.returncode != 0:
        raise IOError(err)


def _cleanup():
    pass


def main(args):
    global _IN_DIR, _OUT_DIR, _COPY_LOG
    _IN_DIR = normpath('C:/Users/nbic/Documents/QC/input')#args.in_folder
    _OUT_DIR = normpath('C:/Users/nbic/Documents/QC')#args.out_folder
    _COPY_LOG = 'robocopy.log' #args.copy_log

    # Start monitoring
    monitor_input()


if __name__ == "__main__":
    # Create and parse commandline arguments
    parser = ArgumentParser(description="QC-workflow for MS data using NIST metrics")
    parser.add_argument('in_folder', type=str, help='Input folder containing (Thermo) RAW files outputted by a mass-spectrometer')
    parser.add_argument('out_folder', type=str, help='Folder in which output (report) PDF files will be written')
    parser.add_argument('copy_log', type=str, help='Logfile (local) that Robocopy uses to write status')

    args = parser.parse_args()
    sys.exit(main(args))


""" *NOTES*

Robocopy command:
Mirror, 10 retries, waiting 30sec, monitor every 1min, display output, no progress, no job header, no job summary
-robocopy "source_path" "\\destination_path" / Mir / R:10 / W:30 / Log + :"\\log_path\logfile.txt" / mot:1 / tee / np / njh / njs
- Note: replace \mir with \e if removing files from source but need to be kept in destination

Graphics:
- time/scan on horizontal (flip the axis)
- projection to one axis (vertical total ion-count)
- maxquant also has heatmap.
- linegraph showing total ion-count per scan (histogram)
 -data might be in the pipeline already
- heatmap for both MS1 and MS2

Metrics:
- Red metrics are based on library searching
- OMMSA is database searching
- Switch OMMSA with Mascot (if possible)
- Add total time of the experiment (combine First / Last MS1)

Notes:
- Optimizing mzXML generation (speed)
"""
