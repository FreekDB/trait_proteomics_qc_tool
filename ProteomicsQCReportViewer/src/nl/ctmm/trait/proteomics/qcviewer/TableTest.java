package nl.ctmm.trait.proteomics.qcviewer;

/*
 * part of this code is taken from 
 *  http://docs.oracle.com/javase/tutorial/uiswing/examples/components/index.html 
 *  http://www.coderanch.com/t/340043/GUI/java/Adding-images-JTable
 *  http://www.java2s.com/Code/Java/Swing-JFC/Therevalidatemethodtodynamicallyupdatethe.htm
 *  AbstractTableModel: http://www.java2s.com/Code/Java/Swing-JFC/CreatingsimpleJTableusingAbstractTableModel.htm
 *  TableCellRenderer: http://www.java2s.com/Tutorial/Java/0240__Swing/implementsTableCellRenderer.htm
 */

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.table.*;

import nl.ctmm.trait.proteomics.qcviewer.input.ReportUnit;

/**
 * This class is responsible for displaying QC Reports in the graphical format, user interaction and UI events handling.   
 * @author 
 *
 */
public class TableTest implements ItemListener 
{
    private static final Logger logger = Logger.getLogger("nl.ctmm.trait.proteomics.qcviewer.TableTest");
    final int
        FIT  = 0,
        FILL = 1;
    //Global variables
    JCheckBox[] columnSelect = null;
    int totalTopColumns = 0;
    private List<ReportUnit> reportArrayList = null;
    JFrame mainFrame = null;
    JTable topTable = null;
    JTable bottomTable = null;
    private Map<String, TableColumn> hiddenColumns = new HashMap<String, TableColumn>();
    String topColumns = "";
    String bottomColumns = "";
    
    /**
     * Obtain all the QC reports in a particular rootFolder and store them in a List format
     * @param rootFolder the location of QC Reports Folder
     */
    public void getAllReports(String rootFolder) {
    	//Pass RootFolder property to reader class and initialize reader class
    	Reader folderReader = new Reader(rootFolder);
    	reportArrayList = folderReader.prepareReportArrayList(); 
    	logger.log(Level.ALL, "Total number of reports = " + reportArrayList.size());
    	for (int i = 0; i < reportArrayList.size(); ++i) {
    		ReportUnit rUnit = reportArrayList.get(i);
    		logger.log(Level.ALL, "Report Number " + i + " :");
    		rUnit.printReportValues(); 
    	}
    }
    
    private JPanel createCheckBoxContainer ()
    {
		//UI element - make sure that the selection of checkboxes is reflected properly 
		JPanel checkBoxContainer = new JPanel(new GridLayout(0,1));
        checkBoxContainer.setBorder(BorderFactory.createTitledBorder("Columns"));
        StringTokenizer stkz = new StringTokenizer(this.topColumns, ",");
        totalTopColumns = stkz.countTokens();
       	logger.log(Level.ALL, "Initiating global ColumnSelect array");
       	columnSelect = new JCheckBox[totalTopColumns];
       	for (int i = 0; i < totalTopColumns; ++i) {
        	String thisColumn = stkz.nextToken();
        	JCheckBox thisBox = new JCheckBox(thisColumn);
        	thisBox.setName(thisColumn);
        	thisBox.setSelected(true);
        	columnSelect[i] = thisBox;
        	//Register a listener for the check boxes.
        	columnSelect[i].addItemListener(this);
        	checkBoxContainer.add(columnSelect[i]);
        }
       	checkBoxContainer.setSize(new Dimension(150, 400)); //height is 400 for topBox
        return checkBoxContainer;
    }

