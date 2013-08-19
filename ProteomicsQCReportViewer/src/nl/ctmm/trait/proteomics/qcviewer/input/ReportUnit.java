package nl.ctmm.trait.proteomics.qcviewer.input;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import nl.ctmm.trait.proteomics.qcviewer.gui.ChartUnit;
import nl.ctmm.trait.proteomics.qcviewer.utils.Constants;

import org.jfree.data.xy.XYSeries;

/**
 * The <code>ReportUnit</code> class contains information from an MS run generated by the QC tool that are displayed by
 * the QC report viewer. The instances of this class each represent a report of a single msrun.
 *
 * @author <a href="mailto:pravin.pawar@nbic.nl">Pravin Pawar</a>
 * @author <a href="mailto:freek.de.bruijn@nbic.nl">Freek de Bruijn</a>
 */
public class ReportUnit implements Comparable<ReportUnit> {
    /**
     * The logger for this class.
     */
    private static final Logger logger = Logger.getLogger(ReportUnit.class.getName());

    // Temporarily enabled or disabled, see the getComparatorV2 method.
    /**
     * The date and time format for parsing the measured field.
     */
    private static final DateFormat DTF = new SimpleDateFormat("yyyy/MMM/dd - HH:mm");

    /**
     * Number of the report displayed in viewer.
     */
    private int reportNum = -1;
    
    /**
     * Index of the report displayed in viewer.
     */
    private int reportIndex = -1;
    
    /**
     * Name of processed RAW file.
     */
    private String msrunName = "";
    
    /**
     * Numeric representation of RAW file size.
     */
    private double fileSize = -1.0;
    
    /**
     * String representation of RAW file size.
     */
    private String fileSizeString = Constants.NOT_AVAILABLE_STRING;
    
    /**
     * Number of MS1 Spectra in the RAW file.
     */
    private String ms1Spectra = Constants.NOT_AVAILABLE_STRING;
    
    /**
     * Number of MS2 Spectra in the RAW file.
     */
    private String ms2Spectra = Constants.NOT_AVAILABLE_STRING;
    
    /**
     * Date and time at which QC pipeline has processed RAW file.
     */
    private String measured = Constants.NOT_AVAILABLE_STRING;
    
    /**
     * Time taken by the QC pipeline to completely process RAW file.
     */
    private String runtime = Constants.NOT_AVAILABLE_STRING;

    /**
     * String describing report error if files or values are missing.
     */
    private String reportErrorString = "";
    
    /**
     * To signify that one or more files belonging to this report are missing.
     */
    private boolean errorFlag;

    /**
     * Metrics values (map from keys to values).
     */
    private Map<String, String> metricsValues;

    /**
     * ChartUnit to hold corresponding chart.
     */
    private ChartUnit ticChartUnit;

    /**
     * One row in the QC Report Viewer table corresponds to one QC ReportUnit.
     * Sets the number of this QC ReportUnit.
     *
     * @param msrunName the unique msrun name - also represents RAW file uniquely
     * @param reportNum the unique report number.
     */
    public ReportUnit(final String msrunName, final int reportNum) {
        this.msrunName = msrunName;
        this.reportNum = reportNum;
        reportIndex = reportNum - 1; 
        //Create default chart unit to handle problems due to missing series data 
        ticChartUnit = new ChartUnit(msrunName, reportIndex, null);
    }

    /**
     * Get value of metric based on metrics key - e.g. "dyn:ds-1a".
     *
     * @param key metrics key in String format.
     * @return value of metric.
     */
    public String getMetricsValueFromKey(final String key) {
        return (metricsValues != null && metricsValues.containsKey(key))
               ? metricsValues.get(key)
               : Constants.NOT_AVAILABLE_STRING;
    }

    /**
     * Get the value of parameter reportNum.
     *
     * @return Serial number of current ReportUnit.
     */
    public int getReportNum() {
        return reportNum;
    }
    
    /**
     * Get the value of parameter reportIndex.
     *
     * @return Index of the current ReportUnit.
     */
    public int getReportIndex() {
        return reportIndex;
    }

    /**
     * Get the value of parameter msrunName as a string.
     *
     * @return the unique msrun name - also represents RAW file uniquely.
     */
    public String getMsrunName() {
        return msrunName;
    }

    /**
     * Get the value of errorFlag.
     * 
     * @return value of errorFlag.
     */
    public boolean getErrorFlag() {
        return errorFlag; 
    }
    
