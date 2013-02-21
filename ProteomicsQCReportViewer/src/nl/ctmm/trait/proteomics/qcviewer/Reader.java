package nl.ctmm.trait.proteomics.qcviewer;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

import nl.ctmm.trait.proteomics.qcviewer.input.ReportUnit;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

/**
 * This class implements logic to read the QC Reports Folder and and prepares data to be displayed in the summary table. 
 * @author 
 * @version 1.0
 */

public class Reader {
    private static final Logger logger = Logger.getLogger("nl.ctmm.trait.proteomics.qcviewer.Reader");
    private static final List<String> MONTH_DIRS = Arrays.asList(
            "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
    );

	String rootFolder;
	private List<ReportUnit> reportArrayList = new ArrayList<ReportUnit>();
	private int currentReportNum = 0; 
	private BufferedImage naIcon = null;
			
	/**
	 * This constructor requires location of QC Reports Directory as input. 
	 * It also constructs fixed N/A Icon that is displayed in case of lack of heatmap and ioncount images.  
	 * @param rootFolder the location of QC Reports Folder
	 */
	public Reader(String rootFolder) {
		this.rootFolder = rootFolder;
		logger.log(Level.ALL, "Root folder = " + this.rootFolder);
		File naFile = new File("images\\na.jpg");
	    try {
			naIcon = ImageIO.read(naFile);
	    } catch (IOException e) {
 			e.printStackTrace();
 		}
	}//End of constructor
	
	/**
	 * This function looks for three files for each msreading in the QC Reports Directory:
	 * Metrics.json: Contains values of following parameters: date of msreading, filesize, runtime, ms1Sepctra and ms2Sepctra.
	 * heatmap.png: Heatmap file.
	 * ions.png: ioncount file.
	 * @return List of all prepared reports - according to the ReportUnit format.
	 * @see ReportUnit
	 */
	public List<ReportUnit> prepareReportArrayList() {
		File directory = new File(rootFolder); 

	    /*The directory has three levels - year, month and msrun.
	    The msreading directory may contain following three files of importance:
	    1) metrics.json: String file which has following format: 
	    {"generic": {"date": "2012/Nov/07 - 14:18", "ms2_spectra": 
	    ["MS2 Spectra", "22298 (22298)"], "runtime": "0:16:23", 
	    "f_size": ["File Size (MB)", "830.9"], "ms1_spectra": 
	    ["MS1 Spectra", "7707 (7707)"]}}
	    2) msrun*_heatmap.png
	    3) msrun*_ions.png
	    */
		
		//Recursive reading of directory and printing the contents
		File[] listOfFiles = directory.listFiles();
	    for (int i = 0; i < listOfFiles.length; i++) {
	      if (listOfFiles[i].isFile()) {
	        logger.log(Level.ALL, "File " + listOfFiles[i].getName());
	      } else if (listOfFiles[i].isDirectory()) {
	        logger.log(Level.ALL, "Directory " + listOfFiles[i].getName());
	        String year = listOfFiles[i].getName();
	        File level1Dir = listOfFiles[i]; 
        	//confirm whether 4 digit number or not
	        if (level1Dir.getName().length() == 4 && isIntNumber(level1Dir.getName())) { //signifies YYYY and sufficient condition for this tool
	        	logger.log(Level.ALL, "Year = " + year);
	        	File yearDir = level1Dir;
	        	File[] monthDirs = yearDir.listFiles(); //it is assumed that the year directory contains all months
	        	for (int j = 0; j < monthDirs.length; ++j) {
	        		logger.log(Level.ALL, "Month = " + monthDirs[j].getName());
	        		File monthDir = monthDirs[j];
                    if (MONTH_DIRS.contains(monthDir.getName())) {
                        File[] msrunDirs = monthDir.listFiles();
                        for (int k = 0; k < msrunDirs.length; ++k) {
                            logger.log(Level.ALL, "Msrun = " + msrunDirs[k].getName());
                            File msrunDir = msrunDirs[k];
                            File[] dataFiles = msrunDir.listFiles();
                            ReportUnit rUnit = new ReportUnit(msrunDirs[k].getName(), ++currentReportNum); //original unit
                            rUnit.setHeatmap(naIcon, "naIcon"); //Default heatmap
                            rUnit.setIoncount(naIcon, "naIcon"); //default ioncount
                            for (final File dataFile : dataFiles) {
                                if (dataFile.isFile()) {
                                    logger.log(Level.ALL, "File " + dataFile.getName());
                                    if (dataFile.getName().equals("metrics.json")) {
                                    	readJsonValues(dataFile, rUnit);
                                    } else if (dataFile.getName().endsWith("heatmap.png")) { //heatmap
                                        BufferedImage heatmap;
                                        try {
                                            heatmap = ImageIO.read(dataFile);
                                            rUnit.setHeatmap(heatmap, dataFile.getName());
                                        } catch (IOException e) {
                                            logger.log(Level.ALL, e.toString());
                                        }
                                    } else if (dataFile.getName().endsWith("ions.png")) { //ioncount
                                        BufferedImage ioncount;
                                        try {
                                            ioncount = ImageIO.read(dataFile);
                                            rUnit.setIoncount(ioncount, dataFile.getName());
                                        } catch (IOException e) {
                                            logger.log(Level.ALL, e.toString());
                                        }
                                    }
                                } else if (dataFile.isDirectory()) {
                                    logger.log(Level.ALL, "Directory " + dataFile.getName());
                                }
                            }
                            reportArrayList.add(rUnit);
                        }
	        		}
	        	}
	        }
	      }
	    }
	    return reportArrayList;
	} 
	
	private boolean isIntNumber(String num){
	    try{
	        Integer.parseInt(num);
	    } catch(NumberFormatException nfe) {
	        return false;
	    }
	    return true;
	}
	
   /**
     * Read the QC parameters from the json file.
     *
     * @param jsonFile   the json file that contains the values of QC parameters.
     * @param reportUnit the report unit where the QC parameters will be stored.
     */
    private void readJsonValues(final File jsonFile, final ReportUnit reportUnit) {
        logger.info("IN readJsonValues - reading file " + jsonFile.getName());
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
