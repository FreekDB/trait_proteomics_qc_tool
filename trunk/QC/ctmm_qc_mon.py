from watchdog.observers import Observer
from watchdog.events import FileSystemEventHandler
from time import gmtime, strftime, sleep
from os.path import normpath
from ctmm_qc import qc_pipeline
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
            print "\n{0} New robocopy event".format(self.get_time())
            print '### RUNNING CTMM QC ###'
            qc_pipeline(self.in_dir, self.out_dir, event.__dict__['_src_path'])
            print '#######################'
        else:
            return

        """
        print event
        for key in sorted(event.__dict__):
            print key, event.__dict__[key]
        """
        print "\n{0} Monitoring for new RAW files..".format(self.get_time())

    def get_time(self):
        t = gmtime()
        return "{hour}:{min}:{sec}".format(hour=strftime("%H", t), 
                                           min=strftime("%M",t), 
                                           sec=strftime("%S", t))

class QCMonitor():
    
    def __init__(self):
        self.in_dir = normpath('C:/Users/brs/Documents/CTMM/Data')
        self.out_dir = normpath('C:/Users/brs/Documents/CTMM/')
        self.copy_log = normpath('C:/ctmm/')
        print "Monitoring:  ", self.copy_log
        print "Input Dir:   ", self.in_dir
        print "Output Dir:  ", self.out_dir
    
    def start_monitor(self):    
        # Create new FileMonitor that starts the QC workflow on file modification
        event_handler = FileMonitor(self.in_dir, self.out_dir, self.copy_log)
    
        observer = Observer()
        observer.schedule(event_handler, self.copy_log, recursive=True)
        observer.start()
        try:
            while True:
                sleep(1)
        except KeyboardInterrupt:
            observer.stop()
        observer.join()

    def stop_monitor(self):
        pass

if __name__ == "__main__":
    monitor = QCMonitor()
    monitor.start_monitor()