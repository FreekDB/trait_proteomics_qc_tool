package nl.ctmm.trait.proteomics.qcviewer.input;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import nl.ctmm.trait.proteomics.qcviewer.gui.SortedListModel;
import nl.ctmm.trait.proteomics.qcviewer.utils.Constants;

/**
 * The class for parsing metrics IDs and names from MetricsListing.txt file
 *
 * @author <a href="mailto:pravin.pawar@nbic.nl">Pravin Pawar</a>
 * @author <a href="mailto:freek.de.bruijn@nbic.nl">Freek de Bruijn</a>
 */
public class MetricsParser {
    private Properties appProperties;
    private Map<String, String> allMetricsMap;
    
    /**
     * Constructor
     * @param appProperties Application properties
     */
    public MetricsParser(final Properties appProperties) {
        this.appProperties = appProperties;
    }
    
    /*
     * Reads all the metrics from MetricsListing.txt file
     */
    private void readMetricsListing() {
        // Create a HashMap which stores Strings as the keys and values
        allMetricsMap = new HashMap<>();
        try {
            final BufferedReader reader = new BufferedReader(new FileReader(Constants.PROPERTY_METRICS_LISTING_FILE));
            String line;
            while ((line = reader.readLine()) != null) {
                final StringTokenizer lineTokenizer = new StringTokenizer(line, ":");
                // The first two tokens together form key of the metric e.g. "ion:is-1b".
                final String key = lineTokenizer.nextToken() + ":" + lineTokenizer.nextToken();
                // The second token is name of the metric.
                final String value = lineTokenizer.nextToken();
                allMetricsMap.put(key, value);
            }
            reader.close();
        } catch (final NumberFormatException | IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Get all the metrics read from MetricsListing.txt file
     * @return map containing key and value of metrics
     * e.g. Key: dyn:ds-1a Value: Ratios of Peptide Ions IDed (Once/Twice)
     */
    public Map<String, String> getMetricsListing() {
        if (allMetricsMap == null) {
            readMetricsListing();
        }
        return allMetricsMap;
    }
    
    /**
     * Update metrics in the report frame
     * Update TopColumnNamesV2 property from appProperties file 
     * @param move A sorted list containing names of selected metrics
     */
    public void updateMetricsToDisplay(final SortedListModel move) {
        String selectedMetrics = "";
        for (int metricIndex = 0; metricIndex < move.getSize(); ++metricIndex) {
            selectedMetrics = selectedMetrics.concat(move.getElementAt(metricIndex) + ",");
        }
        appProperties.setProperty(Constants.PROPERTY_TOP_COLUMN_NAMESV2, selectedMetrics);
        try {
            final FileOutputStream outputStream = new FileOutputStream(Constants.PROPERTIES_FILE_NAME);
            appProperties.store(outputStream, null);
            outputStream.close();
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }
}
