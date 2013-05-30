package nl.ctmm.trait.proteomics.qcviewer.input;

import java.awt.image.BufferedImage;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

import nl.ctmm.trait.proteomics.qcviewer.utils.Utilities;

import org.jfree.data.xy.XYSeries;

/**
 * The object of this class represents report of single msreading.
 * <p/>
 * The <code>ReportUnit</code> class contains information from an MS run generated by the QC tool that are displayed by
 * the QC report viewer.
 *
 * @author <a href="mailto:pravin.pawar@nbic.nl">Pravin Pawar</a>
 * @author <a href="mailto:freek.de.bruijn@nbic.nl">Freek de Bruijn</a>
 */
public class ReportUnit {
    private static final Logger logger = Logger.getLogger(ReportUnit.class.getName());

    /*Printing format:
    File Size (MB) 819.5
	MS1 Spectra 7742 (7742)
	MS2 Spectra 22044 (22044)
	Measured 2012/Nov/07 - 11:26
	QC Runtime 0:16:15
	*/
    private int reportNum = -1;
    private String fileSizeString = "-1.0";
    private String msrunName = ""; 
    private Double fileSize = -1.0;
    private String ms1Spectra = "N/A";
    private String ms2Spectra = "N/A";
    private String measured = "N/A";
    private String runtime = "N/A";
    private URI detailsUri; 
    
    // todo: only public setters for heatmapName and ioncountName; handle images internally.
    private BufferedImage heatmap = Utilities.getNotAvailableImage();
    private BufferedImage scaledHeatmap;
    private BufferedImage ioncount = Utilities.getNotAvailableImage();
    private String heatmapName = Utilities.NOT_AVAILABLE_ICON_NAME;
    private String ioncountName = Utilities.NOT_AVAILABLE_ICON_NAME;
    public HashMap<?, ?> metricsValues = null;
    //ChartUnit to hold corresponding chart
    private ChartUnit ticChartUnit = null; 

    /**
     * One row in the QC Report Viewer table corresponds to one QC ReportUnit.
     * Sets the number of this QC ReportUnit.
     *
     * @param msrunName the unique msrun name - also represents RAW file uniquely
     * @param reportNum the unique report number.
     */
    public ReportUnit(String msrunName, int reportNum) {
    	this.msrunName = msrunName;
        this.reportNum = reportNum;
        //Create default chart unit to handle problems due to missing series data 
        ticChartUnit = new ChartUnit(msrunName, reportNum, null);
    }

    /**
     * Create a report unit specifying values for all fields.
     *
     * @param msrunName 	 the unique msrun name - also represents RAW file uniquely
     * @param reportNum      the unique report number.
     * @param fileSizeString the file size (as a string).
     * @param ms1Spectra     the number of MS1 spectra.
     * @param ms2Spectra     the number of MS2 spectra.
     * @param measured       todo???
     * @param runtime        the runtime (todo: of the proteomics machine or QC pipeline?).
     * @param heatmap        the heatmap image.
     * @param ioncount       the ioncount image.
     * @param heatmapName    the name of the heatmap image.
     * @param ioncountName   the name of the ioncount image.
     */
    //TODO: Check constructor usage in test cases and make sure to remove this constructor as everything is linked to metricsMap
    public ReportUnit(final String msrunName, final int reportNum, final String fileSizeString, final String ms1Spectra,
                      final String ms2Spectra, final String measured, final String runtime, final BufferedImage heatmap,
                      final BufferedImage ioncount, final String heatmapName, final String ioncountName) {
        this.msrunName = msrunName;
    	this.reportNum = reportNum;
        this.fileSizeString = fileSizeString;
        setFileSizeString(fileSizeString);
        this.ms1Spectra = ms1Spectra;
        this.ms2Spectra = ms2Spectra;
        this.measured = measured;
        this.runtime = runtime;
        this.heatmap = heatmap;
        this.ioncount = ioncount;
        this.heatmapName = heatmapName;
        this.ioncountName = ioncountName;
        //Create default chart unit to handle problems due to missing series data 
        ticChartUnit = new ChartUnit(msrunName, reportNum, null);
    } 
    
