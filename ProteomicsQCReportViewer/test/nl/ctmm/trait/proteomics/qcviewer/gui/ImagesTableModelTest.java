package nl.ctmm.trait.proteomics.qcviewer.gui;

import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.List;
import nl.ctmm.trait.proteomics.qcviewer.input.ReportUnit;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for the model for the main JTable that shows the QC report units.
 *
 * @author <a href="mailto:freek.de.bruijn@nbic.nl">Freek de Bruijn</a>
 */
public class ImagesTableModelTest {
    private static final int REPORT_ID_1 = 123456;
    private static final int REPORT_ID_2 = 234567;
    private List<ReportUnit> reportUnits;
    private List<String> columnNames;
    private ImagesTableModel imagesTableModel;

    /**
     * Initialize <code>reportUnits</code> and <code>columnNames</code>.
     */
    @Before
    public void setUp() {
        reportUnits = Arrays.asList(GuiTestUtils.createReportUnit(REPORT_ID_1));
        // Extract column names from comma-separated list.
        final String columnNamesString = "No., File Size(MB), MS1Spectra, MS2Spectra, Measured(yyyy/mmm/dd), " +
                                         "Runtime(hh:mm:ss), heatmap, ioncount";
        columnNames = Arrays.asList(columnNamesString.split(","));
        imagesTableModel = new ImagesTableModel(columnNames, reportUnits.get(0));
    }

    /**
     * Test the <code>getRowCount</code> and <code>getRowCount</code> methods from the <code>ImagesTableModel</code>
     * interface.
     */
    @Test
    public void testSizes() {
        assertEquals(reportUnits.size(), imagesTableModel.getRowCount());
        assertEquals(columnNames.size(), imagesTableModel.getColumnCount());
    }

    /**
     * Test the <code>getValueAt</code> method from the <code>ImagesTableModel</code> interface.
     */
    @Test
    public void testValues() {
        for (int rowIndex = 0; rowIndex < reportUnits.size(); rowIndex++) {
            final ReportUnit reportUnit = reportUnits.get(rowIndex);
            assertEquals(reportUnit.getHeatmap(), imagesTableModel.getValueAt(rowIndex, 0));
            assertEquals(reportUnit.getIoncount(), imagesTableModel.getValueAt(rowIndex, 1));
        }
    }

    /**
     * Test the <code>isCellEditable</code> method: all cells should be read-only.
     */
    @Test
    public void testCellsAreReadOnly() {
        for (int rowIndex = 0; rowIndex < imagesTableModel.getRowCount(); rowIndex++)
            for (int columnIndex = 0; columnIndex < imagesTableModel.getColumnCount(); columnIndex++)
                assertFalse("Cell (row: " + rowIndex + ", column: " + columnIndex + ") is read-only.",
                            imagesTableModel.isCellEditable(rowIndex, columnIndex));
    }

    /**
     * Test the <code>getColumnClass</code> method from the <code>ImagesTableModel</code> interface.
     */
    @Test
    public void testImagesTableModelGetColumnClass() {
        final Class<?> firstColumnClass = imagesTableModel.getColumnClass(0);
        assertEquals("First column: BufferedImage column class.", BufferedImage.class, firstColumnClass);
    }

    /**
     * Test the <code>setReportUnit</code> method from the <code>ImagesTableModel</code> interface.
     */
    @Test
    public void testImagesTableModelSetReportUnit() {
        reportUnits = Arrays.asList(GuiTestUtils.createReportUnit(REPORT_ID_2));
        imagesTableModel.setReportUnit(reportUnits.get(0));
        assertEquals(1, imagesTableModel.getRowCount());
    }
}
