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
        reportUnits = Arrays.asList(createReportUnit(REPORT_ID_1));
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
        assertEquals("First column: BufferedImage column class.", BufferedImage.class, imagesTableModel.getColumnClass(0));
    }

    /**
     * Test the <code>setReportUnit</code> method from the <code>ImagesTableModel</code> interface.
     */
    @Test
    public void testImagesTableModelSetReportUnit() {
    	reportUnits = Arrays.asList(createReportUnit(REPORT_ID_2));
    	imagesTableModel.setReportUnit(reportUnits.get(0));
        assertEquals(reportUnits.size(), imagesTableModel.getRowCount());
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
