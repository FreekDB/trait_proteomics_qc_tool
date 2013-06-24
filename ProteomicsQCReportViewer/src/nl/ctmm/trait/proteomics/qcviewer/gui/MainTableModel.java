package nl.ctmm.trait.proteomics.qcviewer.gui;

import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Vector;
import javax.swing.table.DefaultTableModel;
import nl.ctmm.trait.proteomics.qcviewer.input.ReportUnit;

/**
 * Model for the main JTable that shows the QC report units.
 *
 * @author <a href="mailto:pravin.pawar@nbic.nl">Pravin Pawar</a>
 * @author <a href="mailto:freek.de.bruijn@nbic.nl">Freek de Bruijn</a>
 */
public class MainTableModel extends DefaultTableModel {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    /**
     * The report units to be displayed by this table model.
     */
    private List<ReportUnit> reportUnits;

    /**
     * Constructs a main table model which contains a list of QC report units.
     *
     * @param reportUnits the report units to be displayed.
     */
    public MainTableModel(final List<String> columnNames, final List<ReportUnit> reportUnits) {
        super(new Vector<String>(columnNames), reportUnits != null ? reportUnits.size() : 0);
        this.reportUnits = reportUnits;
    }

    /**
     * Returns the class of the specified column.
     *
     * @param columnIndex the column being queried.
     * @return the column class.
     */
    @Override
    public Class<?> getColumnClass(final int columnIndex) {
        Class<?> columnClass = Object.class;
        if (columnIndex == 0)
            columnClass = Integer.class;
        else if (columnIndex == 1)
            columnClass = Double.class;
        else if (columnIndex >= 2 && columnIndex <= 5)
            columnClass = String.class;
        else if (columnIndex == 6 || columnIndex == 7)
            columnClass = BufferedImage.class;
        return columnClass;
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
        final ReportUnit reportUnit = reportUnits.get(rowIndex);
        Object value = null;
        switch (columnIndex) {
            case 0:
                value = reportUnit.getReportNum();
                break;
            case 1:
                value = reportUnit.getFileSize();
                break;
            case 2:
                value = reportUnit.getMs1Spectra();
                break;
            case 3:
                value = reportUnit.getMs2Spectra();
                break;
            case 4:
                value = reportUnit.getMeasured();
                break;
            case 5:
                value = reportUnit.getRuntime();
                break;
            case 6:
                value = reportUnit.getScaledHeatmap();
                break;
            case 7:
                value = reportUnit.getIoncount();
                break;
        }
        return value;
    }

//    /**
//     * Set the report units to display in the main table.
//     *
//     * @param reportUnits the report units to display.
//     */
//    public void setReportUnits(final List<ReportUnit> reportUnits) {
//        this.reportUnits = reportUnits;
//    }

    /**
     * All cells are read-only.
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
