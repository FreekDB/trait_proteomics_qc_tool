package nl.ctmm.trait.proteomics.qcviewer.input;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import static org.junit.Assert.assertEquals;

import nl.ctmm.trait.proteomics.qcviewer.utils.Utilities;
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
        reportReader = new ReportReader(new MetricsParser(null));
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
            fromDate = sdf.parse("01/01/2013"); 
            tillDate = sdf.parse("30/06/2013");
        } catch (ParseException e) { 
            System.out.println("Unparsable using " + sdf);
        }
        final List<ReportUnit> reports = reportReader.retrieveReports("QCReports", fromDate, tillDate);
        assertEquals("There should be 41 report units.", 41, reports.size());
        // Examine one report unit in detail.
        final ReportUnit reportUnit6 = reports.get(5);
        assertEquals(6, reportUnit6.getReportNum());
        assertEquals("1001.3", reportUnit6.getFileSizeString());
        assertEquals("5789", reportUnit6.getMs1Spectra());
        assertEquals("30242", reportUnit6.getMs2Spectra());
        assertEquals("2013/Jun/15 - 00:17", reportUnit6.getMeasured());
        assertEquals("0:17:40", reportUnit6.getRuntime());
        assertEquals(Utilities.NOT_AVAILABLE_ICON_NAME, reportUnit6.getHeatmapName());
        assertEquals(Utilities.NOT_AVAILABLE_ICON_NAME, reportUnit6.getIoncountName());
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
        final List<ReportUnit> reportUnits = reportReader.retrieveReports("nonExistingDirectory", fromDate, tillDate);
        assertEquals(0, reportUnits.size());
    }
}
