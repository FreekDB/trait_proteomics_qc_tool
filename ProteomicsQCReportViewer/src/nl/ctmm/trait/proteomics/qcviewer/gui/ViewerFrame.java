package nl.ctmm.trait.proteomics.qcviewer.gui;

/**
 * InternalFrames: http://docs.oracle.com/javase/tutorial/uiswing/components/internalframe.html
 * Radio buttons: http://www.leepoint.net/notes-java/GUI/components/50radio_buttons/25radiobuttons.html
 * 
 * Swing layout: http://www.cs101.org/courses/fall05/resources/swinglayout/
 */

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDesktopPane;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.WindowConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import nl.ctmm.trait.proteomics.qcviewer.Main;
import nl.ctmm.trait.proteomics.qcviewer.input.DataEntryForm;
import nl.ctmm.trait.proteomics.qcviewer.input.MetricsParser;
import nl.ctmm.trait.proteomics.qcviewer.input.ReportUnit;
import nl.ctmm.trait.proteomics.qcviewer.utils.Constants;
import nl.ctmm.trait.proteomics.qcviewer.utils.OpenBrowser;

import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartMouseListener;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.Range;
import org.jfree.ui.RefineryUtilities;

/**
 * ViewerFrame with the GUI for the QC Report Viewer V2.
 *
 * @author <a href="mailto:pravin.pawar@nbic.nl">Pravin Pawar</a>
 * @author <a href="mailto:freek.de.bruijn@nbic.nl">Freek de Bruijn</a>
 */

public class ViewerFrame extends JFrame implements ActionListener, ItemListener, ChangeListener, MouseListener {
	private static final long serialVersionUID = 1L;
	private JDesktopPane desktopPane = new ScrollDesktop();
	private JDesktopPane ticGraphPane = new ScrollDesktop();
	List<ChartPanel> chartPanelList = new ArrayList<ChartPanel>(); //necessary for zooming
	private List<Boolean> chartCheckBoxFlags = new ArrayList<Boolean>();
	private static int CHART_HEIGHT = 150; 
	private static int DESKTOP_PANE_WIDTH = 1270; 
	private JTextField minText, maxText;
	private List<ReportUnit> reportUnits = new ArrayList<ReportUnit>(); //preserve original report units 
	private List<ReportUnit> orderedReportUnits = new ArrayList<ReportUnit>(); //use this list for display and other operations
	private List<String> qcParamNames; 
	private List<String> qcParamKeys;
	private List<String> qcParamValues;
	private HashMap<String, String> selectedMetrics; //metrics and Params are interchangeable
	private List<JRadioButton> sortButtons;
    private static final List<Color> LABEL_COLORS = Arrays.asList(
            Color.BLUE, Color.DARK_GRAY, Color.GRAY, Color.MAGENTA, Color.ORANGE, Color.RED, Color.BLACK);
	private static final int CHECK_PANEL_SIZE = 90;
	private static final int LABEL_PANEL_SIZE = 350;
	private static final int CHART_PANEL_SIZE = 800;
	private String currentSortCriteria = "";
	private String newSortCriteria = "";
	private Properties appProperties = null;
	private MetricsParser mParser = null;
	private String pipelineStatus = "";
	private JSplitPane splitPane1 = new JSplitPane(JSplitPane.VERTICAL_SPLIT); 
	private JSplitPane splitPane2 = new JSplitPane(JSplitPane.VERTICAL_SPLIT); 
	private JLabel statusLabel;
	private JPanel statusPanel;
	private int yCoordinate = 0;

    /**
     * Creates a new instance of the demo.
     * 
     * @param title  the title.
     * @param pipelineStatus 
     */
    public ViewerFrame(final MetricsParser mParser, final Properties appProperties, final String title, final List<ReportUnit> reportUnits, final List<String> qcParamNames, String pipelineStatus) {
        super(title);
    	System.out.println("ViewerFrame constructor");
        setPreferredSize(new Dimension(DESKTOP_PANE_WIDTH + 25, CHART_HEIGHT * 10));
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        this.mParser = mParser; 
        this.pipelineStatus = pipelineStatus; 
        this.qcParamNames = qcParamNames;
        qcParamKeys = new ArrayList<String>();
        qcParamValues = new ArrayList<String>();
        selectedMetrics = new HashMap<String, String>();
        //Extract qcParamKeys
        for (int i = 0; i < qcParamNames.size(); ++i) {
        	StringTokenizer stkz = new StringTokenizer(qcParamNames.get(i), ":");
        	String key = stkz.nextToken() + ":" + stkz.nextToken();
        	qcParamKeys.add(key);
        	String value = stkz.nextToken();
        	qcParamValues.add(value); 
        	selectedMetrics.put(key, value);
        }
        this.appProperties = appProperties;
        setReportUnits(reportUnits);
        setOrderedReportUnits(reportUnits);
        assembleComponents();
        setVisible(true);
        // Finally refresh the frame.
        revalidate();
    }
    
