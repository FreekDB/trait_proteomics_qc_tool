package nl.ctmm.trait.proteomics.qcviewer.utils;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import nl.ctmm.trait.proteomics.qcviewer.gui.SortedListModel;

import org.apache.commons.io.FilenameUtils;

/**
 * The class for updating the appProperties file. 
 *
 * @author <a href="mailto:pravin.pawar@nbic.nl">Pravin Pawar</a>
 * @author <a href="mailto:freek.de.bruijn@nbic.nl">Freek de Bruijn</a>
 */

public class PropertyFileWriter {

    /**
     * The logger for this class.
     */
    private static final Logger logger = Logger.getLogger(PropertyFileWriter.class.getName());

    /**
     * The application properties.
     * 
     * TODO: maybe it's better to put the responsibility for writing properties in a separate Settings class? [Freek]
     */
    private static Properties appProperties;
    
    /**
     * Set application properties for further usage. 
     * 
     * @param appProperties the application properties.
     */
    public static void setApplicationProperties(final Properties appProperties) {
        PropertyFileWriter.appProperties = appProperties;
    }
    
    
    /**
     * Save application properties to the property file 
     */
    private static void saveApplicationProperties() {
        try {
            final FileOutputStream outputStream = new FileOutputStream(FilenameUtils.normalize(Constants.PROPERTIES_FILE_NAME));
            appProperties.store(outputStream, null);
            outputStream.close();
        } catch (final IOException e) {
            final String message = "Something went wrong while writing the application properties file: %s.";
            logger.log(Level.SEVERE, String.format(message, Constants.PROPERTIES_FILE_NAME), e);
        }
    }
    
    /**
     * Save new from and till dates in the application properties file. 
     * 
     * @param fromDate Date from which reports are to be shown
     * @param tillDate Date till which reports are to be shown
     */
    public static void updateFromAndTillDates(final String fromDate, final String tillDate) {
        appProperties.setProperty(Constants.PROPERTY_SHOW_REPORTS_FROM_DATE, fromDate);
        appProperties.setProperty(Constants.PROPERTY_SHOW_REPORTS_TILL_DATE, tillDate);
        saveApplicationProperties();
    }
    
    /**
     * Update metrics in the report frame and update the TopColumnNamesV2 property in the application properties file.
     *
     * TODO: looks like updating the GUI is done elsewhere. If that's the case, we can rename this method. [Freek]
     * [Pravin] Renamed from updateMetricsToDisplay(...) to updateMetricsSelection(...) 
     * 
     * @param selectedMetrics a sorted list containing the names of the selected metrics.
     */
    public static void updateMetricsSelection(final SortedListModel selectedMetrics) {
        logger.fine("Updating metrics selection in the property file..");
        String selectedMetricsString = "";
        for (int metricIndex = 0; metricIndex < selectedMetrics.getSize(); metricIndex++) {
            selectedMetricsString += selectedMetrics.getElementAt(metricIndex) + ",";
        }
        appProperties.setProperty(Constants.PROPERTY_TOP_COLUMN_NAMESV2, selectedMetricsString);
        saveApplicationProperties();
    }
    
    /**
     * Save selected root directory in the application properties file
     * @param newRootDirectory Root directory selected by user
     */
   public static void updatePreferredRootDirectory(String newRootDirectory) {
       logger.fine("Changing root directory to " + newRootDirectory);
       appProperties.setProperty(Constants.PROPERTY_ROOT_FOLDER, newRootDirectory);
       saveApplicationProperties();
   }

}
