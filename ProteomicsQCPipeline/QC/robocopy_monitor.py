"""
Module to monitor a robocopy log file for changes, and act on those changes by
invoking the CTMM QC pipeline.

To install the QC pipeline as a service, run with the 'install' argument, otherwise
provide other arguments (check with [-h / --help])
"""

from argparse import ArgumentParser
from run_msqc_pipeline import qc_pipeline
from robocopy_monitor_service import Service, instart
from os.path import normpath
from time import gmtime, strftime, sleep
from watchdog.observers import Observer
from watchdog.events import FileSystemEventHandler
import logging
import sys

__author__ = "Marcel Kempenaar"
__contact__ = "brs@nbic.nl"
__copyright__ = "Copyright, 2012, Netherlands Bioinformatics Centre"
__license__ = "MIT"

#TODO: contemplate reading these settings from a settings file
# These paths are only used when installing as a service
# Please note that when running as a service, the tool is unable to access
# data across a network, the data should be accessible on a local disk.
# Robocopy needs to put its logfile into the IN_DIR folder (named 'robocopy.*')
IN_DIR = normpath('E:/qc-data/QCFull/tempinput2')
# Temporary storage, is emptied after each run
OUT_DIR = normpath('E:/qc-data/QCFull/tempoutputscripts')
# Robocopy log file location (by default located in IN_DIR)
COPY_LOG = IN_DIR


class FileMonitor(FileSystemEventHandler):
    '''
    Class used to track changes to all files in indir
    '''
    def __init__(self, indir, outdir, copylog, service):
        '''
        Sets paths to input / output directories etc.
        @param indir: directory to monitor for file changes
        @param outdir: directory to store output (passed to QC tool)
        @param copylog: actual robocopy log file to monitor
        @param service: when running as a service, this is used to log as a Windows event
        '''
        self.indir = indir
        self.outdir = outdir
        self.copylog = copylog
        self.service = service

    def on_modified(self, event):
        '''
        Listens for modifications of the given log file and executes QC workflow on the newly copied file.
        @param event: is triggered on any file modification within the directory monitored
        '''
        # Only watch for changes on file 'robocopy'
        event_file = event.__dict__['_src_path']
        if not 'robocopy' in event_file:
            return

        self.service.log("{0} New robocopy event".format(self.get_time()))

        # Actually process the robocopy log file and any new RAW files, which will take some time
        qc_pipeline(self.indir, self.outdir, event_file)
        # Call function again in case new file(s) were copied during the run.
        # It will return without running if now new files are ready for processing
        qc_pipeline(self.indir, self.outdir, event_file)

        self.service.log("{0} Monitoring for new RAW files..".format(self.get_time()))

    @staticmethod
    def get_time():
        """
        Get a pretty to print time stamp for now.
        """
        t = gmtime()
        return "{hour}:{min}:{sec}".format(hour=strftime("%H", t),
                                           min=strftime("%M", t),
                                           sec=strftime("%S", t))


class QCMonitorService(Service):
    '''
    Class to run the QC monitor as a Windows Service.
    '''
    def start(self):
        '''
        This method is called on service start using the global parameters pointing to the
        directories / files to monitor
        '''
        indir = IN_DIR
        outdir = OUT_DIR
        copylog = COPY_LOG

        # Logs as a Windows event (viewable in Windows event viewer, under 'Application logs')
        self.log("Monitoring:  {0}\nInput Dir:   {1}\nOutput Dir:  {2}".format(copylog, indir, outdir))
        # Create new FileMonitor that starts the QC workflow on file modification
        observer = Observer()
        event_handler = FileMonitor(indir, outdir, copylog, self)
        observer.schedule(event_handler, copylog, recursive=True)
        observer.start()
        try:
            while True:
                sleep(1)
        except KeyboardInterrupt:
            observer.stop()
        observer.join()


class QCMonitor():
    '''
    Class to run the QC monitor manually.
    '''
    def __init__(self, indir, outdir, copylog):
        '''
        Sets paths to input / output directories etc.
        @param indir: directory to monitor for file changes
        @param outdir: directory to store output (passed to QC tool)
        @param copylog: actual robocopy logfile to monitor
        '''
        self.indir = indir
        self.outdir = outdir
        self.copylog = copylog

        self.observer = Observer()

    def start(self):
        print "Monitoring:  {0}\nInput Dir:   {1}\nOutput Dir:  {2}".format(self.copylog, self.indir, self.outdir)

        # Create new FileMonitor that starts the QC workflow on file modification
        event_handler = FileMonitor(self.indir, self.outdir, self.copylog, self)
        self.observer.schedule(event_handler, self.copylog, recursive=True)
        self.observer.start()
        try:
            while True:
                sleep(1)
        except KeyboardInterrupt:
            self.observer.stop()

        self.observer.join()

    def stop(self):
        self.observer.stop()
        self.observer.join()

    def log(self, msg):
        '''
        FileMonitor calls self.log to log messages, which would work fine if we extended Service.
        But we don't so we need to redirect to our own logging method.
        @param msg: the message to log
        '''
        logging.info(msg)

if __name__ == "__main__":
    # Program can be called with 'install' as only argument which triggers a service installation
    if sys.argv[1] == 'install':
        instart(QCMonitorService, 'ctmm-qc', 'ctmm_qc-monitor')
    else:
        # Create and parse command line arguments
        parser = ArgumentParser(description='QC-workflow monitor for MS data using NIST metrics')
        parser.add_argument('indir', type=str,
                            help='Input folder containing (Thermo) RAW files outputted by a mass-spectrometer')
        parser.add_argument('outdir', type=str, help='Folder in which output (report) PDF files will be written')
        # If no copylog folder is supplied, the QC pipeline will be ran a single time on the indir
        # directory and will process all non-processed RAW files within this folder. After processing
        # the process will stop
        parser.add_argument('copylogdir', type=str, help=('(Optional) Directory containing logfile (local) that'
                                                          ' Robocopy uses to write status messages (should be named:'
                                                          ' "robocopy.*". If missing, the QC tool will process all'
                                                          ' RAW files in the given indir and uses the QC'
                                                          ' log file for tracking already processed files'), 
                            nargs='?')

        #Extract arguments
        args = parser.parse_args()

        # If no copylogdir is given, process all files in indir
        if args.copylogdir == None:
            qc_pipeline(args.indir, args.outdir, None)
        else:
            # Create new monitor checking for changes in the given robocopy logfile
            monitor = QCMonitor(args.indir, args.outdir, args.copylogdir)
            monitor.start()
