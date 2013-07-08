package nl.ctmm.trait.proteomics.qcviewer.input;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.Timer;
import java.util.TimerTask;

import nl.ctmm.trait.proteomics.qcviewer.Main;
import nl.ctmm.trait.proteomics.qcviewer.utils.Constants;
import nl.ctmm.trait.proteomics.qcviewer.input.FileChangeListener;

/**
 * The class for reading qc_status.log file. It contains QC pipeline entries in following format:
 *
 * 2013-06-04 13:40:01.165000    QE2_101109_OPL0004_TSV_mousecelllineL_Q1_2.raw    running
 * 2013-06-04 13:40:01.191000    QE2_101109_OPL0004_TSV_mousecelllineL_Q1_2.raw    completed
 * @author <a href="mailto:pravin.pawar@nbic.nl">Pravin Pawar</a>
 * @author <a href="mailto:freek.de.bruijn@nbic.nl">Freek de Bruijn</a>
 */

public class ProgressLogReader implements FileChangeListener {
    String currentStatus = ""; 
    String runningMsrunName = ""; 
    boolean completed = false; 
    Main owner;
    private BufferedReader br; 
    private Timer timer;
    private Hashtable<String, StatusMonitorTask> timerEntries;
    File logFile; 

    /**
     * Constructor of ProgressLogReader: Parses current status from the logFile and
     * initiates a timer to monitor changes in the logFile
     * @param progressLogFilePath Absolute path to the progressLogFile
     */
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

    /**
     * Get current status from the progressLogFile
     * @return currentStatus
     */
    public String getCurrentStatus() {
        return currentStatus; 
    }
    
    /**
     * Get the name of MS RAW file being processed by the QC pipeline
     * @return name of MS RAW file being processed currently
     */
    public String getRunningMsrunName() {
        return runningMsrunName; 
    }
    
    /**
     * Push current pipeline status to the Main class
     */
    public void pushPipelineStatus() {
        parseCurrentStatus(logFile);
        owner.notifyUpdatePipelineStatus(currentStatus);
    }
    
    /**
     * Parse current pipeline status from the logFile
     * @param logFile 
     */
    private void parseCurrentStatus (File logFile) {
        String lastLine = "";
        try {
            InputStreamReader streamReader = new InputStreamReader(new FileInputStream(logFile));
            br = new BufferedReader(streamReader);
            //Also check for empty lines and white spaces
            while (br.ready()) {
                String thisLine = br.readLine();
                thisLine = thisLine.trim();
                if (thisLine.length() > 0) {
                    lastLine = thisLine; 
                }
            }
        } catch (Exception e) {
            System.out.println(e.toString());
            currentStatus = "Logfile doesn't exist. | | | | | Configured filepath = " + logFile.getAbsolutePath();
            return;
        }
        /*Get timestamp without microseconds. 
         * No support for microseconds in Java SimpleDateFormat 
         * Refer to http://docs.oracle.com/javase/7/docs/api/java/text/SimpleDateFormat.html
         * Example of lastLine: 
         * 2013-05-28 11:11:52.617000    data01QE2_130409_OPL1013_CvA_Bonemarrow_TiOx_S5.raw    running
         * 2013-05-28 13:04:16.180000    QE1_130108_OPL1005_YL_lysRIVM_NKI_BRCA_H1.raw    completed
         */
        lastLine = lastLine.trim();
        if (lastLine.startsWith("20")) { //Hopefully not the Y2K problem!!!!
            StringTokenizer stkz = new StringTokenizer(lastLine, ".");
            String timeStamp = stkz.nextToken();
            DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            try {
                Date logDate = sdf.parse(timeStamp);
                Date currentDate = new Date (System.currentTimeMillis());
                //in milliseconds
                long diff = currentDate.getTime() - logDate.getTime();
                long diffSeconds = diff / 1000 % 60;
                long diffMinutes = diff / (60 * 1000) % 60;
                long diffHours = diff / (60 * 60 * 1000) % 24;
                long diffDays = diff / (24 * 60 * 60 * 1000);
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
                    //Remove trailing .RAW from runningMsrunName
                    int msrunLength = runningMsrunName.length();
                    runningMsrunName = runningMsrunName.substring(0, msrunLength - 4); //Remove trailing .RAW extension
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
            owner.notifyProgressLogFileChanged(getCurrentStatus());
        } else {
            owner.notifyProgressLogFileChanged(getCurrentStatus());
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
            owner.pushPipelineStatus();
        }
      }
}