    private JPanel createTopTableContainer ()
    {
		//UI element
    	topTable = createTopTable(reportArrayList); 

        DefaultTableCellRenderer topRenderer =
            (DefaultTableCellRenderer)topTable.getDefaultRenderer(Image.class);
        topRenderer.setHorizontalAlignment(JLabel.CENTER);
        JScrollPane topTablePane = new JScrollPane(topTable);
        topTablePane.setPreferredSize(new Dimension(800, 400));
        JPanel topTableContainer = new JPanel(new GridLayout(1,1));
        topTableContainer.setBorder(BorderFactory.createTitledBorder("QC Report Table"));
        topTableContainer.add(topTablePane);
        return topTableContainer;
    }
    
    private JPanel createTopHalfContainer() {
        JPanel topHalfContainer = new JPanel();
        topHalfContainer.setLayout(new BoxLayout(topHalfContainer, BoxLayout.X_AXIS));
        topHalfContainer.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        topHalfContainer.add(createCheckBoxContainer());
        topHalfContainer.add(createTopTableContainer());
        topHalfContainer.setMinimumSize(new Dimension(1200, 400));
        topHalfContainer.setPreferredSize(new Dimension(1200, 400));
        return topHalfContainer;
    }
    
    private JPanel createBottomHalfContainer(int rowNum) {
      //Build image area (display bigger image in the bottom).
        JPanel imagePane = new JPanel();
        imagePane.add(new JLabel("Selected images:"));
        createAbstractBottomTable(rowNum); //The row number corresponds to report entry
        DefaultTableCellRenderer bottomRenderer =
            (DefaultTableCellRenderer)bottomTable.getDefaultRenderer(Image.class);
        bottomRenderer.setHorizontalAlignment(JLabel.CENTER);
        JScrollPane bottomTablePane = new JScrollPane(bottomTable);
        JPanel bottomHalfContainer = new JPanel(new BorderLayout());
        bottomHalfContainer.add(imagePane, BorderLayout.NORTH);
        bottomHalfContainer.add(bottomTablePane);
        //XXX: next line needed if bottomHalf is a scroll pane:
        bottomHalfContainer.setMinimumSize(new Dimension(1200, 400));
        bottomHalfContainer.setPreferredSize(new Dimension(1200, 400));
        return bottomHalfContainer;
    }

    private JFrame createMainFrame(int rowNum) {
    	//Do the layout.
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.add(createTopHalfContainer());
        splitPane.add(createBottomHalfContainer(rowNum));
        mainFrame = new JFrame();
        mainFrame.setTitle("OPL Lab QC Report Viewer");
        mainFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        mainFrame.getContentPane().add(splitPane);
        mainFrame.setSize(1200,800);
        mainFrame.setLocation(100,100);
        mainFrame.setResizable(true);
        return mainFrame;
    }
    
    /**
     * Create main frame that holds cColumn Selection panel, QC Report Table panel and Selected Images Table panel.
     * @param rootFolder the location of QC Reports Folder
     * @param topColumns column names in the QC Reports Table (upper table)
     * @param bottomColumns column names in the Selected Images Table (lower table)
     */
    public void displayQCReport (String rootFolder, String topColumns, String bottomColumns)
    {
    	this.topColumns = topColumns;
    	this.bottomColumns = bottomColumns; 
    	getAllReports(rootFolder);
    	//link to the index of selected row in top table
    	mainFrame = createMainFrame(0);
    	mainFrame.setVisible(true);
    }
  
