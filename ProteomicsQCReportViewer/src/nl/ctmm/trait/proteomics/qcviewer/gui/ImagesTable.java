package nl.ctmm.trait.proteomics.qcviewer.gui;

import java.awt.image.BufferedImage;
import javax.swing.JTable;

/**
 * Images table to display larger versions of the heatmap and ioncount images for the selected report unit in the main
 * table.
 *
 * @author <a href="mailto:pravin.pawar@nbic.nl">Pravin Pawar</a>
 * @author <a href="mailto:freek.de.bruijn@nbic.nl">Freek de Bruijn</a>
 */
public class ImagesTable extends JTable {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ImagesTable(final ImagesTableModel imagesTableModel) {
        super(imagesTableModel);
        setShowGrid(true);
        setRowHeightAndColumnWidths();
        setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        // todo: do we need a call to setAutoCreateRowSorter(true) here?
        setDefaultRenderer(BufferedImage.class, new ImageRenderer());
    }

    public void setRowHeightAndColumnWidths() {
        final BufferedImage heatMap = getImage(0);
        final BufferedImage ionCount = getImage(1);
        setRowHeight(Math.max(Math.max(getImageHeight(heatMap), getImageHeight(ionCount)), 50));
        getColumnModel().getColumn(0).setPreferredWidth((heatMap != null) ? heatMap.getWidth() : 50);
        getColumnModel().getColumn(1).setPreferredWidth((ionCount != null) ? ionCount.getWidth() : 50);
    }

    private BufferedImage getImage(final int columnIndex) {
        return (getModel().getRowCount() > 0) ? (BufferedImage) getModel().getValueAt(0, columnIndex) : null;
    }

    private int getImageHeight(final BufferedImage image) {
        return (image != null) ? image.getHeight() : 0;
    }
}
