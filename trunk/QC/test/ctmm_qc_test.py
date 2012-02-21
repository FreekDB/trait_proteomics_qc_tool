import sys
import os
import unittest
from os.path import normpath
from QC import ctmm_qc

class Test(unittest.TestCase):
    
    def setUp(self):    
        self.logfile = normpath("data/qc_logfile.txt")
        self.robocopylog = normpath("data/robocopylog.txt")
        self.msqcRlog = normpath("data/msqc_rlog.txt")

    def test_logfiles(self):
        files = ctmm_qc._read_logfile(self.logfile)
        self.assertEquals(files, {'110215_13.RAW': 'completed', '110308_02.RAW': 'completed'})
        files = ctmm_qc._parse_robocopy_log(self.robocopylog, files)
        self.assertEquals(files, {'110215_13.RAW': 'completed', 'U87_10mg_B.raw': 'new', '110308_02.RAW': 'completed'})
        
if __name__ == '__main__':
    unittest.main()