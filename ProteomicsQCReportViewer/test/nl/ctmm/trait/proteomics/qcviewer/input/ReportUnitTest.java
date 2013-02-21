package nl.ctmm.trait.proteomics.qcviewer.input;

import java.awt.image.BufferedImage;
import nl.ctmm.trait.proteomics.qcviewer.utils.Utilities;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
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
        reportUnit = new ReportUnit(REPORT_NUMBER);
    }

    /**
     * Check whether the constructor initializes the fields as we expect.
     */
    @Test
    public void testConstructor() {
        assertEquals(REPORT_NUMBER, reportUnit.getReportNum());
        assertNull(reportUnit.getFileSize());
        assertEquals("", reportUnit.getFileSizeString());
        assertEquals("", reportUnit.getMs1Spectra());
        assertEquals("", reportUnit.getMs2Spectra());
        assertEquals("", reportUnit.getMeasured());
        assertEquals("", reportUnit.getRuntime());
        assertNotNull(reportUnit.getHeatmap());
        assertEquals(Utilities.NOT_AVAILABLE_ICON_NAME, reportUnit.getHeatmapName());
        assertNotNull(reportUnit.getScaledHeatmap());
        assertNotNull(reportUnit.getIoncount());
        assertEquals(Utilities.NOT_AVAILABLE_ICON_NAME, reportUnit.getIoncountName());
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
     * Test <code>getFileSizeString</code>.
     */
    @Test
    public void testSetFileSizeStringNullOrEmpty() {
        reportUnit.setFileSizeString(null);
        assertNull(reportUnit.getFileSize());
        reportUnit.setFileSizeString("");
        assertNull(reportUnit.getFileSize());
    }

    /**
     * Check that calls <code>getScaledHeatmap</code> return one and the same cached object.
     */
    @Test
    public void testGetScaledHeatmapIsCached() {
        final BufferedImage scaledHeatmap1 = reportUnit.getScaledHeatmap();
        final BufferedImage scaledHeatmap2 = reportUnit.getScaledHeatmap();
        assertNotNull(scaledHeatmap1);
        assertEquals(scaledHeatmap1, scaledHeatmap2);
    }
}
