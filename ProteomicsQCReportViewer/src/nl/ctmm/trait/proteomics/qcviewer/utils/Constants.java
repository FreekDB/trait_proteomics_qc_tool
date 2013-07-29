package nl.ctmm.trait.proteomics.qcviewer.utils;

import java.awt.Font;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

/**
 * This interface contains the most important constants of the project.
 *
 * @author <a href="mailto:pravin.pawar@nbic.nl">Pravin Pawar</a>
 * @author <a href="mailto:freek.de.bruijn@nbic.nl">Freek de Bruijn</a>
 */
public interface Constants {
    /**
     * Application name.
     */
    String APPLICATION_NAME = "Proteomics QC Report Viewer";

    /**
     * Application version.
     */
    String APPLICATION_VERSION = "1.6.5";

    /**
     * Application title.
     */
    String APPLICATION_TITLE = APPLICATION_NAME + " " + APPLICATION_VERSION;

    /**
     * Name of application properties file.
     */
    String PROPERTIES_FILE_NAME = "appProperties";

    /**
     * Property name for the folder to retrieve QC reports from.
     */
    String PROPERTY_ROOT_FOLDER = "RootFolder";

    /**
     * Default property value for the folder to retrieve QC reports from.
     */
    String DEFAULT_ROOT_FOLDER = "QCReports";

    /**
     * Name of the progress log file to monitor for new QC reports.
     *
     * TODO: rename to PROGRESS_LOG_FILE_NAME, since it's not a property. [Freek] Done [Pravin]
     */
    String PROGRESS_LOG_FILE_NAME = "qc_status.log";

    /**
     * Property name for the initial metrics to show.
     */
    String PROPERTY_TOP_COLUMN_NAMESV2 = "TopColumnNamesV2";


    /**
     * Property name for the start date of the QC reports to show.
     */
    String PROPERTY_SHOW_REPORTS_FROM_DATE = "ShowReportsFromDate";

    /**
     * Property name for the end date of the QC reports to show.
     */
    String PROPERTY_SHOW_REPORTS_TILL_DATE = "ShowReportsTillDate";

//    String DEFAULT_REPORTS_DISPLAY_PERIOD = "DefaultReportsDisplayPeriod";
//    String DEFAULT_REPORTS_DISPLAY_PERIOD_VALUE = "14"; //show reports from last two weeks by default

    /**
     * Date format string used for parsing dates.
     *
     * TODO: use DATE_FORMAT below instead of this string? [Freek]
     * 
     * [Pravin] The SIMPLE_DATE_FORMAT_STRING is used as it is. 
     * e.g. fromDateChooser.setDateFormatString(Constants.SIMPLE_DATE_FORMAT_STRING);
     */
    String SIMPLE_DATE_FORMAT_STRING = "dd/MM/yyyy";

    /**
     * Date format used for parsing dates.
     */
    DateFormat DATE_FORMAT = new SimpleDateFormat(SIMPLE_DATE_FORMAT_STRING);

    /**
     * Name of the file with all QC metrics.
     *
     * TODO: rename to METRICS_LISTING_FILE_NAME, since it's not a property. [Freek] Done. [Pravin]
     */
    String METRICS_LISTING_FILE_NAME = "MetricsListing.txt";

    /**
     * Name of the CTMM TraIT logo file.
     *
     * TODO: rename to CTMM_TRAIT_LOGO_FILE_NAME, since PROJECT is not very specific and it's not a property. [Freek] Done. [Pravin]
     */
    String CTMM_TRAIT_LOGO_FILE_NAME = "images\\traitctmmlogo.png";

    /**
     * Name of the NIST logo file.
     *
     * TODO: rename to _LOGO_FILE_NAME, since it's not a property. [Freek] Done. [Pravin]
     */
    String NIST_LOGO_FILE_NAME = "images\\nistlogo.jpg";

    /**
     * Name of the NBIC logo file.
     *
     * TODO: rename to NBIC_LOGO_FILE_NAME, since it's not a property. [Freek] Done. [Pravin]
     */
    String NBIC_LOGO_FILE_NAME = "images\\nbiclogo.png";

    /**
     * Name of the OPL logo file.
     *
     * TODO: rename to OPL_LOGO_FILE_NAME, since it's not a property. [Freek] Done. [Pravin]
     */
    String OPL_LOGO_FILE_NAME = "images\\opllogo.jpg";

    /**
     * Name of the CTMM logo file.
     *
     * TODO: rename to CTMM_LOGO_FILE_NAME, since it's not a property. [Freek] Done. [Pravin]
     */
    String CTMM_LOGO_FILE_NAME = "images\\ctmmlogo.jpg";

    /**
     * The name of the font used in the GUI.
     */
    String FONT_NAME = "Garamond";

    /**
     * The default font.
     */
    Font DEFAULT_FONT = new Font(FONT_NAME, Font.BOLD, 11);

    /**
     * The font used for the report numbers.
     */
    Font REPORT_NUMBER_FONT = new Font(Constants.FONT_NAME, Font.BOLD, 22);

    /**
     * The font used for the titles in the charts.
     */
    Font CHART_TITLE_FONT = new Font(FONT_NAME, Font.BOLD, 13);

    /**
     * The font used for all the metrics in the details frame and the text areas in the about frame.
     */
    Font PLAIN_FONT = new Font(FONT_NAME, Font.PLAIN, 11);

    /**
     * The font used for the metrics headers in the details frame.
     */
    Font DETAILS_HEADER_FONT = new Font(FONT_NAME, Font.BOLD, 12);
}
