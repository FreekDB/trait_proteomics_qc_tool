package nl.ctmm.trait.proteomics.qcviewer.utils;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import nl.ctmm.trait.proteomics.qcviewer.gui.SortedListModel;

import org.apache.commons.io.FilenameUtils;

/**
 * The class for updating the application properties file.
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
     * This is the singleton instance of this class.
     */
    private static final PropertyFileWriter INSTANCE = new PropertyFileWriter();

    /**
     * The application properties.
     * 
     * TODO: maybe it's better to put the responsibility for writing properties in a separate Settings class? [Freek]
     */
    private Properties appProperties;

    /**
     * Hidden constructor.
     */
    private PropertyFileWriter() {
    }

    /**
     * Get the singleton instance.
     *
     * @return the singleton instance.
     */
    public static PropertyFileWriter getInstance() {
        return INSTANCE;
    }

    /**
     * Set application properties for further usage. 
     * 
     * @param appProperties the application properties.
     */
    public void setApplicationProperties(final Properties appProperties) {
        this.appProperties = appProperties;
    }

    /**
     * Save the selected root directory in the application properties file.
     *
     * @param newRootDirectory root directory selected by user.
     */
    public void updatePreferredRootDirectory(final String newRootDirectory) {
        logger.fine("Changing root directory to " + newRootDirectory);
        appProperties.setProperty(Constants.PROPERTY_ROOT_FOLDER, newRootDirectory);
        saveApplicationProperties();
    }

    /**
     * Save new from and till dates in the application properties file.
     *
     * @param fromDate Date from which reports are to be shown
     * @param tillDate Date till which reports are to be shown
     */
    public void updateFromAndTillDates(final String fromDate, final String tillDate) {
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
    public void updateMetricsSelection(final SortedListModel selectedMetrics) {
        logger.fine("Updating metrics selection in the property file..");
        final StringBuilder selectedMetricsStringBuilder = new StringBuilder();
        for (int metricIndex = 0; metricIndex < selectedMetrics.getSize(); metricIndex++) {
            selectedMetricsStringBuilder.append(selectedMetrics.getElementAt(metricIndex));
            selectedMetricsStringBuilder.append(",");
        }
        appProperties.setProperty(Constants.PROPERTY_TOP_COLUMN_NAMESV2, selectedMetricsStringBuilder.toString());
        saveApplicationProperties();
    }

    /**
     * Save application properties to the property file.
     */
    private void saveApplicationProperties() {
        try {
            final String fileName = FilenameUtils.normalize(Constants.PROPERTIES_FILE_NAME);
            final FileOutputStream outputStream = new FileOutputStream(fileName);
            INSTANCE.appProperties.store(outputStream, null);
            outputStream.close();
        } catch (final IOException e) {
            final String message = "Something went wrong while writing the application properties file: %s.";
            logger.log(Level.SEVERE, String.format(message, Constants.PROPERTIES_FILE_NAME), e);
        }
    }
}
