package nl.ctmm.trait.proteomics.qcviewer.input;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import nl.ctmm.trait.proteomics.qcviewer.utils.Constants;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for the <code>ReportUnit</code> class.
 *
 * @author <a href="mailto:freek.de.bruijn@nbic.nl">Freek de Bruijn</a>
 */
public class ReportUnitTest {
    private static final int REPORT_NUMBER = 6;

    private ReportUnit reportUnit;

    /**
     * Initialize a <code>ReportUnit</code>.
     */
    @Before
    public void setUp() {
        /*
         * public ReportUnit(String msrunName, int reportNum)
         */
        reportUnit = new ReportUnit("msrun" + REPORT_NUMBER, REPORT_NUMBER);
    }

    /**
     * Check whether the constructor initializes the fields as we expect.
     */
    @Test
    public void testConstructor() {
        assertEquals(REPORT_NUMBER, reportUnit.getReportNum());
        assertNull(reportUnit.getFileSize());
        assertEquals(Constants.NOT_AVAILABLE_STRING, reportUnit.getFileSizeString());
        assertEquals(Constants.NOT_AVAILABLE_STRING, reportUnit.getMs1Spectra());
        assertEquals(Constants.NOT_AVAILABLE_STRING, reportUnit.getMs2Spectra());
        assertEquals(Constants.NOT_AVAILABLE_STRING, reportUnit.getMeasured());
        assertEquals(Constants.NOT_AVAILABLE_STRING, reportUnit.getRuntime());
    }

    /**
     * Test <code>getFileSize</code>.
     */
    @Test
    public void testGetFileSize() {
        reportUnit.setFileSizeString("1.0");
        assertEquals(1.0, reportUnit.getFileSize(), 0.0000001);
    }

    /**
     * Test <code>setFileSizeString</code> and <code>getFileSizeString</code>.
     */
    @Test
    public void testSetFileSizeStringNullOrEmpty() {
        reportUnit.setFileSizeString(null);
        assertNull(reportUnit.getFileSize());
        reportUnit.setFileSizeString("");
        assertNull(reportUnit.getFileSize());
    }

    /**
     * Test the report unit comparator with different sort key and sort order combinations.
     *
     * TODO: move this test to new ReportUnitComparatorUnitTest class. [Freek]
     */
    @Test
    public void testComparator() {
        // Create some test reports.
        final ReportUnit r1 = createReport(1);
        final ReportUnit r2 = createReport(2);
        final ReportUnit r3 = createReport(3);
        final ReportUnit r4 = createReport(4);

        final List<ReportUnit> unsortedReports = Arrays.asList(r2, r3, r4, r1);
        final List<ReportUnit> sortedReports = Arrays.asList(r1, r2, r3, r4);

        // Create the list with test combinations. We test with the following seven sort keys (the report index and the
        // six default metrics; sort key compare [SORT_ORDER_COMPARE] is handled by the ViewerFrame class itself):
        final List<TestCombination> testCombinations = new ArrayList<>();
        testCombinations.add(new TestCombination(Constants.SORT_KEY_REPORT_INDEX, true, sortedReports));
        testCombinations.add(new TestCombination(Constants.SORT_KEY_FILE_SIZE, true, sortedReports));
        testCombinations.add(new TestCombination(Constants.SORT_KEY_MS1_SPECTRA, true, sortedReports));
        testCombinations.add(new TestCombination(Constants.SORT_KEY_MS2_SPECTRA, true, sortedReports));
        testCombinations.add(new TestCombination(Constants.SORT_KEY_DATE, true, sortedReports));
        testCombinations.add(new TestCombination(Constants.SORT_KEY_RUNTIME, true, sortedReports));
        testCombinations.add(new TestCombination(Constants.SORT_KEY_MAX_INTENSITY, true, sortedReports));

        final List<TestCombination> descendingTestCombinations = new ArrayList<>();
        for (final TestCombination testCombination : testCombinations) {
            final List<ReportUnit> reversedReports = new ArrayList<>(testCombination.expectedReports);
            Collections.reverse(reversedReports);
            descendingTestCombinations.add(new TestCombination(testCombination.sortKey, false, reversedReports));
        }
        testCombinations.addAll(descendingTestCombinations);

        // Start testing.
        for (final TestCombination testCombination : testCombinations) {
//            // Test V1.
//            final List<ReportUnit> reportsV1 = new ArrayList<>(unsortedReports);
//            Collections.sort(reportsV1, ReportUnit.getComparator(testCombination.sortKey, testCombination.sortOrder));
//            assertEquals(testCombination + " V1", testCombination.expectedReports, reportsV1);
            // Test V2.
            final List<ReportUnit> reportsV2 = new ArrayList<>(unsortedReports);
            Collections.sort(reportsV2, ReportUnit.getComparatorV2(testCombination.sortKey, testCombination.sortOrder));
            assertEquals(testCombination + " V2", testCombination.expectedReports, reportsV2);
        }
    }

    /**
     * A single test combination: sort key, sort order and the expected reports.
     */
    private class TestCombination {
        public String sortKey;
        public boolean sortOrder;
        public List<ReportUnit> expectedReports;

        TestCombination(final String sortKey, final boolean sortOrder, final List<ReportUnit> expectedReports) {
            this.sortKey = sortKey;
            this.sortOrder = sortOrder;
            this.expectedReports = expectedReports;
        }

        @Override
        public String toString() {
            return "Sort key: " + sortKey + ", sort order: " + sortOrder + "; expected reports: " + expectedReports;
        }
    }

    private ReportUnit createReport(final int reportNumber) {
        final ReportUnit report = new ReportUnit("msrun" + reportNumber, reportNumber);
        final String reportNumberString = Integer.toString(reportNumber);
        report.setFileSizeString(reportNumberString);
        report.setMs1Spectra(reportNumberString);
        report.setMs2Spectra(reportNumberString);
        final Date measuredDateTime = new DateTime(2013, 8, 18 + reportNumber, 12, 34, 56).toDate();
        report.setMeasured(Constants.MEASURED_DATE_FORMAT.format(measuredDateTime));
        report.setRuntime(reportNumberString);
        report.setMaxIntensityMetric(reportNumberString);
        return report;
    }
}
