"""
Module to periodically invoke the QC pipeline on the input folder and output folder.
Desired behavior: The QC pipeline should be invoked periodically depending on the status. 
"""

from argparse import ArgumentParser
from run_msqc_pipeline import qc_pipeline
from time import gmtime, strftime, sleep
import logging
import sys

__author__ = "Marcel Kempenaar"
__contact__ = "brs@nbic.nl"
__copyright__ = "Copyright, 2012, Netherlands Bioinformatics Centre"
__license__ = "MIT"

class QCMonitor():
    '''
    Class to run the QC monitor manually.
    '''
    def __init__(self, indir, outdir):
        '''
        Sets paths to input / output directories etc.
        @param indir: directory to monitor for file changes
        @param outdir: directory to store output (passed to QC tool)
        '''
        self.indir = indir
        self.outdir = outdir
        print "Monitoring:  \nInput Dir:   {0}\nOutput Dir:  {1}".format(self.indir, self.outdir)

if __name__ == "__main__":
    # Program can be called with 'install' as only argument which triggers a service installation
    # Create and parse command line arguments
    parser = ArgumentParser(description='QC-workflow monitor for MS data using NIST metrics')
    parser.add_argument('indir', type=str,
                        help='Input folder containing (Thermo) RAW files outputted by a mass-spectrometer')
    parser.add_argument('outdir', type=str, help='Folder in which temporary folders and files will be created')
    # If no copylog folder is supplied, the QC pipeline will be ran a single time on the indir
    # directory and will process all non-processed RAW files within this folder. After processing
    # the process will stop

    #Extract arguments
    args = parser.parse_args()
    try:
        while True:
            status = qc_pipeline(args.indir, args.outdir)
            if status == False:
                print"Finished QC processing of existing files from {0}.\n reinvoking QC pipeline after 1 minute.".format(args.indir)
                sleep(60)
    except KeyboardInterrupt:
        print "Shutdown requested...exiting"
        sys.exit(0)