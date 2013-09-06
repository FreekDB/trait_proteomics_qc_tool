'''
Module to extract metrics from log-files.
'''

import csv
import datetime
import json
import logging as log
import os
import re
import time
from os.path import normpath

__author__ = "Marcel Kempenaar"
__contact__ = "brs@nbic.nl"
__copyright__ = "Copyright, 2012, Netherlands Bioinformatics Centre"
__license__ = "MIT"


def create_metrics(working_dir, abs_rawfile, t_start):
    """
    Parses NIST metrics output file extracting relevant metrics subset
    Parses QuaMEter metrics output file extracting relevant metrics subset
    @param abs_rawfile: absolute path to the raw results file from the mass spectrometry device
    @param dirname: the folder within the working directory
    @param t_start: timepoint at which QC pipeline started
    """
    log.info("Creating metrics..")

    #Start with an empty base
    metrics = {}
    print ("Extracting generic metrics..\n")
    #Add basic metrics that do not require any log files
    metrics['generic'] = _extract_generic_metrics(abs_rawfile, t_start)

    #Determine paths to log files
    raw_file_name = os.path.split(abs_rawfile)[1]
    metrics_logs_path = normpath(working_dir + '/' + os.path.splitext(raw_file_name)[0])
    nist_metrics_file = metrics_logs_path + '.msqc'
    rlogfile = metrics_logs_path + '.RLOG'
    quameter_metrics_file = metrics_logs_path + '_quametermetrics.tsv'
    print ("Metrics MSQC file ", metrics_logs_path, "\nNIST RLOG file ", rlogfile, "\nMetrics QuaMeter file ", quameter_metrics_file, "\n")

    if os.path.exists(nist_metrics_file):
        #Update metrics with values from NIST pipeline
        print ("Extracting NIST metrics from MSQC file..\n")
        metrics.update(_extract_nist_metrics(nist_metrics_file))
    else:
        log.warn("NIST metrics file does not exist")

    if os.path.exists(rlogfile):
        #Update metrics with some generic metrics
        print ("Extracting generic metrics from RLOG file..\n")
        metrics['generic'].update(_extract_rlog_metrics(rlogfile))
    else:
        log.warn("R log file does not exist")

    if os.path.exists(quameter_metrics_file):
        #Update metrics with values from QuaMeter IDFree mode
        print ("Extracting QuaMeter metrics from TSV file..\n")
        metrics['QuaMeter'] = _extract_quameter_idfree_metrics(quameter_metrics_file)
        print metrics
    else:
        log.warn("QuaMeter metrics file does not exist")

    return metrics


