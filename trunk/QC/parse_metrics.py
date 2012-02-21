import re

# Compiles all metrics to extract from the NIST metrics output
# Each value is a list of 3 holding the line to match (to get the line number),
# the offset from that line that holds the value of interest and the regex to
# retrieve this value. 
METRICS = {
    # MS1
    'ms1-1': ['Ion Injection Times for IDs', 1, re.compile(r'MS1 Median\s+([0-9\.]+)')],
    'ms1-2a': ['MS1 During Middle', 1, re.compile(r'S/N Median\s+([0-9\.]+)')],
    'ms1-2b': ['MS1 During Middle', 2, re.compile(r'TIC Median/1000\s+([0-9\.]+)')],
    'ms1-3a': ['MS1 ID Max', 6, re.compile(r'95/5 MidRT\s+([0-9\.]+)')],
    'ms1-3b': ['MS1 ID Max', 1, re.compile(r'Median\s+([0-9\.e\+]+)')],
    'ms1-5a': ['Precursor m/z - Peptide Ion m/z', 2, re.compile(r'Median\s+([0-9\.]+)')],
    'ms1-5b': ['Precursor m/z - Peptide Ion m/z', 3, re.compile(r'Mean Asolute\s+([0-9\.]+)')],
    'ms1-5c': ['Precursor m/z - Peptide Ion m/z', 4, re.compile(r'ppm Median\s+([0-9\.]+)')],
    'ms1-5d': ['Precursor m/z - Peptide Ion m/z', 5, re.compile(r'ppm InterQ\s+([0-9\.]+)')],

    # MS2
    'ms2-1': ['Ion Injection Times for IDs', 3, re.compile(r'MS2 Median\s+([0-9\.]+)')],
    'ms2-2': ['MS2 ID Spectra', 3, re.compile(r'S/N Median\s+([0-9\.]+)')],
    'ms2-3': ['MS2 ID Spectra', 1, re.compile(r'NPeaks Median\s+([0-9\.]+)')],

    # Peptide Identification
    'p-1': ['MS2 ID Spectra', 5, re.compile(r'ID Score Median\s+([0-9\.]+)')],
    'p-2a': ['Tryptic Peptide Counts', 3, re.compile(r'Identifications\s+([0-9\.]+)')],
    'p-2b': ['Tryptic Peptide Counts', 2, re.compile(r'Ions\s+([0-9\.]+)')],
    'p-2c': ['Tryptic Peptide Counts', 1, re.compile(r'Peptides\s+([0-9\.]+)')],
    'p-3': ['Peptide Counts', 4, re.compile(r'Semi/Tryp Peps\s+([0-9\.]+)')],

    # Chromatography
    'c-1a': ['Fraction of Repeat Peptide IDs with Divergent', 1, re.compile(r'- 4 min\s+([0-9\.]+)')],
    'c-1b': ['Fraction of Repeat Peptide IDs with Divergent', 2, re.compile(r'\+ 4 min\s+([0-9\.]+)')],
    'c-2a': ['Middle Peptide Retention Time Period', 1, re.compile(r'Half Period\s+([0-9\.]+)')],
    'c-2b': ['Middle Peptide Retention Time Period', 7, re.compile(r'Pep ID Rate\s+([0-9\.]+)')],
    'c-3a': ['Peak Width at Half Height', 1, re.compile(r'Median Value\s+([0-9\.]+)')],
    'c-3b': ['Peak width at Half height for IDs', 5, re.compile(r'Median Disper\s+([0-9\.]+)')],
    'c-4a': ['Peak Widths at Half Max over', 1, re.compile(r'First Decile\s+([0-9\.]+)')],
    'c-4b': ['Peak Widths at Half Max over', 3, re.compile(r'Last Decile\s+([0-9\.]+)')],
    'c-4c': ['Peak Widths at Half Max over', 2, re.compile(r'Median Value\s+([0-9\.]+)')],
    
    # Ion Source
    'is-1a': ['MS1 During Middle', 14, re.compile(r'MS1 Jumps \>10x\s+([0-9\.]+)')],
    'is-1b': ['MS1 During Middle', 15, re.compile(r'MS1 Falls \<\.1x\s+([0-9\.]+)')],
    'is-2': ['Precursor m/z for IDs', 1, re.compile(r'Median\s+([0-9\.]+)')],
    'is-3a': ['Ion IDs by Charge State', 1, re.compile(r'Charge \+1\s+([0-9\.]+)')],
    'is-3b': ['Ion IDs by Charge State', 3, re.compile(r'Charge \+3\s+([0-9\.]+)')],
    'is-3c': ['Ion IDs by Charge State', 4, re.compile(r'Charge \+4\s+([0-9\.]+)')],

    # Dynamic Sampling
    'ds-1a': ['Ratios of Peptide Ions IDed', 1, re.compile(r'Once/Twice\s+([0-9\.]+)')],
    'ds-1b': ['Ratios of Peptide Ions IDed', 2, re.compile(r'Twice/Thrice\s+([0-9\.]+)')],
    'ds-2a': ['Middle Peptide Retention Time Period', 6, re.compile(r'MS1 Scans\s+([0-9\.]+)')],
    'ds-2b': ['Middle Peptide Retention Time Period', 5, re.compile(r'MS2 Scans\s+([0-9\.]+)')],
    'ds-3a': ['MS1max/MS1sampled Abundance', 1, re.compile(r'Median All IDs\s+([0-9\.]+)')],
    'ds-3b': ['MS1max/MS1sampled Abundance', 7, re.compile(r'Med Bottom 1/2\s+([0-9\.]+)')]
}


