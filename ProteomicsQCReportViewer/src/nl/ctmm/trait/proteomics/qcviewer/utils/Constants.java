package nl.ctmm.trait.proteomics.qcviewer.utils;

/**
 * This interface contains the most important constants of the project.
 *
 * @author <a href="mailto:pravin.pawar@nbic.nl">Pravin Pawar</a>
 * @author <a href="mailto:freek.de.bruijn@nbic.nl">Freek de Bruijn</a>
 */
public interface Constants {
    static final String APPLICATION_NAME = "QC Report Viewer";
    static final String APPLICATION_VERSION = "1.1.4";
    static final String PROPERTIES_FILE_NAME = "appProperties";
    static final String PROPERTY_ROOT_FOLDER = "RootFolder";
    static final String DEFAULT_ROOT_FOLDER = "QCReports\\ctmm";
    static final String PROPERTY_TOP_COLUMN_NAMESV1 = "TopColumnNamesV1";
    static final String PROPERTY_TOP_COLUMN_NAMESV2 = "TopColumnNamesV2";
    static final String DEFAULT_TOP_COLUMN_NAMES = "No., File Size, MS1 Spectra, MS2 Spectra, Measured, Runtime, " +
                                                   "Heatmap, Ioncount";
    static final String PROPERTY_BOTTOM_COLUMN_NAMES = "BottomColumnNames";
    static final String DEFAULT_BOTTOM_COLUMN_NAMES = "Heatmap, Ioncount";
    static final String PROPERTY_GUI_VERSION = "GUIVersion"; 
    static final String PROPERTY_PREFERRED_SERVER = "Server";
}
