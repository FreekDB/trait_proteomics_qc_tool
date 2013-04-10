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
public class MainTableModelTest {

    private static final int REPORT_ID_1 = 123456;
    private static final int REPORT_ID_2 = 234567;

    private List<ReportUnit> reportUnits;
    private List<String> columnNames;
    private MainTableModel tableModel;

    /**
     * Initialize <code>reportUnits</code> and <code>columnNames</code>.
     */
    @Before
    public void setUp() {
        reportUnits = Arrays.asList(createReportUnit(REPORT_ID_1), createReportUnit(REPORT_ID_2));
        // Extract column names from comma-separated list.
        final String columnNamesString = "No., File Size(MB), MS1Spectra, MS2Spectra, Measured(yyyy/mmm/dd), " +
                                         "Runtime(hh:mm:ss), heatmap, ioncount";
        columnNames = Arrays.asList(columnNamesString.split(","));
        tableModel = new MainTableModel(columnNames, reportUnits);
    }

    /**
     * Test the <code>getRowCount</code> and <code>getRowCount</code> methods from the <code>TableModel</code>
     * interface.
     */
    @Test
    public void testSizes() {
        assertEquals(reportUnits.size(), tableModel.getRowCount());
        assertEquals(columnNames.size(), tableModel.getColumnCount());
    }

    /**
     * Test the <code>getValueAt</code> method from the <code>TableModel</code> interface.
     */
    @Test
    public void testValues() {
        for (int rowIndex = 0; rowIndex < reportUnits.size(); rowIndex++) {
            final ReportUnit reportUnit = reportUnits.get(rowIndex);
            assertEquals(reportUnit.getReportNum(), tableModel.getValueAt(rowIndex, 0));
            assertEquals(reportUnit.getFileSize(), tableModel.getValueAt(rowIndex, 1));
            assertEquals(reportUnit.getMs1Spectra(), tableModel.getValueAt(rowIndex, 2));
            assertEquals(reportUnit.getMs2Spectra(), tableModel.getValueAt(rowIndex, 3));
            assertEquals(reportUnit.getMeasured(), tableModel.getValueAt(rowIndex, 4));
            assertEquals(reportUnit.getRuntime(), tableModel.getValueAt(rowIndex, 5));
            assertEquals(reportUnit.getScaledHeatmap(), tableModel.getValueAt(rowIndex, 6));
            assertEquals(reportUnit.getIoncount(), tableModel.getValueAt(rowIndex, 7));
        }
    }

    /**
     * Test the <code>isCellEditable</code> method: all cells should be read-only.
     */
    @Test
    public void testCellsAreReadOnly() {
        for (int rowIndex = 0; rowIndex < tableModel.getRowCount(); rowIndex++)
            for (int columnIndex = 0; columnIndex < tableModel.getColumnCount(); columnIndex++)
                assertFalse("Cell (row: " + rowIndex + ", column: " + columnIndex + ") is read-only.",
                            tableModel.isCellEditable(rowIndex, columnIndex));
    }

    /**
     * Test the <code>getColumnClass</code> method from the <code>TableModel</code> interface.
     */
    @Test
    public void testTableModelGetColumnClass() {
        assertEquals("First column: Integer column class.", Integer.class, tableModel.getColumnClass(0));
        assertEquals("Second column: Double column class.", Double.class, tableModel.getColumnClass(1));
        assertEquals("Third column: String column class.", String.class, tableModel.getColumnClass(2));
        assertEquals("Fourth column: String column class.", String.class, tableModel.getColumnClass(3));
        assertEquals("Fifth column: String column class.", String.class, tableModel.getColumnClass(4));
        assertEquals("Sixth column: String column class.", String.class, tableModel.getColumnClass(5));
        assertEquals("Seventh column: BufferedImage column class.", BufferedImage.class, tableModel.getColumnClass(6));
        assertEquals("Eighth column: BufferedImage column class.", BufferedImage.class, tableModel.getColumnClass(7));
    }

    /**
     * Create a report unit for testing.
     *
     * @param reportNumber the unique report number.
     * @return the new report unit.
     */
    private ReportUnit createReportUnit(final int reportNumber) {
    	/*public ReportUnit(final String msrunName, final int reportNum, final String fileSizeString, final String ms1Spectra,
                final String ms2Spectra, final String measured, final String runtime, final BufferedImage heatmap,
                final BufferedImage ioncount, final String heatmapName, final String ioncountName)*/
        final String s = Integer.toString(reportNumber);
        return new ReportUnit("msrun", reportNumber, s + "1", s + "2", s + "3", s + "4", s + "5", null, null, s + "heatmap.png",
                              s + "ions.png");
    }
}
