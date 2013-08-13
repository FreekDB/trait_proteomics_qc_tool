package nl.ctmm.trait.proteomics.qcviewer;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import nl.ctmm.trait.proteomics.qcviewer.gui.DataEntryForm;
import nl.ctmm.trait.proteomics.qcviewer.gui.ViewerFrame;
import nl.ctmm.trait.proteomics.qcviewer.input.MetricsParser;
import nl.ctmm.trait.proteomics.qcviewer.input.ProgressLogMonitor;
import nl.ctmm.trait.proteomics.qcviewer.input.ProgressLogReader;
import nl.ctmm.trait.proteomics.qcviewer.input.ReportReader;
import nl.ctmm.trait.proteomics.qcviewer.input.ReportUnit;
import nl.ctmm.trait.proteomics.qcviewer.utils.Constants;
import nl.ctmm.trait.proteomics.qcviewer.utils.PropertyFileWriter;

import org.apache.commons.io.FilenameUtils;
import org.jfree.ui.RefineryUtilities;

/**
 * The class that starts the QC Report Viewer.
 *
 * TODO: fix JCalendar dependency:
 * http://search.maven.org/#artifactdetails%7Ccom.toedter%7Cjcalendar%7C1.3.2%7Cjar
 * http://mvnrepository.com/artifact/com.toedter/jcalendar
 * https://ci.nbiceng.net/nexus/index.html#view-repositories;thirdparty~uploadPanel
 * http://stackoverflow.com/questions/4029532/upload-artifacts-to-nexus-without-maven
 *
 * TODO: move nl directory in source directory to source\main\java directory. [Freek]
 * TODO: move test directory to source\test\java directory. [Freek]
 * TODO: move images directory (with logo image files) to source\main\resources directory. [Freek]
 * TODO: change QE*.raw file names into less descriptive names (see ProgressLogReader.parseCurrentStatus). [Freek]
 * 
 * TODO: Use HashMap<String, ReportUnit> instead of Arraylist <ReportUnit> [Pravin]
 * TODO: Refactor Main.java and create flowchart [Pravin]
 *  
 * @author <a href="mailto:pravin.pawar@nbic.nl">Pravin Pawar</a>
 * @author <a href="mailto:freek.de.bruijn@nbic.nl">Freek de Bruijn</a>
 */
public class Main {
    /**
     * The logger for this class.
     */
    private static final Logger logger = Logger.getLogger(Main.class.getName());
    /**
     * This is the singleton instance of this class.
     */
    private static final Main INSTANCE = new Main();

    /**
     * Message written to the logger when a report is skipped.
     */
    private static final String SKIPPED_REPORT_MESSAGE = "Skipped report unit %s. Logfile says it is running %s.";

    /**
     * Message written to the logger to show the number of reports.
     */
    private static final String NUMBER_OF_REPORTS_MESSAGE = "Number of reports is %s.";

    /**
     * Message written to the logger to show the updated number of reports.
     */
    private static final String NEW_NUMBER_OF_REPORTS_MESSAGE = "Updated %s entries. New number of reports is %s.";

    /**
     * Message written to the logger when no reports are found.
     */
    private static final String NO_REPORTS_MESSAGE = "No reports found in %s.";

    /**
     * The application properties such as root folder and default metrics to show.
     */
    private Properties applicationProperties;

    /**
     * The parser that can read all metrics supported by the NIST pipeline.
     */
    private MetricsParser metricsParser;

    /**
     * The current pipeline status like idle or currently analyzing ...
     */
    private String pipelineStatus = "";

    /**
     * Start of the date range of report units to include for display.
     */
    private Date fromDate;

    /**
     * End of the date range of report units to include for display.
     */
    private Date tillDate;

    /**
     * The main GUI frame of the viewer application.
     */
    private ViewerFrame frame;

    /**
     * Support for selecting the root directory to read the report units from and specifying the preferred date range.
     * <p/>
     * TODO: do we need to keep this around or can we construct it if needed? [Freek]
     */
    private DataEntryForm dataEntryForm;

