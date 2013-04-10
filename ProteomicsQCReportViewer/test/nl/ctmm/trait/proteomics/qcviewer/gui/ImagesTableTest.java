package nl.ctmm.trait.proteomics.qcviewer.gui;

import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.List;

import javax.swing.JTable;

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
public class ImagesTableTest {

    private static final int REPORT_ID_1 = 123456;
    private static final int REPORT_ID_2 = 234567;
    private List<ReportUnit> reportUnits;
    private List<String> columnNames;
    private ImagesTable imagesTable;
    private ImagesTableModel imagesTableModel;

    /**
     * Initialize <code>reportUnits</code>, <code>columnNames</code>, 
     * <code>imagesTableModel</code> and <code>imagesTable</code>.
     */
    @Before
    public void setUp() {
        reportUnits = Arrays.asList(createReportUnit(REPORT_ID_1));
        // Extract column names from comma-separated list.
        final String columnNamesString = "No., File Size(MB), MS1Spectra, MS2Spectra, Measured(yyyy/mmm/dd), " +
                                         "Runtime(hh:mm:ss), heatmap, ioncount";
        columnNames = Arrays.asList(columnNamesString.split(","));
        imagesTableModel = new ImagesTableModel(columnNames, reportUnits.get(0));
        imagesTable = new ImagesTable(imagesTableModel);
    }

    /**
     * Test the <code>setRowHeightAndColumnWidths</code> methods from the <code>ImagesTable/code> interface.
     */
    @Test
    public void testSetRowHeightAndColumnWidths() {
    	imagesTable.setRowHeightAndColumnWidths();
    	JTable baseTable = (JTable) imagesTable;
        assertEquals(50, baseTable.getColumnModel().getColumn(0).getPreferredWidth());
        assertEquals(50, baseTable.getColumnModel().getColumn(1).getPreferredWidth());
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
