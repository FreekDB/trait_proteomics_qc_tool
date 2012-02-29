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
    '''
    Test functions for the run_msqc_pipeline code in the QC module
    '''

    def setUp(self):
        # Log files
        self.logfile = resource_filename(__name__, "data/qc_logfile.txt")
        self.robocopylog = resource_filename(__name__, "data/robocopylog.txt")

        # Data
        self.rawfile = 'ltq_ctmm_test_data.RAW'
        self.rawfilebase = os.path.split(os.path.splitext(self.rawfile)[0])[1]
        
        # Temp working dir
        self.temp_folder = tempfile.mkdtemp(prefix='test_run_nist_')

    def test_logfiles(self):
        ''' Tests the reading of the robocopy- and status-logfiles '''
        files = run_msqc_pipeline._read_logfile(self.logfile)
        self.assertEquals(files, {'110215_13.RAW': 'completed', '110308_02.RAW': 'completed'})
        files = run_msqc_pipeline._parse_robocopy_log(self.robocopylog, files)
        self.assertEquals(files, {'110215_13.RAW': 'completed', 'U87_10mg_B.raw': 'new', '110308_02.RAW': 'completed'})

    def test_raw_format_conversions(self):
        ''' Test the conversion of RAW files into mzXML / MGF files '''
        run_msqc_pipeline._raw_format_conversions(self.rawfile, self.temp_folder)
        
        # Check if output paths for the mzXML and MGF files exist
        mzxml_file = os.path.join(self.temp_folder, self.rawfilebase + '.RAW.mzXML')
        self.failUnless(os.path.exists(mzxml_file))
        
        mgf_file = os.path.join(self.temp_folder, self.rawfilebase + '.RAW.MGF')
        self.failUnless(os.path.exists(mgf_file))

    def test_run_nist(self):
        ''' Run NIST, and assure the msqc file is output to the correct path.'''
        run_msqc_pipeline._run_nist(self.rawfile, self.temp_folder)

        #Check output path exists
        msqc_file = os.path.join(self.temp_folder, self.rawfilebase + '.msqc')
        self.failUnless(os.path.exists(msqc_file))

    def test_run_r_script(self):
        ''' Run the R script generating the graphics '''
        run_msqc_pipeline._run_r_script(self.temp_folder, self.temp_folder, self.rawfilebase)
        
        # Test if graphics and logfile exists
        heatmaps = [self.rawfilebase + '_heatmap.png', self.rawfilebase + '_heatmap.pdf']
        ionplots = [self.rawfilebase + '_ions.png', self.rawfilebase + '_ions.pdf']
        rlog = self.rawfilebase + '.RLOG'
        
        self.failUnless([os.path.exists(x) for x in heatmaps])
        self.failUnless([os.path.exists(x) for x in ionplots])
        self.failUnless(os.path.exists(rlog))

    def tearDown(self):
        ''' Cleans up tests '''
        shutil.rmtree(self.temp_folder)

if __name__ == '__main__':
    unittest.main()
