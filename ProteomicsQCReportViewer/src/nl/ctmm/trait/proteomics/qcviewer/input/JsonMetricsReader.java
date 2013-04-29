package nl.ctmm.trait.proteomics.qcviewer.input;

import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.logging.Logger;

import nl.ctmm.trait.proteomics.qcviewer.gui.SortedListModel;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class JsonMetricsReader {

	private static final Logger logger = Logger.getLogger(ReportReader.class.getName());
	private MetricsParser mParser = null; 
	private HashMap<String, String> allMetricsMap = null;
	public JsonMetricsReader(MetricsParser mParser) {
		this.mParser = mParser;
		//Keys of allMetricsMap to be used for reading JSON metrics values
		allMetricsMap = mParser.getMetricsListing();
	}
	
    /**
     * Read the QC parameters from the json file.
     *
     * @param jsonFile   the json file that contains the QC parameters.
     * @param reportUnit the report unit where the QC parameters will be stored.
     */
    public HashMap<String, String> readJsonValues(final File jsonFile) {
        System.out.println("IN readJsonValues - reading file " + jsonFile.getName());
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
            	//Uncomment following line to include generic parameter. 
            	//if (jsonObject.containsKey(objectName)) {
            	//Comment following line to include generic parameter
            	if (!objectName.equals("generic") && jsonObject.containsKey(objectName)) {
            		JSONObject jObject = (JSONObject) jsonObject.get(objectName);
                	//System.out.println("Key = " + (String) keys[i] + " Object = " + objectName + " paramName = " + paramName);
                	if (paramName.equals("date") || paramName.equals("runtime")) { 
                		paramValue = (String) jObject.get(paramName);
                	} else {
                		paramValue = (String) ((JSONArray) jObject.get(paramName)).get(1);
                	}
            	}
            	metricsValues.put((String) keys[i], paramValue);
            	//System.out.println("Key = " + (String) keys[i] + " paramValue = " + paramValue);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return metricsValues;
    }
	
}
