package nl.ctmm.trait.proteomics.qcviewer.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import nl.ctmm.trait.proteomics.qcviewer.input.ReportUnit;
import org.jfree.ui.RefineryUtilities;

/**
 * The class for displaying complete QC metrics report corresponding to one report unit.
 *
 * @author <a href="mailto:pravin.pawar@nbic.nl">Pravin Pawar</a>
 * @author <a href="mailto:freek.de.bruijn@nbic.nl">Freek de Bruijn</a>
 */
public class DetailsFrame extends JFrame implements ActionListener {
    private static final long serialVersionUID = 1L;

    /**
      * Constructor
      * @param metricsListing map of all QC metrics
      * @param reportUnit Report unit consisting of all QC metrics values
      */
    public DetailsFrame(final Map<String, String> metricsListing, final ReportUnit reportUnit) {
        super("All QC Metrics Values for " + reportUnit.getMsrunName());
        setSize(520, 740);
        setBackground(Color.gray);

        // Create a panel to hold all other components.
        final JPanel topPanel = new JPanel();
        topPanel.setLayout(new BorderLayout());

        // Create columns names
        final String columnNames[] = {"Metrics ID", "Description", "Value"};

        //Read metricsListing - key - metricsID Value - MetricName
        System.out.println("In DetailsFrame - preparing to create dataset for " + reportUnit.getMsrunName());
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
        header.setFont(new Font("Garamond", Font.BOLD, 12));
        detailsTable.setFont(new Font("Garamond", Font.PLAIN, 11));
        final TableColumn column0 = detailsTable.getColumnModel().getColumn(0);
        column0.setPreferredWidth(130);
        final TableColumn column1 = detailsTable.getColumnModel().getColumn(1);
        column1.setPreferredWidth(320);
        final TableColumn column2 = detailsTable.getColumnModel().getColumn(2);
        column2.setPreferredWidth(150);
        // Add the table to a scrolling pane
        final JScrollPane scrollPane = new JScrollPane(detailsTable);
        topPanel.add(scrollPane, BorderLayout.CENTER);
        final JButton submitButton = new JButton("OK");
        submitButton.setPreferredSize(new Dimension(80, 30));
        submitButton.addActionListener(this);
        submitButton.setActionCommand("OK");
        final JPanel buttonPanel = new JPanel();
        buttonPanel.add(submitButton);
        topPanel.add(buttonPanel, BorderLayout.SOUTH);
        getContentPane().add(topPanel);
        setBounds(0, 0, 540, 780);
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
        private static final long serialVersionUID = 1L;

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