    /*
     * New report units are available
     */
    public void updateReportUnits(List<ReportUnit> newReportUnits, final String newPipelineStatus) {
    	System.out.println("In updateReportUnits yCoordinate = " + yCoordinate);
    	int numReportUnits = orderedReportUnits.size();
    	if (newReportUnits.size() > 0) {
    		for (int i = 0; i < newReportUnits.size(); ++i) {
        		ReportUnit thisUnit = newReportUnits.get(i);
        		reportUnits.add(thisUnit);
        		orderedReportUnits.add(thisUnit);
        		chartCheckBoxFlags.add(false);
        		//update desktopFrame
        	    JInternalFrame chartFrame = createChartFrame(i + numReportUnits, thisUnit.getChartUnit().getTicChart(), thisUnit);
        		chartFrame.setBorder(BorderFactory.createRaisedBevelBorder());
        		chartFrame.pack();
        		chartFrame.setLocation(0, yCoordinate);
        		desktopPane.add(chartFrame);
        		chartFrame.setVisible(true);
        		System.out.println("yCoordinate = " + yCoordinate);
        		yCoordinate +=  CHART_HEIGHT + 15;
        	}
    	    int totalReports = orderedReportUnits.size();
    	    desktopPane.setPreferredSize(new Dimension(DESKTOP_PANE_WIDTH, totalReports * (CHART_HEIGHT + 15)));
    	}
		updatePipelineStatus(newPipelineStatus);
		revalidate();
	}
    
    /**
     * Update pipelineStatus in the report viewer
     */
    public void updatePipelineStatus(final String newPipelineStatus) {
    	System.out.println("ViewerFrame updatePipelineStatus");
    	this.pipelineStatus = newPipelineStatus; 
    	statusPanel.removeAll();
        int style = Font.BOLD;
	    Font font = new Font ("Garamond", style , 11);
	    String status = pipelineStatus + " | | | | | Number of report units = " + orderedReportUnits.size();
    	statusLabel = new JLabel (status);
    	statusLabel.setFont(font);
    	statusLabel.setBackground(Color.CYAN);
    	statusPanel.setBackground(Color.CYAN);
    	statusPanel.add(statusLabel);
	    revalidate();
    }
    
    /**
     * Assemble components of the ViewerFrame
     */
    private void assembleComponents() { 
    	System.out.println("ViewerFrame assembleComponents");
        //We need two split panes to create 3 regions in the main frame

        //Add static (immovable) Control frame
    	JInternalFrame controlFrame = getControlFrame();
	    //Add desktopPane for displaying graphs and other QC Control
	    int totalReports = orderedReportUnits.size();
	    desktopPane.setPreferredSize(new Dimension(DESKTOP_PANE_WIDTH, totalReports * (CHART_HEIGHT + 15)));
	    prepareChartsInAscendingOrder(true);
	    splitPane2.add(new JScrollPane(desktopPane), 0);
	    ticGraphPane.setPreferredSize(new Dimension(DESKTOP_PANE_WIDTH, 2 * CHART_HEIGHT));
	    splitPane2.add(new JScrollPane(ticGraphPane), 1);
	    //Set initial tic Graph - specify complete chart in terms of orderedReportUnits
	    setTicGraphPaneChart(orderedReportUnits.get(0).getReportNum() - 1);
	    splitPane2.setOneTouchExpandable(true); //hide-show feature
	    splitPane2.setDividerLocation(500); //DesktopPane holding graphs will appear 500 pixels large
        splitPane1.add(controlFrame);
	    splitPane1.add(splitPane2);
	    splitPane1.setOneTouchExpandable(true); //hide-show feature
	    splitPane1.setDividerLocation(170); //control panel will appear 170 pixels large
	    splitPane1.setPreferredSize(new Dimension(DESKTOP_PANE_WIDTH + 15, (int)(6.5 * CHART_HEIGHT)));
	    getContentPane().add(splitPane1, "Center");
	    setJMenuBar(createMenuBar());
    }
    
    /**
     * Create Menu Bar for settings and about tab
     */
    private JMenuBar createMenuBar() {
    	JMenuBar menuBar = new JMenuBar();
    	JMenu settingsMenu = new JMenu("Settings");
    	menuBar.add(settingsMenu);
    	JMenuItem newDirAction = new JMenuItem("Set Root Directory...");
    	settingsMenu.add(newDirAction);
    	newDirAction.setActionCommand("ChangeRootDirectory");
    	newDirAction.addActionListener(this);
    	JMenuItem newWebserAction = new JMenuItem("Set Webserver...");
    	settingsMenu.add(newWebserAction);
    	newWebserAction.setActionCommand("ChangeServer");
    	newWebserAction.addActionListener(this);
    	JMenuItem filterAction = new JMenuItem("Set Filter...");
    	settingsMenu.add(filterAction);
    	filterAction.setActionCommand("SetFilter");
    	filterAction.addActionListener(this);
    	JMenuItem metricsAction = new JMenuItem("Select Metrics...");
    	settingsMenu.add(metricsAction);
    	metricsAction.setActionCommand("SelectMetrics");
    	metricsAction.addActionListener(this);
    	JMenuItem aboutAction = new JMenuItem("About...");
    	settingsMenu.add(aboutAction);
    	aboutAction.setActionCommand("About");
    	aboutAction.addActionListener(this);    	
    	JMenuItem newRefAction = new JMenuItem("Refresh");
    	settingsMenu.add(newRefAction);
    	newRefAction.setActionCommand("Refresh");
    	newRefAction.addActionListener(this);
    	return menuBar;
    }
    
