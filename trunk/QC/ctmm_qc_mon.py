from argparse import ArgumentParser
from ctmm_qc import qc_pipeline
from ctmm_service import Service, instart
from os.path import normpath
from time import gmtime, strftime, sleep
from watchdog.events import FileSystemEventHandler
from watchdog.observers import Observer
import sys

#Contemplate reading these settings from a settings file
IN_DIR = normpath('C:/Users/brs/Documents/CTMM/Data')
OUT_DIR = normpath('C:/Users/brs/Documents/CTMM/')
COPY_LOG = normpath('C:/ctmm/')


class FileMonitor(FileSystemEventHandler):
    """
    Class to track changes on files using Watchdog.
    """

    def __init__(self, in_dir, out_dir, copy_log, service):
        self.in_dir = in_dir
        self.out_dir = out_dir
        self.copy_log = copy_log
        self.service = service

    def on_modified(self, event):
        """
        Listens for modifications of the given logfile and executes QC workflow on the newly copied file.
        """
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
    """
    Class to run the QC monitor as a Windows Service.
    """

    def start(self):
        in_dir = IN_DIR
        out_dir = OUT_DIR
        copy_log = COPY_LOG

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
    """
    Class to run the QC monitor manually.
    """
    def __init__(self, indir, outdir, copylog):
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
        """
        FileMonitor calls self.log to log messages, which would work fine if we extended Service.
        But we don't so wee need ot provide our own implementation of log.
        @param msg: the message to log
        """
        print msg

if __name__ == "__main__":
    if sys.argv[1] == 'install':
        instart(QCMonitorService, 'ctmm', 'ctmm_monitor')
    elif sys.argv[1] == 'run':
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
