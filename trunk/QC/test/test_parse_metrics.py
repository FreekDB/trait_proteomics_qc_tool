'''
Created on Feb 27, 2012

@author: marcelk
'''
from QC import parse_metrics
from pkg_resources import resource_filename # @UnresolvedImport
import time
import unittest


class Test(unittest.TestCase):
    ''' Test functions for the parse_metrics code in the QC module '''

    def setUp(self):
        # Start time (used for computing runtime)
        self.t_start = time.time()
        self.default_metrics = parse_metrics._get_default_nist_metrics()
        
        # NIST and R output metrics files
        self.nist_metrics = resource_filename(__name__, "data/nist_metrics.msqc")
        self.msqcrlog = resource_filename(__name__, "data/msqc_rlog.txt")
        
        # Data
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
        self.assertEquals(generic_metrics, {'date': date, 'runtime': '0:00:01', 'f_size': '97.8'})
        
    def test_extract_nist_metrics(self):
        ''' Compares a subset of the complete list of metrics retrieved from the NIST output file '''
        nist_metrics = parse_metrics._extract_nist_metrics(self.nist_metrics)
        # TODO: manually create the full dictionary and compare with nist_metrics instead of subset testing
        nist_subset = {'ms1-3a': '28.1', 'ms1-5a': '0.1460', 'c-4a': '6.41', 'p-2c': '859', 'ds-3a': '55.63'}
        self.assertDictContainsSubset(nist_subset, nist_metrics)
    
    def test_extract_rlog_metrics(self):
        ''' Tests the parsing of the R log file showing details about the spectra in the mzXML file '''
        rmetrics = parse_metrics._extract_rlog_metrics(self.msqcrlog)
        self.assertEquals(rmetrics, {'ms2_spectra': '1871 (1871)', 'ms1_spectra': '6349 (4135)'})


if __name__ == "__main__":
    #import sys;sys.argv = ['', 'Test.testName']
    unittest.main()
