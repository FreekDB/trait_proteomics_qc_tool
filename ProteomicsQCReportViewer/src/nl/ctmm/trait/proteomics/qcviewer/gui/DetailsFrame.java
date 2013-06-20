package nl.ctmm.trait.proteomics.qcviewer.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;

import nl.ctmm.trait.proteomics.qcviewer.input.MetricsParser;
import nl.ctmm.trait.proteomics.qcviewer.input.ReportUnit;
import nl.ctmm.trait.proteomics.qcviewer.utils.Constants;
import nl.ctmm.trait.proteomics.qcviewer.utils.Utilities;

import org.jfree.ui.RefineryUtilities;

/**
 * The class for displaying complete QC metrics report corresponding to one report unit.
 *
 * @author <a href="mailto:pravin.pawar@nbic.nl">Pravin Pawar</a>
 */
public class DetailsFrame extends JFrame implements ActionListener {

	private static final long serialVersionUID = 1L;
	int style = Font.PLAIN;
    Font font = new Font ("Garamond", style , 11);
 // Instance attributes used in this example
 	private	JPanel		topPanel;
 	private	JTable		table;
 	private	JScrollPane scrollPane;
    
 	/**
 	 * Constructor
 	 * @param metricsListing List of all QC metrics
 	 * @param rUnit Report unit consisting of all QC metrics values
 	 */
	public DetailsFrame(HashMap<String, String> metricsListing, ReportUnit rUnit) {
		super ("All QC Metrics Values for " + rUnit.getMsrunName());
		setSize(520, 740);
		setBackground( Color.gray );

		// Create a panel to hold all other components
		topPanel = new JPanel();
		topPanel.setLayout( new BorderLayout());

		// Create columns names
		String columnNames[] = { "Metrics ID", "Description", "Value" };

		//Read metricsListing - key - metricsID Value - MetricName
		System.out.println("In DetailsFrame - preparing to create dataset for " + rUnit.getMsrunName());
		// Create data to show inside the table
		String dataValues[][] = new String[metricsListing.size()][3];
		int index = 0; 
		//Read metricsValues corresponding to rUnit
		HashMap<?, ?> metricsValues = rUnit.getMetricsValues();
		Iterator<?> it = metricsListing.entrySet().iterator();
	    while (it.hasNext()) {
	        Map.Entry pairs = (Map.Entry)it.next();
	        String listingKey = (String) pairs.getKey();
	        String listingValue = (String) pairs.getValue();
	        //Use listingKey to get value of metrics from metricsValues
	        String metricsValue = "N/A"; 
	        if (metricsValues != null) {
	        	metricsValue = (String) metricsValues.get(listingKey);
	        }
	        //This corresponds to one row in the metricsTable
	        //System.out.println("listingKey = " + listingKey + " listingValue = " + listingValue
	        //		+ " metricsValue = " + metricsValue);
	        String[] row = new String[3];
	        row[0] = listingKey;
	        row[1] = listingValue;
	        row[2] = metricsValue;
			dataValues[index] = row;
	        ++index;
	    }
		
		// Create a new table instance
		table = new JTable(dataValues, columnNames);
		table.setAutoCreateRowSorter(true);
		table.getRowSorter().toggleSortOrder(0);
		table.setDefaultRenderer(Object.class, new TableCellRender()); 
		table.setFont(font);
		TableColumn column0 = table.getColumnModel().getColumn(0);
		column0.setPreferredWidth(130);
		TableColumn column1 = table.getColumnModel().getColumn(1);
		column1.setPreferredWidth(320);
		TableColumn column2 = table.getColumnModel().getColumn(2);
		column2.setPreferredWidth(150);
		JTableHeader header = table.getTableHeader();
		int hStyle = Font.BOLD;
	    Font hFont = new Font ("Garamond", hStyle , 12);
		header.setBackground(Color.yellow);
		header.setFont(hFont);
		// Add the table to a scrolling pane
		scrollPane = new JScrollPane(table);
		topPanel.add(scrollPane, BorderLayout.CENTER);
		JButton SUBMIT = new JButton("OK");
    	SUBMIT.setPreferredSize(new Dimension(80, 30));
  	  	SUBMIT.addActionListener(this);
  	  	SUBMIT.setActionCommand("OK");
  	  	JPanel buttonPanel = new JPanel();
  	  	buttonPanel.add(SUBMIT);
  	  	topPanel.add(buttonPanel, BorderLayout.SOUTH);
		getContentPane().add(topPanel);
		setBounds(0, 0, 540, 780);
		RefineryUtilities.centerFrameOnScreen(this);
	}

	/**
	 * Event handler for user actions such as pressing the OK button
	 */
	@Override
	public void actionPerformed(ActionEvent ae) {
		if (ae.getActionCommand().equals("OK")) {
			dispose();
		}
	}
	
	/**
	 * Renderer class specifying cell backgrounds for the details table
	 * @author <a href="mailto:pravin.pawar@nbic.nl">Pravin Pawar</a>
	 *
	 */
	class TableCellRender extends DefaultTableCellRenderer {  
		private static final long serialVersionUID = 1L;
		public TableCellRender() {  
			setOpaque(true);  
		}  
		   
		public Component getTableCellRendererComponent (JTable table, 
			Object obj, boolean isSelected, boolean hasFocus, int row, int column) {
			Component cell = super.getTableCellRendererComponent(table, obj, isSelected, hasFocus, row, column);
			if (isSelected) {
				cell.setBackground(Color.green);
			} else {
				if (row % 2 == 0) {
					cell.setBackground(Color.white);
				} else {
					cell.setBackground(Color.lightGray);
				}
			}
			return cell;
		}
	}   
}