def create_metrics(rawfile, outdir, metrics, dirname, basename, t_start):
    """ Parses NIST metrics output file extracting relevant metrics subset """
    metrics_file = normpath('{0}/{1}_report.msqc'.format(outdir, dirname))
    Rlogfile = normpath('{0}/{1}.RLOG'.format(outdir, basename))
    # Extracting metrics from NIST report file
    if exists(metrics_file):
            metrics = _nist_metrics(metrics, metrics_file)

    if exists(Rlogfile):
            metrics = _generic_metrics(metrics, rawfile, t_start, Rlogfile)

    return metrics
            
def _nist_metrics(metrics, metrics_file):
    try:
        with open(metrics_file, 'r') as f:
            nist_metrics = f.readlines()
    except IOError:
        for metric in metrics.keys():
            metrics[metric] = 'NIST Failed'
        return metrics
        
    for metric in metrics.keys():
        index = next((num for num, line in enumerate(nist_metrics) if metrics[metric][0] in line), None)
        if index:
            result = metrics[metric][-1].search(nist_metrics[index + metrics[metric][1]])
            metrics[metric] = result.group(1) if result else "NIST Failed"
    return metrics
    
def _generic_metrics(metrics, rawfile, t_start, logfile):
    # Extracting metrics (MS1, MS2 scans) from R log file
    try:
        with open(logfile, 'r') as f:
            Rlog = ''.join(f.readlines())
    except IOError:
        return metrics

    ms1_num = re.search('Number of MS1 scans: ([0-9]+)', Rlog)
    ms1_peaks = re.search('MS1 scans containing peaks: ([0-9]+)', Rlog)
    ms1_num = ms1_num.group(1) if ms1_num else "NA"
    ms1_peaks = ms1_peaks.group(1) if ms1_peaks else "NA"
    metrics['ms1_spectra'] = '{0} ({1})'.format(ms1_num, ms1_peaks)
    
    ms2_num = re.search('Number of MS2 scans: ([0-9]+)', Rlog)
    ms2_peaks = re.search('MS2 scans containing peaks: ([0-9]+)', Rlog)
    ms2_num = ms2_num.group(1) if ms2_num else "NA"
    ms2_peaks = ms2_peaks.group(1) if ms2_peaks else "NA"
    metrics['ms2_spectra'] = '{0} ({1})'.format(ms2_num, ms2_peaks)   
    
    # Other generic metrics
    metrics['f_size'] = "%0.1f" % (os.stat(rawfile).st_size / (1024 * 1024.0))
    metrics['runtime'] = str(timedelta(seconds=round(time() - t_start)))
    t = gmtime()
    metrics['date'] = '{year}/{month}/{day} - {hour}:{min}'.format(year=strftime("%Y", t),
                                                                   month=strftime("%b", t),
                                                                   day=strftime("%d", t),
                                                                   hour=strftime("%H", t),
                                                                   min=strftime("%M",t))
    return metrics

if __name__ == '__main__':
    print METRICS