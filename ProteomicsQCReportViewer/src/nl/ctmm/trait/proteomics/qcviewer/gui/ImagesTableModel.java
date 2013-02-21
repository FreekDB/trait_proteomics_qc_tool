package nl.ctmm.trait.proteomics.qcviewer.gui;

import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Vector;
import javax.swing.table.DefaultTableModel;
import nl.ctmm.trait.proteomics.qcviewer.input.ReportUnit;

/**
 * Model for the related JTable that shows the zoomed in images for the selected row in the main table.
 *
 * @author <a href="mailto:pravin.pawar@nbic.nl">Pravin Pawar</a>
 * @author <a href="mailto:freek.de.bruijn@nbic.nl">Freek de Bruijn</a>
 */
public class ImagesTableModel extends DefaultTableModel {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
     * The report unit to be displayed by this table model.
     */
    private ReportUnit reportUnit;

    /**
     * Constructs a images table model which contains a single QC report unit.
     *
     * @param reportUnit the report unit to be displayed.
     */
    public ImagesTableModel(final List<String> columnNames, final ReportUnit reportUnit) {
        super(new Vector<String>(columnNames), reportUnit != null ? 1 : 0);
        this.reportUnit = reportUnit;
    }

    /**
     * Returns the class of the specified column.
     *
     * @param columnIndex the column being queried.
     * @return always the buffered image class.
     */
    @Override
    public Class<?> getColumnClass(final int columnIndex) {
        return BufferedImage.class;
    }

    /**
     * Returns the number of rows in this data table.
     *
     * @return the number of rows in the model
     */
    @Override
    public int getRowCount() {
        return reportUnit != null ? 1 : 0;
    }

    /**
     * Returns an attribute value for the cell at <code>row</code> and <code>column</code>.
     *
     * @param rowIndex    the row whose value is to be queried.
     * @param columnIndex the column whose value is to be queried.
     * @return the value at the specified cell.
     * @throws ArrayIndexOutOfBoundsException if an invalid row or column was given.
     * @throws NullPointerException           if the contents of the model are invalid.
     */
    @Override
    public Object getValueAt(final int rowIndex, final int columnIndex) {
        Object value = null;
        switch (columnIndex) {
            case 0:
                value = reportUnit.getHeatmap();
                break;
            case 1:
                value = reportUnit.getIoncount();
                break;
        }
        return value;
    }

    /**
     * Set the report unit for which to display the images and notify all listeners of the change.
     *
     * @param reportUnit the report unit to display.
     */
    public void setReportUnit(final ReportUnit reportUnit) {
        this.reportUnit = reportUnit;
        fireTableDataChanged();
    }

    /**
     * All cells are read only.
     *
     * @param rowIndex    the row whose value is to be queried.
     * @param columnIndex the column whose value is to be queried.
     * @return false.
     */
    @Override
    public boolean isCellEditable(final int rowIndex, final int columnIndex) {
        return false;
    }
}