    /**
     * The map that contains all current reports within the date range. New reports are added as they are generated by
     * the pipeline. The msrun names are used as keys in the map.
     */
    //private Map<String, ReportUnit> reportUnitsTable = new HashMap<>();

    /**
     * The list contains keys of reports being displayed in the main user interface.
     * This list is updated for every new report. 
     */
    private List<String> reportUnitsKeys = new ArrayList<String>();
    
    /**
     * Reader for the pipeline log file - qc_status.log from the preferredRootDirectory.
     */
    private ProgressLogReader progressLogReader;

    /**
     * Record progressLogFilePath to remove listener in progressLogMonitor.
     */
    private String currentProgressLogFilePath;

    /**
     * Another way to monitor the pipeline log file - qc_status.log from the preferredRootDirectory.
     * <p/>
     * TODO: this can probably be removed because ProgressLogReader is sufficient. Check with Sander and Thang first.
     */
    @SuppressWarnings("FieldCanBeLocal")
    private ProgressLogMonitor progressLogMonitor;

    /**
     * The directory to which the QC pipeline writes the QC reports.
     */
    private String preferredRootDirectory;

    /**
     * The constructor is private so only the singleton instance can be used.
     */
    private Main() {
    }

    /**
     * The starting method for the QC Report Viewer.
     *
     * @param arguments the command-line arguments, which are currently not used.
     */
    // CHECKSTYLE_OFF: UncommentedMain
    public static void main(final String[] arguments) {
        getInstance().runReportViewer();
    }
    // CHECKSTYLE_ON: UncommentedMain

    /**
     * Get the main instance.
     *
     * @return the main instance.
     */
    public static Main getInstance() {
        return INSTANCE;
    }

    /**
     * Get the from date.
     *
     * @return the from date.
     */
    public Date getFromDate() {
        return fromDate;
    }

    /**
     * Get the till date.
     *
     * @return the till date.
     */
    public Date getTillDate() {
        return tillDate;
    }

    /**
     * Start the QC Report Viewer.
     * <p/>
     * TODO: see whether we can update the application instead of restarting it. [Freek]
     * Added updateReportViewer method [Pravin]
     */
    public void runReportViewer() {
        prepareAllLoggers();
        applicationProperties = loadProperties();
        preferredRootDirectory = applicationProperties.getProperty(Constants.PROPERTY_ROOT_FOLDER);
        /*Experimenting with toAbsolutePath().normalize() to make sure that root directory can be 
         * specified like: ../ProteomicsQCPipeline/Reports. [Pravin]*/
        preferredRootDirectory = Paths.get(preferredRootDirectory).toAbsolutePath().normalize().toString();
        dataEntryForm = new DataEntryForm();
        dataEntryForm.setRootDirectoryName(preferredRootDirectory);
        dataEntryForm.displayInitialDialog();
        logger.fine("in Main preferredRootDirectory = " + preferredRootDirectory);
        metricsParser = new MetricsParser();
        //Determine fromDate and TillDate range to select the reports
        determineReportDateRange();
        //Monitor pipeline status file to periodically obtain pipeline status
        final boolean logFileFlag = setProgressLogReader();
        //Check whether pipeline is processing RAW file - using runningMsrunName
        String runningMsrunName = "";
        if (logFileFlag) {
            runningMsrunName = progressLogReader.getRunningMsrunName();
        }
        //Obtain initial set of reports according to date filter
        Map<String, ReportUnit> reportUnitsTable = getReportUnits(preferredRootDirectory, runningMsrunName, fromDate, tillDate);
        logger.fine(String.format(NUMBER_OF_REPORTS_MESSAGE, reportUnitsTable.size()));
        List<ReportUnit> displayableReportUnits = new ArrayList<ReportUnit>(reportUnitsTable.values());
        //Maintain reportUnitsKeys
        reportUnitsKeys = new ArrayList<String> (reportUnitsTable.keySet());
        logger.fine("Size of report units keys = " + reportUnitsKeys);
        //Start main user interface
        startQCReportViewerGui(applicationProperties, displayableReportUnits, pipelineStatus);
        dataEntryForm.disposeInitialDialog();
        if (displayableReportUnits.size() == 0) {
            // There are no reports in the current root directory. Obtain new directory location from the user. 
            dataEntryForm.displayNoReportsFoundDialogue(preferredRootDirectory);
        } 
        //Start monitoring progress log file after starting main user interface
        if (logFileFlag) {
            progressLogReader.startProgressLogFileMonitor();
        }
    }
    
