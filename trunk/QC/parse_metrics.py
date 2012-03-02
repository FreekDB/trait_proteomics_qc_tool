"""Module to extract metrics from logfiles."""

import datetime
import os
import re
import time


def create_metrics(abs_rawfile, t_start):
    """
    Parses NIST metrics output file extracting relevant metrics subset
    @param abs_rawfile: absolute path to the raw results file from the mass spectrometry device
    @param dirname: the folder within the working directory
    @param t_start: timepoint at which QC pipeline started
    """
    #Start with and empty base
    metrics = {}

    #Add basic metrics that do not require any log files
    metrics.update(_extract_generic_metrics(abs_rawfile, t_start))

    #Determine paths to log files
    rawfilebase = os.path.splitext(abs_rawfile)[0]
    metrics_file = rawfilebase + '.msqc'
    Rlogfile = rawfilebase + '.RLOG'

    if os.path.exists(metrics_file):
        #Update metrics with values from NIST pipeline
        metrics.update(_extract_nist_metrics(metrics_file))

    if os.path.exists(Rlogfile):
        #Update metrics with some generic metrics
        metrics.update(_extract_rlog_metrics(Rlogfile))

    return metrics


def _get_default_nist_metrics():
    """
    Compiles all metrics to extract from the NIST metrics output
    Each value is a list of 3 holding the line to match (to get the line number),
    the offset from that line that holds the value of interest and the regex to
    retrieve this value.
    """
    return {
        # MS1
        'ms1': {
            'ms1-1': ['Ion Injection Times for IDs', 1, re.compile(r'MS1 Median\s+([0-9\.]+)')],
            'ms1-2a': ['MS1 During Middle', 1, re.compile(r'S/N Median\s+([0-9\.]+)')],
            'ms1-2b': ['MS1 During Middle', 2, re.compile(r'TIC Median/1000\s+([0-9\.]+)')],
            'ms1-3a': ['MS1 ID Max', 6, re.compile(r'95/5 MidRT\s+([0-9\.]+)')],
            'ms1-3b': ['MS1 ID Max', 1, re.compile(r'Median\s+([0-9\.e\+]+)')],
            'ms1-5a': ['Precursor m/z - Peptide Ion m/z', 2, re.compile(r'Median\s+([0-9\.]+)')],
            'ms1-5b': ['Precursor m/z - Peptide Ion m/z', 3, re.compile(r'Mean Asolute\s+([0-9\.]+)')],
            'ms1-5c': ['Precursor m/z - Peptide Ion m/z', 4, re.compile(r'ppm Median\s+([0-9\.]+)')],
            'ms1-5d': ['Precursor m/z - Peptide Ion m/z', 5, re.compile(r'ppm InterQ\s+([0-9\.]+)')],
        },
        # MS2
        'ms2': {
            'ms2-1': ['Ion Injection Times for IDs', 3, re.compile(r'MS2 Median\s+([0-9\.]+)')],
            'ms2-2': ['MS2 ID Spectra', 3, re.compile(r'S/N Median\s+([0-9\.]+)')],
            'ms2-3': ['MS2 ID Spectra', 1, re.compile(r'NPeaks Median\s+([0-9\.]+)')],
        },
        # Peptide Identification
        'pep': {
            'p-1': ['MS2 ID Spectra', 5, re.compile(r'ID Score Median\s+([0-9\.]+)')],
            'p-2a': ['Tryptic Peptide Counts', 3, re.compile(r'Identifications\s+([0-9\.]+)')],
            'p-2b': ['Tryptic Peptide Counts', 2, re.compile(r'Ions\s+([0-9\.]+)')],
            'p-2c': ['Tryptic Peptide Counts', 1, re.compile(r'Peptides\s+([0-9\.]+)')],
            'p-3': ['Peptide Counts', 4, re.compile(r'Semi/Tryp Peps\s+([0-9\.]+)')],
        },
        # Chromatography
        'chrom': {
            'c-1a': ['Fraction of Repeat Peptide IDs with Divergent', 1, re.compile(r'- 4 min\s+([0-9\.]+)')],
            'c-1b': ['Fraction of Repeat Peptide IDs with Divergent', 2, re.compile(r'\+ 4 min\s+([0-9\.]+)')],
            'c-2a': ['Middle Peptide Retention Time Period', 1, re.compile(r'Half Period\s+([0-9\.]+)')],
            'c-2b': ['Middle Peptide Retention Time Period', 7, re.compile(r'Pep ID Rate\s+([0-9\.]+)')],
            'c-3a': ['Peak Width at Half Height', 1, re.compile(r'Median Value\s+([0-9\.]+)')],
            'c-3b': ['Peak Width at Half height for IDs', 5, re.compile(r'Median Disper\s+([0-9\.]+)')],
            'c-4a': ['Peak Widths at Half Max over', 1, re.compile(r'First Decile\s+([0-9\.]+)')],
            'c-4b': ['Peak Widths at Half Max over', 3, re.compile(r'Last Decile\s+([0-9\.]+)')],
            'c-4c': ['Peak Widths at Half Max over', 2, re.compile(r'Median Value\s+([0-9\.]+)')],
        },
        # Ion Source
        'ion': {
            'is-1a': ['MS1 During Middle', 14, re.compile(r'MS1 Jumps \>10x\s+([0-9\.]+)')],
            'is-1b': ['MS1 During Middle', 15, re.compile(r'MS1 Falls \<\.1x\s+([0-9\.]+)')],
            'is-2': ['Precursor m/z for IDs', 1, re.compile(r'Median\s+([0-9\.]+)')],
            'is-3a': ['Ion IDs by Charge State', 1, re.compile(r'Charge \+1\s+([0-9\.]+)')],
            'is-3b': ['Ion IDs by Charge State', 3, re.compile(r'Charge \+3\s+([0-9\.]+)')],
            'is-3c': ['Ion IDs by Charge State', 4, re.compile(r'Charge \+4\s+([0-9\.]+)')],
        },
        # Dynamic Sampling
        'dyn': {
            'ds-1a': ['Ratios of Peptide Ions IDed', 1, re.compile(r'Once/Twice\s+([0-9\.]+)')],
            'ds-1b': ['Ratios of Peptide Ions IDed', 2, re.compile(r'Twice/Thrice\s+([0-9\.]+)')],
            'ds-2a': ['Middle Peptide Retention Time Period', 6, re.compile(r'MS1 Scans\s+([0-9\.]+)')],
            'ds-2b': ['Middle Peptide Retention Time Period', 5, re.compile(r'MS2 Scans\s+([0-9\.]+)')],
            'ds-3a': ['MS1max/MS1sampled Abundance', 1, re.compile(r'Median All IDs\s+([0-9\.]+)')],
            'ds-3b': ['MS1max/MS1sampled Abundance', 7, re.compile(r'Med Bottom 1/2\s+([0-9\.]+)')]
        }
    }


