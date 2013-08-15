package nl.ctmm.trait.proteomics.qcviewer.input;

import java.awt.image.BufferedImage;

import nl.ctmm.trait.proteomics.qcviewer.utils.Constants;
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
        Double fileSize = -1.0;
        assertEquals(REPORT_NUMBER, reportUnit.getReportNum());
        assertEquals(fileSize, reportUnit.getFileSize());
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
     * Test <code>getFileSizeString</code>.
     */
    @Test
    public void testSetFileSizeStringNullOrEmpty() {
        reportUnit.setFileSizeString(null);
        assertNull(reportUnit.getFileSize());
        reportUnit.setFileSizeString("");
        assertNull(reportUnit.getFileSize());
    }
 
}
