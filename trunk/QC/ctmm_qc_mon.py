from watchdog.observers import Observer
from watchdog.events import LoggingEventHandler, FileSystemEventHandler
from time import gmtime, strftime, sleep
from argparse import ArgumentParser
import ctmm_qc
import sys

class FileMonitor(FileSystemEventHandler):

    def __init__(self, in_dir, out_dir, copy_log):
        self.in_dir = in_dir
        self.out_dir = out_dir
        self.copy_log = copy_log
        
    def on_modified(self, event):
        """Listenes for modifications of the given logfile and executes
        QC workflow on the newly copied file"""
        
        # A file modification also triggers a directory change, is ignored
        if not event.__dict__['_is_directory'] and \
		'robocopy' in event.__dict__['_src_path']:
            t =  gmtime()
            print 'New event: {hour}:{min}:{sec}'.format(hour=strftime("%H", t), 
                                              min=strftime("%M",t), 
                                              sec=strftime("%S", t))
            print '#########################'
            ctmm_qc.monitor_input(self.in_dir, self.out_dir, event.__dict__['_src_path'])
            print '#########################'
        else:
            return

        print event
        for key in sorted(event.__dict__):
            print key, event.__dict__[key]
        print

def monitor(args):
    in_dir = args.in_folder
    out_dir = args.out_folder
    copy_log = args.copy_log

    print "Monitoring:  ", copy_log
    print "Input Dir:   ", in_dir
    print "Output Dir:  ", out_dir
        
    # Create new FileMonitor that starts the QC workflow on file modification
    event_handler = FileMonitor(in_dir, out_dir, copy_log)
    
    observer = Observer()
    observer.schedule(event_handler, copy_log, recursive=True)
    observer.start()
    try:
        while True:
            sleep(1)
    except KeyboardInterrupt:
        observer.stop()
    observer.join()

if __name__ == "__main__":
    # Create and parse commandline arguments
    parser = ArgumentParser(description="QC-workflow monitor for MS data using NIST metrics")
    parser.add_argument('in_folder', type=str, help='Input folder containing (Thermo) RAW files outputted by a mass-spectrometer')
    parser.add_argument('out_folder', type=str, help='Folder in which output (report) PDF files will be written')
    parser.add_argument('copy_log', type=str, help='Logfile location (local) that Robocopy uses to write status (looking for "robocopy.log"')

    args = parser.parse_args()
    monitor(args)