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
    public void readJsonValues(final File jsonFile, final ReportUnit reportUnit) {
        logger.fine("IN readJsonValues - reading file " + jsonFile.getName());
        try {
            final JSONObject jsonObject = (JSONObject) new JSONParser().parse(new FileReader(jsonFile));
            Object[] keys = allMetricsMap.keySet().toArray();
            for (int i = 0; i < keys.length; ++i) {
            	//Split the key into jsonObject and parameters
            	StringTokenizer stkz = new StringTokenizer((String) keys[i], ":");
            	String objectName = stkz.nextToken();
            	String paramName = stkz.nextToken();
            	JSONObject jObject = (JSONObject) jsonObject.get(objectName);
            	String paramValue = "";
            	System.out.print("Object = " + objectName + " paramName = " + paramName);
            	if (paramName.equals("date") || paramName.equals("runtime")) { 
            		paramValue = (String) jObject.get(paramName);
            	} else {
            		paramValue = (String) ((JSONArray) jObject.get(paramName)).get(1);
            	}
            	System.out.println(" paramValue = " + paramValue);
            }
            /*final JSONObject genericObject = (JSONObject) jsonObject.get("generic");
            reportUnit.setFileSizeString((String) ((JSONArray) genericObject.get("f_size")).get(1));
            reportUnit.setMs1Spectra((String) ((JSONArray) genericObject.get("ms1_spectra")).get(1));
            reportUnit.setMs2Spectra((String) ((JSONArray) genericObject.get("ms2_spectra")).get(1));
            reportUnit.setMeasured((String) genericObject.get("date"));
            reportUnit.setRuntime((String) genericObject.get("runtime"));*/
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
	
}