    /**
     * Handles the events corresponding to the selection of columns to show/hide in the QC Report Table.
     */
	@Override
	public void itemStateChanged(ItemEvent e) {
		// Find out which check box and the state of check box - checked or unchecked.
        final Object source = e.getItemSelectable();
        logger.log(Level.ALL, "Source class name = " + source.getClass().getName());
        // @TODO check whether columnSelect is necessary
        if (source instanceof JCheckBox) {
        	final String checkBoxName = ((JCheckBox) source).getName();
        	logger.log(Level.ALL, "Name of changed check box = " + checkBoxName);

            // stackoverflow.com/questions/10088853/could-not-set-the-column-width-to-zero-i-e-not-made-column-invisible
            // stackoverflow.com/questions/8693799/how-to-insert-a-column-at-a-specific-position-in-jtable
            if (e.getStateChange() == ItemEvent.DESELECTED) {
                // Check box is deselected: remove column from table.
                final TableColumn tableColumn = topTable.getColumn(checkBoxName);
                topTable.removeColumn(tableColumn);
                hiddenColumns.put(checkBoxName, tableColumn);
            } else if (hiddenColumns.containsKey(checkBoxName)) {
                // Check box is selected: add column to table.
                topTable.addColumn(hiddenColumns.get(checkBoxName));
                hiddenColumns.remove(checkBoxName);
                // Determine column index based on column order (columnSelect) and visibility (hiddenColumns).
                int targetColumnIndex = 0;
                for (final JCheckBox checkBox : columnSelect) {
                    final String columnName = checkBox.getName();
                    if (columnName.equals(checkBoxName)) {
                        break;
                    } else if (!hiddenColumns.containsKey(columnName)) {
                        targetColumnIndex++;
                    }
                }
                topTable.moveColumn(topTable.getColumnCount() - 1, targetColumnIndex);
            }
            topTable.validate();
        }
	}
 
    private BufferedImage getScaledImage(BufferedImage in, int type) {
        int WIDTH = 100;
        int HEIGHT = 100;
        BufferedImage out;
        out = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = out.createGraphics();
        g2.setColor(Color.white);
        g2.fillRect(0, 0, WIDTH, HEIGHT);
        double width  = in.getWidth();
        double height = in.getHeight();
        double xScale = WIDTH  / width;
        double yScale = HEIGHT / height;
        double scale = 1.0;
        switch(type)
        {
            case FIT:
            scale = Math.min(xScale, yScale);  // scale to fit
            break;
            case FILL:
            scale = Math.max(xScale, yScale);  // scale to fill
        }
        double x = (WIDTH - width * scale)/2;
        double y = (HEIGHT - height * scale)/2;
        AffineTransform at = AffineTransform.getTranslateInstance(x, y);
        at.scale(scale, scale);
        g2.drawRenderedImage(in, at);
        g2.dispose();
        return out;
    }
 