       /**
     * Determine the date range for displaying reports.
     */
    private void determineReportDateRange() {
        Constants.DATE_FORMAT.setLenient(false);
        final String reportsFromDate = applicationProperties.getProperty(Constants.PROPERTY_SHOW_REPORTS_FROM_DATE);
        final String reportsTillDate = applicationProperties.getProperty(Constants.PROPERTY_SHOW_REPORTS_TILL_DATE);
        if (!"".equals(reportsFromDate.trim()) && !"".equals(reportsFromDate.trim())) {
            // Dates are specified. Now check if the dated are valid.
            try {
                fromDate = Constants.DATE_FORMAT.parse(reportsFromDate);
                tillDate = Constants.DATE_FORMAT.parse(reportsTillDate);
            } catch (final ParseException e) {
                fromDate = null;
                tillDate = null;
                logger.log(Level.SEVERE, "Something went wrong while processing fromDate and tillDate", e);
            }
        }
        if (tillDate == null) {
            // The date interval is not specified: make it the last two weeks.
            final Calendar now = Calendar.getInstance();
            now.add(Calendar.WEEK_OF_YEAR, -2);
            fromDate = now.getTime();
            tillDate = Calendar.getInstance().getTime();
        }
        logger.fine("fromDate = " + Constants.DATE_FORMAT.format(fromDate) + " tillDate = "
                    + Constants.DATE_FORMAT.format(tillDate));
    }

    /**
     * Determine progress log file path.
     * Setup progressLogReader to read current pipeline status.
     * Setup progressLogMonitor to monitor changes to progress log file.
     * 
     * @return true if progress log file exists, otherwise return false. 
     */
    private boolean setProgressLogReader() {
        final String progressLogFilePath = FilenameUtils.normalize(preferredRootDirectory + "\\"
                + Constants.PROGRESS_LOG_FILE_NAME);
        logger.fine("progressLogFilePath = " + progressLogFilePath);
        progressLogReader = ProgressLogReader.getInstance(); 
        if (progressLogReader.setProgressLogFile(progressLogFilePath)) {
            pipelineStatus = progressLogReader.getCurrentStatus();
            //Start the progress log monitor to monitor qc_status.log file
            // TODO: keep a reference to this progressLogMonitor (declare as a field)? [Freek]
            progressLogMonitor = ProgressLogMonitor.getInstance();
            try {
                if (currentProgressLogFilePath != null) {
                    progressLogMonitor.removeFileChangeListener(progressLogReader, currentProgressLogFilePath);
                }
                progressLogMonitor.addFileChangeListener(progressLogReader, progressLogFilePath,
                                                         Constants.POLL_INTERVAL_PIPELINE_LOG);
                currentProgressLogFilePath = progressLogFilePath; 
            } catch (final FileNotFoundException e1) {
                e1.printStackTrace();
                logger.fine("progress log file not found. Configured path: " + progressLogFilePath);
                return false;
            } //Refresh period is 5 seconds
            return true;
        } else return false;
    }

