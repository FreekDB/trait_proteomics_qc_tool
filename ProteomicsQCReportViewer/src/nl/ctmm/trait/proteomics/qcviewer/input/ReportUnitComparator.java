package nl.ctmm.trait.proteomics.qcviewer.input;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import nl.ctmm.trait.proteomics.qcviewer.utils.Constants;

/**
 * The comparator used to compare reports to each other.
 *
 * @author <a href="mailto:pravin.pawar@nbic.nl">Pravin Pawar</a>
 * @author <a href="mailto:freek.de.bruijn@nbic.nl">Freek de Bruijn</a>
 */
public class ReportUnitComparator implements Comparator<ReportUnit> {
    /**
     * The logger for this class.
     */
    private static final Logger logger = Logger.getLogger(ReportUnitComparator.class.getName());

    /**
     * The date and time format for parsing the measured field.
     */
    private static final DateFormat DTF = new SimpleDateFormat("yyyy/MMM/dd - HH:mm");

    /**
     * The sort key to use, for example {@link Constants#SORT_KEY_REPORT_INDEX}.
     */
    private final String sortKey;

    /**
     * Whether to sort ascending (1) or descending (-1).
     */
    private final int sortFactor;

    /**
     * Create a comparator for reports.
     *
     * @param sortKey   the sort key to use, for example {@link Constants#SORT_KEY_REPORT_INDEX}.
     * @param ascending whether to sort ascending (<code>true</code>) or descending (<code>false</code>).
     */
    public ReportUnitComparator(final String sortKey, final boolean ascending) {
        this.sortKey = sortKey;
        this.sortFactor = ascending ? 1 : -1;
    }

    @Override
    public int compare(final ReportUnit reportUnit1, final ReportUnit reportUnit2) {
        int result;
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
            } else {
                result = compareMetricKey(reportUnit1, reportUnit2, value1, value2);
            }
        }
        return result;
    }

    /**
     * Compare two reports on metric key.
     *
     * @param reportUnit1 the first report.
     * @param reportUnit2 the second report.
     * @param value1      the metric value of the first report.
     * @param value2      the metric value of the second report.
     * @return a negative integer, zero, or a positive integer as the first argument is less than, equal to, or greater
     *         than the second.
     */
    private int compareMetricKey(final ReportUnit reportUnit1, final ReportUnit reportUnit2, final String value1,
                                 final String value2) {
        int result = 0;
        if (Constants.LIST_SORT_KEYS_DOUBLE.contains(sortKey)) {
            result = sortFactor * Double.compare(Double.parseDouble(value1), Double.parseDouble(value2));
        } else if (Constants.LIST_SORT_KEYS_INT.contains(sortKey)) {
            result = sortFactor * Integer.compare(Integer.parseInt(value1), Integer.parseInt(value2));
        } else if (sortKey.equals(Constants.SORT_KEY_DATE)) {
            try {
                final Date measured1 = DTF.parse(reportUnit1.getMeasured());
                final Date measured2 = DTF.parse(reportUnit2.getMeasured());
                result = sortFactor * measured1.compareTo(measured2);
            } catch (final ParseException e) {
                logger.log(Level.SEVERE, "thisValue: " + value1 + "; otherValue: " + value2, e);
            }
        } else if (sortKey.equals(Constants.SORT_KEY_RUNTIME)) {
            result = sortFactor * reportUnit1.getRuntime().compareToIgnoreCase(reportUnit2.getRuntime());
        // Since ReportUnit.createChartUnit stores the max intensity in the metricsValues map, there is no need for
        // special code for the SORT_KEY_MAX_INTENSITY.
//        } else if (sortKey.equals(Constants.SORT_KEY_MAX_INTENSITY)) {
//            final double maxTicIntensity1 = reportUnit1.getChartUnit().getMaxTicIntensity();
//            final double maxTicIntensity2 = reportUnit2.getChartUnit().getMaxTicIntensity();
//            result = sortFactor * Double.compare(maxTicIntensity1, maxTicIntensity2);
        } else {
            result = sortFactor * Double.compare(Double.parseDouble(value1), Double.parseDouble(value2));
        }
        return result;
    }
}
