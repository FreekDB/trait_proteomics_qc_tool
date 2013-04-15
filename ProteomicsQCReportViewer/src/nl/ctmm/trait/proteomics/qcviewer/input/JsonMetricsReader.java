package nl.ctmm.trait.proteomics.qcviewer.input;

import java.io.File;
import java.io.FileReader;
import java.util.logging.Logger;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class JsonMetricsReader {

	private static final Logger logger = Logger.getLogger(ReportReader.class.getName());
	
    /**
     * Read the QC parameters from the json file.
     *
     * @param jsonFile   the json file that contains the QC parameters.
     * @param reportUnit the report unit where the QC parameters will be stored.
     */
    private void readJsonValues(final File jsonFile, final ReportUnit reportUnit) {
        logger.fine("IN readJsonValues - reading file " + jsonFile.getName());
        try {
            final JSONObject jsonObject = (JSONObject) new JSONParser().parse(new FileReader(jsonFile));
            final JSONObject genericObject = (JSONObject) jsonObject.get("generic");
            reportUnit.setFileSizeString((String) ((JSONArray) genericObject.get("f_size")).get(1));
            reportUnit.setMs1Spectra((String) ((JSONArray) genericObject.get("ms1_spectra")).get(1));
            reportUnit.setMs2Spectra((String) ((JSONArray) genericObject.get("ms2_spectra")).get(1));
            reportUnit.setMeasured((String) genericObject.get("date"));
            reportUnit.setRuntime((String) genericObject.get("runtime"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
	
}