    /**
     * Prepare the loggers for this application:
     * - set ConsoleHandler as handler.
     * - set logging level to ALL.
     */
    private void prepareAllLoggers() {
        final ConsoleHandler handler = new ConsoleHandler();
        handler.setLevel(Level.ALL);
        final Logger rootLogger = Logger.getLogger("nl.ctmm.trait.proteomics.qcviewer");
        rootLogger.setLevel(Level.ALL);
        rootLogger.addHandler(handler);
    }

    /**
     * Load the application properties from the properties file.
     *
     * @return the application properties.
     */
    private Properties loadProperties() {
        final Properties appProperties = new Properties();
        // Set a default for root folder property.
        appProperties.setProperty(Constants.PROPERTY_ROOT_FOLDER, Constants.DEFAULT_ROOT_FOLDER);
        // Load the actual properties from the property file.
        try {
            final String fileName = FilenameUtils.normalize(Constants.PROPERTIES_FILE_NAME);
            final FileInputStream fileInputStream = new FileInputStream(fileName);
            appProperties.load(fileInputStream);
            fileInputStream.close();
        } catch (final IOException e) {
            logger.log(Level.SEVERE, "Loading of application properties failed.", e);
        }
        PropertyFileWriter.setApplicationProperties(appProperties); 
        return appProperties;
    }

    /**
     * Progress log file has changed. Refresh the application automatically on this notification.
     *
     * @param newPipelineStatus the new status of the QC pipeline.
     */
    public void notifyProgressLogFileChanged(final String newPipelineStatus) {
        /* The tillDate has to be updated as currentTime - since the pipeline status has changed.
        * FromDate could be specified by the user
        */
        final String runningMsrunName = progressLogReader.getRunningMsrunName();
        tillDate = Calendar.getInstance().getTime();
        // TODO: can we use the preferredRootDirectory field below? [Freek] Yes & done [Pravin]
        final Map<String, ReportUnit> reportUnitsTable = getReportUnits(preferredRootDirectory, runningMsrunName, fromDate, tillDate);
        if (reportUnitsTable.size() == 0) {
            // There exist no reports in current root directory.
            // Get new location to read reports from.
            dataEntryForm.displayErrorMessage(String.format(NO_REPORTS_MESSAGE, preferredRootDirectory));
            dataEntryForm.displayRootDirectoryChooser();
        } else {
            //Compare newReportUnits with reportUnits
            //TODO: Compare newReportUnitsTable and reportUnitsTable to obtain newReportUnits
            ArrayList<ReportUnit> newReportUnits = new ArrayList<ReportUnit>(reportUnitsTable.values());
            //Maintain reportUnitsKeys
            reportUnitsKeys.addAll(new ArrayList<String> (reportUnitsTable.keySet()));
            logger.fine("Size of report units keys = " + reportUnitsKeys);
            reportUnitsTable.clear(); 
            //Refresh ViewerFrame with new Report Units
            frame.updateReportUnits(newReportUnits, newPipelineStatus, false);
        }
    }

    /**
     * Get the report units from the directory structure below the root directory.
     *
     * @param rootDirectoryName the root directory to search in.
     * @param runningMsrunName 
     * @param fromDate          the start of the date range to search.
     * @param tillDate          the end of the date range to search.
     * @return the list with report units.
     */
    private Map<String, ReportUnit> getReportUnits(final String rootDirectoryName, final String runningMsrunName, final Date fromDate, final Date tillDate) {
        return new ReportReader(metricsParser).retrieveReports(rootDirectoryName, runningMsrunName, reportUnitsKeys, fromDate, tillDate);
    }

    /**
     * Received a notification about a possible change in the QC pipeline status. If the status has indeed changed, push
     * the new pipeline status to the report viewer.
     *
     * @param newPipelineStatus updated pipeline status as read from the qc_status.log file.
     */
    public void notifyUpdatePipelineStatus(final String newPipelineStatus) {
        if (!pipelineStatus.equals(newPipelineStatus)) {
            pipelineStatus = newPipelineStatus;
            if (frame != null) {
                frame.updatePipelineStatus(pipelineStatus);
            }
        }
    }

