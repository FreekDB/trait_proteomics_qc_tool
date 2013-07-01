package nl.ctmm.trait.proteomics.qcviewer.input;

import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.StringTokenizer;

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

    private HashMap<String, String> allMetricsMap = null;
    public JsonMetricsReader(MetricsParser mParser) {
        //Keys of allMetricsMap to be used for reading JSON metrics values
        allMetricsMap = mParser.getMetricsListing();
    }
    
    /**
     * Read the QC parameters from the json file.
     * @param jsonFile the json file that contains the QC parameters.
     * @return Hashmap containing names of QC metrics and their values - as read from the jSON file
     */
    public HashMap<String, String> readJsonValues(final File jsonFile) {
        HashMap<String, String> metricsValues = new HashMap<String, String>();
        Object[] keys = allMetricsMap.keySet().toArray();
        //Initialize metricsValues to N/A
        try {
            final JSONObject jsonObject = (JSONObject) new JSONParser().parse(new FileReader(jsonFile));
            for (int i = 0; i < keys.length; ++i) {
                String paramValue = "N/A";
                //Split the key into jsonObject and parameters
                StringTokenizer stkz = new StringTokenizer((String) keys[i], ":");
                String objectName = stkz.nextToken();
                String paramName = stkz.nextToken();
                //Check whether json file contains objectname
                if (jsonObject.containsKey(objectName)) {
                    JSONObject jObject = (JSONObject) jsonObject.get(objectName);
                    if (jObject.containsKey(paramName)) {
                        if (paramName.equals("date") || paramName.equals("runtime")) { 
                            paramValue = (String) jObject.get(paramName);
                        } else {
                            paramValue = (String) ((JSONArray) jObject.get(paramName)).get(1);
                        }
                    }
                }
                metricsValues.put((String) keys[i], paramValue);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return metricsValues;
    }
    
}
