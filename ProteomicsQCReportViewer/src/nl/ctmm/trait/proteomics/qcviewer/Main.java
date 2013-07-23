package nl.ctmm.trait.proteomics.qcviewer;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
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

import org.jfree.ui.RefineryUtilities;

/**
 * The class that starts the QC Report Viewer.
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
    private static final Main instance = new Main();
    private Properties applicationProperties;
    private MetricsParser metricsParser;
    private String pipelineStatus = "";
    private Date fromDate, tillDate;
    private ViewerFrame frame;
    private DataEntryForm dataEntryForm;
    private int reportNum;
    //reportUnitsTable holds all the current reports in memory 
    //New reports are added as they are generated by the pipeline
    private Map<String, ReportUnit> reportUnitsTable = new HashMap<>();
    //Reads the pipeline log file - qc_status.log from the preferredRootDirectory
    private ProgressLogReader progressLogReader;
    //Monitors the pipeline log file - qc_status.log from the preferredRootDirectory
    private ProgressLogMonitor progressLogMonitor;
    //The directory to which QC pipeline writes the QC reports
    private String preferredRootDirectory;

    /**
     * The starting method for the QC Report Viewer.
     *
     * @param arguments the command-line arguments, which are currently not used.
     */
    public static void main(final String[] arguments) {
        getInstance().runReportViewer();
    }

    /**
     * Get the main instance.
     * 
     * @return the main instance.
     */
    public static Main getInstance() {
        return instance;
    }

    /**
     * The constructor is private so only the singleton instance can be used.
     */
    private Main() {
    }

    /**
     * Start the QC Report Viewer.
     *
     * TODO: see whether we can update the application instead of restarting it. [Freek]
     */
    public void runReportViewer() {
    	prepareLogger();
        applicationProperties = loadProperties();
        metricsParser = new MetricsParser(applicationProperties);
        preferredRootDirectory = applicationProperties.getProperty(Constants.PROPERTY_ROOT_FOLDER);
        logger.fine("in Main preferredRootDirectory = " + preferredRootDirectory);
        final String progressLogFilePath = preferredRootDirectory + "\\" + Constants.PROPERTY_PROGRESS_LOG;
        logger.fine("progressLogFilePath = " + progressLogFilePath);
        progressLogReader = new ProgressLogReader(progressLogFilePath);
        pipelineStatus = progressLogReader.getCurrentStatus();
        
        dataEntryForm = new DataEntryForm(this, applicationProperties);
        dataEntryForm.setRootDirectoryName(preferredRootDirectory);
        dataEntryForm.displayInitialDialog();

        //Determine date interval for which to display reports for
        final SimpleDateFormat sdf = new SimpleDateFormat(Constants.SIMPLE_DATE_FORMAT_STRING);
        sdf.setLenient(false);
        final String reportsFromDate = applicationProperties.getProperty(Constants.PROPERTY_SHOW_REPORTS_FROM_DATE);
        final String reportsTillDate = applicationProperties.getProperty(Constants.PROPERTY_SHOW_REPORTS_TILL_DATE);
        if (!reportsFromDate.trim().equals("") && !reportsFromDate.trim().equals("")) { //Dates are specified
            //Check date format validity
            try {
                //if not valid, it will throw ParseException
                fromDate = sdf.parse(reportsFromDate);
                tillDate = sdf.parse(reportsTillDate);
                logger.fine("fromDate = " + fromDate.toString() + " tillDate = " + tillDate.toString());
                logger.fine("fromDate = " + sdf.format(fromDate) + " tillDate = " + sdf.format(tillDate));
            } catch (final ParseException e) {
                fromDate = null;
                tillDate = null;
                logger.log(Level.SEVERE, "Something went wrong while processing fromDate and tillDate", e);
            }
        }
        if (tillDate == null) { //The date interval is not specified. 
            tillDate = new Date(); //Till Date is current date
            final Calendar now = Calendar.getInstance();
            now.add(Calendar.DATE, -14);
            fromDate = now.getTime();
            logger.fine("fromDate = " + sdf.format(fromDate) + " tillDate = " + sdf.format(tillDate));
        }
        logger.fine("fromDate = " + sdf.format(fromDate) + " tillDate = " + sdf.format(tillDate));
        //Obtain initial set of reports according to date filter
        processInitialReports();
        //Start the progress log monitor to monitor qc_status.log file
        // TODO: keep a reference to this progressLogMonitor (declare as a field)? [Freek]
        progressLogMonitor = ProgressLogMonitor.getInstance();
        try {
            progressLogMonitor.addFileChangeListener(progressLogReader, progressLogFilePath, 5000);
        } catch (FileNotFoundException e1) {
            logger.log(Level.SEVERE, "progress log file not found. Configured path: " + progressLogFilePath, e1);
        } //Refresh period is 5 seconds
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
            final FileInputStream fileInputStream = new FileInputStream(Constants.PROPERTIES_FILE_NAME);
            appProperties.load(fileInputStream);
            fileInputStream.close();
        } catch (final IOException e) {
            logger.log(Level.SEVERE, "Loading of application properties failed.", e);
        }
        return appProperties;
    }

    
	/**
     * Prepare the logger for this class
     * Set ConsoleHandler as handler
     * Set logging level to ALL
     * 
     */
    private void prepareLogger() {
    	//Set logger and handler levels to Level.ALL
    	logger.setLevel(Level.ALL);
        ConsoleHandler handler = new ConsoleHandler();
        handler.setLevel(Level.ALL);
        logger.addHandler(handler);
	}
    
    /**
     * Read initial set of QC Reports from the preferredRootDirectory. The reports are filtered according to date
     * criteria.
     */
    private void processInitialReports() {
        logger.fine("Reading initial set of reports..");
        final String runningMsrunName = progressLogReader.getRunningMsrunName();
        final List<ReportUnit> reportUnits = getReportUnits(preferredRootDirectory, fromDate, tillDate);
        //Reinitialize reportUnitsTable
        reportUnitsTable = new HashMap<>();
        reportNum = 0; 
        final List<ReportUnit> displayableReportUnits = new ArrayList<>();
        //populate reportUnitsTable
        final int reportUnitsSize = reportUnits.size();
        logger.fine("All reportUnitsSize = " + reportUnitsSize + " runningMsrunName = " + runningMsrunName);
        for (final ReportUnit thisUnit : reportUnits) {
            final String thisMsrun = thisUnit.getMsrunName();
            if (!thisMsrun.equals(runningMsrunName)) { //Currently processing this msrun. Do not include in the report
                ++reportNum;
                thisUnit.setReportNum(reportNum);
                //for identifying duplicate reports
                if (reportUnitsTable.containsKey(thisMsrun)) {
                    logger.warning("Alert!! Already exists in ReportUnitsTable " + thisMsrun);
                }
                //Update reportUnit in reportUnitsTable
                reportUnitsTable.put(thisUnit.getMsrunName(), thisUnit);
                displayableReportUnits.add(thisUnit);
            } else {
                logger.fine("Skipped report unit " + thisMsrun + " Logfile says it is running " + runningMsrunName);
            }
        }
        dataEntryForm.disposeInitialDialog();
        logger.fine("ReportUnitsTable size is " + reportUnitsTable.size());
        if (reportUnits.size() == 0) { //There exist no reports in current root directory
            //Get new location to read reports from
            dataEntryForm.displayErrorMessage("No Reports found in " + preferredRootDirectory);
            dataEntryForm.displayRootDirectoryChooser();
        } else {
            //Always start with GUI version
            reportUnits.removeAll(reportUnits); //clear reportUnits
            //Start main user interface
            startQCReportViewerGui(applicationProperties, displayableReportUnits, pipelineStatus);
        }
    }
    
    /**Progress log file has changed.
     * Refresh ReportViewer automatically on this notification
     */
    public void notifyProgressLogFileChanged(final String newPipelineStatus) {
        /* The tillDate has to be updated as currentTime - since the pipeline status has changed.
        * FromDate could be specified by the user
        */
        final String runningMsrunName = progressLogReader.getRunningMsrunName();
        final Calendar now = Calendar.getInstance();
        tillDate = now.getTime();
        final String preferredRootDirectory = applicationProperties.getProperty(Constants.PROPERTY_ROOT_FOLDER);
        final List<ReportUnit> reportUnits = getReportUnits(preferredRootDirectory, fromDate, tillDate);
        if (reportUnits.size() == 0) { //There exist no reports in current root directory
              //Get new location to read reports from
               dataEntryForm.displayErrorMessage("No Reports found in " + preferredRootDirectory);
               dataEntryForm.displayRootDirectoryChooser();
        } else {
            //Compare newReportUnits with reportUnits
            final List<ReportUnit> newReportUnits = new ArrayList<>();
            int numUpdates = 0;
            for (final ReportUnit thisUnit : reportUnits) {
                //if not in reportUnits, then add to newReportUnits
                final String thisMsrun = thisUnit.getMsrunName();
                if (!thisMsrun.equals(runningMsrunName)) {
                    // Can someone explain the comment below? [Freek] - Explained [Pravin]
                    /* The pipeline is currently processing runningMsrunName. e.g. 
                     * 2013-06-04 13:40:01.165000    QE2_101109_OPL0004_TSV_mousecelllineL_Q1_2.raw    running
                     * The QC report is being generated. 
                     * Hence do not add this report yet to the reportUnitsTable.  
                     */
                    if (reportUnitsTable.containsKey(thisMsrun)) {
                        ReportUnit existingUnit = reportUnitsTable.get(thisMsrun);
                        int existingNum = existingUnit.getReportNum();
                        //Assign this number to thisUnit and update reportUnitsTable
                        thisUnit.setReportNum(existingNum);
                        reportUnitsTable.remove(thisMsrun);
                        reportUnitsTable.put(thisMsrun, thisUnit);
                        numUpdates++;
                    } else {
                        reportNum++;
                        logger.fine("Does not exist in reportUnitsTable. " + thisUnit.getMsrunName() +
                                    " Adding to new report units with reportNum " + reportNum);
                        thisUnit.setReportNum(reportNum);
                        newReportUnits.add(thisUnit);
                        //Add to hashTable
                        reportUnitsTable.put(thisUnit.getMsrunName(), thisUnit);
                    }
                } else {
                    logger.fine("Skipped report unit " + thisMsrun +
                                " Logfile says it is running " + runningMsrunName);
                }
            }
            logger.fine("ReportUnitsTable size is " + reportUnitsTable.size() + " Updated " + numUpdates +
                        " entries. newReportUnits size is " + newReportUnits.size());
            reportUnits.clear();
            //Refresh ViewerFrame with new Report Units
            frame.updateReportUnits(newReportUnits, newPipelineStatus);
        }
    }

    /**
     * Get the report units from the directory structure below the root directory.
     *
     * @param rootDirectoryName the root directory to search in.
     * @param fromDate the start of the date range to search.
     * @param tillDate the end of the date range to search.
     * @return the list with report units.
     */
    private List<ReportUnit> getReportUnits(final String rootDirectoryName, final Date fromDate, final Date tillDate) {
        return new ReportReader(metricsParser).retrieveReports(rootDirectoryName, fromDate, tillDate);
    }

    /**Received notification about change in pipeline status.
     * Push new pipeline status to the report viewer
     * @param newPipelineStatus Updated pipeline status as read from the qc_status.log file
     */
    public void notifyUpdatePipelineStatus(final String newPipelineStatus) {
        /* Refresh ReportViewer automatically on this notification
         */
        pipelineStatus = newPipelineStatus;
        //Refresh ViewerFrame pipelineStatus
        if (frame != null) {
            frame.updatePipelineStatus(pipelineStatus);
        }
    }

    /**
     * Create and start the GUI - of QC Report Viewer.
     *
     * @param appProperties the application properties.
     * @param reportUnits   the report units to be displayed.
     */
    private void startQCReportViewerGui(final Properties appProperties, final List<ReportUnit> reportUnits,
                                        final String pipelineStatus) {
        logger.fine("Main startQCReportViewerGui");
        final List<String> qcParamNames = getColumnNames(appProperties, Constants.PROPERTY_TOP_COLUMN_NAMESV2);
        //Create ViewerFrame and set it visible
        frame = new ViewerFrame(metricsParser, appProperties, Constants.APPLICATION_NAME + " " +
                                Constants.APPLICATION_VERSION, reportUnits, qcParamNames, pipelineStatus);
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
}
