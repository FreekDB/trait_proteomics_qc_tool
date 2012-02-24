from QC import ctmm_qc
import pkg_resources as pkg
import unittest


class Test(unittest.TestCase):

    def setUp(self):
        # Logfiles
        self.logfile = pkg.resource_filename(__name__, "data/qc_logfile.txt")
        self.robocopylog = pkg.resource_filename(__name__, "data/robocopylog.txt")
        self.msqcRlog = pkg.resource_filename(__name__, "data/msqc_rlog.txt")

        # Data
        self.mzXML = pkg.resource_filename(__name__, "data/ltq_subset.mzXML")

    def test_logfiles(self):
        files = ctmm_qc._read_logfile(self.logfile)
        self.assertEquals(files, {'110215_13.RAW': 'completed', '110308_02.RAW': 'completed'})
        files = ctmm_qc._parse_robocopy_log(self.robocopylog, files)
        self.assertEquals(files, {'110215_13.RAW': 'completed', 'U87_10mg_B.raw': 'new', '110308_02.RAW': 'completed'})

if __name__ == '__main__':
    unittest.main()
