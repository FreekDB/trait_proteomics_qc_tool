package nl.ctmm.trait.proteomics.qcviewer.gui;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.List;

import javax.swing.JTable;

import nl.ctmm.trait.proteomics.qcviewer.input.ReportUnit;

import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for the model for the main JTable that shows the QC report units.
 *
 * @author <a href="mailto:freek.de.bruijn@nbic.nl">Freek de Bruijn</a>
 */
public class ScrollDesktopTest {

    private ScrollDesktop scrollDesktop;

    /**
     * Initialize <code>scrollDesktop</code>.
     */
    @Before
    public void setUp() {
        scrollDesktop = new ScrollDesktop();
    }

    /**
     * Test the <code>getScrollableUnitIncrement</code>, <code>getScrollableBlockIncrement</code>,
     * <code>getScrollableTracksViewportWidth</code>, <code>getScrollableTracksViewportHeight</code> methods from the <code>ScrollDesktop</code> interface.
     */
    @Test
    public void testScrollDesktopMethods() {
    	Dimension dimension = new Dimension(0,0);
        assertEquals(50, scrollDesktop.getScrollableUnitIncrement(null, 0, 0));
        assertEquals(200, scrollDesktop.getScrollableBlockIncrement(null, 0, 0));
        assertFalse("", scrollDesktop.getScrollableTracksViewportWidth());
        assertFalse("", scrollDesktop.getScrollableTracksViewportHeight());
        //TODO: Check the Dimension.class result
        assertEquals("Expecting Dimension class.", Dimension.class, scrollDesktop.getPreferredScrollableViewportSize());
    }

}
