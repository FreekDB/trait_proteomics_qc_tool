"""
Module to test the run_msqc_pipeline.
"""

from QC import run_msqc_pipeline
from pkg_resources import resource_filename  # @UnresolvedImport
import unittest


class Test(unittest.TestCase):

    def setUp(self):
        # Log files
        self.logfile = resource_filename(__name__, "data/qc_logfile.txt")
        self.robocopylog = resource_filename(__name__, "data/robocopylog.txt")
        self.msqcRlog = resource_filename(__name__, "data/msqc_rlog.txt")

        # Data
        self.mzXML = resource_filename(__name__, "data/ltq_subset.mzXML")

    def test_logfiles(self):
        files = run_msqc_pipeline._read_logfile(self.logfile)
        self.assertEquals(files, {'110215_13.RAW': 'completed', '110308_02.RAW': 'completed'})
        files = run_msqc_pipeline._parse_robocopy_log(self.robocopylog, files)
        self.assertEquals(files, {'110215_13.RAW': 'completed', 'U87_10mg_B.raw': 'new', '110308_02.RAW': 'completed'})

if __name__ == '__main__':
    unittest.main()