    /**
     * Get value of metrics based on key
     */
    public String getMetricsValueFromKey(String key) {
    	String value = "N/A";
    	if (metricsValues == null) { //Corresponding metrics.json file not found for this report
    		return value; 
    	} else if (metricsValues.containsKey(key)) {
    		value = (String) metricsValues.get(key);
    	} 
    	//System.out.println("Key = " + key + " Value = " + value);
    	return value;
    }

    /**
     * Get the value of parameter reportNum
     *
     * @return Serial number of current ReportUnit
     */
    public int getReportNum() {
        return reportNum;
    }

    /**
     * Get the value of parameter msrunName as a string.
     *
     * @return the unique msrun name - also represents RAW file uniquely
     */
    public String getMsrunName() {
        return msrunName;
    }

    
    /**
     * Get the value of parameter fileSize as a string.
     *
     * @return size of the RAW MS data file (in MB)
     */
    public String getFileSizeString() {
        return fileSizeString;
    }

    /**
     * Get the value of parameter fileSize
     *
     * @return size of the RAW MS data file (in MB)
     */
    public Double getFileSize() {
        return fileSize;
    }

    /**
     * Create ticChart and corresponding chart data for this report unit 
     * @param msrunName
     * @param series
     */
    public void createChartUnit(final XYSeries series) {
    	if (ticChartUnit != null) {
    		ticChartUnit = null;
    	}
    	ticChartUnit = new ChartUnit(msrunName, reportNum, series);
    }
    
    /**
     * Get ticChart and corresponding chart data for this report unit 
     * @return ChartUnit corresponding to this reportUnit
     */
    public ChartUnit getChartUnit() {
    	return ticChartUnit;
    }
    
    /**
     * Set the value of parameter fileSize
     *
     * @param fileSizeString size of the RAW MS data file (in MB)
     */
    public void setFileSizeString(final String fileSizeString) {
        this.fileSizeString = fileSizeString;
        fileSize = (!fileSizeString.equals("N/A") && fileSizeString != null && !fileSizeString.trim().isEmpty())
                ? Double.parseDouble(fileSizeString)
                : null;
    }

    /**
     * Get the value of parameter ms1Spectra
     *
     * @return number of ms1 spectra
     */
    public String getMs1Spectra() {
        return this.ms1Spectra;
    }

