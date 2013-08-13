package nl.ctmm.trait.proteomics.qcviewer.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;
import java.util.logging.Logger;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
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
    private static final int BUTTON_PANEL_HEIGHT = 25;
    
    /**
     * Width of the Metrics ID column.
     */
    private static final int METRICSID_COLUMN_WIDTH = 120;
    
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
    private static final int DETAILS_FRAME_HEIGHT = 780;

    /**
     * Width of the Details frame. 
     */
    private static final int DETAILS_TABLE_WIDTH = 520;
    
    /**
     * Height of the Details frame. 
     */
    private static final int DETAILS_TABLE_HEIGHT = 700;
    
    /**
     * Width of the Details frame. 
     */
    private static final int DETAILS_PANEL_WIDTH = 540;
    
    /**
     * Height of the Details frame. 
     */
    private static final int DETAILS_PANEL_HEIGHT = 760;
    
    /**
      * Constructor
      * @param metricsListing map of all QC metrics
      * @param reportUnit Report unit consisting of all QC metrics values
      */
    public DetailsFrame(final Map<String, String> metricsListing, final ReportUnit reportUnit) {
        super("All QC Metrics Values for " + reportUnit.getMsrunName());
        setSize(DETAILS_FRAME_WIDTH, DETAILS_FRAME_HEIGHT + 10);
        setBackground(Color.gray);
        setResizable(false);
        // Create a panel to hold all other components.
        final JPanel detailsPanel = new JPanel(); 
        detailsPanel.setLayout(new BoxLayout(detailsPanel, BoxLayout.Y_AXIS));
        detailsPanel.setPreferredSize(new Dimension(DETAILS_PANEL_WIDTH, DETAILS_PANEL_HEIGHT));
        // Create columns names
        final String columnNames[] = {"Metrics ID", "Description", "Value"};

        //Read metricsListing - key - metricsID Value - MetricName
        logger.fine("In DetailsFrame - preparing to create dataset for " + reportUnit.getMsrunName());
        // Create data to show inside the table
        final String dataValues[][] = new String[metricsListing.size()][3];
        int index = 0; 
        //Read metricsValues corresponding to reportUnit
        final Map<String, String> metricsValues = reportUnit.getMetricsValues();
        for (final Map.Entry<String, String> metricsData : metricsListing.entrySet()) {
            final String[] row = new String[3];
            row[0] = metricsData.getKey();
            row[1] = metricsData.getValue();
            row[2] = (metricsValues != null) ? metricsValues.get(metricsData.getKey()) : "N/A";
            dataValues[index] = row;
            index++;
        }
        
        // Create a new table instance.
        final JTable detailsTable = new JTable(dataValues, columnNames);
        detailsTable.setAutoCreateRowSorter(true);
        detailsTable.getRowSorter().toggleSortOrder(0);
        detailsTable.setDefaultRenderer(Object.class, new DetailsTableCellRender());
        final JTableHeader header = detailsTable.getTableHeader();
        header.setBackground(Color.yellow);
        header.setFont(Constants.DETAILS_HEADER_FONT);
        detailsTable.setFont(Constants.PLAIN_FONT);
        final TableColumn metricsIDColumn = detailsTable.getColumnModel().getColumn(0);
        metricsIDColumn.setPreferredWidth(METRICSID_COLUMN_WIDTH);
        final TableColumn DescriptionColumn = detailsTable.getColumnModel().getColumn(1);
        DescriptionColumn.setPreferredWidth(DESCRIPTION_COLUMN_WIDTH);
        final TableColumn valueColumn = detailsTable.getColumnModel().getColumn(2);
        valueColumn.setPreferredWidth(VALUE_COLUMN_WIDTH);
        // Add the table to a scrolling pane
        final JScrollPane scrollPane = new JScrollPane(detailsTable);
        scrollPane.setPreferredSize(new Dimension(DETAILS_TABLE_WIDTH, DETAILS_TABLE_HEIGHT));
        detailsPanel.add(scrollPane);
        final JButton submitButton = new JButton("OK");
        submitButton.addActionListener(this);
        submitButton.setActionCommand("OK");
        submitButton.setPreferredSize(new Dimension(BUTTON_PANEL_WIDTH, BUTTON_PANEL_HEIGHT));
        final JPanel buttonPanel = new JPanel(new FlowLayout()); 
        buttonPanel.add(submitButton);
        buttonPanel.setPreferredSize(new Dimension(BUTTON_PANEL_WIDTH, BUTTON_PANEL_HEIGHT));
        detailsPanel.add(Box.createRigidArea(Constants.DIMENSION_0X10));
        detailsPanel.add(buttonPanel);
        detailsPanel.add(Box.createRigidArea(Constants.DIMENSION_0X10));
        getContentPane().add(detailsPanel);
        setBounds(0, 0, DETAILS_FRAME_WIDTH, DETAILS_FRAME_HEIGHT + 10);
        RefineryUtilities.centerFrameOnScreen(this);
    }

    /**
     * Event handler for user actions such as pressing the OK button.
     * @param actionEvent the action event that has occurred.
     */
    @Override
    public void actionPerformed(final ActionEvent actionEvent) {
        if (actionEvent.getActionCommand().equals("OK")) {
            dispose();
        }
    }
    
    /**
     * Renderer class specifying cell backgrounds for the details table.
     */
    class DetailsTableCellRender extends DefaultTableCellRenderer {
        /**
         * The version number for (de)serialization of this class (UID: universal identifier).
         */
        private static final long serialVersionUID = 1;

        public DetailsTableCellRender() {
            setOpaque(true);  
        }  
           
        public Component getTableCellRendererComponent(JTable table, Object obj, boolean isSelected, boolean hasFocus,
                                                       int row, int column) {
            final Component cell = super.getTableCellRendererComponent(table, obj, isSelected, hasFocus, row, column);
            cell.setBackground(isSelected ? Color.green : ((row % 2 == 0) ? Color.white : Color.lightGray));
            return cell;
        }
    }   
}
