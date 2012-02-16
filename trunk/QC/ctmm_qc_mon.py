from watchdog.observers import Observer
from watchdog.events import LoggingEventHandler, FileSystemEventHandler
from time import gmtime, strftime, sleep
from argparse import ArgumentParser
import pythoncom
import win32serviceutil
import win32service
import win32event
import servicemanager
import socket
import sys
# QC pipeline
import ctmm_qc

class AppServerSvc(win32serviceutil.ServiceFramework):
    _svc_name = "CTMM_QC_Monitor"
    _svc_display_name = "CTMM QC Monitor"
    
    def __init__(self, args):
        win32serviceutil.ServiceFramework.__init__(self,args)
        self.hWaitStop = win32event.CreateEvent(None,0,0,None)
        socket.setdefaulttimeout(60)

    def SvcStop(self):
        self.ReportServiceStatus(win32service.SERVICE_STOP_PENDING)
        win32event.SetEvent(self.hWaitStop)

    def SvcDoRun(self):
        servicemanager.LogMsg(servicemanager.EVENTLOG_INFORMATION_TYPE,
                              servicemanager.PYS_SERVICE_STARTED,
                              (self._svc_name_,''))
        self.main()

    def main(self):
        in_dir = normpath('C:/Users/brs/Documents/CTMM/Data')
        out_dir = normpath('C:/Users/brs/Documents/CTMM/')
        copy_log = normpath('C:/ctmm/')

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
        pass
        

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
            ctmm_qc.qc_pipeline(self.in_dir, self.out_dir, event.__dict__['_src_path'])
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


if __name__ == "__main__":
    win32serviceutil.HandleCommandLine(AppServerSvc)