    /**
     * Sets the report units to be displayed.
     *
     * @param reportUnits the report units to be displayed.
     */
    private void setReportUnits(final List<ReportUnit> reportUnits) {
    	System.out.println("ViewerFrame setReportUnits No. of reportUnits = " + reportUnits.size());
    	if (this.reportUnits != null) {
    		this.reportUnits.clear();
    	}
    	this.reportUnits = reportUnits;
        //Initialize chartCheckBoxFlags to false
        for (int i = 0; i < reportUnits.size(); ++i) {
        	chartCheckBoxFlags.add(false);
        }
    }
    
    /**
     * Sets the report units to be displayed.
     *
     * @param reportUnits the report units to be displayed.
     */
    private void setOrderedReportUnits(final List<ReportUnit> reportUnits) {
    	System.out.println("ViewerFrame setOrderedReportUnits No. of reportUnits = " + reportUnits.size());
    	if (orderedReportUnits != null) {
    		orderedReportUnits.clear();
    	}
    	for (int i = 0; i < reportUnits.size(); ++i) { 
    		orderedReportUnits.add(reportUnits.get(i));
    	}
    	System.out.println("ViewerFrame setOrderedReportUnits No. of ordered reportUnits = " + orderedReportUnits.size());
    }
    
    private JInternalFrame getControlFrame() {
    	System.out.println("ViewerFrame getControlFrame");
    	JInternalFrame controlFrame = new JInternalFrame("Control Panel", true);
        javax.swing.plaf.InternalFrameUI ifu= controlFrame.getUI();
        ((javax.swing.plaf.basic.BasicInternalFrameUI)ifu).setNorthPane(null);
        controlFrame.setBorder(null);
        controlFrame.setLayout(new BorderLayout(0, 0));
        controlFrame.setBackground(Color.WHITE);
        GridLayout layout = new GridLayout(2,1);
        JPanel zoomPanel = new JPanel();
        zoomPanel.setLayout(layout);
        zoomPanel.setPreferredSize(new Dimension(230, 130));
        zoomPanel.setBackground(Color.WHITE);
        // Zoom all - in, original, out
        JRadioButton inButton = new JRadioButton("In", false);
        inButton.setActionCommand("Zoom In");
        inButton.setBackground(Color.WHITE);
        inButton.addActionListener(this);
        JRadioButton originalButton = new JRadioButton("Original", true);
        originalButton.setActionCommand("Zoom Original");
        originalButton.setBackground(Color.WHITE);
        originalButton.addActionListener(this);
        JRadioButton outButton = new JRadioButton("Out", false);
        outButton.setActionCommand("Zoom Out");
        outButton.setBackground(Color.WHITE);
        outButton.addActionListener(this);
        ButtonGroup zoomGroup = new ButtonGroup();
        zoomGroup.add(inButton);
        zoomGroup.add(originalButton);
        zoomGroup.add(outButton);
        layout = new GridLayout(1,3);
        JPanel zoomPanelRadio = new JPanel();
        zoomPanelRadio.setPreferredSize(new Dimension(230, 40));
        zoomPanelRadio.setBackground(Color.WHITE); 
        zoomPanelRadio.setLayout(layout);
        zoomPanelRadio.add(inButton);
        zoomPanelRadio.add(originalButton);
        zoomPanelRadio.add(outButton);
        // Zoom all - Min, Max and Submit
        layout = new GridLayout(1,5);
        JPanel zoomPanelForm = new JPanel(); 
        JLabel minLabel = new JLabel("Min: ");
        minText = new JFormattedTextField(NumberFormat.getInstance());
        minText.setPreferredSize(new Dimension(20, 20));
        minText.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
            	ZoomMinMax();
            }
        });
        JLabel maxLabel = new JLabel("Max: ");
        maxText = new JFormattedTextField(NumberFormat.getInstance());
        maxText.setPreferredSize(new Dimension(20, 20));
        maxText.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
            	ZoomMinMax();
            }
        });
        JButton zoomButton = new JButton("Zoom");
        zoomButton.setActionCommand("ZoomMinMax");
        zoomButton.addActionListener(this);
        zoomPanelForm.add(minLabel);
        zoomPanelForm.add(minText);
        zoomPanelForm.add(maxLabel);
        zoomPanelForm.add(maxText); 
        zoomPanelForm.add(zoomButton); 
        zoomPanelForm.setPreferredSize(new Dimension(230, 80));
        zoomPanelForm.setBackground(Color.WHITE); 
        zoomPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "Zoom All"));
        zoomPanel.add(zoomPanelRadio, 0);
        zoomPanel.add(zoomPanelForm, 1);
        ButtonGroup sortGroup = new ButtonGroup();
        layout = new GridLayout(qcParamValues.size()/2+1,2);
        sortButtons = new ArrayList<JRadioButton>();
        JPanel sortPanel = new JPanel();
        sortPanel.setLayout(layout);
        sortPanel.setPreferredSize(new Dimension(700, 130));
        sortPanel.setBackground(Color.WHITE); 
        int style = Font.BOLD;
	    Font font = new Font ("Garamond", style , 11);
        for (int i = 0; i < qcParamValues.size(); ++i) {
        	JLabel thisLabel = new JLabel(qcParamValues.get(i) + ": ");
        	thisLabel.setFont(font);
        	thisLabel.setBackground(Color.WHITE);
        	JPanel namePanel = new JPanel(new GridLayout(1,1));
        	namePanel.add(thisLabel);
        	namePanel.setBackground(Color.WHITE);
        	//Sort ascending button
        	JRadioButton ascButton = new JRadioButton("Asc", false);
        	ascButton.setBackground(Color.WHITE);
        	ascButton.setActionCommand("Sort@" + qcParamKeys.get(i) + "@Asc");
        	ascButton.addActionListener(this);
        	sortGroup.add(ascButton);
        	sortButtons.add(ascButton);
        	//Sort descending button
        	JRadioButton desButton = new JRadioButton("Des", false);
        	desButton.setBackground(Color.WHITE);
        	desButton.setActionCommand("Sort@" + qcParamKeys.get(i) + "@Des");
        	desButton.addActionListener(this);
        	sortGroup.add(desButton); 
        	sortButtons.add(desButton);
        	JPanel buttonPanel = new JPanel(new GridLayout(1,2));
        	buttonPanel.add(ascButton);
        	buttonPanel.add(desButton);
        	JPanel metricPanel = new JPanel(new GridLayout(1,2));
        	metricPanel.add(namePanel, 0);
        	metricPanel.add(buttonPanel, 1);
        	sortPanel.add(metricPanel);
        }
        //Add sorting according to Compare 
    	JLabel thisLabel = new JLabel("Compare: ");
    	thisLabel.setFont(font);
    	thisLabel.setBackground(Color.WHITE);
    	JPanel namePanel = new JPanel(new GridLayout(1,1));
    	namePanel.setBackground(Color.WHITE);
    	namePanel.add(thisLabel);
    	//Sort ascending button
    	JRadioButton ascButton = new JRadioButton("Asc", false);
    	ascButton.setBackground(Color.WHITE);
    	ascButton.setActionCommand("Sort@" + "Compare" + "@Asc");
    	ascButton.addActionListener(this);
    	sortGroup.add(ascButton);
    	sortButtons.add(ascButton);
    	//Sort descending button
    	JRadioButton desButton = new JRadioButton("Des", false);
    	desButton.setBackground(Color.WHITE);
    	desButton.setActionCommand("Sort@" + "Compare" + "@Des");
    	desButton.addActionListener(this);
    	sortGroup.add(desButton); 
    	sortButtons.add(desButton); 
    	JPanel buttonPanel = new JPanel(new GridLayout(1,2));
    	buttonPanel.add(ascButton);
    	buttonPanel.add(desButton);
    	JPanel metricPanel = new JPanel(new GridLayout(1,2));
    	metricPanel.add(namePanel, 0);
    	metricPanel.add(buttonPanel, 1);
    	sortPanel.add(metricPanel);
    	
        //Set first button selected
        sortButtons.get(0).setSelected(true);
        this.currentSortCriteria = sortButtons.get(0).getActionCommand(); 
        sortPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "Sort Options"));
        //Add opllogo to control frame
        BufferedImage oplLogo = null;
		try {
			oplLogo = ImageIO.read(new File(Constants.PROPERTY_OPL_LOGO_FILE));
		} catch (IOException e) {
			e.printStackTrace();
		}
        JLabel oplLabel = new JLabel(new ImageIcon(oplLogo));
        JPanel oplPanel = new JPanel();
        oplPanel.add(oplLabel);
        
        //Add traitlogo to control frame
        BufferedImage traitctmmLogo = null;
		try {
			traitctmmLogo = ImageIO.read(new File(Constants.PROPERTY_PROJECT_LOGO_FILE));
		} catch (IOException e) {
			e.printStackTrace();
		}
        JLabel traitctmmLabel = new JLabel(new ImageIcon(traitctmmLogo));
        JPanel traitctmmPanel = new JPanel();
        traitctmmPanel.add(traitctmmLabel);
        JPanel controlPanel = new JPanel();
        //controlPanel.setBackground(Color.WHITE);
        //oplPanel.setBackground(Color.WHITE);
        //zoomPanel.setBackground(Color.WHITE);
        //sortPanel.setBackground(Color.WHITE);
        //traitctmmPanel.setBackground(Color.WHITE);
        controlPanel.add(oplPanel, 0);
        controlPanel.add(zoomPanel, 1);
        controlPanel.add(sortPanel, 2);
        controlPanel.add(traitctmmPanel, 3);
        controlFrame.getContentPane().add(controlPanel, BorderLayout.NORTH);
        String status = pipelineStatus + " | | | | | Number of report units = " + orderedReportUnits.size(); 
        statusLabel = new JLabel(status);
        statusLabel.setFont(font);
        statusLabel.setBackground(Color.CYAN);
        statusPanel = new JPanel();
        statusPanel.setBackground(Color.CYAN); 
        statusPanel.add(statusLabel);
        controlFrame.getContentPane().add(statusPanel, BorderLayout.SOUTH);
        controlFrame.setSize(new Dimension(DESKTOP_PANE_WIDTH + 30, 170));
        controlFrame.pack();
        controlFrame.setLocation(0, 0);
        //controlFrame.setBackground(Color.WHITE);
        //controlFrame.setForeground(Color.WHITE);
        //TO avoid resizing and repositioning of components in the controlFrame
        controlPanel.addComponentListener(new ComponentListener() {  
            public void componentResized(ComponentEvent e) {  
            	//JPanel controlPanel = (JPanel)e.getSource();  
            	//controlPanel.setSize(new Dimension(DESKTOP_PANE_WIDTH + 30, 170));
            }

			@Override
			public void componentHidden(ComponentEvent arg0) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void componentMoved(ComponentEvent arg0) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void componentShown(ComponentEvent arg0) {
				// TODO Auto-generated method stub
				
			}  
        });  
        
        
        
        controlFrame.setVisible(true);
        return controlFrame;
    }
    
    private void setTicGraphPaneChart(int reportNum) {
    	System.out.println("ViewerFrame setTicGraphPaneChart " + reportNum);
	    if (ticGraphPane != null) {
	    	ticGraphPane.removeAll();
	    }
        int yCoordinate = 0;
        //Create the visible chart panel
        final ChartPanel chartPanel = new ChartPanel(reportUnits.get(reportNum).getChartUnit().getTicChart());
        chartPanel.setPreferredSize(new Dimension(DESKTOP_PANE_WIDTH, 2 * CHART_HEIGHT));
        final JInternalFrame chartFrame = new JInternalFrame("Chart " + reportNum, true);
        javax.swing.plaf.InternalFrameUI ifu= chartFrame.getUI();
        ((javax.swing.plaf.basic.BasicInternalFrameUI)ifu).setNorthPane(null);
        chartFrame.getContentPane().add(chartPanel);
        chartFrame.setPreferredSize(new Dimension(DESKTOP_PANE_WIDTH, 2 * CHART_HEIGHT));
        chartFrame.setBorder(null);
       	chartFrame.pack();
        chartFrame.setLocation(0, yCoordinate);
        chartFrame.setVisible(true);
        ticGraphPane.add(chartFrame);
        // Finally refresh the frame.
        revalidate();
    }

	@Override
	public void stateChanged(ChangeEvent e) {
	}
	
	
	private void ZoomMinMax() {
		String minValue = minText.getText();
		String maxValue = maxText.getText();
		int min = 0, max = 99; 
		try {
			min = Integer.parseInt(minValue); 
			max = Integer.parseInt(maxValue); 
		} catch (NumberFormatException e) {
			JOptionPane.showMessageDialog(this,"Incorrect min or max. Resetting to 10 and 80",
					  "Error",JOptionPane.ERROR_MESSAGE);
			minText.setText("10");
			maxText.setText("80");
			min = 10; 
			max = 80; 
		}
		if (min < 0 || max > 99 || min > 99 || max < 1 || min > max) {
			JOptionPane.showMessageDialog(this,"Incorrect min or max. Resetting to 10 and 80",
					  "Error",JOptionPane.ERROR_MESSAGE);
			minText.setText("10");
			maxText.setText("80");
			min = 10; 
			max = 80; 
		}
		System.out.println("minValue = " + minValue + " maxValue = " + maxValue + " min = " + min + " max = " + max);
		Iterator<ChartPanel> it = chartPanelList.iterator();
		System.out.println("Number of chart panels = " + chartPanelList.size());
		while(it.hasNext()) {
			ChartPanel cPanel = (ChartPanel) it.next();
			JFreeChart chart = cPanel.getChart(); 
			XYPlot plot = (XYPlot) chart.getPlot();
			plot.getDomainAxis().setRange(min, max);
			cPanel.setRefreshBuffer(true);
			cPanel.repaint();
		}
	}
	
	@Override
	public void actionPerformed(ActionEvent evt) {
		System.out.println("Corresponding action command is " + evt.getActionCommand() 
				+ " evt class = " + evt.getClass());
		//Check whether Details button is pressed - in order to open corresponding hyperlink 

		if (evt.getActionCommand().startsWith("Details")) {
			//Parse actioncommand to get reportUnit number
			StringTokenizer stkz = new StringTokenizer(evt.getActionCommand(), "-");
			stkz.nextToken();
			int reportNum = Integer.parseInt(stkz.nextToken());
			System.out.println("Details requested for reportNum " + reportNum);
			ReportUnit rUnit = reportUnits.get(reportNum - 1); //-1 to adjust index
			DetailsFrame detailsFrame = new DetailsFrame(mParser.getMetricsListing(), rUnit);
			detailsFrame.setVisible(true);
			detailsFrame.revalidate();
		}
		//Check whether zoom to particular range is pressed 
		else if (evt.getActionCommand().equals("ZoomMinMax")) { 
			ZoomMinMax();
		} //Check whether zoom in - all is selected
		else if (evt.getActionCommand().equals("Zoom In")) {
			Iterator<ChartPanel> it = chartPanelList.iterator();
			System.out.println("Number of chart panels = " + chartPanelList.size());
			while(it.hasNext()) {
				ChartPanel cPanel = (ChartPanel) it.next();
				cPanel.zoomInDomain(0, 0);
				cPanel.setRefreshBuffer(true);
				cPanel.repaint();
			}
		} //Check whether zoom Original - all is selected 
		else if (evt.getActionCommand().equals("Zoom Original")) {
			Iterator<ChartPanel> it = chartPanelList.iterator();
			System.out.println("Number of chart panels = " + chartPanelList.size());
			while(it.hasNext()) {
				ChartPanel cPanel = (ChartPanel) it.next();
				cPanel.restoreAutoBounds();
				cPanel.setRefreshBuffer(true);
				cPanel.repaint();
			}
		} //Check whether zoom out - all is selected 
		else if (evt.getActionCommand().equals("Zoom Out")) {
			Iterator<ChartPanel> it = chartPanelList.iterator();
			System.out.println("Number of chart panels = " + chartPanelList.size());
			while(it.hasNext()) {
				ChartPanel cPanel = (ChartPanel) it.next();
				cPanel.zoomOutDomain(0, 0);
				cPanel.setRefreshBuffer(true);
				cPanel.repaint();
			}
		} else if (evt.getActionCommand().startsWith("Sort")) {
			newSortCriteria = evt.getActionCommand();
			//if (! newSortCriteria.equals(currentSortCriteria)) {
				sortChartFrameList();
			//} else System.out.println("Already sorted according to " + newSortCriteria);
		} else if (evt.getActionCommand().equals("ChangeRootDirectory")) {
			//Get new location to read reports from
        	DataEntryForm deForm = new DataEntryForm(this, appProperties);
        	deForm.displayRootDirectoryChooser();
		} else if (evt.getActionCommand().equals("ChangeServer")) {
			//Get new server location to read web-based report
        	DataEntryForm deForm = new DataEntryForm(this, appProperties);
        	deForm.displayPreferredServerEntryForm();
		} else if (evt.getActionCommand().equals("SetFilter")) {
			//Get new location to read reports from
        	DataEntryForm deForm = new DataEntryForm(this, appProperties);
        	deForm.displayDateFilterEntryForm();
		} else if (evt.getActionCommand().equals("SelectMetrics")) {
			//Display ChooseMetricsForm to select metrics to display
        	ChooseMetricsForm cmForm = new ChooseMetricsForm(this, mParser, selectedMetrics);
        	cmForm.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        	cmForm.pack();
        	RefineryUtilities.centerFrameOnScreen(cmForm);
        	cmForm.setVisible(true);
		} else if (evt.getActionCommand().equals("Refresh")) {
			clean();
			dispose();
			new Main().runReportViewer();
		} else if (evt.getActionCommand().equals("About")) {
			System.out.println("Pressed About button..");
			//JOptionPane.showMessageDialog(this,"Soon you will see more information about this software.",
			//		  "Status",JOptionPane.INFORMATION_MESSAGE);
			AboutFrame aboutFrame = new AboutFrame();
			aboutFrame.setVisible(true);
			aboutFrame.revalidate();
		}
	}
	
	public void clean() {
		if (desktopPane != null) {
			desktopPane.removeAll();
			desktopPane = null;
		}
		if (ticGraphPane != null) {
			ticGraphPane.removeAll();
			ticGraphPane = null;
		}
		if (chartPanelList != null) {
			chartPanelList.clear();
			chartPanelList = null;
		}
		if (chartCheckBoxFlags != null) {
			chartCheckBoxFlags.clear();
			chartCheckBoxFlags = null;
		}
		if (reportUnits != null) {
			reportUnits.clear();
			reportUnits = null;
		}
		if (orderedReportUnits != null) {
			orderedReportUnits.clear();
			orderedReportUnits = null;
		}
		if (sortButtons != null) {
			sortButtons.clear();
			sortButtons = null;
		}
	}
	
    private void sortChartFrameList() {
		System.out.println("sortChartFrameList From " + currentSortCriteria + " To " + newSortCriteria);
		StringTokenizer stkz = new StringTokenizer(newSortCriteria, "@");
		stkz.nextToken();
		String sortKey = stkz.nextToken(); //e.g. generic:date
		String sortOrder = stkz.nextToken(); //e.g. Asc or Des
		System.out.println("Sort requested according to " + sortKey + " order " + sortOrder);
		//Remove currently ordered report units and recreate them according to sort criteria
		if (orderedReportUnits != null) {
			orderedReportUnits.clear();
		}
		orderedReportUnits = new ArrayList<ReportUnit>();
		if (!sortKey.equals("Compare")) { //Except for Compare based sort
			orderedReportUnits.add(reportUnits.get(0)); //add initial element
			//Sort in ascending order
			for (int i = 1; i < reportUnits.size(); ++i) {
				int insertAtIndex = orderedReportUnits.size(); //new element will be inserted at position j or at the end of list
				for (int j = 0; j < orderedReportUnits.size(); ++j) {
					int result = reportUnits.get(i).compareTo(orderedReportUnits.get(j), sortKey); //comparing new and old lists
					System.out.println(" Result = " + result);
					if (result == -1) { //reportUnit(i) is < orderedUnit(j)
						insertAtIndex = j;
						break;
					}
				}
				orderedReportUnits.add(insertAtIndex, reportUnits.get(i)); //Add to specified index
				//System.out.println("i = " + i + " insertAtIndex = " + insertAtIndex + " new size = " + orderedReportUnits.size());
			}	
		} else if (sortKey.equals("Compare")) { 
			//Check checkboxflag status and group those reports together at the beginning of orderedReportUnits 
			//Add all selected reports first i refers to original report number
			for (int i = 0; i < chartCheckBoxFlags.size(); ++i) {
				if (chartCheckBoxFlags.get(i)) {
					System.out.println("Selected report index = " + i);
					orderedReportUnits.add(reportUnits.get(i));
				}
			}
			//Later add all deselected reports 
			for (int i = 0; i < chartCheckBoxFlags.size(); ++i) {
				if (!chartCheckBoxFlags.get(i)) {
					System.out.println("Deselected report index = " + i);
					orderedReportUnits.add(reportUnits.get(i));
				}
			}
		}
		if (desktopPane != null) {
	    	desktopPane.removeAll(); //A new chart frame will be given to every report
	    }
		if (sortOrder.equals("Asc")) {
			prepareChartsInAscendingOrder(true);
			//Set first report graph in the Tic Pane. -1 adjusts to the index. 
			setTicGraphPaneChart(orderedReportUnits.get(0).getReportNum() - 1); 
		} else if (sortOrder.equals("Des")) {
			prepareChartsInAscendingOrder(false);
			setTicGraphPaneChart(orderedReportUnits.get(orderedReportUnits.size()-1).getReportNum() - 1); ////Set last report graph in the Tic Pane
		}
		currentSortCriteria = newSortCriteria; 
		newSortCriteria = "";
	}
    
    /**
     * 
     * @param flag if true, charts will be prepared in ascending order. if false, the charts will be prepared in descending order
     */
    private void prepareChartsInAscendingOrder(boolean flag) {
		System.out.println("ViewerFrame prepareChartsInAscendingOrder");
		if (chartPanelList != null) {
			chartPanelList.clear();
		}
        yCoordinate = 0;
        System.out.println("No. of orderedReportUnits = " + orderedReportUnits.size());
        for (int i = 0; i < orderedReportUnits.size(); ++i) {
        	JInternalFrame chartFrame;
        	if (flag) {
        		System.out.print("Report URI = " + orderedReportUnits.get(i).getDetailsUri() + " ");
        			chartFrame = createChartFrame(i, orderedReportUnits.get(i).getChartUnit().getTicChart(), orderedReportUnits.get(i));
        	} else {
        		int index = orderedReportUnits.size() - i - 1;
        		System.out.print("Report URI = " + orderedReportUnits.get(index).getDetailsUri() + " ");
        		chartFrame = createChartFrame(i, orderedReportUnits.get(index).getChartUnit().getTicChart(), orderedReportUnits.get(orderedReportUnits.size() - i - 1));
        	}
        	chartFrame.setBorder(BorderFactory.createRaisedBevelBorder());
        	chartFrame.pack();
        	chartFrame.setLocation(0, yCoordinate);
        	chartFrame.setVisible(true);
        	desktopPane.add(chartFrame);
        	System.out.println("yCoordinate = " + yCoordinate);
        	yCoordinate += CHART_HEIGHT + 15;
        }
	}
    
	/**
     * Creates an internal frame.
     * setSelected is required to preserve check boxes status in the display
     * @return An internal frame.
     */
    private JInternalFrame createChartFrame(int chartNum, JFreeChart chart, ReportUnit reportUnit) {
    	System.out.println("ViewerFrame createChartFrame " + chartNum);
        //Create the visible chart panel
        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(CHART_PANEL_SIZE, CHART_HEIGHT - 10));
        chartPanelList.add(chartPanel);
        final JInternalFrame frame = new JInternalFrame("Chart " + chartNum, true);
        frame.setName(Integer.toString(reportUnit.getReportNum() - 1)); //Set report index number as frame name
        javax.swing.plaf.InternalFrameUI ifu= frame.getUI();
        ((javax.swing.plaf.basic.BasicInternalFrameUI)ifu).setNorthPane(null);
        int style = Font.BOLD;
	    Font font = new Font ("Garamond", style , 11);
        //Create a checkbox for selection
        JCheckBox chartCheckBox = new JCheckBox("Compare");
        chartCheckBox.setFont(font);
        chartCheckBox.setBackground(Color.WHITE);
      //ChartCheckBoxName is same as report number which is unique
      //chartCheckBoxFlags are organized according to original report num - which is same as ChartCheckBoxName
        chartCheckBox.setName(Integer.toString(reportUnit.getReportNum() - 1)); //Since reportNum is > 0
        if (chartCheckBoxFlags.get(reportUnit.getReportNum() - 1)) {
        	chartCheckBox.setSelected(true); 
        } else chartCheckBox.setSelected(false);
        chartCheckBox.addItemListener(this);
        JButton detailsButton = new JButton("Details");
        detailsButton.setFont(font);
        detailsButton.setPreferredSize(new Dimension(80, 20));
        //Hyperlink to be replaced with path to browser report
        detailsButton.setActionCommand("Details-" + Integer.toString(reportUnit.getReportNum()));
        detailsButton.addActionListener(this);
        
        JPanel checkPanel = new JPanel();
        checkPanel.setFont(font);
        checkPanel.setBackground(Color.WHITE);
        checkPanel.setForeground(Color.WHITE); 
        //GridLayout layout = new GridLayout(2, 1);
        //checkPanel.setLayout(layout);
        checkPanel.add(detailsButton, 0);
        checkPanel.add(chartCheckBox, 1);
        JLabel numLabel = new JLabel(Integer.toString(reportUnit.getReportNum()));
        Font numFont = new Font ("Garamond", style , 22);
        numLabel.setFont(numFont);
		checkPanel.add(numLabel);
        checkPanel.setPreferredSize(new Dimension(CHECK_PANEL_SIZE, CHART_HEIGHT));
        
        JPanel labelPanel = new JPanel();
        labelPanel.setFont(font);
        labelPanel.setBackground(Color.WHITE);
        GridLayout layout = new GridLayout(qcParamNames.size(),1);
        labelPanel.setLayout(layout);
        labelPanel.setPreferredSize(new Dimension(LABEL_PANEL_SIZE, CHART_HEIGHT));
        // add qcparam labels, one in each cell
        //No., File Size(MB), MS1Spectra, MS2Spectra, Measured, Runtime(hh:mm:ss), maxIntensity
        for (int i = 0; i < qcParamNames.size(); ++i) { 
    		Color fgColor = LABEL_COLORS.get(i%LABEL_COLORS.size());
    		JLabel thisLabel = new JLabel(qcParamValues.get(i) + ": " + (reportUnit.getMetricsValueFromKey(qcParamKeys.get(i))));
    		thisLabel.setFont(font);
    		thisLabel.setForeground(fgColor);
    		labelPanel.add(thisLabel);
        }
        JPanel displayPanel = new JPanel();
        displayPanel.add(checkPanel, 0);
        displayPanel.add(labelPanel, 1);
        displayPanel.add(chartPanel, 2);
        displayPanel.setBorder(null);
        frame.getContentPane().add(displayPanel);
        frame.addMouseListener(this);
        frame.setBorder(null);
        return frame;
    }
 
	@Override
	public void itemStateChanged(ItemEvent evt) {
		//Find out index of selection, checked-unchecked and update CheckBoxList
		if (evt.getSource().getClass().getName().equals("javax.swing.JCheckBox")) {
			JCheckBox thisCheckBox = (JCheckBox) evt.getSource();
			System.out.println("Check box name = " + thisCheckBox.getName());
			int checkBoxFlagIndex = Integer.parseInt(thisCheckBox.getName());
			//chartCheckBoxFlags will be maintained all the time according to reportNum
			if (evt.getStateChange() == ItemEvent.SELECTED) {
				System.out.print("Selected");
				chartCheckBoxFlags.set(checkBoxFlagIndex, true);
			} else if (evt.getStateChange() == ItemEvent.DESELECTED) {
				System.out.print("DeSelected");
				chartCheckBoxFlags.set(checkBoxFlagIndex, false); 
			}
		}
	}
	
	@Override
	public void mouseClicked(MouseEvent arg0) {
		Component clickedComponent = arg0.getComponent(); 
		if (clickedComponent.getClass().getName().equals("javax.swing.JInternalFrame")) {
			JInternalFrame clickedFrame = (JInternalFrame) clickedComponent;
			System.out.println("Frame title = " + clickedFrame.getTitle() + " Frame name = " + clickedFrame.getName());
			setTicGraphPaneChart(Integer.parseInt(clickedFrame.getName()));
		}
	} 

	@Override
	public void mouseEntered(MouseEvent arg0) {
	}

	@Override
	public void mouseExited(MouseEvent arg0) {
	}

	@Override
	public void mousePressed(MouseEvent arg0) {
	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
	}

}

