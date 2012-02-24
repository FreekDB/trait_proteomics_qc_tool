"""
Module to test the run_msqc_pipeline.
"""

from QC import run_msqc_pipeline
from pkg_resources import resource_filename # @UnresolvedImport
import os.path
import tempfile
import unittest
import shutil


class Test(unittest.TestCase):

    def setUp(self):
        # Log files
        self.logfile = resource_filename(__name__, "data/qc_logfile.txt")
        self.robocopylog = resource_filename(__name__, "data/robocopylog.txt")
        self.msqcRlog = resource_filename(__name__, "data/msqc_rlog.txt")

        # Data
        self.mzXML = resource_filename(__name__, "data/ltq_subset.mzXML")
        self.rawfile = 'ltq_ctmm_test_data.RAW'

    def test_logfiles(self):
        files = run_msqc_pipeline._read_logfile(self.logfile)
        self.assertEquals(files, {'110215_13.RAW': 'completed', '110308_02.RAW': 'completed'})
        files = run_msqc_pipeline._parse_robocopy_log(self.robocopylog, files)
        self.assertEquals(files, {'110215_13.RAW': 'completed', 'U87_10mg_B.raw': 'new', '110308_02.RAW': 'completed'})

    def test_run_NIST(self):
        """Run NIST, and assure the msqc file is output to the correct path."""
        temp_folder = tempfile.mkdtemp(prefix='test_run_nist_')

        #Run NIST
        run_msqc_pipeline._run_NIST(self.rawfile, temp_folder)

        #Check output path exists
        rawfilebase = os.path.split(os.path.splitext(self.rawfile)[0])[1]
        msqc_file = os.path.join(temp_folder, rawfilebase + '.msqc')
        self.failUnless(os.path.exists(msqc_file))
        mzxml_file = os.path.join(temp_folder, rawfilebase + '.RAW.mzXML')
        self.failUnless(os.path.exists(mzxml_file))
        mgf_file = os.path.join(temp_folder, rawfilebase + '.RAW.MGF')
        self.failUnless(os.path.exists(mgf_file))

        #Clean up
        shutil.rmtree(temp_folder)

if __name__ == '__main__':
    unittest.main()
