package nl.ctmm.trait.proteomics.qcviewer;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
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
 * TODO: fix JCalendar dependency: [Freek]
 * http://search.maven.org/#artifactdetails%7Ccom.toedter%7Cjcalendar%7C1.3.2%7Cjar
 * http://mvnrepository.com/artifact/com.toedter/jcalendar
 * https://ci.nbiceng.net/nexus/index.html#view-repositories;thirdparty~uploadPanel
 * http://stackoverflow.com/questions/4029532/upload-artifacts-to-nexus-without-maven
 *
 * TODO: move nl directory in source directory to source\main\java directory. [Freek]
 * TODO: move test directory to source\test\java directory. [Freek]
 * TODO: move images directory (with logo image files) to source\main\resources directory. [Freek]
 * TODO: change QE*.raw file names into less descriptive names (see ProgressLogReader.parseCurrentStatus). [Freek]
 * TODO: make all sort radio buttons visible again. [Freek]
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
     * Message written to the logger to show the number of reports.
     */
    private static final String NUMBER_OF_REPORTS_MESSAGE = "Number of reports is %s.";
    
    /**
     * Message written to the logger when log file is not found.
     */
    private static final String LOG_FILE_NOT_FOUND_MESSAGE = "progress log file not found. Configured path: %s";
    /**
     * Message written to the logger when no reports are found.
     */
    private static final String NO_REPORTS_MESSAGE = "No reports found in %s.";

    /**
     * Message written to the logger to report number of report unit keys.
     */
    private static final String SIZE_REPORTS_KEYS_MESSAGE = "Size of report units keys: %d.";
    
    /**
     * Message written to the logger when something goes wrong while processing dates.
     */
    private static final String INCORRECT_DATES_MESSAGE = "Something went wrong while processing fromDate and tillDate";

    /**
     * Message written to the logger for printing from and till dates.
     */
    private static final String FROM_AND_TILL_DATES_MESSAGE = "fromDate = %s tillDate = %s";

    /**
     * Message written to the logger for printing progress log file path.
     */
    private static final String LOG_FILE_PATH_MESSAGE = "progressLogFilePath = %s";

    /**
     * Message written to the logger for printing preferred root directory.
     */
    private static final String PREFERRED_ROOT_DIRECTORY_MESSAGE = "Preferred root directory = %s"; 

    /**
     * Message written to the logger if the xception occurs while loading application properties.
     */
    private static final String APP_PROPERTIES_EXCEPTION_MESSAGE = 
                                            "Loading of application properties failed.";

    
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
     * The list contains keys of reports being displayed in the main user interface.
     * This list is updated for every new report. 
     */
    private List<String> reportUnitsKeys = new ArrayList<>();
    
    /**
     * Reader for the pipeline log file - qc_status.log from the preferredRootDirectory.
     */
    private ProgressLogReader progressLogReader;

    /**
     * logFileFlag represents the success/failure of progressLogReader setup. 
     */
    private boolean logFileFlag;
    
    /**
     * The directory to which the QC pipeline writes the QC reports.
     */
    private String preferredRootDirectory;

    /**
     * Name of the RAW file being processed by the QC pipeline.
     */
    private String runningMsrunName;
    
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
        //Determine absolute path of preferred root directory
        normalizePreferredRootDirectory();
        dataEntryForm = new DataEntryForm();
        dataEntryForm.displayInitialDialog(preferredRootDirectory);
        //Determine fromDate and TillDate range to select the reports
        determineReportDateRange();
        //Set progress log reader and running msrun name
        setProgressLogReaderAndRunningMsrunName();
        metricsParser = new MetricsParser();

        //Obtain displayable set of reports 
        final Map<String, ReportUnit> reportUnitsTable = getDisplayableReportUnitsTable();
        logger.fine(String.format(NUMBER_OF_REPORTS_MESSAGE, reportUnitsTable.size()));
        final List<ReportUnit> displayableReportUnits = new ArrayList<>(reportUnitsTable.values());
        //Sort displayableReportUnits in ascending order of report index
        Collections.sort(displayableReportUnits, ReportUnit.getComparator(Constants.SORT_KEY_REPORT_INDEX, true));
        //Start main user interface
        startQCReportViewerGui(applicationProperties, displayableReportUnits, pipelineStatus);
        dataEntryForm.disposeInitialDialog();
        if (reportUnitsTable.size() == 0) {
            // There are no reports in the current root directory. Obtain new directory location from the user. 
            dataEntryForm.displayNoReportsFoundDialogue(preferredRootDirectory);
        } else {
            //Maintain reportUnitsKeys
            reportUnitsKeys = new ArrayList<>(reportUnitsTable.keySet());
            logger.fine(String.format(SIZE_REPORTS_KEYS_MESSAGE, reportUnitsKeys.size()));
        }
        startProgressLogFileMonitor();
    }
    
    /**
     * Setup progressLogReader to read current pipeline status.
     * Check whether pipeline is processing RAW file. If yes, determine
     * name of the RAW file being processed - represented by runningMsrunName. 
     */
    private void setProgressLogReaderAndRunningMsrunName() {
        //Monitor pipeline status file to periodically obtain pipeline status
        logFileFlag = setProgressLogReader();
        //Check whether pipeline is processing RAW file - using runningMsrunName
        if (logFileFlag) {
            runningMsrunName = progressLogReader.getRunningMsrunName();
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
                logger.log(Level.SEVERE, INCORRECT_DATES_MESSAGE, e);
            }
        }
        if (tillDate == null) {
            // The date interval is not specified: make it the last two weeks.
            final Calendar now = Calendar.getInstance();
            now.add(Calendar.WEEK_OF_YEAR, -2);
            fromDate = now.getTime();
            tillDate = Calendar.getInstance().getTime();
        }
        logger.fine(String.format(FROM_AND_TILL_DATES_MESSAGE, fromDate, tillDate));
    }

    /**
     * Determine progress log file path.
     * Setup progressLogReader to read current pipeline status periodically.
     * 
     * @return true if progress log file exists, otherwise return false. 
     */
    private boolean setProgressLogReader() {
        final String progressLogFilePath = FilenameUtils.normalize(preferredRootDirectory + "\\"
                + Constants.PROGRESS_LOG_FILE_NAME);
        logger.fine(String.format(LOG_FILE_PATH_MESSAGE, progressLogFilePath));
        progressLogReader = ProgressLogReader.getInstance(); 
        if (progressLogReader.setProgressLogFile(progressLogFilePath)) {
            pipelineStatus = progressLogReader.getCurrentStatus();
            return true;
        } else {
            logger.fine(String.format(LOG_FILE_NOT_FOUND_MESSAGE, progressLogFilePath));
            return false;
        }
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
            logger.log(Level.SEVERE, APP_PROPERTIES_EXCEPTION_MESSAGE, e);
        }
        PropertyFileWriter.getInstance().setApplicationProperties(appProperties);
        return appProperties;
    }

    /**
     * Read preferred root directory from the application properties and 
     * normalize its absolute path.  
     */
    private void normalizePreferredRootDirectory() {
        preferredRootDirectory = loadProperties().getProperty(Constants.PROPERTY_ROOT_FOLDER);
        preferredRootDirectory = Paths.get(preferredRootDirectory).toAbsolutePath().normalize().toString();
        logger.fine(String.format(PREFERRED_ROOT_DIRECTORY_MESSAGE, preferredRootDirectory));
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
        runningMsrunName = progressLogReader.getRunningMsrunName();
        tillDate = Calendar.getInstance().getTime();
        final Map<String, ReportUnit> reportUnitsTable = getDisplayableReportUnitsTable(); 
        if (reportUnitsTable.size() == 0) {
            // There exist no reports in current root directory.
            // Get new location to read reports from.
            dataEntryForm.displayErrorMessage(String.format(NO_REPORTS_MESSAGE, preferredRootDirectory));
            dataEntryForm.displayRootDirectoryChooser();
        } else {
            final List<ReportUnit> newReportUnits = new ArrayList<>(reportUnitsTable.values());
            //Sort newReportUnits in ascending order of report index
            Collections.sort(newReportUnits, ReportUnit.getComparator(Constants.SORT_KEY_REPORT_INDEX, true));
            //Maintain reportUnitsKeys
            reportUnitsKeys.addAll(new ArrayList<>(reportUnitsTable.keySet()));
            logger.fine(String.format(SIZE_REPORTS_KEYS_MESSAGE, reportUnitsKeys.size()));
            reportUnitsTable.clear(); 
            //Refresh ViewerFrame with new Report Units
            frame.updateReportUnits(newReportUnits, newPipelineStatus, false);
        }
    }

    /**
     * Get the report units from the directory structure below the root directory.
     * @return the list with report units. 
     */
    private Map<String, ReportUnit> getDisplayableReportUnitsTable() {
        /** preferredRootDirectory the root directory to search in.
        * runningMsrunName  the name of the currently running msrun.
        * reportUnitsKeys   list containing names of report units being displayed.
        * fromDate          the start of the date range to search.
        * tillDate          the end of the date range to search.
        * */
        return new ReportReader(metricsParser).retrieveReports(preferredRootDirectory, runningMsrunName, 
                reportUnitsKeys, fromDate, tillDate);
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
          final List<String> qcParamNames = getColumnNames(appProperties, Constants.PROPERTY_TOP_COLUMN_NAMESV2);
        //Create ViewerFrame and set it visible
        frame = new ViewerFrame(metricsParser, Constants.APPLICATION_TITLE, reportUnits, qcParamNames, pipelineStatus);
        frame.pack();
        RefineryUtilities.centerFrameOnScreen(frame);
        frame.setVisible(true);
    }

    /**
     * Start monitoring progress log file.
     */
    private void startProgressLogFileMonitor() {
        if (logFileFlag) {
            progressLogReader.startProgressLogFileMonitor();
        }
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
     * TODO: can we decrease code duplication between the runReportViewer and updateReportViewer methods? [Freek]
     * [Pravin] Split common code in intuitive functions normalizePreferredRootDirectory(),
     * setProgressLogReaderAndRunningMsrunName() and startProgressLogFileMonitor().
     * @param directoryChanged if true, the root directory has changed
     */
    public void updateReportViewer(final boolean directoryChanged) {
        logger.fine("Main updateReportViewer");
        normalizePreferredRootDirectory(); 
        if (directoryChanged) {
            setProgressLogReaderAndRunningMsrunName();
        }
        determineReportDateRange();
        //Clear reportUnitsKeys 
        reportUnitsKeys.clear();
        //Obtain set of reports to be displayed in the viewer
        final Map<String, ReportUnit> reportUnitsTable = getDisplayableReportUnitsTable();
        logger.fine(String.format(NUMBER_OF_REPORTS_MESSAGE, reportUnitsTable.size()));
        if (reportUnitsTable.size() == 0) {
            // There exist no reports in selected root directory conforming date range.
            dataEntryForm.displayNoReportsFoundDialogue(preferredRootDirectory);
        } else {
            //Maintain reportUnitsKeys
            reportUnitsKeys = new ArrayList<>(reportUnitsTable.keySet());
            logger.fine(String.format(SIZE_REPORTS_KEYS_MESSAGE, reportUnitsKeys.size()));
            final List<ReportUnit> displayableReportUnits = new ArrayList<>(reportUnitsTable.values());
            //Sort displayableReportUnits in ascending order of report index
            Collections.sort(displayableReportUnits, ReportUnit.getComparator(Constants.SORT_KEY_REPORT_INDEX, true));
            // Refresh ViewerFrame with new Report Units.
            frame.updateReportUnits(displayableReportUnits, progressLogReader.getCurrentStatus(), true);
        }
        startProgressLogFileMonitor();
    }
}
