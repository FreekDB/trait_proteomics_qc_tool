package nl.ctmm.trait.proteomics.qcviewer.input;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for the <code>ReportReader</code> class.
 *
 * @author <a href="mailto:freek.de.bruijn@nbic.nl">Freek de Bruijn</a>
 */
public class ReportReaderTest {
    private ReportReader reportReader;

    // todo: project name TempQCReportViewerSWT -> TempQCReportViewerSWT?
    // todo: code coverage: Cobertura? CodeCover?
    
    /**
     * Initialize a <code>ReportReader</code>.
     */
    @Before
    public void setUp() {
        reportReader = new ReportReader();
    }
    
    /**
     * Test the <code>retrieveReports</code> method with actual data.
     */
    @Test
    public void testRetrieveReportsActualDataFreek() {
    	/*
    	 * public List<ReportUnit> retrieveReports(final String rootDirectoryName, String serverAddress, final Date fromDate, final Date tillDate)
    	 */
    	SimpleDateFormat sdf = new SimpleDateFormat ("dd/MM/yyyy"); 
    	String serverAddress = "10.13.1.103";
    	Date fromDate = null, tillDate = null;
        try { 
            fromDate = sdf.parse("01/01/2013"); 
            tillDate = sdf.parse("10/04/2013"); 
        } catch (ParseException e) { 
            System.out.println("Unparseable using " + sdf); 
        }
        final List<ReportUnit> reports = reportReader.retrieveReports("QCReports/ctmm", serverAddress, fromDate, tillDate);
        assertEquals("There should be 213 report units.", 213, reports.size());
        // Examine one report unit in detail.
        final ReportUnit reportUnit6 = reports.get(5);
        assertEquals(6, reportUnit6.getReportNum());
        assertEquals("1120.4", reportUnit6.getFileSizeString());
        assertEquals("5678 (5678)", reportUnit6.getMs1Spectra());
        assertEquals("30799 (30799)", reportUnit6.getMs2Spectra());
        assertEquals("2013/Feb/02 - 23:12", reportUnit6.getMeasured());
        assertEquals("0:24:23", reportUnit6.getRuntime());
        assertEquals("QE1_121127_OPL1016_DC_Mefopa_CSF_AA1_heatmap.png", reportUnit6.getHeatmapName());
        assertEquals("QE1_121127_OPL1016_DC_Mefopa_CSF_AA1_ions.png", reportUnit6.getIoncountName());
    }

    /**
     * Test the <code>retrieveReports</code> method with a non existing directory.
     */
    @Test
    public void testRetrieveReportsNonExistingDirectoryFreek() {
        final ReportReader reader = new ReportReader();
    	SimpleDateFormat sdf = new SimpleDateFormat ("dd/MM/yyyy"); 
    	String serverAddress = "10.13.1.103";
    	Date fromDate = null, tillDate = null;
        try { 
            fromDate = sdf.parse("01/01/2013"); 
            tillDate = sdf.parse("10/04/2013"); 
        } catch (ParseException e) { 
            System.out.println("Unparseable using " + sdf); 
        }
        final List<ReportUnit> reportUnits = reader.retrieveReports("nonExistingDirectory", serverAddress, fromDate, tillDate);
        assertEquals(0, reportUnits.size());
    }
}