    /**
     * Set the value of parameter detailsUri that points to detailed report on the server
     *
     * @param detailsUri Complete link address to report on the server
     */
    public void setDetailsUri(String Uri) {
    	Uri = Uri.replace(" ", "%20");
    	try {
			this.detailsUri = new URI(Uri);
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    /**
     * Get the value of parameter detailsUri that points to detailed report on the server
     *
     * @return detailsUri Complete link address to report on the server
     */
    public URI getDetailsUri() {
    	return detailsUri;
    }
    
    /**
     * Set the value of parameter ms1Spectra
     *
     * @param ms1Spectra number of ms1spectra
     */
    public void setMs1Spectra(String ms1Spectra) {
        this.ms1Spectra = ms1Spectra;
    }

    /**
     * Get the value of parameter ms2Spectra
     *
     * @return number of ms2spectra
     */
    public String getMs2Spectra() {
        return this.ms2Spectra;
    }

    /**
     * Set the value of parameter ms2Spectra
     *
     * @param ms2Spectra number of ms2spectra
     */
    public void setMs2Spectra(String ms2Spectra) {
        this.ms2Spectra = ms2Spectra;
    }

    /**
     * Get the value of parameter measured
     *
     * @return day and time at which QC processing of the RAW MS data file begun
     */
    public String getMeasured() {
        return this.measured;
    }

    /**
     * Set the value of parameter measured
     *
     * @param measured day and time at which QC processing of the RAW MS data file begun
     */
    public void setMeasured(String measured) {
        this.measured = measured;
    }

    /**
     * Get the value of parameter runtime
     *
     * @return time (in hh:mm:ss) taken to complete the QC processing of RAW data file
     */
    public String getRuntime() {
        return this.runtime;
    }

    /**
     * Set the value of parameter runtime
     *
     * @param runtime time (in hh:mm:ss) taken to complete the QC processing of RAW data file
     */
    public void setRuntime(String runtime) {
        this.runtime = runtime;
    }

    /**
   	 * Sets the values of following parameters in the QC ReportUnit:
   	 * @param heatmap heatmap image file in the bufferedImage format
   	 * @param heatmapName Name of the heatmap file
   	 */
    public void setHeatmap(BufferedImage heatmap, String heatmapName) {
        this.heatmap = heatmap;
        this.heatmapName = heatmapName;
    }

    /**
   	 * Sets the values of following parameters in the QC ReportUnit:
   	 * @param ioncount ioncount image file in the bufferedImage format
   	 * @param ioncountName Name of the ioncount image file
     */
    public void setIoncount(BufferedImage ioncount, String ioncountName) {
        this.ioncount = ioncount;
        this.ioncountName = ioncountName;
    }

    /**
   	 * Get the heatmap image file
   	 * @return the heatmap image file
   	 */
    public BufferedImage getHeatmap() {
        return heatmap;
    }

    // todo: move to class in gui package that encapsulates ReportUnit?
    public BufferedImage getScaledHeatmap() {
        if (scaledHeatmap == null && heatmap != null)
            scaledHeatmap = Utilities.scaleImage(heatmap, Utilities.SCALE_FIT, 100, 100);
        return scaledHeatmap;
    }

    public String getHeatmapName() {
        return heatmapName;
    }

    /**
   	 * Get the ioncount image file
   	 * @return the ioncount image file
   	 */
    public BufferedImage getIoncount() {
        return this.ioncount;
    }

    public String getIoncountName() {
        return ioncountName;
    }

    public void printReportValues() {
        logger.log(Level.ALL, "Num : " + this.reportNum + " fileSize = " + this.fileSizeString + " ms1Spectra = " +
                              this.ms1Spectra + " ms2Spectra = " + this.ms2Spectra + " measured = " + measured + " runtime = " +
                              runtime + " heatmap Name = " + this.heatmapName + " ioncount Name = " + this.ioncountName);
    }

    // todo: is this method still used?
    public void setImages(BufferedImage heatmap, BufferedImage ioncount) {
        this.heatmap = heatmap;
        this.ioncount = ioncount;
    }

	public int compareTo(final ReportUnit otherUnit, final String sortKey) {
		//if this report unit has higher value, it returns 1, equal value 0, lower value -1
		//No.,File Size(MB),MS1Spectra,MS2Spectra,Measured,Runtime(hh:mm:ss),maxIntensity
		//return -2; //invalid sort option 
		
		//Primary check on N/A values
		
		String thisValue = this.getMetricsValueFromKey(sortKey);
		String otherValue = otherUnit.getMetricsValueFromKey(sortKey);
		System.out.print("thisValue = " + thisValue + " otherValue = " + otherValue);
		if (thisValue.equals(otherValue)) {
			return 0; 
		} else if (otherValue.equals("N/A")) { //thisValue is valid and present
			return 1;
		} else if (thisValue.equals("N/A")) { //otherValue is valid and present
			return -1;
		}
		if (sortKey.equals("No.")) {
			if (this.reportNum > otherUnit.reportNum) {
				return 1;
			} else if (this.reportNum < otherUnit.reportNum) {
				return -1;
			} else return 0; //equal reportNum
		} else if (sortKey.equals("generic:f_size")) {
			if (this.fileSize > otherUnit.fileSize) {
				return 1;
			} else if (this.fileSize < otherUnit.fileSize) {
				return -1;
			} else return 0;
		} else if (sortKey.equals("generic:ms1_spectra")) {
			StringTokenizer stkz1 = new StringTokenizer(this.ms1Spectra, " ");
			StringTokenizer stkz2 = new StringTokenizer(otherUnit.ms1Spectra, " ");
			int thisms1Spectra = Integer.parseInt(stkz1.nextToken());
			int otherms1Spectra = Integer.parseInt(stkz2.nextToken());
			if (thisms1Spectra > otherms1Spectra) {
				return 1;
			} else if (thisms1Spectra < otherms1Spectra) {
				return -1;
			} else return 0;
		} else if (sortKey.equals("generic:ms2_spectra")) {
			StringTokenizer stkz1 = new StringTokenizer(this.ms2Spectra, " ");
			StringTokenizer stkz2 = new StringTokenizer(otherUnit.ms2Spectra, " ");
			int thisms2Spectra = Integer.parseInt(stkz1.nextToken());
			int otherms2Spectra = Integer.parseInt(stkz2.nextToken());
			if (thisms2Spectra > otherms2Spectra) {
				return 1;
			} else if (thisms2Spectra < otherms2Spectra) {
				return -1;
			} else return 0;
		} else if (sortKey.equals("generic:date")) {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MMM/dd - HH:mm");
			Date thisDate = null;
			Date otherDate = null;
			try {
				thisDate = sdf.parse(this.measured);
				otherDate = sdf.parse(otherUnit.measured);
			} catch (ParseException e) {
				e.printStackTrace();
			}
			System.out.print(" ThisDate = " + thisDate + " OtherDate = " + otherDate);
			if (thisDate.compareTo(otherDate) > 0) {
				return 1; 
			} else if (thisDate.compareTo(otherDate) < 0) {
				return -1; 
			} else return 0;
		} else if (sortKey.equals("generic:runtime")) {
			if (this.runtime.compareToIgnoreCase(otherUnit.runtime) > 0) {
				return 1;
			} else if (this.runtime.compareToIgnoreCase(otherUnit.runtime) < 0) {
				return -1;
			} else return 0; 
		} else if (sortKey.equals("maxIntensity")) {
			if (this.getChartUnit().getMaxTicIntensity() > otherUnit.getChartUnit().getMaxTicIntensity()) { 
				return 1;
			} else if (this.getChartUnit().getMaxTicIntensity() < otherUnit.getChartUnit().getMaxTicIntensity()) { 
				return -1;
			} else return 0; 
		} else {
			double thisDouble = Double.parseDouble(thisValue);
			double otherDouble = Double.parseDouble(otherValue);
			if (thisDouble > otherDouble) { 
				return 1;
			} else if (thisDouble < otherDouble) { 
				return -1;
			} else return 0; 
		}
	}

	public void setMetricsValues(HashMap<String, String> metricsValues) {
		//System.out.println("In ReportUnit setMetricsValues. No. of metrics = " + metricsValues.size());
		if (metricsValues instanceof HashMap<?, ?>) {
			this.metricsValues = (HashMap<?, ?>) metricsValues.clone();
			//Set values of certain parameters to aid in the comparison
	        this.fileSizeString = this.getMetricsValueFromKey("generic:f_size");
	        setFileSizeString(fileSizeString);
	        this.ms1Spectra = this.getMetricsValueFromKey("generic:ms1_spectra");
	        this.ms2Spectra = this.getMetricsValueFromKey("ggeneric:ms2_spectra");;
	        this.measured = this.getMetricsValueFromKey("generic:date");;
	        this.runtime = this.getMetricsValueFromKey("generic:runtime");;
		}
	}
	
	public HashMap<?, ?> getMetricsValues() {
		if (metricsValues != null) {
			return metricsValues;
		} else return null;
	}
}
