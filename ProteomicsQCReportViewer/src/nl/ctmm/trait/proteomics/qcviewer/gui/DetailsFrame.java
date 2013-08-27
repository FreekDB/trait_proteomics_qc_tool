package nl.ctmm.trait.proteomics.qcviewer.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;
import java.util.logging.Logger;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;

import nl.ctmm.trait.proteomics.qcviewer.input.ReportUnit;
import nl.ctmm.trait.proteomics.qcviewer.utils.Constants;

import org.jfree.ui.RefineryUtilities;

/**
 * The class for displaying complete QC metrics report corresponding to one report unit.
 *
 * @author <a href="mailto:pravin.pawar@nbic.nl">Pravin Pawar</a>
 * @author <a href="mailto:freek.de.bruijn@nbic.nl">Freek de Bruijn</a>
 */
public class DetailsFrame extends JFrame implements ActionListener {
    /**
     * The version number for (de)serialization of this class (UID: universal identifier).
     */
    private static final long serialVersionUID = 1;

    /**
     * The logger for this class.
     */
    private static final Logger logger = Logger.getLogger(DetailsFrame.class.getName());
    
    /**
     * Width of the button panel.
     */
    private static final int BUTTON_PANEL_WIDTH = 80;

    /**
     * Height of the button panel.
     */
    private static final int BUTTON_PANEL_HEIGHT = 40;
    
    /**
     * Width of the Metrics ID column.
     */
    private static final int METRICS_ID_COLUMN_WIDTH = 120;
    
    /**
     * Width of the Description column.
     */
    private static final int DESCRIPTION_COLUMN_WIDTH = 280;
    
    /**
     * Width of the Value column. 
     */
    private static final int VALUE_COLUMN_WIDTH = 120;
    
    /**
     * Width of the Details frame. 
     */
    private static final int DETAILS_FRAME_WIDTH = 540;
    
    /**
     * Height of the Details frame. 
     */
    private static final int DETAILS_FRAME_HEIGHT = 720;

    /**
     * Width of the Details frame. 
     */
    private static final int DETAILS_TABLE_WIDTH = 520;
    
    /**
     * Width of the Details frame. 
     */
    private static final int DETAILS_PANEL_WIDTH = 540;
    
    /**
     * Height of the Details frame. 
     */
    private static final int DETAILS_PANEL_HEIGHT = 760;

    /**
     * Title of the details frame.
     */
    private static final String DETAILS_FRAME_TITLE = "All QC Metrics Values for %s";

    /**
     * Message written to the logger while creating metrics key-values dataset.
     */
    private static final String PREPARING_DATASET_MESSAGE = "In DetailsFrame - " 
            + "preparing to create dataset for %s";
    
    /**
      * Constructor for a dialog showing metrics details.
     *
      * @param metricsListing map of all QC metrics
      * @param reportUnit Report unit consisting of all QC metrics values
      */
    public DetailsFrame(final Map<String, String> metricsListing, final ReportUnit reportUnit) {
        super(String.format(DETAILS_FRAME_TITLE, reportUnit.getMsrunName()));
        // Create and fill a metrics table model.
        final DefaultTableModel metricsTableModel = fillMetricsTableModel(metricsListing, reportUnit);
        // Create a table and add it to a scrolling pane.
        final JScrollPane scrollPane = new JScrollPane(createMetricsTable(metricsTableModel));
        // Create the OK button.
        final JButton okButton = new JButton(Constants.OK_BUTTON_TEXT);
        okButton.addActionListener(this);
        okButton.setActionCommand(Constants.OK_BUTTON_TEXT);
        final JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setPreferredSize(new Dimension(BUTTON_PANEL_WIDTH, BUTTON_PANEL_HEIGHT));
        buttonPanel.add(okButton);
        // Create a panel to hold all other components.
        final JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setPreferredSize(new Dimension(DETAILS_PANEL_WIDTH, DETAILS_PANEL_HEIGHT));
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        // Prepare the frame.
        setSize(DETAILS_FRAME_WIDTH, DETAILS_FRAME_HEIGHT);
        setBackground(Color.gray);
//        setResizable(false);
        getContentPane().add(mainPanel);
        RefineryUtilities.centerFrameOnScreen(this);
    }

