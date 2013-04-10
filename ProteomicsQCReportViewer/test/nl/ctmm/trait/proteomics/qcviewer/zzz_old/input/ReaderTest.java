package nl.ctmm.trait.proteomics.qcviewer.zzz_old.input;

import java.util.List;
import nl.ctmm.trait.proteomics.qcviewer.input.ReportUnit;
import nl.ctmm.trait.proteomics.qcviewer.Reader;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 * Unit tests for the <code>ReportReader</code> class.
 *
 * @author <a href="mailto:freek.de.bruijn@nbic.nl">Freek de Bruijn</a>
 */
public class ReaderTest {
    /**
     * Test the prepareReportArrayList method with actual data.
     */
    @Test
    public void testPrepareReportListActualData() {
        final Reader reader = new Reader("QCReports/ctmm");
        final List reports = reader.prepareReportArrayList();
        assertEquals("There should be 44 report units.", 44, reports.size());
        assertEquals(ReportUnit.class, reports.get(5).getClass());
        // Examine one report unit in detail.
        final ReportUnit reportUnit6 = (ReportUnit) reports.get(5);
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
     * Test the prepareReportArrayList method with a non existing directory.
     */
    @Test(expected = NullPointerException.class)
    public void testPrepareReportListNonExistingDirectory() {
        final Reader reader = new Reader("nonExistingDirectory");
        reader.prepareReportArrayList();
    }
}