def _get_default_nist_metrics():
    '''
    Compiles all metrics to extract from the NIST metrics output
    Each value is a list of 3 holding the line (header) to match (to get the line number),
    the offset from that line that holds the value of interest and the description
    of the value to be found.
    '''
    return {
        # MS1
        'ms1': {
            'ms1-1': ['Ion Injection Times for IDs', 1, 'MS1 Median'],
            'ms1-2a': ['MS1 During Middle', 1, 'S/N Median'],
            #'ms1-2b': ['MS1 During Middle', 2, 'TIC Median/1000'], nistms_metrics.exe
            'ms1-2b': ['MS1 During Middle', 2, 'TIC Medi/10000'],
            'ms1-3a': ['MS1 ID Max', 6, '95/5 MidRT'],
            'ms1-3b': ['MS1 ID Max', 1, 'Median'],
            'ms1-5a': ['Precursor m/z - Peptide Ion m/z', 2, 'Median'],
            'ms1-5b': ['Precursor m/z - Peptide Ion m/z', 3, 'Mean Absolute'],
            'ms1-5c': ['Precursor m/z - Peptide Ion m/z', 4, 'ppm Median'],
            'ms1-5d': ['Precursor m/z - Peptide Ion m/z', 5, 'ppm InterQ']
        },
        # MS2
        'ms2': {
            'ms2-1': ['Ion Injection Times for IDs', 3, 'MS2 Median'],
            'ms2-2': ['MS2 ID Spectra', 3, 'S/N Median'],
            'ms2-3': ['MS2 ID Spectra', 1, 'NPeaks Median']
        },
        # Peptide Identification
        'pep': {
            # FIXME: Failing due to multiple 'Peptide Counts' occurrences
            #'p-3': ['Peptide Counts', 4, 'Semi/Tryp Peps'], Modified by Pravin. Moved to top for searching
            'p-3': ['Peptide Counts', 5, 'Semi/Tryp Peps'],
            'p-1': ['MS2 ID Spectra', 5, 'ID Score Median'],
            'p-2a': ['Tryptic Peptide Counts', 3, 'Identifications'],
            'p-2b': ['Tryptic Peptide Counts', 2, 'Ions'],
            'p-2c': ['Tryptic Peptide Counts', 1, 'Peptides']
        },
        # Chromatography
        'chrom': {
            'c-1a': ['Fraction of Repeat Peptide IDs with Divergent', 1, '- 4 min'],
            'c-1b': ['Fraction of Repeat Peptide IDs with Divergent', 2, '+ 4 min'],
            'c-2a': ['Middle Peptide Retention Time Period', 1, 'Half Period'],
            'c-2b': ['Middle Peptide Retention Time Period', 7, 'Pep ID Rate'],
            'c-3a': ['Peak Width at Half Height', 1, 'Median Value'],
            'c-3b': ['Peak Width at Half height for IDs', 5, 'Median Disper'],
            'c-4a': ['Peak Widths at Half Max over', 1, 'First Decile'],
            'c-4b': ['Peak Widths at Half Max over', 3, 'Last Decile'],
            'c-4c': ['Peak Widths at Half Max over', 2, 'Median Value']
        },
        # Ion Source
        'ion': {
            # FIXME: escaping does not work (it does in interactive Python environment)
            #'is-1a': ['MS1 During Middle', 14, 'MS1 Jumps >10x'], Modified by Pravin
            #'is-1b': ['MS1 During Middle', 15, 'MS1 Falls <.1x'], Modified by Pravin
            'is-1a': ['MS1 During Middle', 20, 'MS1 Jumps >10x'],
            'is-1b': ['MS1 During Middle', 21, 'MS1 Falls <.1x'],
            'is-2': ['Precursor m/z for IDs', 1, 'Median'],
            #'is-3a': ['Ion IDs by Charge State', 1, 'Charge +1'], Modified by Pravin
            #'is-3b': ['Ion IDs by Charge State', 3, 'Charge +3'], Modified by Pravin
            #'is-3c': ['Ion IDs by Charge State', 4, 'Charge +4'] Modified by Pravin
            'is-3a': ['Ion IDs by Charge State', 2, 'Charge +1'],
            'is-3b': ['Ion IDs by Charge State', 4, 'Charge +3'],
            'is-3c': ['Ion IDs by Charge State', 5, 'Charge +4']
        },
        # Dynamic Sampling
        'dyn': {
            'ds-1a': ['Ratios of Peptide Ions IDed', 1, 'Once/Twice'],
            'ds-1b': ['Ratios of Peptide Ions IDed', 2, 'Twice/Thrice'],
            'ds-2a': ['Middle Peptide Retention Time Period', 6, 'MS1 Scans'],
            'ds-2b': ['Middle Peptide Retention Time Period', 5, 'MS2 scans'],
            'ds-3a': ['MS1max/MS1sampled Abundance', 1, 'Median All IDs'],
            'ds-3b': ['MS1max/MS1sampled Abundance', 7, 'Med Bottom 1/2']
        }
    }

