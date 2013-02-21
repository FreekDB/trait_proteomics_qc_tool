package nl.ctmm.trait.proteomics.qcviewer.gui;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.table.TableColumn;
import nl.ctmm.trait.proteomics.qcviewer.input.ReportUnit;

/**
 * ViewerPanel with the GUI for the QC Report Viewer.
 *
 * @author <a href="mailto:pravin.pawar@nbic.nl">Pravin Pawar</a>
 * @author <a href="mailto:freek.de.bruijn@nbic.nl">Freek de Bruijn</a>
 */
public class ViewerPanel extends JPanel implements MainSelectionListener, ItemListener {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final List<String> mainTableColumnNames;
    private List<ReportUnit> reportUnits;
    private MainTableModel mainTableModel;
    private JTable mainTable;
    private Map<String, TableColumn> hiddenColumns = new HashMap<String, TableColumn>();
    private JPanel selectionPanel;
    private List<JCheckBox> selectionCheckBoxes;
    private ImagesTableModel imagesTableModel;
    private ImagesTable imagesTable;

    /**
     * Construct a viewer panel with the necessary GUI components.
     *
     * @param mainTableColumnNames   the list with the column names for the main table.
     * @param imagesTableColumnNames the list with the column names for the zoomed images table.
     */
    public ViewerPanel(final List<String> mainTableColumnNames, final List<String> imagesTableColumnNames) {
        this.mainTableColumnNames = mainTableColumnNames;
        this.reportUnits = new ArrayList<ReportUnit>();
        createComponents(mainTableColumnNames, imagesTableColumnNames);
        addComponents();
    }

    /**
     * Create the GUI components.
     *
     * @param mainTableColumnNames   the list with the column names for the main table.
     * @param imagesTableColumnNames the list with the column names for the zoomed images table.
     */
    private void createComponents(final List<String> mainTableColumnNames, final List<String> imagesTableColumnNames) {
        this.mainTableModel = new MainTableModel(mainTableColumnNames, this.reportUnits);
        this.mainTable = new MainTable(this.mainTableModel, this);
        this.selectionPanel = new JPanel(new GridLayout(0, 1));
        this.selectionCheckBoxes = new ArrayList<JCheckBox>();
        for (final String columnName : mainTableColumnNames) {
            final JCheckBox selectionCheckBox = new JCheckBox(columnName);
            selectionCheckBox.setName(columnName);
            selectionCheckBox.setSelected(true);
            selectionCheckBox.addItemListener(this);
            this.selectionCheckBoxes.add(selectionCheckBox);
        }
        this.imagesTableModel = new ImagesTableModel(imagesTableColumnNames, null);
        this.imagesTable = new ImagesTable(this.imagesTableModel);
    }

    /**
     * Initialize and add the components to this viewer panel and add the selection check boxes to the selection panel.
     */
    private void addComponents() {
        // todo: use layout managers instead of fixed sizes?
        final JScrollPane mainTableScrollPane = new JScrollPane(mainTable);
        mainTableScrollPane.setPreferredSize(new Dimension(1000, 400));
        selectionPanel.setBorder(BorderFactory.createTitledBorder("Columns"));
        selectionPanel.setSize(new Dimension(150, 400));
        for (final JCheckBox selectionCheckBox : selectionCheckBoxes)
            selectionPanel.add(selectionCheckBox);
        final JSplitPane topSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        topSplitPane.add(mainTableScrollPane);        
        topSplitPane.add(selectionPanel);
        final JScrollPane imagesTableScrollPane = new JScrollPane(imagesTable);
        imagesTableScrollPane.setBorder(BorderFactory.createTitledBorder("Selected images"));
        imagesTableScrollPane.setMinimumSize(new Dimension(1000, 100));
        imagesTableScrollPane.setPreferredSize(new Dimension(1000, 400));
        final JSplitPane mainSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        mainSplitPane.add(topSplitPane);
        mainSplitPane.add(imagesTableScrollPane);
        add(mainSplitPane);
    }

    /**
     * Get the main table component.
     *
     * @return the main table.
     */
    public JTable getMainTable() {
        return mainTable;
    }

    /**
     * Get the selected report unit.
     *
     * @return the selected report unit or <code>null</code> if no row is selected.
     */
    public ReportUnit getSelectedReportUnit() {
        final int rowIndex = mainTable.getSelectedRow();
        final boolean rowAvailable = reportUnits != null && rowIndex >= 0 && rowIndex < reportUnits.size();
        return rowAvailable ? reportUnits.get(rowIndex) : null;
    }

    /**
     * Get the selection panel component (which contains the column selection check boxes).
     *
     * @return the selection panel.
     */
    public JPanel getSelectionPanel() {
        return selectionPanel;
    }

    /**
     * Get the list with the selection check boxes.
     *
     * @return the selection check boxes.
     */
    public List<JCheckBox> getSelectionCheckBoxes() {
        return selectionCheckBoxes;
    }

    /**
     * Get the zoomed images table component.
     *
     * @return the zoomed images table.
     */
    public JTable getImagesTable() {
        return imagesTable;
    }

    /**
     * Set the report units to be displayed.
     *
     * @param reportUnits the report units to be displayed.
     */
    public void setReportUnits(final List<ReportUnit> reportUnits) {
        this.reportUnits = reportUnits;
        this.mainTableModel = new MainTableModel(this.mainTableColumnNames, this.reportUnits);
        this.mainTable.setModel(this.mainTableModel);
        if (this.mainTable.getRowCount() > 0)
            this.mainTable.setRowSelectionInterval(0, 0);
        this.imagesTableModel.setReportUnit(getSelectedReportUnit());
        this.imagesTable.setRowHeightAndColumnWidths();
    }

    /**
     * Handle the selection event in the main table: show the corresponding images.
     *
     * @param rowIndex the index of the selected row.
     */
    @Override
    public void selectRow(final int rowIndex) {
        showImagesForSelectedReportUnit();
    }

    /**
     * Select the images in the zoomed images tabled that correspond to the selected row in the main table.
     */
    public void showImagesForSelectedReportUnit() {
        imagesTableModel.setReportUnit(getSelectedReportUnit());
        imagesTable.setRowHeightAndColumnWidths();
    }

    /**
     * Handles check box events corresponding to the selection of columns to show/hide in the main table.
     *
     * @param itemEvent the item event data.
     */
    @Override
    public void itemStateChanged(final ItemEvent itemEvent) {
        final String checkBoxName = ((JCheckBox) itemEvent.getItemSelectable()).getName();
        if (itemEvent.getStateChange() == ItemEvent.DESELECTED) {
            // Check box is deselected: remove column from table.
            final TableColumn tableColumn = mainTable.getColumn(checkBoxName);
            mainTable.removeColumn(tableColumn);
            hiddenColumns.put(checkBoxName, tableColumn);
        } else if (hiddenColumns.containsKey(checkBoxName)) {
            // Check box is selected: add column to table.
            mainTable.addColumn(hiddenColumns.get(checkBoxName));
            hiddenColumns.remove(checkBoxName);
            // Determine column index based on column order (columnSelect) and visibility (hiddenColumns).
            int targetColumnIndex = 0;
            for (final JCheckBox checkBox : selectionCheckBoxes) {
                final String columnName = checkBox.getName();
                if (columnName.equals(checkBoxName)) {
                    break;
                } else if (!hiddenColumns.containsKey(columnName)) {
                    targetColumnIndex++;
                }
            }
            mainTable.moveColumn(mainTable.getColumnCount() - 1, targetColumnIndex);
        }
        mainTable.validate();
    }
}
