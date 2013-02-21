package nl.ctmm.trait.proteomics.qcviewer.gui;

import java.awt.Component;
import java.awt.image.BufferedImage;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import nl.ctmm.trait.proteomics.qcviewer.utils.Utilities;

/**
 * This class renders the images in the tables.
 *
 * @author <a href="mailto:pravin.pawar@nbic.nl">Pravin Pawar</a>
 * @author <a href="mailto:freek.de.bruijn@nbic.nl">Freek de Bruijn</a>
 */
public class ImageRenderer extends DefaultTableCellRenderer {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
     * Create a specific table cell renderer for images.
     *
     * @param table      the <code>JTable</code>.
     * @param value      the value to assign to the cell at <code>[row, column]</code>.
     * @param isSelected true if cell is selected.
     * @param hasFocus   true if cell has focus.
     * @param row        the row of the cell to render.
     * @param column     the column of the cell to render.
     * @return the default table cell renderer.
     */
    public Component getTableCellRendererComponent(final JTable table, final Object value, final boolean isSelected,
                                                   final boolean hasFocus, final int row, final int column) {
        super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        setHorizontalAlignment(JLabel.CENTER);
        setIcon(new ImageIcon(value != null ? (BufferedImage) value : Utilities.getNotAvailableImage()));
//        setText("");
        // replace default font
//        setFont(new Font("Helvetica Bold", Font.ITALIC, 22));
        return this;
    }
}