def _extract_generic_metrics(rawfile, t_start):
    '''
    Return dictionary with generic metrics based on raw file name and QC runtime.
    @param rawfile: the raw filename, only used to determine filesize
    @param t_start: starttime of the QC run, used to determine runtime
    '''
    generic_metrics = {}
    # Other generic metrics
    generic_metrics['f_size'] = ['File Size (MB)', "%0.1f" % (os.stat(rawfile).st_size / (1024 * 1024.0))]
    generic_metrics['runtime'] = str(datetime.timedelta(seconds=round(time.time() - t_start)))
    #cur_time = time.gmtime() #Changed by Pravin to localtime() to suite for Daylight Savings Time
    cur_time = time.localtime()
    generic_metrics['date'] = '{year}/{month}/{day} - {hour}:{min}'.format(year=time.strftime("%Y", cur_time),
    return generic_metrics


def _extract_nist_metrics(metrics_file):
    '''
    Return dictionary with the values found in the metrics file based upon patterns defined in default NIST metrics.
    @param metrics_file: NIST metrics file
    '''
    metrics = _get_default_nist_metrics()
    nist_metrics = {}
    with open(metrics_file, 'r') as mfile:
        lines = mfile.readlines()

    # This regular expression parses all common numerical (including scientific) notations
    num_regex = '\s*([+\-]?(?:0|[1-9]\d*)(?:\.\d*)?(?:[eE][+\-]?\d+)?)'

    # For each metric class (mcl) ('pep', 'ms1', etc.) perform regex searches in the NIST metrics
    # output file and add the found values to the metrics dictionary.
    for mcl in metrics.keys():
        nist_metrics[mcl] = {}
        # For each metric ID ('ms1-1', 'ms1-2a', 'ms1-2b', etc.) lookup the section containing the requested data
        # by its section header (metrics[mcl][metric][0]). Next, use the line offset (metrics[mcl][metric][1] to 
        # get the line which contains the actual data (metrics[mcl][metric][2])
        for metric in metrics[mcl].keys():
            # Search for paragraph header, store line number
            #index = next((num for num, line in enumerate(lines) if metrics[mcl][metric][0] in line), None)
            index = next((num for num, line in enumerate(lines) if line.startswith(metrics[mcl][metric][0])), None)
            if index:
                #print ("Metric ", metrics[mcl][metric][0], " index ", index)
                regex = re.compile('{0}{1}'.format(re.escape(metrics[mcl][metric][-1]), num_regex))
                # Add the offset to the index to get the line with the actual data of interest
                result = regex.search(lines[index + metrics[mcl][metric][1]])
                # Format the metric for the report table ('Paragraph' ('Metric name')
                metric_descr = '{0} ({1})'.format(metrics[mcl][metric][0], metrics[mcl][metric][2])
                # Create a list with the description and the resulting value
                if result:
                    nist_metrics[mcl][metric] = [metric_descr, result.group(1)]
                else:
                    nist_metrics[mcl][metric] = [metric_descr, "N/A"]
                    log.warn("Metric '{0}' could not be found".format(metric))

    return nist_metrics


def _extract_quameter_idfree_metrics(metrics_file):
    '''
    Return dictionary with the values found in the metrics file based upon patterns defined in default QuaMeter metrics.
    @param metrics_file: QuaMeter metrics file
    '''
    metrics = _get_quameter_idfree_metrics()
    quameter_metrics = {}
    with open(metrics_file, 'r') as mfile:
        lines = mfile.readlines()
    per_row = []
    for line in lines:
        per_row.append(line.split('\t'))
    quameter_metrics = zip(*per_row)
    return quameter_metrics

    
def _extract_rlog_metrics(logfile):
    '''
    Return dictionary of values extracted from R logfile.
    @param logfile: R-logfile to scan for # of scans, # of peaks and other generic metrics
    '''
    rlog_metrics = {}

    # Extracting metrics (MS1, MS2 scans) from R log file
    with open(logfile, 'r') as rlogfile:
        rlog = ''.join(rlogfile.readlines())

    #TODO extract these patterns to an external dictionary, analogous to get_default_nist_metrics
    # Values are set to NA if not found
    ms1_num = re.search('Number of MS1 scans: ([0-9]+)', rlog)
    ms1_num = ms1_num.group(1) if ms1_num else "NA"
    rlog_metrics['ms1_spectra'] = ['MS1 Spectra', '{0}'.format(ms1_num)]

    ms2_num = re.search('Number of MS2 scans: ([0-9]+)', rlog)
    ms2_num = ms2_num.group(1) if ms2_num else "NA"
    rlog_metrics['ms2_spectra'] = ['MS2 Spectra', '{0}'.format(ms2_num)]

    maxIntensity = re.search('maxIntensity: ([a-zA-Z0-9.]+)', rlog)
    maxIntensity = maxIntensity.group(1) if maxIntensity else "NA"
    rlog_metrics['maxIntensity'] = ['maxIntensity', '{0}'.format(maxIntensity)]
    
    return rlog_metrics


def export_metrics_json(metrics, webdir):
    '''
    Stores the NIST metrics dictionary as JSON file used when rendering the report
    @param metrics: dictionary holding all NIST metrics
    @param webdir: directory where the report is stored
    '''
    json_structure = json.dumps(metrics)

    with open(os.path.join(webdir, 'metrics.json'), "w") as metrics_file:
        metrics_file.writelines(json_structure)
