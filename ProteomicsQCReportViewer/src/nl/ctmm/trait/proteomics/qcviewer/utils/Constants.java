package nl.ctmm.trait.proteomics.qcviewer.utils;

/**
 * This interface contains the most important constants of the project.
 *
 * @author <a href="mailto:pravin.pawar@nbic.nl">Pravin Pawar</a>
 * @author <a href="mailto:freek.de.bruijn@nbic.nl">Freek de Bruijn</a>
 */
public interface Constants {
    static final String APPLICATION_NAME = "MSQC Report Viewer";
    static final String APPLICATION_VERSION = "1.6.5";
    static final String PROPERTIES_FILE_NAME = "appProperties";
    static final String PROPERTY_ROOT_FOLDER = "RootFolder";
    static final String PROPERTY_PROGRESS_LOG = "qc_status.log";
    static final String DEFAULT_ROOT_FOLDER = "QCReports\\ctmm";
    static final String PROPERTY_TOP_COLUMN_NAMESV1 = "TopColumnNamesV1";
    static final String PROPERTY_TOP_COLUMN_NAMESV2 = "TopColumnNamesV2";
    static final String DEFAULT_TOP_COLUMN_NAMES = "No., File Size, MS1 Spectra, MS2 Spectra, Measured, Runtime, " +
                                                   "Heatmap, Ioncount";
    static final String PROPERTY_BOTTOM_COLUMN_NAMES = "BottomColumnNames";
    static final String DEFAULT_BOTTOM_COLUMN_NAMES = "Heatmap, Ioncount";
    static final String PROPERTY_GUI_VERSION = "GUIVersion"; 
    static final String PROPERTY_PREFERRED_WEBSERVER = "Server";
    static final String SERVER_LINK_POSTAMBLE = ":8080/ctmm/";
    static final String PROPERTY_SHOW_REPORTS_FROM_DATE = "ShowReportsFromDate";
    static final String PROPERTY_SHOW_REPORTS_TILL_DATE = "ShowReportsTillDate";
    static final String DEFALUT_REPORTS_DISPLAY_PERIOD = "DefaultReportsDisplayPeriod";
    static final String DEFALUT_REPORTS_DISPLAY_PERIOD_VALUE = "14"; //show reports from last two weeks by default
    static final String SIMPLE_DATE_FORMAT_STRING = "dd/MM/yyyy";
    static final String PROPERTY_METRICS_LISTING_FILE = "MetricsListing.txt";
    static final String PROPERTY_PROJECT_LOGO_FILE = "images\\traitctmm.jpg";
    static final String PROPERTY_NIST_LOGO_FILE = "images\\nistlogo.jpg";
    static final String PROPERTY_NBIC_LOGO_FILE = "images\\nbiclogo.png";
    static final String PROPERTY_OPL_LOGO_FILE = "images\\opllogo.jpg";
    static final String PROPERTY_CTMM_LOGO_FILE = "images\\ctmmlogo.jpg";
}
