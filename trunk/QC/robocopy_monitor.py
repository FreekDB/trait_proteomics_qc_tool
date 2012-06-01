"""
Module to monitor a robocopy log file for changes, and act on those changes by invoking CTMM QC pipeline.
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
IN_DIR = normpath('//opl-data/data/qe-raw-data')
OUT_DIR = normpath('C:/QC-pipeline/output')
COPY_LOG = IN_DIR


class FileMonitor(FileSystemEventHandler):
    '''
    Class used to track changes to all files in in_dir
    '''
    def __init__(self, in_dir, out_dir, copy_log, service):
        '''
        Sets paths to input / output directories etc.
        @param in_dir: directory to monitor for file changes
        @param out_dir: directory to store output (passed to QC tool)
        @param copy_log: actual robocopy logfile to monitor
        @param service: when running as a service, this is used to log as a Windows event
        '''
        self.in_dir = in_dir
        self.out_dir = out_dir
        self.copy_log = copy_log
        self.service = service

    def on_modified(self, event):
        '''
        Listens for modifications of the given logfile and executes QC workflow on the newly copied file.
        @param event: is triggered on any file modification within the directory monitored
        '''
        print 'Waiting for event..'
        #Only watch for changes on file 'robocopy'
        event_file = event.__dict__['_src_path']
        if not 'robocopy' in event_file:
            return

        self.service.log("{0} New robocopy event".format(self.get_time()))

        #Actually process the robocopy log file, which will take some time
        qc_pipeline(self.in_dir, self.out_dir, event_file)

        self.service.log("{0} Monitoring for new RAW files..".format(self.get_time()))

    @staticmethod
    def get_time():
        """
        Get a pretty to print timestamp for now.
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
        in_dir = IN_DIR
        out_dir = OUT_DIR
        copy_log = COPY_LOG

        # Logs as a Windows event (viewable in Windows event viewer, under 'Application logs')
        self.log("Monitoring:  {0}\nInput Dir:   {1}\nOutput Dir:  {2}".format(copy_log, in_dir, out_dir))
        # Create new FileMonitor that starts the QC workflow on file modification
        observer = Observer()
        event_handler = FileMonitor(in_dir, out_dir, copy_log, self)
        observer.schedule(event_handler, copy_log, recursive=True)
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
        @param in_dir: directory to monitor for file changes
        @param out_dir: directory to store output (passed to QC tool)
        @param copy_log: actual robocopy logfile to monitor
        '''
        self.in_dir = indir
        self.out_dir = outdir
        self.copy_log = copylog

        self.observer = Observer()

    def start(self):
        print "Monitoring:  {0}\nInput Dir:   {1}\nOutput Dir:  {2}".format(self.copy_log, self.in_dir, self.out_dir)
        
        # Create new FileMonitor that starts the QC workflow on file modification
        event_handler = FileMonitor(self.in_dir, self.out_dir, self.copy_log, self)
        self.observer.schedule(event_handler, self.copy_log, recursive=True)
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
    if sys.argv[1] == 'install':
        instart(QCMonitorService, 'ctmm', 'ctmm_monitor')
    else:# sys.argv[1] == 'run':
        # Create and parse commandline arguments
        parser = ArgumentParser(description="QC-workflow monitor for MS data using NIST metrics")
        parser.add_argument('in_folder', type=str,
                            help='Input folder containing (Thermo) RAW files outputted by a mass-spectrometer')
        parser.add_argument('out_folder', type=str, help='Folder in which output (report) PDF files will be written')
        parser.add_argument('copy_log', type=str, help='Logfile (local) that Robocopy uses to write status')

        #Extract arguments
        args = parser.parse_args()
        monitor = QCMonitor(args.in_folder, args.out_folder, args.copy_log)
        monitor.start()
