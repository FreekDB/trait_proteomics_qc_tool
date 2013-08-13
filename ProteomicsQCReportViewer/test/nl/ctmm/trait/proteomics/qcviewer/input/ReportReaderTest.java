package nl.ctmm.trait.proteomics.qcviewer.input;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for the <code>ReportReader</code> class.
 *
 * @author <a href="mailto:freek.de.bruijn@nbic.nl">Freek de Bruijn</a>
 */
public class ReportReaderTest {
    // TODO: code coverage: Cobertura? CodeCover? [Freek]

    private ReportReader reportReader;

    /**
     * Initialize a <code>ReportReader</code>.
     */
    @Before
    public void setUp() {
        reportReader = new ReportReader(new MetricsParser());
    }
    
    /**
     * Test the <code>retrieveReports</code> method with actual data.
     */
    @Test
    public void testRetrieveReportsActualData() {
        /*
         * public List<ReportUnit> retrieveReports(final String rootDirectoryName, String serverAddress,
         *                                         final Date fromDate, final Date tillDate)
         */
        final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        Date fromDate = null, tillDate = null;
        try { 
            fromDate = sdf.parse("01/07/2013"); 
            tillDate = sdf.parse("31/07/2013");
        } catch (ParseException e) { 
            System.out.println("Unparsable using " + sdf);
        }
        //TODO: Change unit test to support Map<String, ReportUnit>
        final List<String> reportUnitsKeys = new ArrayList<String>();
        final List<ReportUnit> reports = new ArrayList<ReportUnit> (reportReader.retrieveReports("QCReports", "", reportUnitsKeys, fromDate, tillDate).values());
        assertEquals("There should be 4 report units.", 4, reports.size());
        // Examine one report unit in detail.
        final ReportUnit reportUnit2 = reports.get(1);
        assertEquals(2, reportUnit2.getReportNum());
        // TODO: look into why these values below are all not available. [Freek]
        assertEquals("N/A", reportUnit2.getFileSizeString());
        assertEquals("N/A", reportUnit2.getMs1Spectra());
        assertEquals("N/A", reportUnit2.getMs2Spectra());
        assertEquals("N/A", reportUnit2.getMeasured());
        assertEquals("N/A", reportUnit2.getRuntime());
    }

    /**
     * Test the <code>retrieveReports</code> method with a non existing directory.
     */
    @Test
    public void testRetrieveReportsNonExistingDirectory() {
        final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        Date fromDate = null, tillDate = null;
        try { 
            fromDate = sdf.parse("01/01/2013"); 
            tillDate = sdf.parse("10/04/2013"); 
        } catch (ParseException e) { 
            System.out.println("Unparsable using " + sdf);
        }
        final List<String> reportUnitsKeys = new ArrayList<String>();
        final List<ReportUnit> reportUnits = new ArrayList<ReportUnit> (reportReader.retrieveReports("nonExistingDirectory", "", reportUnitsKeys, fromDate, tillDate).values());
        assertEquals(0, reportUnits.size());
    }
}
