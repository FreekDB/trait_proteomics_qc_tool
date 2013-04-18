package nl.ctmm.trait.proteomics.qcviewer.input;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Properties;
import java.util.StringTokenizer;

import nl.ctmm.trait.proteomics.qcviewer.gui.SortedListModel;
import nl.ctmm.trait.proteomics.qcviewer.utils.Constants;

public class MetricsParser {

	private Properties appProperties = null; 
	private HashMap<String,String> allMetricsMap = null;
	
	public MetricsParser(final Properties appProperties) {
		this.appProperties = appProperties;
	}
	
	/*
	 * Reads all the metrics from MetricsListing.txt file
	 */
    private void readMetricsListing() {
    	// Create a HashMap which stores Strings as the keys and values
    	allMetricsMap = new HashMap<String,String>();
    	BufferedReader br = null;
    	try {
    		br = new BufferedReader(new FileReader(Constants.PROPERTY_METRICS_LISTING_FILE));
    	} catch (FileNotFoundException e) {
    		e.printStackTrace();
    	}
        String line;
        try {
    		while ((line = br.readLine()) != null) {
    		    StringTokenizer st = new StringTokenizer(line, ":");
    		    // The first two tokens together form key of the metrics e.g. ion:is-1b
    		    String key = st.nextToken() + ":" + st.nextToken();
    		    // The second token is name of the metrics
    		    String value = st.nextToken();
    		    allMetricsMap.put(key, value);
    		}
    	    br.close();
    	} catch (NumberFormatException | IOException e) {
    		e.printStackTrace();
    	}
    }
    
	/*
	 * Get all the metrics read from MetricsListing.txt file
	 */
    public HashMap<String,String> getMetricsListing() {
    	if (allMetricsMap == null) {
    		readMetricsListing();
    	}
    	return allMetricsMap;
    }
    
    /**
     * Update TopColumnNamesV2 property from appProperties file 
     */
    public void updateMetricsToDisplay(SortedListModel move) {
    	String selectedMetrics = "";
    	for (int i = 0; i < move.getSize(); ++i) { 
    		selectedMetrics = selectedMetrics.concat((String)move.getElementAt(i) + ",");
    	}
     	appProperties.setProperty(Constants.PROPERTY_TOP_COLUMN_NAMESV2, selectedMetrics);
		try {
			FileOutputStream out = new FileOutputStream(Constants.PROPERTIES_FILE_NAME);
			appProperties.store(out, null);
	    	out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
	
}
