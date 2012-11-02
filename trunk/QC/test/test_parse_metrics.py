''' Test module for creating and managing metrics '''
from QC import parse_metrics
from pkg_resources import resource_filename  # @UnresolvedImport
import json
import os
import shutil
import tempfile
import time
import unittest

__author__ = "Marcel Kempenaar"
__contact__ = "brs@nbic.nl"
__copyright__ = "Copyright, 2012, Netherlands Bioinformatics Centre"
__license__ = "MIT"


class Test(unittest.TestCase):
    ''' Test functions for the parse_metrics code in the QC module '''

    def setUp(self):
        # Start time (used for computing runtime)
        self.t_start = time.time()
        self.default_metrics = parse_metrics._get_default_nist_metrics()

        # NIST and R output metrics files
        self.nist_metrics = resource_filename(__name__, "data/nist_metrics.msqc")
        self.msqcrlog = resource_filename(__name__, "data/msqc_rlog.txt")

        # Data (only available on CTMM Jenkins test node)
        self.mzxmlfile = resource_filename(__name__, "data/ltq_ctmm_test_data.RAW.mzXML")

    def test_extract_generic_metrics(self):
        ''' Checks the few generic metrics that are shown on each report '''
        # Rawfile in this case is only used to get the file size, so substituted by another 'large' file
        rawfile = self.mzxmlfile
        # Current date / time formatting
        ctime = time.gmtime()
        date = '{year}/{month}/{day} - {hour}:{min}'.format(year=time.strftime("%Y", ctime),
                                                            month=time.strftime("%b", ctime),
                                                            day=time.strftime("%d", ctime),
                                                            hour=time.strftime("%H", ctime),
                                                            min=time.strftime("%M", ctime))
        # Sleep for one second, this should result in a runtime of 1sec
        # Note: test failure can be caused by interfering processes increasing runtime
        time.sleep(1)
        generic_metrics = parse_metrics._extract_generic_metrics(rawfile, self.t_start)
        self.assertEquals(generic_metrics, {'date': date, 'runtime': '0:00:01', 'f_size': ['File Size (MB)', '97.8']})

    def test_extract_nist_metrics(self):
        ''' Compares a subset of the complete list of metrics retrieved from the NIST output file '''
        nist_metrics = parse_metrics._extract_nist_metrics(self.nist_metrics)

        # TODO: manually create the full dictionary and compare with nist_metrics instead of subset testing
        nist_subset = {"ms1-2b": ["MS1 During Middle (TIC Median/1000)", "57"],
                       "ms1-5a": ["Precursor m/z - Peptide Ion m/z (Median)", "0.1460"],
                       "ms1-3b": ["MS1 ID Max (Median)", "2.3e+7"]}
        self.assertDictContainsSubset(nist_subset, nist_metrics['ms1'])
        nist_subset = {"c-4a": ["Peak Widths at Half Max over (First Decile)", "6.41"],
                       "c-1b": ["Fraction of Repeat Peptide IDs with Divergent (+ 4 min)", "0.0022"],
                       "c-2a": ["Middle Peptide Retention Time Period (Half Period)", "9.35"]}
        self.assertDictContainsSubset(nist_subset, nist_metrics['chrom'])

    def test_extract_rlog_metrics(self):
        ''' Tests the parsing of the R log file showing details about the spectra in the mzXML file '''
        rmetrics = parse_metrics._extract_rlog_metrics(self.msqcrlog)
        self.assertEquals(rmetrics, {'ms2_spectra': ['MS2 Spectra', '1871 (1871)'], 
		                             'ms1_spectra': ['MS1 Spectra', '6349 (4135)']})

    def test_export_metrics_json(self):
        '''
        Tests the writing of metrics in JSON format
        '''
        # Create temporary directory and store metrics in JSON format
        temp_folder = tempfile.mkdtemp(prefix='test_parse_metrics_')
        nist_metrics = parse_metrics._extract_nist_metrics(self.nist_metrics)
        parse_metrics.export_metrics_json(nist_metrics, temp_folder)
        json_metrics = os.path.join(temp_folder, 'metrics.json')
        # Test if JSON file is present
        self.failUnless(os.path.exists(json_metrics))

        # Read JSON file and verify
        nist_metrics = json.load(open('{0}/{1}'.format(temp_folder, 'metrics.json', 'r')))
        self.assertEqual('589.90', nist_metrics['ion']['is-2'][1])

        shutil.rmtree(temp_folder)

if __name__ == "__main__":
    #import sys;sys.argv = ['', 'Test.testName']
    unittest.main()
