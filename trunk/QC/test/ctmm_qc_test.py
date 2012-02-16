import sys
import os
import unittest
from QC import ctmm_qc

class Test(unittest.TestCase):
    
    def setUp(self):    
        self.logfile = normpath("data/qc_logfile.txt")
        self.robocopylog = normpath("data/robocopylog.txt")
        self.msqcRlog = normpath("data/msqc_rlog.txt")

    def test_logfiles(self):
        files = ctmm_qc._read_logfile(self.logfile)
        self.assertEquals(files, {'': '', '': ''})
        files = ctmm_qc._parse_robocopy_log(self.robocopylog, files)
        self.assertEquals(files, {'': '', '': ''})
        
if __name__ == '__main__':
    unittest.main()