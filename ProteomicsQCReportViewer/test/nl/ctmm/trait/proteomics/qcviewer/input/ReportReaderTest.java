package nl.ctmm.trait.proteomics.qcviewer.input;

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
        final List<ReportUnit> reports = reportReader.retrieveReports("QCReports/ctmm");
        assertEquals("There should be 44 report units.", 44, reports.size());
        // Examine one report unit in detail.
        final ReportUnit reportUnit6 = reports.get(5);
        assertEquals(6, reportUnit6.getReportNum());
        assertEquals("1154.9", reportUnit6.getFileSizeString());
        assertEquals("10944 (10944)", reportUnit6.getMs1Spectra());
        assertEquals("21306 (21306)", reportUnit6.getMs2Spectra());
        assertEquals("2012/Dec/04 - 13:58", reportUnit6.getMeasured());
        assertEquals("0:18:05", reportUnit6.getRuntime());
        assertEquals("QE1_121005_OPL0000_jurkat2ug_01_heatmap.png", reportUnit6.getHeatmapName());
        assertEquals("QE1_121005_OPL0000_jurkat2ug_01_ions.png", reportUnit6.getIoncountName());
    }

    /**
     * Test the <code>retrieveReports</code> method with a non existing directory.
     */
    @Test
    public void testRetrieveReportsNonExistingDirectoryFreek() {
        final ReportReader reader = new ReportReader();
        final List<ReportUnit> reportUnits = reader.retrieveReports("nonExistingDirectory");
        assertEquals(0, reportUnits.size());
    }
}
