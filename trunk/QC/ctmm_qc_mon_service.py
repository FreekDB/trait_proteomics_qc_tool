import pythoncom
import win32serviceutil
import win32service
import win32event
import servicemanager
import socket
import sys
# QC pipeline monitor
from ctmm_qc_mon import QCMonitor

class AppServerSvc(win32serviceutil.ServiceFramework):
    _svc_name_ = "CTMM_QC_Monitor"
    _svc_display_name_ = "CTMM QC Monitor"
    
    def __init__(self, args):
        win32serviceutil.ServiceFramework.__init__(self, args)
        self.hWaitStop = win32event.CreateEvent(None, 0, 0, None)
        socket.setdefaulttimeout(60)
        self.monitor = QCMonitor()

    def SvcStop(self):
        self.ReportServiceStatus(win32service.SERVICE_STOP_PENDING)
        self.monitor.stop_monitor()
        win32event.SetEvent(self.hWaitStop)

    def SvcDoRun(self):
        servicemanager.LogMsg(servicemanager.EVENTLOG_INFORMATION_TYPE,
                              servicemanager.PYS_SERVICE_STARTED,
                              (self._svc_name_,''))
        self.main()

    def main(self):
        self.monitor.start_monitor()
        
if __name__ == "__main__":
    win32serviceutil.HandleCommandLine(AppServerSvc)