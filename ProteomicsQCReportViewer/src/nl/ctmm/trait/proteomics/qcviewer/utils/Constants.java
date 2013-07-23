package nl.ctmm.trait.proteomics.qcviewer.utils;

import java.awt.Font;

import org.apache.commons.io.FilenameUtils;

/**
 * This interface contains the most important constants of the project.
 *
 * @author <a href="mailto:pravin.pawar@nbic.nl">Pravin Pawar</a>
 * @author <a href="mailto:freek.de.bruijn@nbic.nl">Freek de Bruijn</a>
 */
public interface Constants {
    static final String APPLICATION_NAME = "MSQC Report Viewer";
    static final String APPLICATION_VERSION = "1.6.5";
    static final String PROPERTIES_FILE_NAME = FilenameUtils.normalize("appProperties");
    static final String PROPERTY_ROOT_FOLDER = FilenameUtils.normalize("RootFolder");
    static final String PROPERTY_PROGRESS_LOG = "qc_status.log";
    static final String DEFAULT_ROOT_FOLDER = FilenameUtils.normalize("QCReports");
    static final String PROPERTY_TOP_COLUMN_NAMESV2 = "TopColumnNamesV2";
    static final String PROPERTY_GUI_VERSION = "GUIVersion"; 
    static final String PROPERTY_SHOW_REPORTS_FROM_DATE = "ShowReportsFromDate";
    static final String PROPERTY_SHOW_REPORTS_TILL_DATE = "ShowReportsTillDate";
    static final String SIMPLE_DATE_FORMAT_STRING = "dd/MM/yyyy";
    static final String PROPERTY_METRICS_LISTING_FILE = FilenameUtils.normalize("MetricsListing.txt");
    static final String PROPERTY_PROJECT_LOGO_FILE = FilenameUtils.normalize("images\\traitctmm.jpg");
    static final String PROPERTY_NIST_LOGO_FILE = FilenameUtils.normalize("images\\nistlogo.jpg");
    static final String PROPERTY_NBIC_LOGO_FILE = FilenameUtils.normalize("images\\nbiclogo.png");
    static final String PROPERTY_OPL_LOGO_FILE = FilenameUtils.normalize("images\\opllogo.jpg");
    static final String PROPERTY_CTMM_LOGO_FILE = FilenameUtils.normalize("images\\ctmmlogo.jpg");

    /**
     * The name of the font used in the GUI.
     */
    static final String FONT_NAME = "Garamond";

    /**
     * The default font.
     */
    static final Font DEFAULT_FONT = new Font(FONT_NAME, Font.BOLD, 11);

    /**
     * The font used for the report numbers.
     */
    static final Font REPORT_NUMBER_FONT = new Font(Constants.FONT_NAME, Font.BOLD, 22);

    /**
     * The font used for the titles in the charts.
     */
    static final Font CHART_TITLE_FONT = new Font("Garamond", Font.BOLD, 13);

    /**
     * The font used for all the metrics in the details frame and the text areas in the about frame.
     */
    static final Font PLAIN_FONT = new Font("Garamond", Font.PLAIN, 11);

    /**
     * The font used for the metrics headers in the details frame.
     */
    static final Font DETAILS_HEADER_FONT = new Font("Garamond", Font.BOLD, 12);
}
