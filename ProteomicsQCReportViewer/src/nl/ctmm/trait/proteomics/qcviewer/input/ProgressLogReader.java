package nl.ctmm.trait.proteomics.qcviewer.input;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.Timer;
import java.util.TimerTask;

import nl.ctmm.trait.proteomics.qcviewer.Main;
import nl.ctmm.trait.proteomics.qcviewer.input.ProgressLogMonitor.FileMonitorTask;
import nl.ctmm.trait.proteomics.qcviewer.utils.Constants;

public class ProgressLogReader implements FileChangeListener {

	String currentStatus = ""; 
	String runningMsrunName = ""; 
	boolean completed = false; 
	Main owner;
	private BufferedReader br; 
	private Timer timer;
    private Hashtable<String, StatusMonitorTask> timerEntries;
	File logFile; 

	public ProgressLogReader (String progressLogFilePath) {
		this.owner = Main.getInstance(); 
		logFile = new File(progressLogFilePath);
		parseCurrentStatus(logFile);
		System.out.println("Current QC Pipeline Status: " + currentStatus);
	    // Create timer, run timer thread as daemon.
	    timer = new Timer(true);
	    timerEntries = new Hashtable<String, StatusMonitorTask>();
	    StatusMonitorTask task = new StatusMonitorTask(this);
	    timerEntries.put("StatusMonitor", task);
	    timer.schedule(task, 5000, 5000);
	}

	
	public String getCurrentStatus() {
		return currentStatus; 
	}
	
	public String getRunningMsrunName() {
		return runningMsrunName; 
	}
	
	public void pushPipelineStatus() {
		parseCurrentStatus(logFile);
		owner.notifyUpdatePipelineStatus(currentStatus);
	}
	
	private void parseCurrentStatus (File logFile) {
		String lastLine = "";
		try {
			InputStreamReader streamReader = new InputStreamReader(new FileInputStream(logFile));
			br = new BufferedReader(streamReader);
			//System.out.println(logFile.getName());
			//Also check for empty lines and white spaces
			while (br.ready()) {
				String thisLine = br.readLine();
				thisLine = thisLine.trim();
				if (thisLine.length() > 0) {
					lastLine = thisLine; 
				}
			}
		} catch (IOException e) {
			System.out.println(e.toString());
			currentStatus = "Logfile doesn't exist. | | | | | Configured filepath = " + logFile.getAbsolutePath();
			return;
		}
		/*Get timestamp without microseconds. 
		 * No support for microseconds in Java SimpleDateFormat 
		 * Refer to http://docs.oracle.com/javase/7/docs/api/java/text/SimpleDateFormat.html
		 * Example of lastLine: 
		 * 2013-05-28 11:11:52.617000	data01QE2_130409_OPL1013_CvA_Bonemarrow_TiOx_S5.raw	running
		 * 2013-05-28 13:04:16.180000	QE1_130108_OPL1005_YL_lysRIVM_NKI_BRCA_H1.raw	completed
		 */
		lastLine = lastLine.trim();
		if (lastLine.startsWith("20")) { //Hopefully not the Y2K problem!!!!
			StringTokenizer stkz = new StringTokenizer(lastLine, ".");
			String timeStamp = stkz.nextToken();
			DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			try {
				Date logDate = sdf.parse(timeStamp);
				//System.out.println("Parsed log date is " + logDate.toString());
				Date currentDate = new Date (System.currentTimeMillis());
				//System.out.println("Current date is " + currentDate.toString());
				//in milliseconds
				long diff = currentDate.getTime() - logDate.getTime();
				long diffSeconds = diff / 1000 % 60;
				long diffMinutes = diff / (60 * 1000) % 60;
				long diffHours = diff / (60 * 60 * 1000) % 24;
				long diffDays = diff / (24 * 60 * 60 * 1000);
				//System.out.print(diffDays + " days, ");
				//System.out.print(diffHours + " hours, ");
				//System.out.print(diffMinutes + " minutes, ");
				//System.out.println(diffSeconds + " seconds.");
				if (lastLine.endsWith("running")) {
					completed = false; 
					stkz = new StringTokenizer(lastLine);
					stkz.nextToken();
					stkz.nextToken();
					String rawFileName = stkz.nextToken();
					currentStatus = "Currently analyzing " + rawFileName + " | | | | | Active for " + 
							diffDays + " days, " + diffHours + " hours, " + diffMinutes + " minutes, " + diffSeconds + " seconds.";
					runningMsrunName = rawFileName; 
					runningMsrunName = runningMsrunName.trim(); 
					//Remove last .RAW from runningMsrunName
			    	int msrunLength = runningMsrunName.length();
			    	runningMsrunName = runningMsrunName.substring(0, msrunLength - 4); //Remove trailing .RAW extension
			    	//System.out.println("ProgressLogReader runningMsrunName = " + runningMsrunName);
				} else if (lastLine.endsWith("completed")) {
					runningMsrunName = "";
					completed = true;
					currentStatus = "Idle.. | | | | | Inactive for " + 
							diffDays + " days, " + diffHours + " hours, " + diffMinutes + " minutes, " + diffSeconds + " seconds.";
				}
			} catch (ParseException e) {
				e.printStackTrace();
			}
		} else currentStatus = "QC pipeline logfile " + Constants.PROPERTY_PROGRESS_LOG + " is empty.";
	}

	@Override
	public void fileChanged(File logFile) {
		System.out.println("ProgressLogReader: logFile changed. Refreshing current status..");
		parseCurrentStatus(logFile);
		System.out.println("Now current status is " + getCurrentStatus());
		if (completed) {
			owner.notifyProgressLogFileChanged(getCurrentStatus(), true);
		} else {
			owner.notifyProgressLogFileChanged(getCurrentStatus(), false);
		}
	}
	
	  /**
	   * File monitoring task.
	   */
	  class StatusMonitorTask extends TimerTask {
	    ProgressLogReader owner; 

	    public StatusMonitorTask(ProgressLogReader owner) {
	    	this.owner = owner; 
	    }

	    public void run() {
	    	//System.out.println("StatusMonitorTask running owner.pushPipelineStatus()"); 
	    	owner.pushPipelineStatus();
	    }
	  }
}

/*
 * def _read_logfile(status_log):
    with open(status_log, 'r') as logfile:
        data = logfile.readlines()
    # Get / parse the timestamp of the latest update and calculate difference
    last_update = data[-1].strip().split('\t')
    t_start = strptime(last_update[0], '%Y-%m-%d %H:%M:%S.%f')
    t_diff = datetime.now() - datetime.fromtimestamp(mktime(t_start))
    # Remove milliseconds from time difference
    t_diff = str(t_diff).split('.')[0]

    if last_update[2] == 'running':
        img = '<img border="0" src="/ctmm/report/images/check_icon.png" height="18">'
        logline = '{0} Currently analyzing <b><i>{1}</i></b> (active for: {2})'.format(img, last_update[1], t_diff)
    else:
        img = '<img border="0" src="/ctmm/report/images/warning_icon.png" height="18">'
        logline = '{0} Idle.. (inactive for: {1})'.format(img, t_diff)
    return logline
*/