    /**
     * Create and start the GUI - of the report viewer.
     *
     * @param appProperties  the application properties.
     * @param reportUnits    the report units to be displayed.
     * @param pipelineStatus the current status of the pipeline.
     */
    private void startQCReportViewerGui(final Properties appProperties, final List<ReportUnit> reportUnits,
                                        final String pipelineStatus) {
        logger.fine("Main startQCReportViewerGui");
        final List<String> qcParamNames = getColumnNames(appProperties, Constants.PROPERTY_TOP_COLUMN_NAMESV2);
        //Create ViewerFrame and set it visible
        frame = new ViewerFrame(metricsParser, Constants.APPLICATION_TITLE, reportUnits, qcParamNames, pipelineStatus);
        frame.pack();
        RefineryUtilities.centerFrameOnScreen(frame);
        frame.setVisible(true);
    }

    /**
     * Get a property with a comma-separated string with column names and convert it into a list of strings.
     *
     * @param applicationProperties the application properties.
     * @param propertyName          the name of the property that contains the comma-separated string with column names.
     * @return the column names in a list of strings.
     */
    private List<String> getColumnNames(final Properties applicationProperties, final String propertyName) {
        return Arrays.asList(applicationProperties.getProperty(propertyName).split(","));
    }
    
    
    /* TODO: updateReportViewer: why is the call to progressLogOperations needed? [Freek]
     * The progress log file (qc_status.log) is usually located inside the root directory.
     * On changes to the root directory, it is required to change the log file path.
     * Hence the call to progressLogOperations is needed to make sure that correct 
     * qc_status.log file is to be used. [Pravin]
     */
    
    /**
     * Update all the reports in the QC Report Viewer. It is called in the following two cases:
     * 1) New root directory is chosen.
     * 2) Change in the date range.
     * 
     * @param directoryChanged if true, the root directory has changed
     */
    
    public void updateReportViewer(final boolean directoryChanged) {
        logger.fine("Main updateReportViewer");
        preferredRootDirectory = loadProperties().getProperty(Constants.PROPERTY_ROOT_FOLDER);
        preferredRootDirectory = Paths.get(preferredRootDirectory).toAbsolutePath().normalize().toString();
        boolean logFileFlag = false; 
        //Check whether pipeline is processing RAW file - using runningMsrunName
        String runningMsrunName = "";
        if (directoryChanged) {
            logFileFlag = setProgressLogReader();
            if (logFileFlag) {
                runningMsrunName = progressLogReader.getRunningMsrunName();
            }
        }
        determineReportDateRange();
        reportUnitsKeys.clear();
        logger.fine("Size of report units keys = " + reportUnitsKeys);
        //Obtain initial set of reports according to date filter
        Map<String, ReportUnit> reportUnitsTable = getReportUnits(preferredRootDirectory, runningMsrunName, fromDate, tillDate);
        logger.fine(String.format(NUMBER_OF_REPORTS_MESSAGE, reportUnitsTable.size()));
        //Maintain reportUnitsKeys
        reportUnitsKeys = new ArrayList<String> (reportUnitsTable.keySet());
        logger.fine("Size of report units keys = " + reportUnitsKeys);
        List<ReportUnit> displayableReportUnits = new ArrayList<ReportUnit>(reportUnitsTable.values());
        if (displayableReportUnits.size() == 0) {
            // There exist no reports in selected root directory conforming date range.
            // Get new location to read reports from.
            dataEntryForm.displayErrorMessage(String.format(NO_REPORTS_MESSAGE, preferredRootDirectory));
            dataEntryForm.displayRootDirectoryChooser();
        } else {
            // Refresh ViewerFrame with new Report Units.
            frame.updateReportUnits(displayableReportUnits, progressLogReader.getCurrentStatus(), true);
        }
        //Start monitoring progress log file after updating main user interface
        if (logFileFlag) {
            progressLogReader.startProgressLogFileMonitor();
        }
    }
}