    /**
     * Set the value of errorFlag.
     *
     * @param flag the new value for error flag.
     */
    public void setErrorFlag(final boolean flag) {
        errorFlag = flag; 
    }
    
    /**
     * Set report error string.
     *
     * @param reportErrorString error string.
     */
    public void setReportErrorString(final String reportErrorString) {
        this.reportErrorString = reportErrorString;
    }
    
    /**
     * Get report error string.
     *
     * @return report error string.
     */
    public String getReportErrorString() {
        return reportErrorString;
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
     * Get the value of parameter fileSize.
     *
     * @return size of the RAW MS data file (in MB).
     */
    public Double getFileSize() {
        return fileSize;
    }

    /**
     * Create ticChart and corresponding chart data for this report unit.
     *
     * @param series a sequence of (x, y) data items.
     */
    public void createChartUnit(final XYSeries series) {
        ticChartUnit = new ChartUnit(msrunName, reportIndex, series);
    }
    
    /**
     * Get ticChart and corresponding chart data for this report unit.
     *
     * @return ChartUnit corresponding to this reportUnit.
     */
    public ChartUnit getChartUnit() {
        return ticChartUnit;
    }
    
    /**
     * Set the value of parameter fileSize.
     *
     * @param fileSizeString size of the RAW MS data file (in MB).
     */
    public void setFileSizeString(final String fileSizeString) {
        this.fileSizeString = fileSizeString;
        this.fileSize = (fileSizeString != null && !fileSizeString.equals(Constants.NOT_AVAILABLE_STRING)
                         && !fileSizeString.trim().isEmpty())
                        ? Double.parseDouble(fileSizeString)
                        : null;
    }

    /**
     * Get the value of parameter ms1Spectra.
     *
     * @return number of ms1 spectra.
     */
    public String getMs1Spectra() {
        return this.ms1Spectra;
    }
    
    /**
     * Set the value of parameter ms1Spectra.
     *
     * @param ms1Spectra number of ms1spectra.
     */
    public void setMs1Spectra(final String ms1Spectra) {
        this.ms1Spectra = ms1Spectra;
    }

    /**
     * Get the value of parameter ms2Spectra.
     *
     * @return number of ms2spectra.
     */
    public String getMs2Spectra() {
        return this.ms2Spectra;
    }

    /**
     * Set the value of parameter ms2Spectra.
     *
     * @param ms2Spectra number of ms2spectra.
     */
    public void setMs2Spectra(final String ms2Spectra) {
        this.ms2Spectra = ms2Spectra;
    }

    /**
     * Get the value of parameter measured.
     *
     * @return day and time at which QC processing of the RAW MS data file begun.
     */
    public String getMeasured() {
        return this.measured;
    }

    /**
     * Set the value of parameter measured.
     *
     * @param measured day and time at which QC processing of the RAW MS data file begun.
     */
    public void setMeasured(final String measured) {
        this.measured = measured;
    }

    /**
     * Get the value of parameter runtime.
     *
     * @return time (in hh:mm:ss) taken to complete the QC processing of RAW data file.
     */
    public String getRuntime() {
        return this.runtime;
    }

    /**
     * Set the value of parameter runtime.
     *
     * @param runtime time (in hh:mm:ss) taken to complete the QC processing of RAW data file.
     */
    public void setRuntime(final String runtime) {
        this.runtime = runtime;
    }

    /**
     * Set values of QC metrics in this report.
     *
     * @param metricsValues map containing QC metrics keys and corresponding values.
     */
    public void setMetricsValues(final Map<String, String> metricsValues) {
        if (metricsValues != null) {
            this.metricsValues = new HashMap<>(metricsValues);
            //Set values of certain parameters to aid in the comparison
            this.fileSizeString = this.getMetricsValueFromKey(Constants.METRIC_KEY_FILE_SIZE);
            setFileSizeString(fileSizeString);
            this.ms1Spectra = this.getMetricsValueFromKey(Constants.METRIC_KEY_MS1_SPECTRA);
            this.ms2Spectra = this.getMetricsValueFromKey(Constants.METRIC_KEY_MS2_SPECTRA);
            this.measured = this.getMetricsValueFromKey(Constants.METRIC_KEY_MEASURED);
            this.runtime = this.getMetricsValueFromKey(Constants.METRIC_KEY_RUNTIME);
        }
    }
    
    /**
     * Get map with values of QC metrics in this report.
     *
     * @return metricsValues map containing QC metrics keys and corresponding values.
     */
    public Map<String, String> getMetricsValues() {
        return metricsValues;
    }

    @Override
    public int compareTo(final ReportUnit otherUnit) {
        return 0;
    }

    /**
     * Get a comparator to compare report units.
     * 
     * @param sortKey the key to sort on.
     * @param ascending whether to sort in ascending or descending order.               
     * @return the comparator to compare report units.
     */
    public static Comparator<ReportUnit> getComparator(final String sortKey, final boolean ascending) {
        return new Comparator<ReportUnit>() {
            @Override
            public int compare(final ReportUnit reportUnit1, final ReportUnit reportUnit2) {
                /*Needs following special provision to sort according to report index
                 * since call to getMetricsValueFromKey(sortKey) returns N/A for report index  
                 */
                if (sortKey.equals(Constants.SORT_KEY_REPORT_INDEX)) {
                    if (reportUnit1.getReportIndex() > reportUnit2.getReportIndex()) {
                        return ascending ? 1 : -1; 
                    } else if (reportUnit1.getReportIndex() < reportUnit2.getReportIndex()) {
                        return ascending ? -1 : 1; 
                    } else
                        return 0; //equal reportIndex
                }  
                final String value1 = reportUnit1.getMetricsValueFromKey(sortKey);
                final String value2 = reportUnit2.getMetricsValueFromKey(sortKey);
                try {
                    if (value1.equals(value2)) {
                        return 0; 
                    } else if (value2.equals("N/A")) { //thisValue is valid and present
                        return ascending ? 1 : -1; //if ascending = true, return 1 else return -1
                    } else if (value1.equals("N/A")) { //otherValue is valid and present
                        return ascending ? -1 : 1; //if ascending = true, return -1 else return 1
                    } else if (sortKey.equals(Constants.SORT_KEY_FILE_SIZE)) {
                        if (reportUnit1.fileSize > reportUnit2.fileSize) {
                            return ascending ? 1 : -1; 
                        } else if (reportUnit1.fileSize < reportUnit2.fileSize) {
                            return ascending ? -1 : 1; 
                        } else
                            return 0;
                    } else if (sortKey.equals(Constants.SORT_KEY_MS1_SPECTRA)) {
                        int thisms1Spectra = Integer.parseInt(value1);
                        int otherms1Spectra = Integer.parseInt(value2);
                        if (thisms1Spectra > otherms1Spectra) {
                            return ascending ? 1 : -1; 
                        } else if (thisms1Spectra < otherms1Spectra) {
                            return ascending ? -1 : 1; 
                        } else return 0;
                    } else if (sortKey.equals(Constants.SORT_KEY_MS2_SPECTRA)) {
                        int thisms2Spectra = Integer.parseInt(value1);
                        int otherms2Spectra = Integer.parseInt(value2);
                        if (thisms2Spectra > otherms2Spectra) {
                            return ascending ? 1 : -1; 
                        } else if (thisms2Spectra < otherms2Spectra) {
                            return ascending ? -1 : 1; 
                        } else return 0;
                    } else if (sortKey.equals(Constants.SORT_KEY_DATE)) {
                        Date thisDate = Constants.MEASURED_DATE_FORMAT.parse(reportUnit1.measured);
                        Date otherDate = Constants.MEASURED_DATE_FORMAT.parse(reportUnit2.measured);
                        if (thisDate.compareTo(otherDate) > 0) {
                            return ascending ? 1 : -1; 
                        } else if (thisDate.compareTo(otherDate) < 0) {
                            return ascending ? -1 : 1; 
                        } else return 0;
                    } else if (sortKey.equals(Constants.SORT_KEY_RUNTIME)) {
                        if (reportUnit1.runtime.compareToIgnoreCase(reportUnit2.runtime) > 0) {
                            return ascending ? 1 : -1; 
                        } else if (reportUnit1.runtime.compareToIgnoreCase(reportUnit2.runtime) < 0) {
                            return ascending ? -1 : 1; 
                        } else return 0; 
                    } else if (sortKey.equals(Constants.SORT_KEY_MAX_INTENSITY)) {
                        if (reportUnit1.getChartUnit().getMaxTicIntensity() > reportUnit2.getChartUnit().getMaxTicIntensity()) { 
                            return ascending ? 1 : -1; 
                        } else if (reportUnit1.getChartUnit().getMaxTicIntensity() < reportUnit2.getChartUnit().getMaxTicIntensity()) { 
                            return ascending ? -1 : 1; 
                        } else return 0; 
                    } else {
                        double thisDouble = Double.parseDouble(value1);
                        double otherDouble = Double.parseDouble(value2);
                        if (thisDouble > otherDouble) { 
                            return ascending ? 1 : -1; 
                        } else if (thisDouble < otherDouble) { 
                            return ascending ? -1 : 1; 
                        } else return 0; 
                    }
                } catch (Exception e) {
                    logger.warning("Exception type " + e.getClass().toString() + " thisValue = " + value1 +
                                       " otherValue = " + value2);
                    e.printStackTrace();
                }
                return 0; 
            }
        };
    }

    /**
     * Get a comparator to compare report units.
     *
     * @param sortKey the key to sort on.
     * @param ascending whether to sort in ascending or descending order.
     * @return the comparator to compare report units.
     */
    public static Comparator<ReportUnit> getComparatorV2(final String sortKey, final boolean ascending) {
        return new Comparator<ReportUnit>() {
            // TODO: Analyze new version of compare(...) for correct sorting mechanism
            @Override
            public int compare(final ReportUnit reportUnit1, final ReportUnit reportUnit2) {
                int result = 0;
                final int sortFactor = ascending ? 1 : -1;
                // Needs following special provision to sort according to report index since call to
                // getMetricsValueFromKey(sortKey) returns "N/A" for report index.
                if (sortKey.equals(Constants.SORT_KEY_REPORT_INDEX)) {
                    result = sortFactor * Integer.compare(reportUnit1.getReportIndex(), reportUnit2.getReportIndex());
                } else {
                    final String value1 = reportUnit1.getMetricsValueFromKey(sortKey);
                    final String value2 = reportUnit2.getMetricsValueFromKey(sortKey);
                    if (value1.equals(value2)) {
                        result = 0;
                    } else if (value2.equals(Constants.NOT_AVAILABLE_STRING)) {
                        //value1 is valid and present
                        result = sortFactor;
                    } else if (value1.equals(Constants.NOT_AVAILABLE_STRING)) {
                        //value2 is valid and present
                        result = -sortFactor;
                    } else if (Constants.LIST_SORT_KEYS_DOUBLE.contains(sortKey)) {
                        result = sortFactor * Double.compare(Double.parseDouble(value1), Double.parseDouble(value2));
                    } else if (Constants.LIST_SORT_KEYS_INT.contains(sortKey)) {
                        result = sortFactor * Integer.compare(Integer.parseInt(value1), Integer.parseInt(value2));
                    } else if (sortKey.equals(Constants.SORT_KEY_DATE)) {
                        try {
                            final Date measured1 = DTF.parse(reportUnit1.measured);
                            final Date measured2 = DTF.parse(reportUnit2.measured);
                            result = sortFactor * measured1.compareTo(measured2);
                        } catch (final ParseException e) {
                            logger.log(Level.SEVERE, "thisValue: " + value1 + "; otherValue: " + value2, e);
                        }
                    } else if (sortKey.equals(Constants.SORT_KEY_RUNTIME)) {
                        result = sortFactor * reportUnit1.runtime.compareToIgnoreCase(reportUnit2.runtime);
                    } else if (sortKey.equals(Constants.SORT_KEY_MAX_INTENSITY)) {
                        final double maxTicIntensity1 = reportUnit1.getChartUnit().getMaxTicIntensity();
                        final double maxTicIntensity2 = reportUnit2.getChartUnit().getMaxTicIntensity();
                        result = sortFactor * Double.compare(maxTicIntensity1, maxTicIntensity2);
                    } else {
                        result = sortFactor * Double.compare(Double.parseDouble(value1), Double.parseDouble(value2));
                    }
                }
                return result;
            }
        };
    }


    // For debugging purposes:
//    public void printReportValues() {
//        logger.log(Level.ALL, "Num : " + this.reportNum + " fileSize = " + this.fileSizeString + " ms1Spectra = " +
//                              this.ms1Spectra + " ms2Spectra = " + this.ms2Spectra + " measured = " + measured +
//                              " runtime = " + runtime);
//    }


    // For debugging purposes:
    @Override
    public String toString() {
        return "Number: " + reportNum + ", file size: " + fileSizeString + ", ms1 spectra: " + ms1Spectra
               + ", ms2 spectra: " + ms2Spectra + ", measured: " + measured + ", runtime: " + runtime;
    }
}
