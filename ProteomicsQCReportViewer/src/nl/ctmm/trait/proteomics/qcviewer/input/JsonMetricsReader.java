package nl.ctmm.trait.proteomics.qcviewer.input;

import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

/**
 * The class for reading metrics values from .json file
 *
 * @author <a href="mailto:pravin.pawar@nbic.nl">Pravin Pawar</a>
 * @author <a href="mailto:freek.de.bruijn@nbic.nl">Freek de Bruijn</a>
 */
public class JsonMetricsReader {
	/**
     * The logger for this class.
     */
    private static final Logger logger = Logger.getLogger(JsonMetricsReader.class.getName());
    private Map<String, String> allMetricsMap;

    public JsonMetricsReader(final MetricsParser metricsParser) {
        //Keys of allMetricsMap to be used for reading JSON metrics values
        allMetricsMap = metricsParser.getMetricsListing();
    }
    
	/**
     * Read the QC parameters from the json file.
     * @param jsonFile the json file that contains the QC parameters.
     * @return map containing names of QC metrics and their values - as read from the jSON file
     */
    public Map<String, String> readJsonValues(final File jsonFile) {
        final Map<String, String> metricsValues = new HashMap<>();
        try {
            final JSONObject jsonObject = (JSONObject) new JSONParser().parse(new FileReader(jsonFile));
            for (final String key : allMetricsMap.keySet()) {
                //Initialize metricsValues to N/A
                String paramValue = "N/A";
                //Split the key into jsonObject and parameters
                final StringTokenizer keyTokenizer = new StringTokenizer(key, ":");
                final String objectName = keyTokenizer.nextToken();
                final String paramName = keyTokenizer.nextToken();
                //Check whether json file contains object name
                if (jsonObject.containsKey(objectName)) {
                    final JSONObject jObject = (JSONObject) jsonObject.get(objectName);
                    if (jObject.containsKey(paramName)) {
                        if (paramName.equals("date") || paramName.equals("runtime")) {
                            paramValue = (String) jObject.get(paramName);
                        } else {
                            paramValue = (String) ((JSONArray) jObject.get(paramName)).get(1);
                        }
                    }
                }
                metricsValues.put(key, paramValue);
            }
        } catch (Exception e) {
        	logger.log(Level.SEVERE, "Something went wrong while reading JSON file", e);
        }
        return metricsValues;
    }
}
