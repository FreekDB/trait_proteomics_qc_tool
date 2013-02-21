package nl.ctmm.trait.proteomics.qcviewer.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.List;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;

/**
 * Main table to display the report units for the QC Report Viewer.
 *
 * @author <a href="mailto:pravin.pawar@nbic.nl">Pravin Pawar</a>
 * @author <a href="mailto:freek.de.bruijn@nbic.nl">Freek de Bruijn</a>
 */
public class MainTable extends JTable implements ListSelectionListener {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
     * The selection listener that gets notified when row selection changes.
     */
    private final MainSelectionListener mainSelectionListener;

    /**
     * Constructs a <code>JTable</code> that is initialized with <code>mainTableModel</code> as the data model, a
     * default column model, and a default selection model.
     *
     * @param mainTableModel the data model for the table.
     * @see #createDefaultColumnModel
     * @see #createDefaultSelectionModel
     */
    public MainTable(final MainTableModel mainTableModel, final MainSelectionListener mainSelectionListener) {
        super(mainTableModel);

        this.mainSelectionListener = mainSelectionListener;
        getSelectionModel().addListSelectionListener(this);
        setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        setRowSelectionAllowed(true);
        setColumnSelectionAllowed(false);
        if (getRowCount() > 0)
            setRowSelectionInterval(0, 0);
        setShowGrid(true);
        setRowHeight(120);
        setPreferredColumnWidths(getColumnModel());
        setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        setAutoCreateRowSorter(true);
        setDefaultRenderer(BufferedImage.class, new ImageRenderer());
    }

    /**
     * Set the preferred widths of the 8 columns in the column model.
     *
     * @param columnModel the column model to change.
     */
    private void setPreferredColumnWidths(final TableColumnModel columnModel) {
        // No., File Size(MB), MS1Spectra, MS2Spectra, Measured(yyyy/mmm/dd), Runtime(hh:mm:ss), heatmap, ioncount.
        final List<Integer> columnWidths = Arrays.asList(30, 90, 100, 100, 140, 120, 100, 800);
        for (int columnIndex = 0; columnIndex < columnWidths.size(); columnIndex++)
            columnModel.getColumn(columnIndex).setPreferredWidth(columnWidths.get(columnIndex));
    }

    /**
     * This overridden version of <code>prepareRenderer</code> adds alternating background colors for non selected rows.
     *
     * @param renderer    the <code>TableCellRenderer</code> to prepare.
     * @param rowIndex    the row of the cell to render, where 0 is the first row.
     * @param columnIndex the column of the cell to render, where 0 is the first column.
     * @return the renderer with a light gray background for odd rows.
     */
    // http://tips4java.wordpress.com/2010/01/24/table-row-rendering
    // http://www.camick.com/java/source/TableRowRenderingTip.java
    public Component prepareRenderer(final TableCellRenderer renderer, final int rowIndex, final int columnIndex) {
        final Component component = super.prepareRenderer(renderer, rowIndex, columnIndex);
        if (!isRowSelected(rowIndex))
            component.setBackground(rowIndex % 2 == 0 ? getBackground() : Color.LIGHT_GRAY);
        return component;
    }

    /**
     * Handle selection changes.
     *
     * @param selectionEvent the selection event that has happened.
     */
    public void valueChanged(final ListSelectionEvent selectionEvent) {
        super.valueChanged(selectionEvent);

        // Only respond to final messages.
        if (!selectionEvent.getValueIsAdjusting()) {
            final ListSelectionModel selectionModel = (ListSelectionModel) selectionEvent.getSource();
            if (!selectionModel.isSelectionEmpty()) {
                mainSelectionListener.selectRow(selectionModel.getMinSelectionIndex());
            }
        }
    }
}
