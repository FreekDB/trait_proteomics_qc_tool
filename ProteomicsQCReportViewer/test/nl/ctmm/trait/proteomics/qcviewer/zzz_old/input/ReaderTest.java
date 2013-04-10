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
        assertEquals("There should be 213 report units.", 213, reports.size());
        assertEquals(ReportUnit.class, reports.get(5).getClass());
        // Examine one report unit in detail.
        final ReportUnit reportUnit6 = (ReportUnit) reports.get(5);
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
     * Test the prepareReportArrayList method with a non existing directory.
     */
    @Test(expected = NullPointerException.class)
    public void testPrepareReportListNonExistingDirectory() {
        final Reader reader = new Reader("nonExistingDirectory");
        reader.prepareReportArrayList();
    }
}