def _extract_generic_metrics(rawfile, t_start):
    """
    Return dictionary with generic metrics based on raw file name and runtime.
    @param rawfile: the raw filename, only used to determine filesize
    @param t_start: starttime of the QC run, used to determine runtime
    """
    generic_metrics = {}
    # Other generic metrics
    generic_metrics['f_size'] = "%0.1f" % (os.stat(rawfile).st_size / (1024 * 1024.0))
    generic_metrics['runtime'] = str(datetime.timedelta(seconds=round(time.time() - t_start)))
    t = time.gmtime()
    generic_metrics['date'] = '{year}/{month}/{day} - {hour}:{min}'.format(year=time.strftime("%Y", t),
                                                                           month=time.strftime("%b", t),
                                                                           day=time.strftime("%d", t),
                                                                           hour=time.strftime("%H", t),
                                                                           min=time.strftime("%M", t))
    return generic_metrics


def _extract_nist_metrics(metrics_file):
    """
    Return dictionary with the values found in the metrics file based upon patterns defined in default nist metrics.
    @param metrics_file: NIST metrics file
    """
    nist_metrics = _get_default_nist_metrics()
    with open(metrics_file, 'r') as f:
        lines = f.readlines()

    # For each metric class ('pep', 'ms1', etc.) perform regex searches in the NIST metrics
    # output file and add the found values to the metrics dictionary.
    for mc in nist_metrics.keys():
        for metric in nist_metrics[mc].keys():
            index = next((num for num, line in enumerate(lines) if nist_metrics[mc][metric][0] in line), None)
            if index:
                result = nist_metrics[mc][metric][-1].search(lines[index + nist_metrics[mc][metric][1]])
                nist_metrics[metric] = result.group(1) if result else "N/A"
    return nist_metrics


def _extract_rlog_metrics(logfile):
    """
    Return dictionary of values extracted from R logfile.
    @param logfile: R-logfile to scan for # of scans, # of peaks and other generic metrics
    """
    rlog_metrics = {}

    # Extracting metrics (MS1, MS2 scans) from R log file
    with open(logfile, 'r') as f:
        Rlog = ''.join(f.readlines())

    #TODO extract these patterns to an external dictionary, analogous to get_default_nist_metrics
    ms1_num = re.search('Number of MS1 scans: ([0-9]+)', Rlog)
    ms1_peaks = re.search('MS1 scans containing peaks: ([0-9]+)', Rlog)
    ms1_num = ms1_num.group(1) if ms1_num else "NA"
    ms1_peaks = ms1_peaks.group(1) if ms1_peaks else "NA"
    rlog_metrics['ms1_spectra'] = '{0} ({1})'.format(ms1_num, ms1_peaks)

    ms2_num = re.search('Number of MS2 scans: ([0-9]+)', Rlog)
    ms2_peaks = re.search('MS2 scans containing peaks: ([0-9]+)', Rlog)
    ms2_num = ms2_num.group(1) if ms2_num else "NA"
    ms2_peaks = ms2_peaks.group(1) if ms2_peaks else "NA"
    rlog_metrics['ms2_spectra'] = '{0} ({1})'.format(ms2_num, ms2_peaks)

    return rlog_metrics
