package nl.ctmm.trait.proteomics.qcviewer.gui;

import nl.ctmm.trait.proteomics.qcviewer.input.ReportUnit;
import org.junit.Before;
import org.junit.Test;

import javax.swing.JTable;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for the model for the main JTable that shows the QC report units.
 *
 * @author <a href="mailto:freek.de.bruijn@nbic.nl">Freek de Bruijn</a>
 */
public class ImagesTableTest {
    /**
     *
     */
    private static final int REPORT_ID_1 = 123456;

    /**
     * The <code>imagesTable</code> to test.
     */
    private ImagesTable imagesTable;
    private ImagesTableModel imagesTableModel;

    /**
     * Initialize the <code>imagesTable</code>.
     */
    @Before
    public void setUp() {
        final List<ReportUnit> reportUnits = Arrays.asList(GuiTestUtils.createReportUnit(REPORT_ID_1));
        // Extract column names from comma-separated list.
        final String columnNamesString = "No., File Size(MB), MS1Spectra, MS2Spectra, Measured(yyyy/mmm/dd), " +
                                         "Runtime(hh:mm:ss), heatmap, ioncount";
        final List<String> columnNames = Arrays.asList(columnNamesString.split(","));
        imagesTableModel = new ImagesTableModel(columnNames, reportUnits.get(0));
        imagesTable = new ImagesTable(imagesTableModel);
    }

    /**
     * Test the <code>ImagesTable/code> constructor.
     */
    @Test
    public void testConstructor() {
        assertEquals(imagesTableModel, imagesTable.getModel());
        assertEquals(JTable.AUTO_RESIZE_OFF, imagesTable.getAutoResizeMode());
        assertTrue(imagesTable.getShowHorizontalLines());
        assertTrue(imagesTable.getShowVerticalLines());
        assertEquals(ImageRenderer.class, imagesTable.getDefaultRenderer(BufferedImage.class).getClass());
    }

    /**
     * Test the <code>setRowHeightAndColumnWidths</code> method from the <code>ImagesTable/code> class.
     */
    @Test
    public void testSetRowHeightAndColumnWidths() {
        imagesTable.setRowHeightAndColumnWidths();
        assertEquals(50, imagesTable.getColumnModel().getColumn(0).getPreferredWidth());
        assertEquals(50, imagesTable.getColumnModel().getColumn(1).getPreferredWidth());
    }
}
