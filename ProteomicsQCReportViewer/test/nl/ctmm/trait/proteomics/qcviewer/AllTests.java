package nl.ctmm.trait.proteomics.qcviewer;

import junit.framework.JUnit4TestAdapter;
import junit.textui.TestRunner;
import nl.ctmm.trait.proteomics.qcviewer.gui.ImageRendererTest;
import nl.ctmm.trait.proteomics.qcviewer.gui.MainTableModelTest;
import nl.ctmm.trait.proteomics.qcviewer.gui.ImagesTableModelTest;
import nl.ctmm.trait.proteomics.qcviewer.gui.ImagesTableTest;
import nl.ctmm.trait.proteomics.qcviewer.gui.ViewerPanelTest;
import nl.ctmm.trait.proteomics.qcviewer.gui.ScrollDesktopTest;
import nl.ctmm.trait.proteomics.qcviewer.input.ChartUnitTest;
import nl.ctmm.trait.proteomics.qcviewer.input.ReportReaderTest;
import nl.ctmm.trait.proteomics.qcviewer.input.ReportUnitTest;
import nl.ctmm.trait.proteomics.qcviewer.utils.UtilitiesTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

// This section declares all of the unit test classes in the project.
@RunWith(Suite.class)
@Suite.SuiteClasses(
        {
                ImageRendererTest.class, MainTableModelTest.class, ReportReaderTest.class, ReportUnitTest.class,
                UtilitiesTest.class, ViewerPanelTest.class, ImagesTableModelTest.class, ImagesTableTest.class, 
                ScrollDesktopTest.class, ChartUnitTest.class
        }
)

/**
 * Code to run all the unit tests in the project.
 *
 * @author <a href="mailto:freek.de.bruijn@nbic.nl">Freek de Bruijn</a>
 */
public class AllTests {
    /**
     * This main method runs all unit tests with the text test runner.
     *
     * @param args the command-line arguments are not used.
     */
    public static void main(final String[] args) {
        TestRunner.run(new JUnit4TestAdapter(AllTests.class));
    }
}
