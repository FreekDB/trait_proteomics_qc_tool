package nl.ctmm.trait.proteomics.qcviewer.gui;

import java.awt.Dimension;
import java.awt.Rectangle;

import javax.swing.JDesktopPane;
import javax.swing.Scrollable;

/**
 * This class represents the scrollable desktop pane.
 *
 * Pravin found the initial version of this class on
 * http://www.java2s.com/Tutorial/Java/0240__Swing/extendsJDesktopPaneimplementsScrollable.htm
 *
 * @author <a href="mailto:pravin.pawar@nbic.nl">Pravin Pawar</a>
 * @author <a href="mailto:freek.de.bruijn@nbic.nl">Freek de Bruijn</a>
 */
public class ScrollDesktop extends JDesktopPane implements Scrollable {
    /**
     * The version number for (de)serialization of this class (UID: universal identifier).
     */
    private static final long serialVersionUID = 1;

    /**
     * The amount of pixels to scroll when the user scrolls a single unit.
     */
    private static final int SCROLLABLE_UNIT_INCREMENT = 50;

    /**
     * The amount of pixels to scroll when the user scrolls a block.
     */
    private static final int SCROLLABLE_BLOCK_INCREMENT = 200;

    @Override
    public Dimension getPreferredScrollableViewportSize() {
        return getPreferredSize();
    }

    @Override
    public int getScrollableUnitIncrement(final Rectangle visibleRect, final int orientation, final int direction) {
        return SCROLLABLE_UNIT_INCREMENT;
    }

    @Override
    public int getScrollableBlockIncrement(final Rectangle visibleRect, final int orientation, final int direction) {
        return SCROLLABLE_BLOCK_INCREMENT;
    }

    @Override
    public boolean getScrollableTracksViewportWidth() {
        return false;
    }

    @Override
    public boolean getScrollableTracksViewportHeight() {
        return false;
    }
}
