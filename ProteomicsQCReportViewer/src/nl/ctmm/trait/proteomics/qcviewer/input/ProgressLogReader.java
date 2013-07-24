package nl.ctmm.trait.proteomics.qcviewer.input;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.StringTokenizer;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import nl.ctmm.trait.proteomics.qcviewer.Main;
import nl.ctmm.trait.proteomics.qcviewer.utils.Constants;

/**
 * The class for reading qc_status.log file. It contains QC pipeline entries in following format:
 *
 * 2013-06-04 13:40:01.165000    QE2_101109_OPL0004_TSV_mousecelllineL_Q1_2.raw    running
 * 2013-06-04 13:40:01.191000    QE2_101109_OPL0004_TSV_mousecelllineL_Q1_2.raw    completed
 *
 * @author <a href="mailto:pravin.pawar@nbic.nl">Pravin Pawar</a>
 * @author <a href="mailto:freek.de.bruijn@nbic.nl">Freek de Bruijn</a>
 */
public class ProgressLogReader implements FileChangeListener {
    /**
    * The logger for this class.
    */
    private static final Logger logger = Logger.getLogger(ProgressLogReader.class.getName());
	private static final DateFormat LOG_FILE_DATE_TIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private String currentStatus = "";
    private String runningMsrunName = "";
    private Main owner;
    private File logFile;

    /**
     * Constructor of ProgressLogReader: Parses current status from the logFile and
     * initiates a timer to monitor changes in the logFile
     * @param progressLogFilePath Absolute path to the progressLogFile
     */
    public ProgressLogReader(final String progressLogFilePath) {
    	prepareLogger();
        this.owner = Main.getInstance();
        this.logFile = new File(progressLogFilePath);
        parseCurrentStatus(logFile);
        logger.fine("Current QC Pipeline Status: " + currentStatus);
        // Create timer, run timer thread as daemon.
        final Timer timer = new Timer(true);
        final StatusMonitorTask task = new StatusMonitorTask(this);
        timer.schedule(task, 5000, 5000);
    }

	/**
     * Prepare the logger for this class
     * Set ConsoleHandler as handler
     * Set logging level to ALL 
     */
    private void prepareLogger() {
    	//Set logger and handler levels to Level.ALL
    	logger.setLevel(Level.ALL);
        ConsoleHandler handler = new ConsoleHandler();
        handler.setLevel(Level.ALL);
        logger.addHandler(handler);
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
     * @param logFile the log file to parse
     */
    private void parseCurrentStatus(final File logFile) {
        final String lastLine = getLastLine(logFile);
        // Examples of last lines:
        // 2013-05-28 11:11:52.617000    data01QE2_130409_OPL1013_CvA_Bonemarrow_TiOx_S5.raw    running
        // 2013-05-28 13:04:16.180000    QE1_130108_OPL1005_YL_lysRIVM_NKI_BRCA_H1.raw    completed
        if (lastLine != null && lastLine.startsWith("20")) { //Hopefully not the Y2K problem!!!!
            try {
                // Get timestamp without microseconds.
                final String timeStamp = lastLine.substring(0, lastLine.indexOf('.'));
                final String duration = getDuration(timeStamp);
                if (lastLine.endsWith("running")) {
                    final StringTokenizer lineTokenizer = new StringTokenizer(lastLine);
                    lineTokenizer.nextToken();
                    lineTokenizer.nextToken();
                    final String rawFileName = lineTokenizer.nextToken();
                    currentStatus = "Currently analyzing " + rawFileName + " | | | | | Active for " + duration;
                    runningMsrunName = rawFileName.trim();
                    //Remove trailing .RAW from runningMsrunName
                    runningMsrunName = runningMsrunName.substring(0, runningMsrunName.length() - 4);
                } else if (lastLine.endsWith("completed")) {
                    runningMsrunName = "";
                    currentStatus = "Idle.. | | | | | Inactive for " + duration;
                }
            } catch (final ParseException e) {
                e.printStackTrace();
            }
        } else
            currentStatus = "QC pipeline logfile " + Constants.PROPERTY_PROGRESS_LOG + " is empty.";
    }

    /**
     * Retrieve last line from the logFile
     * @param logFile the log file to parse
     * @return the last line read from the file
     */
    private String getLastLine(final File logFile) {
        String lastLine = "";
        try {
            final InputStreamReader streamReader = new InputStreamReader(new FileInputStream(logFile));
            final BufferedReader bufferedReader = new BufferedReader(streamReader);
            //Also check for empty lines and white spaces
            while (bufferedReader.ready()) {
                final String thisLine = bufferedReader.readLine().trim();
                if (thisLine.length() > 0) {
                    lastLine = thisLine;
                }
            }
            bufferedReader.close();
        } catch (Exception e) {
        	
        	logger.log(Level.SEVERE, "Something went wrong while reading logfile " + logFile.getAbsolutePath(), e);
            currentStatus = "Logfile doesn't exist. | | | | | Configured file path = " + logFile.getAbsolutePath();
            lastLine = null;
        }
        return lastLine != null ? lastLine.trim() : lastLine;
    }

    /**
     * Get the time difference between a date/time string and the current date/time.
     * @param timeStamp the date/time string to compare to the current date/time.
     * @return the time difference as a string specifying elapsed days, hours, minutes and seconds.
     * @throws ParseException
     */
    private String getDuration(final String timeStamp) throws ParseException {
        final Date logDate = LOG_FILE_DATE_TIME_FORMAT.parse(timeStamp);
        //in milliseconds
        final long diff = System.currentTimeMillis() - logDate.getTime();
        final long diffSeconds = diff / 1000 % 60;
        final long diffMinutes = diff / (60 * 1000) % 60;
        final long diffHours = diff / (60 * 60 * 1000) % 24;
        final long diffDays = diff / (24 * 60 * 60 * 1000);
        return diffDays + " days, " + diffHours + " hours, " + diffMinutes + " minutes, " +
               diffSeconds + " seconds.";
    }

    @Override
    public void fileChanged(final File logFile) {
        logger.fine("ProgressLogReader: logFile changed. Refreshing current status..");
        parseCurrentStatus(logFile);
        logger.fine("Now current status is " + getCurrentStatus());
        owner.notifyProgressLogFileChanged(getCurrentStatus());
    }
    
    /**
     * File monitoring task.
     */
    class StatusMonitorTask extends TimerTask {
        private final ProgressLogReader owner;

        public StatusMonitorTask(ProgressLogReader owner) {
            this.owner = owner; 
        }

        public void run() {
            owner.pushPipelineStatus();
        }
    }
}
