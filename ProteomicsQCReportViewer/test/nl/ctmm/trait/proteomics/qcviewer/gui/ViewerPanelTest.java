package nl.ctmm.trait.proteomics.qcviewer.gui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import nl.ctmm.trait.proteomics.qcviewer.input.ReportUnit;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.number.OrderingComparison.lessThan;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for the <code>ViewerPanel</code> class.
 * <p/>
 * This class was inspired by the article "The Test/Code Cycle in XP, Part 2: GUI" by Bill Wake
 * (see http://xp123.com/articles/the-testcode-cycle-in-xp-part-2-gui).
 *
 * @author <a href="mailto:freek.de.bruijn@nbic.nl">Freek de Bruijn</a>
 */
public class ViewerPanelTest {
    /**
     * The list of column names for the main table initialized by <code>setUp</code>.
     */
    private List<String> mainTableColumnNames;

    /**
     * The number of column names for the main table initialized by <code>setUp</code>.
     */
    private int columnCount;

    /**
     * The <code>ViewerPanel</code> to be tested, initialized by <code>setUp</code>.
     */
    private ViewerPanel viewerPanel;

    /**
     * Initialize a <code>ViewerPanel</code>.
     */
    @Before
    public void setUp() {
        final String columnNamesString = "No., File Size(MB), MS1Spectra, MS2Spectra, Measured(yyyy/mmm/dd), " +
                                         "Runtime(hh:mm:ss), heatmap, ioncount";
        mainTableColumnNames = Arrays.asList(columnNamesString.split(","));
        columnCount = mainTableColumnNames.size();
        viewerPanel = new ViewerPanel(mainTableColumnNames, Arrays.asList("heatmap", "ioncount"));
    }

    /**
     * Check whether the <code>ViewerPanel</code> contains the controls we expect.
     */
    @Test
    public void testControlsPresent() {
        assertNotNull(viewerPanel.getMainTable());
        assertNotNull(viewerPanel.getSelectionCheckBoxes());
        assertNotNull(viewerPanel.getImagesTable());
    }

    /**
     * Check the initial contents of the controls.
     */
    @Test
    public void testInitialContents() {
        assertEquals(columnCount, viewerPanel.getMainTable().getColumnCount());
        assertEquals(0, viewerPanel.getMainTable().getRowCount());
        assertEquals(columnCount, viewerPanel.getSelectionCheckBoxes().size());
        for (int checkIndex = 0; checkIndex < viewerPanel.getSelectionCheckBoxes().size(); checkIndex++) {
            final JCheckBox checkBox = viewerPanel.getSelectionCheckBoxes().get(checkIndex);
            assertEquals(mainTableColumnNames.get(checkIndex), checkBox.getText());
        }
        assertEquals(2, viewerPanel.getImagesTable().getColumnCount());
    }

    /**
     * Test setReportUnits with different numbers of report units.
     */
    @Test
    public void testSetReportUnits() {
        assertEquals(columnCount, viewerPanel.getMainTable().getColumnCount());
        assertEquals(0, viewerPanel.getMainTable().getRowCount());
        for (int reportCount = 0; reportCount < 7; reportCount++) {
            final List<ReportUnit> reportUnits = new ArrayList<>();
            for (int reportIndex = 0; reportIndex < reportCount; reportIndex++)
                reportUnits.add(new ReportUnit("msrun" + reportIndex, reportIndex));
            viewerPanel.setReportUnits(reportUnits);
            assertEquals("We expect 8 columns.", columnCount, viewerPanel.getMainTable().getColumnCount());
            assertEquals("We expect " + reportUnits.size() + " report units.",
                         reportUnits.size(), viewerPanel.getMainTable().getRowCount());
        }
    }

    /**
     * Test the positions of the GUI components.
     */
    @Test
    public void testPositions() {
        final JFrame testFrame = new JFrame("test frame");
        testFrame.getContentPane().add(viewerPanel);
        // Use negative x and y to keep the test frame off the screen.
        testFrame.setBounds(-1024, -768, 1024, 768);
        // A components location on the screen can only be determined when it is visible.
        testFrame.setVisible(true);

        assertThat("We expect the main table to be on the left of the selection panel.",
                   viewerPanel.getMainTable().getLocationOnScreen().x,
                   is(lessThan(viewerPanel.getSelectionPanel().getLocationOnScreen().x)));
        assertThat("We expect the main table to be above the images table.",
                   viewerPanel.getMainTable().getLocationOnScreen().y,
                   is(lessThan(viewerPanel.getImagesTable().getLocationOnScreen().y)));
        assertThat("We expect the selection panel to be above the images table.",
                   viewerPanel.getSelectionPanel().getLocationOnScreen().y,
                   is(lessThan(viewerPanel.getImagesTable().getLocationOnScreen().y)));

        testFrame.dispose();
    }

    @Test
    public void testSelectMainRow() {
        viewerPanel.showImagesForSelectedReportUnit();
    }

    // todo: select a row in the main table and check the images table.

//    /**
//     * Test the <code>createGui</code> method of the <code>TableTestFreek</code> class.
//     * <p/>
//     * todo: is now replaced by the other unit tests?
//     */
//    @Test
//    public void testCreateGui() {
//        final JFrame viewerFrame = new TableTestFreek().createGui("1,2,3", new ArrayList<ReportUnit>());
//        assertEquals(1, viewerFrame.getContentPane().getComponents().length);
//    }
}
