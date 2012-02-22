from watchdog.observers import Observer
from watchdog.events import FileSystemEventHandler
from time import gmtime, strftime, sleep
from os.path import normpath
from ctmm_qc import qc_pipeline
from ctmm_service import Service, instart
import sys

class FileMonitor(FileSystemEventHandler):

    def __init__(self, in_dir, out_dir, copy_log, service):
        self.in_dir = in_dir
        self.out_dir = out_dir
        self.copy_log = copy_log
        self.service = service
    
    def on_modified(self, event):
        """Listenes for modifications of the given logfile and executes
        QC workflow on the newly copied file"""
        
        # A file modification also triggers a directory change, is ignored
        if not event.__dict__['_is_directory'] and \
		'robocopy' in event.__dict__['_src_path']:
            self.service.log("{0} New robocopy event\n".format(self.get_time()))
            qc_pipeline(self.in_dir, self.out_dir, event.__dict__['_src_path'])
        else:
            return

        self.service.log("\n{0} Monitoring for new RAW files..".format(self.get_time()))

    def get_time(self):
        t = gmtime()
        return "{hour}:{min}:{sec}".format(hour=strftime("%H", t), 
                                           min=strftime("%M",t), 
                                           sec=strftime("%S", t))
	

class QCMonitorService(Service):
    def start(self):
        in_dir = normpath('C:/Users/brs/Documents/CTMM/Data')
        out_dir = normpath('C:/Users/brs/Documents/CTMM/')
        copy_log = normpath('C:/ctmm/')
        
        self.runflag=True
        self.log("Monitoring:  {0}\nInput Dir:   {1}\nOutput Dir:  {2}".format(copy_log,
                 in_dir, out_dir))
        
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
    def __init__(self):
        self.observer = Observer()

    def start(self):
        in_dir = normpath('C:/Users/brs/Documents/CTMM/Data')
        out_dir = normpath('C:/Users/brs/Documents/CTMM/')
        copy_log = normpath('C:/ctmm/')
        
        self.runflag=True
        print "Monitoring:  {0}\nInput Dir:   {1}\nOutput Dir:  {2}".format(copy_log,
                 in_dir, out_dir)
        
        # Create new FileMonitor that starts the QC workflow on file modification
        event_handler = FileMonitor(in_dir, out_dir, copy_log, self)
        self.observer.schedule(event_handler, copy_log, recursive=True)
        self.observer.start()
        try:
            while True:
                sleep(1)
        except KeyboardInterrupt:
            self.observer.stop()
        self.observer.join()
    
    def stop(self):
        self.observer.stop()
		
    def log(self, msg):
        print msg

if __name__ == "__main__":
    if sys.argv[1] == 'install':
        instart(QCMonitorService, 'ctmm', 'ctmm_monitor')
    elif sys.argv[1] == 'run':
        monitor = QCMonitor()
        monitor.start()	