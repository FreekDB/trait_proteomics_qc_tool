package nl.ctmm.trait.proteomics.qcviewer.input;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

import nl.ctmm.trait.proteomics.qcviewer.utils.Constants;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 * The class for reading metrics values from json files.
 *
 * @author <a href="mailto:pravin.pawar@nbic.nl">Pravin Pawar</a>
 * @author <a href="mailto:freek.de.bruijn@nbic.nl">Freek de Bruijn</a>
 */
public class JsonMetricsReader {
    /**
     * The logger for this class.
     */
    private static final Logger logger = Logger.getLogger(JsonMetricsReader.class.getName());
    
    /**
     * Message written to the logger in case exception occurs while reading JSON file. 
     */
    private static final String JSON_FILE_EXCEPTION_MESSAGE = 
                        "Something went wrong while reading JSON file.";

    /**
     * Name of parameter generic:date.
     */
    private static final String DATE_PARAM_NAME = "date";

    /**
     * Name of parameter generic:runtime. 
     */
    private static final String RUNTIME_PARAM_NAME = "runtime";

    /**
     * Name of the QuaMeter json object "qm".
     */
    private static final Object QUAMETER_OBJECT_NAME = "qm";
    
    /**
     * Colon string to be used as separator. 
     */
    private static final String COLON_SEPERATOR = ":";
    
    /**
     * The map with all metrics supported by the NIST QC pipeline. The keys are "category:code" strings and the values
     * are descriptions of the metrics.
     */
    private final Map<String, String> allMetricsMap;

    /**
     * Read all supported metrics.
     *
     * @param metricsParser the metrics parser used to read the metrics definition file.
     */
    public JsonMetricsReader(final MetricsParser metricsParser) {
        allMetricsMap = metricsParser.getMetricsListing();
    }
    
    /**
     * Read the QC parameters for generic metrics and NIST metrics from the json file.
     *
     * @param jsonFile the json file that contains the QC parameters.
     * @return map containing names of QC metrics and their values - as read from the json file.
     */
    public Map<String, String> readJsonValuesForGenericAndNISTMetrics(final File jsonFile) {
        final Map<String, String> metricsValues = new HashMap<>();
        try {
            final JSONObject jsonObject = (JSONObject) new JSONParser().parse(new FileReader(jsonFile));
            /* TODO: can we read the available metrics from the json file instead of using allMetricsMap? [Freek]
            [Pravin] The json file does not always contain all the metrics and their values.
            e.g. In case one of the pipeline stages do not work as expected (such as spectral search)
            then corresponding metrics are not written to the json file. No metrics information 
            will be written to the json file in case of erroneous RAW data. 
            Hence I prefer to use allMetricsMap.  
            */ 
            for (final String key : allMetricsMap.keySet()) {
                //Initialize metricsValues to N/A
                String paramValue = Constants.NOT_AVAILABLE_STRING;
                //Split the key into jsonObject and parameters
                final StringTokenizer keyTokenizer = new StringTokenizer(key, COLON_SEPERATOR);
                final String objectName = keyTokenizer.nextToken();
                final String paramName = keyTokenizer.nextToken();
                //Check whether json file contains object name
                if (!QUAMETER_OBJECT_NAME.equals(objectName) && jsonObject.containsKey(objectName)) {
                    final JSONObject jObject = (JSONObject) jsonObject.get(objectName);
                    if (DATE_PARAM_NAME.equals(paramName) || RUNTIME_PARAM_NAME.equals(paramName)) {
                        paramValue = (String) jObject.get(paramName);
                    } else {
                        paramValue = (String) ((JSONArray) jObject.get(paramName)).get(1);
                    }
                }
                metricsValues.put(key, paramValue);
            }
        } catch (final IOException | ParseException e) {
            logger.log(Level.SEVERE, JSON_FILE_EXCEPTION_MESSAGE, e);
        }
        return metricsValues;
    }

    /**
     * Read the QC parameters for QuaMeter IDFree metrics from the json file.
     *
     * @param jsonFile the json file that contains the QC parameters.
     * @param metricsValues map containing values for generic and NIST QC metrics.  
     * @return map containing names of Generic, NIST and QuaMeter metrics and their values - as read from the json file.
     */
    public Map<String, String> readJsonValuesForQuaMeterIDFreeMetrics(final File jsonFile, final Map<String, String> metricsValues) {
        try {
            final JSONObject jsonObject = (JSONObject) new JSONParser().parse(new FileReader(jsonFile));
            /* A JSONArray is an ordered sequence of values. 
             *  Its external text form is a string wrapped in square brackets with commas separating the values.
             * logger.fine("qmArray string = " + qmArray.toString());
             * qmArray string = [["Filename","QE1_121213_OPL0000_mq_15.raw"],
             * ["StartTimeStamp","2012-12-14T12:58:54Z"],["XIC-WideFrac","0.219303"],["XIC-FWHM-Q1","8.04827"],
             * ["XIC-FWHM-Q2","9.85281"],["XIC-FWHM-Q3","12.5852"],["XIC-Height-Q2","0.542627"],["XIC-Height-Q3","0.71677"],
             * ["XIC-Height-Q4","4.68125"],["RT-Duration","5099.99"],["RT-TIC-Q1","0.63985"],["RT-TIC-Q2","0.284944"],
             * ["RT-TIC-Q3","0.0166225"],["RT-TIC-Q4","0.0585837"],["RT-MS-Q1","0.13553"],["RT-MS-Q2","0.311506"],
             * ["RT-MS-Q3","0.344944"],["RT-MS-Q4","0.20802"],["RT-MSMS-Q1","0.302877"],["RT-MSMS-Q2","0.225113"],["RT-MSMS-Q3","0.210201"],
             * ["RT-MSMS-Q4","0.261809"],["MS1-TIC-Change-Q2","0.855981"],["MS1-TIC-Change-Q3","0.804706"],["MS1-TIC-Change-Q4","6.12754"],
             * ["MS1-TIC-Q2","0.176212"],["MS1-TIC-Q3","0.168427"],["MS1-TIC-Q4","3.08612"],["MS1-Count","6490"],
             * ["MS1-Freq-Max","3.25913"],["MS1-Density-Q1","728"],["MS1-Density-Q2","1221"],["MS1-Density-Q3","1418.75"],
             * ["MS2-Count","28618"],["MS2-Freq-Max","7.14872"],["MS2-Density-Q1","18"],["MS2-Density-Q2","30"],["MS2-Density-Q3","52"],
             * ["MS2-PrecZ-1","0"],["MS2-PrecZ-2","0.316689"],["MS2-PrecZ-3","0.503879"],["MS2-PrecZ-4","0.130547"],
             * ["MS2-PrecZ-5","0.034978"],["MS2-PrecZ-more","0.0139073"],["MS2-PrecZ-likely-1","0"],["MS2-PrecZ-likely-multi","0"]]
             */
            final JSONArray qmArray = (JSONArray) jsonObject.get(QUAMETER_OBJECT_NAME);
            if (qmArray != null) {
                for (int i = 0; i < qmArray.size(); ++i) {
                    //["Filename","QE1_121213_OPL0000_mq_15.raw"]
                    final JSONArray keyValuePair = (JSONArray) qmArray.get(i);
                    //Add key and value to metricsValues
                    metricsValues.put(QUAMETER_OBJECT_NAME + COLON_SEPERATOR + keyValuePair.get(0).toString(), keyValuePair.get(1).toString());
                }
            }
        } catch (final IOException | ParseException e) {
            logger.log(Level.SEVERE, JSON_FILE_EXCEPTION_MESSAGE, e);
        }
        return metricsValues; 
    }
}