    private JTable createTopTable(List<ReportUnit> reportArrayList) //reportArrayList represents rows
    {
    	//Prepare Object[][] allData to hold information in the table. Setups values and column names.
        //find out which columns are selected. Display information only for those. 
    	//Only include the columns which are selected
    	
    	int numSelectedColumns = 0; 
      	String[] columnHeaders;

        for (final JCheckBox checkBox : columnSelect) {
            //First check total number of selected columns
            if (checkBox.isSelected()) {
                ++numSelectedColumns;
            }
        }
    	logger.log(Level.ALL, "Number of selected columns = " + numSelectedColumns);
    	columnHeaders = new String[numSelectedColumns];
    	//copy column names to columnHeaders
    	int currHeaderNum = 0;
        for (final JCheckBox checkBox : columnSelect) {
            //First check total number of selected columns
            if (checkBox.isSelected()) {
                columnHeaders[currHeaderNum] = checkBox.getName();
                logger.log(Level.ALL, "ColumnHeaders[" + currHeaderNum + "] = " + columnHeaders[currHeaderNum]);
                ++currHeaderNum;
            }
        }
    	
    	final Object[][] allData = new Object[reportArrayList.size()][numSelectedColumns]; //rows,columns
    	
        //copy reportArrayList to values
        for (int i = 0; i < reportArrayList.size(); ++i) {
        	ReportUnit rUnit = reportArrayList.get(i);
        	//copy individual elements into values one by one depending on check-box status
        	int currColumn = 0; 
        	//The danger of hard-coding column number 
        	if (columnSelect[0].isSelected()) {
        		allData[i][currColumn] = rUnit.getReportNum();
        		++currColumn;
        	}
        	if (columnSelect[1].isSelected()) {
                if (!rUnit.getFileSizeString().trim().isEmpty()) {
        		    allData[i][currColumn] = Double.parseDouble(rUnit.getFileSizeString());
                }
        		++currColumn;
        	}
        	if (columnSelect[2].isSelected()) {
        		allData[i][currColumn] = rUnit.getMs1Spectra();
        		++currColumn;
        	}
        	if (columnSelect[3].isSelected()) {
        		allData[i][currColumn] = rUnit.getMs2Spectra();
        		++currColumn;
        	}
        	if (columnSelect[4].isSelected()) {
        		allData[i][currColumn] = rUnit.getMeasured();
        		++currColumn;
        	}
        	if (columnSelect[5].isSelected()) {
        		allData[i][currColumn] = rUnit.getRuntime();
        		++currColumn;
        	}
        	if (columnSelect[6].isSelected()) {
        		allData[i][currColumn] = getScaledImage(rUnit.getHeatmap(), FIT);
        		++currColumn;
        	}
        	if (columnSelect[7].isSelected()) {
        		allData[i][currColumn] = rUnit.getIoncount();
        		++currColumn;
        	}
        }
       
        DefaultTableModel topTableModel = new DefaultTableModel(allData, columnHeaders)
        {
            public Class getColumnClass(int col)
            {
                switch (col) {
                case 0: return Integer.class;
                case 1: return Double.class;
                case 2: return String.class;
                case 3: return String.class;
                case 4: return String.class;
                case 5: return String.class;
                case 6: return BufferedImage.class;
                case 7: return BufferedImage.class;
                case 8: return String.class;
                case 9: return String.class;
            }
            return Object.class;
            }
 
            public boolean isCellEditable(int row, int col)
            {
            	return false;   // All cells are NOT editable!
            }

            public Object getValueAt(int row, int col) {
                return allData[row][col];
            }
        };

        JTable table = new JTable(topTableModel) {
            /**
             * This overridden version of <code>prepareRenderer</code> adds alternating background colors for rows.
             * @param renderer  the <code>TableCellRenderer</code> to prepare.
             * @param row       the row of the cell to render, where 0 is the first row.
             * @param column    the column of the cell to render, where 0 is the first column.
             * @return the renderer with a light gray background for odd rows.
             */
            // http://tips4java.wordpress.com/2010/01/24/table-row-rendering
            // http://www.camick.com/java/source/TableRowRenderingTip.java
            public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
                final Component component = super.prepareRenderer(renderer, row, column);
                if (!isRowSelected(row))
                    component.setBackground(row % 2 == 0 ? getBackground() : Color.LIGHT_GRAY);
                return component;
            }
   		};
        table.setDefaultRenderer(BufferedImage.class, new ImageRenderer());
        table.setName("QC Report Table");
        table.setRowHeight(100);
        table.getColumnModel().getColumn(0).setPreferredWidth(30); //No.
        table.getColumnModel().getColumn(1).setPreferredWidth(90); //FileSize
        table.getColumnModel().getColumn(2).setPreferredWidth(100); //MS1Spectra
        table.getColumnModel().getColumn(3).setPreferredWidth(100); //MS2Spectra
        table.getColumnModel().getColumn(4).setPreferredWidth(140); //Measured
        table.getColumnModel().getColumn(5).setPreferredWidth(120); //Runtime
        table.getColumnModel().getColumn(6).setPreferredWidth(100); //heatmap
        table.getColumnModel().getColumn(7).setPreferredWidth(900); //ioncount
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setShowGrid(true);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF); 
        table.setAutoCreateRowSorter(true);
        return table;
    }
    
    class BottomTableData {
    	  private BufferedImage heatMap = null;
    	  private BufferedImage ionCount = null;

    	  public BottomTableData(BufferedImage heatMap, BufferedImage ionCount) {
    	    this.heatMap = heatMap;
    	    this.ionCount = ionCount;
    	  }

    	  public BufferedImage getHeatMap() {
    	    return heatMap;
    	  }

    	  public BufferedImage getIonCount() {
    	    return ionCount;
    	  }
    	  
    	  public void setHeatMap(BufferedImage heatMap) {
    		  this.heatMap = heatMap;
    	  }
    	  
       	  public void setIonCount(BufferedImage ionCount) {
    		  this.ionCount = ionCount;
    	  }
    }
    
    class BottomTableModel extends AbstractTableModel {
       	   public String[] m_colNames = {"heatmap", "ioncount"};
    	    public Class[] m_colTypes = {BufferedImage.class, BufferedImage.class};
    	    Vector m_macDataVector;

    	    public BottomTableModel(Vector macDataVector) {
    	      m_macDataVector = macDataVector;
    	    }
    	    
    	    public int getColumnCount() {
    	      return m_colNames.length;
    	    }
    	    
    	    public int getRowCount() {
    	      return m_macDataVector.size();
    	    }
    	    
    	    public void setValueAt(Object value, int row, int col) {
    	      BottomTableData macData = (BottomTableData) (m_macDataVector.elementAt(row));
    	      switch (col) {
    	      case 0:
    	        macData.setHeatMap((BufferedImage) value);
    	        fireTableDataChanged();
    	        break;
    	      case 1:
    	        macData.setIonCount((BufferedImage) value);
    	        fireTableDataChanged();
    	        break;
    	      }
    	    }

    	    public String getColumnName(int col) {
    	      return m_colNames[col];
    	    }

    	    public Class getColumnClass(int col) {
    	      return m_colTypes[col];
    	    }

    	    public Object getValueAt(int row, int col) {
    	    	BottomTableData macData = (BottomTableData) (m_macDataVector.elementAt(row));
       	        switch (col) {
    	        case 0:
    	            return macData.getHeatMap();
    	        case 1:
    	            return macData.getIonCount();
    	      }
       	        return null; //empty object - unreachable???
    	    }
    }
    
    private void createAbstractBottomTable(int topTableRowNum)
    {
    	BufferedImage naIcon = null;
       
        File naFile = new File("images\\na.jpg");
        try {
			naIcon = ImageIO.read(naFile);
	    } catch (IOException e) {
 			e.printStackTrace();
 		}
        
        //Setups values and column names.
        Vector<BottomTableData> rowData = new Vector<BottomTableData>(1); //only one row of data
        BufferedImage heatMap;
        BufferedImage ionCount;
        ReportUnit rUnit = reportArrayList.get(topTableRowNum);
        if (rUnit.getHeatmap() != null) {
        	heatMap = rUnit.getHeatmap();
        } else heatMap = naIcon;

        if (rUnit.getIoncount() != null) {
        	ionCount = rUnit.getIoncount();
        } else ionCount = naIcon;
        rowData.addElement(new BottomTableData(heatMap, ionCount));
        BottomTableModel bottomTableModel = new BottomTableModel(rowData); 
        bottomTable = new JTable(bottomTableModel);
        bottomTable.setDefaultRenderer(BufferedImage.class, new ImageRenderer());
        bottomTable.setName("Selected Images Table");
        //Get dimensions of the images
        int rowHeight = (heatMap != null) ? heatMap.getHeight() : 0;
        if ((ionCount != null) && (rowHeight < ionCount.getHeight())) rowHeight = ionCount.getHeight();
        bottomTable.setRowHeight(rowHeight);
        int columnWidthHeatMap = (heatMap != null) ? heatMap.getWidth() : 0;
        bottomTable.getColumnModel().getColumn(0).setPreferredWidth(columnWidthHeatMap); 
        int columnWidthIonCount = (ionCount != null) ? ionCount.getWidth() : 0;
        bottomTable.getColumnModel().getColumn(1).setPreferredWidth(columnWidthIonCount); 
        logger.log(Level.ALL, "Bottom table Row Height = " + rowHeight + " heatMap Width = " + columnWidthHeatMap
        		+ " ionCount Width = " + columnWidthIonCount);
        bottomTable.setShowGrid(true);
        bottomTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF); 
    }
    
    private void updateBottomTable(int topTableRowNum)
    {
       	BufferedImage naIcon = null;
        
        File naFile = new File("images\\na.jpg");
        try {
			naIcon = ImageIO.read(naFile);
	    } catch (IOException e) {
 			e.printStackTrace();
 		}
        
        BufferedImage heatMap;
        BufferedImage ionCount;
        ReportUnit rUnit = reportArrayList.get(topTableRowNum);
        if (rUnit.getHeatmap() != null) {
        	heatMap = rUnit.getHeatmap();
        } else heatMap = naIcon;

        if (rUnit.getIoncount() != null) {
        	ionCount = rUnit.getIoncount();
        } else ionCount = naIcon;
        bottomTable.setValueAt(heatMap, 0, 0);
        bottomTable.setValueAt(ionCount, 0, 1);
        bottomTable.validate();
    }
    
	private Properties loadProperties() {
		Properties defaultProps = new Properties();
		defaultProps.setProperty("RootFolder", "QCReports\\ctmm");
		defaultProps.setProperty("TopColumnNames", "No., File Size, MS1 Spectra, MS2 Spectra, Measured, Runtime, heatmap, ioncount");
		defaultProps.setProperty("BottomColumnNames", "heatmap, ioncount");
		try {
			FileInputStream in = new FileInputStream("appProperties");
			defaultProps.load(in);
			in.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return defaultProps; 
	}
    
	class ImageRenderer extends DefaultTableCellRenderer  
	{  
	    public Component getTableCellRendererComponent(JTable table,  
	                                                   Object value,  
	                                                   boolean isSelected,  
	                                                   boolean hasFocus,  
	                                                   int row, int column)  
	    {  
	        super.getTableCellRendererComponent(table, value, isSelected,  
	                                            hasFocus, row, column); 
	        if (isSelected && table.getName().equals("QC Report Table")) { //the row is clicked - isSelected = true, hasFocus = false
	        	logger.log(Level.ALL, "TableName = " + table.getName() + " rowNum = " + row);
	        	updateBottomTable(row);
	        }
	        if (value != null) {
	        	ImageIcon valueIcon = new ImageIcon((BufferedImage)value);
	        	setIcon(valueIcon);
	        }
	        else {
	        	 File naFile = new File("images\\na.jpg");
	             try {
	     			BufferedImage naIcon = ImageIO.read(naFile);
	     			ImageIcon naaIcon = new ImageIcon(naIcon);
	     			//logger.log(Level.ALL, "TableName = " + table.getName() + "row = " + row + " column = " + column + " Value = null"  
	     			//		+ " isSelected = " + isSelected + " hasFocus = " + hasFocus);
	     			setIcon(naaIcon);
	     		} catch (IOException e) {
	     			// TODO Auto-generated catch block
	     			e.printStackTrace();
	     		}
	        }
	        setHorizontalAlignment(JLabel.CENTER);  
	        setText("");  
	        // replace default font
	        setFont(new Font("Helvetica Bold", Font.ITALIC, 22));
	        if (table.getName().equals("QC Report Table")) {
	        	if (row % 2 == 0) {
	        		//setForeground(Color.black);          
		            //table.setSelectionBackground(Color.red); 
	        	}  else  {      
	        		//table.setSelectionBackground(Color.cyan);      
	        		//setForeground(Color.black);      
	        	}  
	        }
	        return this;
	    }  
	}  

    public static void main(String[] args)
    {
    	TableTest thisTableTest = new TableTest();
		Properties defaultProps = thisTableTest.loadProperties();
		String rootFolder = defaultProps.getProperty("RootFolder");
		String topColumns = defaultProps.getProperty("TopColumnNames");
		String bottomColumns = defaultProps.getProperty("BottomColumnNames");
		thisTableTest.displayQCReport(rootFolder, topColumns, bottomColumns);
    }
}