    /**
     * Create a table model with all supported metrics and their values for the specified report (or "N/A" if not
     * available).
     *
     * @param allMetricsMap the map with all metrics (key: metric ID, value: metric description).
     * @param reportUnit the report for which the metrics details are to be shown.
     * @return the table model.
     */
    private DefaultTableModel fillMetricsTableModel(final Map<String, String> allMetricsMap,
                                                    final ReportUnit reportUnit) {
        // Create columns names.
        final String columnNames[] = {Constants.METRICS_ID_COLUMN_NAME, Constants.DESCRIPTION_COLUMN_NAME, Constants.VALUE_COLUMN_NAME};
        // Read allMetricsMap - key: metricsID, value: MetricName.
        logger.fine(String.format(PREPARING_DATASET_MESSAGE, reportUnit.getMsrunName()));
        // Create data to show inside the table.
        final String dataValues[][] = new String[allMetricsMap.size()][columnNames.length];
        int metricIndex = 0;
        // Read metricsValues corresponding to reportUnit.
        final Map<String, String> metricsValues = reportUnit.getMetricsValues();
        for (final Map.Entry<String, String> metricsData : allMetricsMap.entrySet()) {
            final String[] row = new String[columnNames.length];
            row[0] = metricsData.getKey();
            row[1] = metricsData.getValue();
            row[2] = (metricsValues != null) ? metricsValues.get(metricsData.getKey()) : Constants.NOT_AVAILABLE_STRING;
            dataValues[metricIndex] = row;
            metricIndex++;
        }
        return new DefaultTableModel(dataValues, columnNames);
    }

    /**
     * Create the GUI table with the specified table model.
     *
     * @param metricsTableModel the table model to be used.
     * @return the <code>JTable</code> GUI component.
     */
    private JTable createMetricsTable(final DefaultTableModel metricsTableModel) {
        // Create a new table instance.
        final JTable metricsTable = new JTable(metricsTableModel);
        final int tableHeight = metricsTable.getRowHeight() * metricsTableModel.getRowCount();
        metricsTable.setPreferredSize(new Dimension(DETAILS_TABLE_WIDTH, tableHeight));
        metricsTable.setAutoCreateRowSorter(true);
        metricsTable.setFont(Constants.PLAIN_FONT);
        metricsTable.getRowSorter().toggleSortOrder(0);
        metricsTable.setDefaultRenderer(Object.class, new DetailsTableCellRender());
        final JTableHeader header = metricsTable.getTableHeader();
        header.setBackground(Color.yellow);
        header.setFont(Constants.DETAILS_HEADER_FONT);
        final TableColumn metricsIdColumn = metricsTable.getColumnModel().getColumn(0);
        metricsIdColumn.setPreferredWidth(METRICS_ID_COLUMN_WIDTH);
        final TableColumn descriptionColumn = metricsTable.getColumnModel().getColumn(1);
        descriptionColumn.setPreferredWidth(DESCRIPTION_COLUMN_WIDTH);
        final TableColumn valueColumn = metricsTable.getColumnModel().getColumn(2);
        valueColumn.setPreferredWidth(VALUE_COLUMN_WIDTH);
        return metricsTable;
    }

    /**
     * Event handler for user actions such as pressing the OK button.
     * @param actionEvent the action event that has occurred.
     */
    @Override
    public void actionPerformed(final ActionEvent actionEvent) {
        if (actionEvent.getActionCommand().equals(Constants.OK_BUTTON_TEXT)) {
            dispose();
        }
    }
    
    /**
     * Renderer class specifying cell backgrounds for the details table.
     */
    private static class DetailsTableCellRender extends DefaultTableCellRenderer {
        /**
         * The version number for (de)serialization of this class (UID: universal identifier).
         */
        private static final long serialVersionUID = 1;

        /**
         * Constructor for the custom metrics cell renderer.
         */
        public DetailsTableCellRender() {
            setOpaque(true);  
        }

        @Override
        public Component getTableCellRendererComponent(final JTable table, final Object obj, final boolean isSelected,
                                                       final boolean hasFocus, final int row, final int column) {
            final Component cell = super.getTableCellRendererComponent(table, obj, isSelected, hasFocus, row, column);
            cell.setBackground(isSelected ? Color.green : ((row % 2 == 0) ? Color.white : Color.lightGray));
            return cell;
        }
    }   
}
