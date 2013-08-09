package nl.ctmm.trait.proteomics.qcviewer.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
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
import javax.swing.border.Border;

import nl.ctmm.trait.proteomics.qcviewer.input.MetricsParser;
import nl.ctmm.trait.proteomics.qcviewer.input.ReportUnit;
import nl.ctmm.trait.proteomics.qcviewer.utils.Constants;
import nl.ctmm.trait.proteomics.qcviewer.utils.Utilities;

import org.apache.commons.io.FilenameUtils;
import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartMouseListener;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.plot.XYPlot;
import org.jfree.ui.RefineryUtilities;

/**
 * ViewerFrame with the GUI for the QC Report Viewer.
 *
 * Some useful links on Swing:
 * - InternalFrames: http://docs.oracle.com/javase/tutorial/uiswing/components/internalframe.html
 * - Radio buttons: http://www.leepoint.net/notes-java/GUI/components/50radio_buttons/25radiobuttons.html
 * - Swing layout: http://www.cs101.org/courses/fall05/resources/swinglayout/
 *
 * TODO: Clicking on right part of the main table (on a graph) does not select that row.
 *       (And therefore does not show that graph large at the bottom).
 *
 * @author <a href="mailto:pravin.pawar@nbic.nl">Pravin Pawar</a>
 * @author <a href="mailto:freek.de.bruijn@nbic.nl">Freek de Bruijn</a>
 */
public class ViewerFrame extends JFrame implements ActionListener, ItemListener, MouseListener, ChartMouseListener {
    /**
     * The logger for this class.
     */
    private static final Logger logger = Logger.getLogger(ViewerFrame.class.getName());

    /**
     * The version number for (de)serialization of this class (UID: universal identifier).
     */
    private static final long serialVersionUID = 1;

    /*private static final List<Color> LABEL_COLORS = Arrays.asList(
            Color.BLUE, Color.DARK_GRAY, Color.GRAY, Color.MAGENTA, Color.ORANGE, Color.RED, Color.BLACK);*/

    /**
     * Alternating colors used for the metrics.
     */
    private static final List<Color> LABEL_COLORS = Arrays.asList(
            Color.BLUE, Color.DARK_GRAY, Color.BLACK);

    /**
     * Height of a row with information from one report in the list.
     */
    private static final int REPORT_ROW_HEIGHT = 165;

    /**
     * The height of a chart from one report in the list and some extra space.
     *
     * The Checkstyle MagicNumberCheck currently only allows the multiplication operator to be used in constant
     * expressions (& also unary plus and minus). See the ALLOWED_PATH_TOKENTYPES array in the MagicNumberCheck class:
     * http://checkstyle.hg.sourceforge.net/hgweb/checkstyle/checkstyle/file/a485366ec8c3/src/checkstyle/com/puppycrawl/
     *     tools/checkstyle/checks/coding/MagicNumberCheck.java
     *
     * TODO: rename this constant since it's not equal to the actual chart height. [Freek]
     */
    // CHECKSTYLE_OFF: MagicNumberCheck
    private static final int CHART_HEIGHT = REPORT_ROW_HEIGHT - 15;
    // CHECKSTYLE_ON: MagicNumberCheck

    /**
     * Actual height of a chart from one report in the list.
     */
    // CHECKSTYLE_OFF: MagicNumberCheck
    private static final int ACTUAL_CHART_HEIGHT = (int) (CHART_HEIGHT - 10);
    // CHECKSTYLE_ON: MagicNumberCheck

    /**
     * Width of the left panel for each report in the list, with the report number, the metrics details button and the
     * selection check box.
     */
    private static final int CHECK_PANEL_WIDTH = 90;

    /**
     * Width of the panel showing the selected metrics for each report in the list.
     */
    private static final int METRICS_PANEL_WIDTH = 350;

    /**
     * Width of the panel with a TIC chart for each report in the list.
     */
    private static final int CHART_PANEL_WIDTH = 800;

    /**
     * Initial width of the desktop pane.
     */
    private static final int DESKTOP_PANE_WIDTH = 1270;

    /**
     * Default width for the viewer application.
     */
    // CHECKSTYLE_OFF: MagicNumberCheck
    private static final int VIEWER_WIDTH = DESKTOP_PANE_WIDTH + 25;
    // CHECKSTYLE_ON: MagicNumberCheck

    /**
     * Default height for the viewer application.
     */
    private static final int VIEWER_HEIGHT = CHART_HEIGHT * 10;

    /**
     * Default divider location of the top split pane, which separates the top control panel from the rest of the GUI.
     */
    private static final int SPLIT_PANE_1_DIVIDER_LOCATION = 185;

    /**
     * Default width for the top split pane, which separates the top control panel from the rest of the GUI.
     */
    // CHECKSTYLE_OFF: MagicNumberCheck
    private static final int SPLIT_PANE_1_WIDTH = DESKTOP_PANE_WIDTH + 15;
    // CHECKSTYLE_ON: MagicNumberCheck

    /**
     * Default height for the top split pane, which separates the top control panel from the rest of the GUI.
     */
    private static final int SPLIT_PANE_1_HEIGHT = (int) (6.5 * CHART_HEIGHT);

    /**
     * Default divider location of the bottom split pane, which separates the central list with the QC results from the
     * bottom panel with the larger version of the TIC graph that is selected in the central list.
     */
    private static final int SPLIT_PANE_2_DIVIDER_LOCATION = 500;

    /**
     * Width of the panel with sorting controls.
     */
    private static final int SORT_PANEL_WIDTH = 700;

    /**
     * Height of the panel with sorting controls.
     */
    private static final int SORT_PANEL_HEIGHT = 140;
    
    /**
     * Width of the zoom panel controls.
     */
    private static final int ZOOM_PANEL_WIDTH = 280;

    /**
     * Height of the zoom panel controls.
     */
    private static final int ZOOM_PANEL_HEIGHT = 140;

    /**
     * Height of individual zoom panel components - Zoom All and Zoom Along X Axis.
     */
    private static final int ZOOM_PANEL_FORM_HEIGHT = 60;

    /**
     * Width of an individual zoom button.
     */
    private static final int ZOOM_BUTTON_WIDTH = 85;

    /**
     * Height of an individual zoom button.
     */
    private static final int ZOOM_BUTTON_HEIGHT = 25;

    /**
     * Width of min - max text box.
     */
    private static final int TEXT_BOX_WIDTH = 30;

    /**
     * Height of min - max text box.
     */
    private static final int TEXT_BOX_HEIGHT = 20;

    /**
     * Text of the change root directory menu command.
     */
    private static final String CHANGE_ROOT_DIRECTORY_COMMAND = "ChangeRootDirectory";

    /**
     * Text of the set filter menu command.
     */
    private static final String SET_FILTER_COMMAND = "SetFilter";

    /**
     * Text of the select metrics menu command.
     */
    private static final String SELECT_METRICS_COMMAND = "SelectMetrics";

    /**
     * Text of the about menu command.
     */
    private static final String ABOUT_COMMAND = "About";

    /**
     * Text of the Zoom In button.
     */
    private static final String ZOOM_IN_BUTTON_TEXT = "Zoom In";

    /**
     * Text of the Zoom Original button.
     */
    private static final String ZOOM_ORIGINAL_BUTTON_TEXT = "Original";

    /**
     * Text of the Zoom Out button.
     */
    private static final String ZOOM_OUT_BUTTON_TEXT = "Zoom Out";

    /**
     * Text of the Zoom In command.
     */
    private static final String ZOOM_IN_COMMAND = ZOOM_IN_BUTTON_TEXT;

    /**
     * Text of the Zoom Original command.
     */
    private static final String ZOOM_ORIGINAL_COMMAND = "Zoom Original";

    /**
     * Text of the Zoom Out command.
     */
    private static final String ZOOM_OUT_COMMAND = ZOOM_OUT_BUTTON_TEXT;

    /**
     * Text of the Zoom Along X axis button.
     */
    private static final String ZOOM_X_AXIS_BUTTON_TEXT = "Zoom X Axis";

    /**
     * The zoom min max action command.
     */
    private static final String ZOOM_MIN_MAX_COMMAND = "zoomMinMax";

    /**
     * The prefix used for sort action commands.
     */
    private static final String SORT_COMMAND_PREFIX = "Sort";

    /**
     * The separator used for sort action commands.
     */
    private static final String SORT_COMMAND_SEPARATOR = "@";

    /**
     * The label used for ascending sort radio buttons.
     */
    private static final String SORT_ORDER_ASCENDING_LABEL = "Asc";

    /**
     * The suffix used for ascending sort action commands.
     */
    private static final String SORT_ORDER_ASCENDING = SORT_ORDER_ASCENDING_LABEL;

    /**
     * The label used for ascending sort radio buttons.
     */
    private static final String SORT_ORDER_DESCENDING_LABEL = "Des";

    /**
     * The suffix used for descending sort action commands.
     */
    private static final String SORT_ORDER_DESCENDING = SORT_ORDER_DESCENDING_LABEL;

    /**
     * The label used for the compare selected reports sort radio button.
     */
    private static final String SORT_ORDER_COMPARE_LABEL = "Compare";

    /**
     * The suffix used for the compare selected reports command.
     */
    private static final String SORT_ORDER_COMPARE = SORT_ORDER_COMPARE_LABEL;

    /**
     * Width of the OPL and CTMM TraIT logos on the top left and top right of the application.
     */
    private static final int LOGO_WIDTH = 179;

    /**
     * Height of the OPL and CTMM TraIT logos on the top left and top right of the application.
     */
    private static final int LOGO_HEIGHT = 100;

    /**
     * Preferred width of the control frame.
     */
    private static final int CONTROL_FRAME_WIDTH = 1333;

    /**
     * Preferred height of the control frame.
     */
    private static final int CONTROL_FRAME_HEIGHT = 165;

    /**
     * Preferred width of the control panel.
     */
    private static final int CONTROL_PANEL_WIDTH = 1333;

    /**
     * Preferred height of the control panel.
     */
    private static final int CONTROL_PANEL_HEIGHT = 140;

    /**
     * Preferred width of the status panel.
     */
    private static final int STATUS_PANEL_WIDTH = 1333;

    /**
     * Preferred height of the status panel.
     */
    private static final int STATUS_PANEL_HEIGHT = 20;

    /**
     * Width of a metric label.
     */
    private static final int METRIC_LABEL_WIDTH = 200;

    /**
     * Height of a metric label.
     */
    private static final int METRIC_LABEL_HEIGHT = 30;

    /**
     * Separator used in the metrics definition file.
     */
    private static final String METRICS_SEPARATOR = ":";

    /**
     * Prefix used for the action command of the metrics details buttons.
     */
    private static final String DETAILS_ACTION_PREFIX = "Details-";

    /**
     * Dimension object for filler areas of 5x0 pixels for GUI layout.
     */
    private static final Dimension DIMENSION_5X0 = new Dimension(5, 0);

    /**
     * Dimension object for filler areas of 10x0 pixels for GUI layout.
     */
    private static final Dimension DIMENSION_10X0 = new Dimension(10, 0);

    /**
     * Dimension object for filler areas of 0x10 pixels for GUI layout.
     */
    private static final Dimension DIMENSION_0X10 = new Dimension(0, 10);

    /**
     * Default start percentage for zooming along the x axis.
     */
    private static final int ZOOM_X_AXIS_DEFAULT_START = 10;

    /**
     * Default end percentage for zooming along the x axis.
     */
    private static final int ZOOM_X_AXIS_DEFAULT_END = 80;

    /**
     * The prefix used for the titles of the chart frames. The report number is appended to this prefix.
     */
    private static final String CHART_FRAME_TITLE_PREFIX = "Chart ";

    /**
     * The main desktop pane with the TIC graphs of all the reports.
     */
    private final JDesktopPane desktopPane = new ScrollDesktop();

    /**
     * The bottom pane showing the enlarged TIC graph of the selected report.
     */
    private final JDesktopPane ticGraphPane = new ScrollDesktop();

    /**
     * The list with all chart panels to be able to zoom all TIC graphs.
     */
    private final List<ChartPanel> chartPanelList = new ArrayList<>();

    /**
     * Whether a report is selected for comparison.
     */
    private final List<Boolean> reportIsSelected = new ArrayList<>();

    /**
     * The text field for specifying the start percentage for zooming along the x axis.
     */
    private final JFormattedTextField minText = new JFormattedTextField(NumberFormat.getInstance());

    /**
     * The text field for specifying the end percentage for zooming along the x axis.
     */
    private final JFormattedTextField maxText = new JFormattedTextField(NumberFormat.getInstance());

    /**
     * The reports to be displayed.
     */
    private List<ReportUnit> reportUnits = new ArrayList<>();

    //use this list for display and other operations

    /**
     * The reports sorted according to the selected options.
     */
    private List<ReportUnit> orderedReportUnits = new ArrayList<>();

    /**
     * Map from reports to the corresponding metrics panels. This map is used when updating the selected metrics.
     */
    private final Map<ReportUnit, JPanel> reportUnitToMetricsPanel = new HashMap<>();

    /**
     * Mapping from the keys of the selected metrics to their names.
     */
    private Map<String, String> selectedMetrics;

    /**
     * The current sort criteria (key and order).
     */
    private String currentSortCriteria = "";

    /**
     * The new sort criteria (key and order).
     *
     * TODO: do we need current and new sort criteria as fields? [Freek]
     */
    private String newSortCriteria = "";

    /**
     * The application properties.
     */
    private Properties appProperties;

    /**
     * The parser for reading the metrics.
     */
    private MetricsParser metricsParser;

    /**
     * The status of the QC pipeline.
     */
    private String pipelineStatus;

    /**
     * The text field showing the QC pipeline status.
     */
    private JTextField statusField;

    /**
     * The split pane that contains the control panel and the rest of the GUI.
     */
    private JSplitPane splitPane1 = new JSplitPane(JSplitPane.VERTICAL_SPLIT);

    /**
     * The split pane that contains the main table of reports and the bottom pane showing the enlarged TIC graph of the
     * selected report.
     */
    private JSplitPane splitPane2 = new JSplitPane(JSplitPane.VERTICAL_SPLIT);

    /**
     * The sort panel that contains all the sorting controls.
     */
    private final JPanel sortPanel = new JPanel();

    /**
     * The y coordinate used while laying out the reports in the main table.
     */
    private int yCoordinate;

    /**
     * Creates a new instance of the main frame of the proteomics QC viewer.
     *
     * @param metricsParser the metrics parser to use.
     * @param appProperties the application properties to use.
     * @param title the frame title.
     * @param reportUnits the initial report units to show.
     * @param selectedMetricsData the data of the selected metrics (keys and names).
     * @param pipelineStatus the initial pipeline status to show.
     */
    public ViewerFrame(final MetricsParser metricsParser, final Properties appProperties, final String title,
                       final List<ReportUnit> reportUnits, final List<String> selectedMetricsData,
                       final String pipelineStatus) {
        super(title);
        logger.fine("ViewerFrame constructor");
        setPreferredSize(new Dimension(VIEWER_WIDTH, VIEWER_HEIGHT));
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        this.metricsParser = metricsParser;
        this.appProperties = appProperties;
        this.pipelineStatus = pipelineStatus;
        parseSelectedMetricsData(selectedMetricsData);
        setReportUnits(reportUnits);
        setOrderedReportUnits(reportUnits);
        assembleComponents();
        setVisible(true);
        zoomMinMax();
        // Finally refresh the frame.
        revalidate();
    }

    /**
     * Parse the data of the selected metrics: create lists of metrics keys and metrics names.
     *
     * @param selectedMetricsData the data of the selected metrics (keys and names).
     */
    private void parseSelectedMetricsData(final List<String> selectedMetricsData) {
        selectedMetrics = new HashMap<>();
        // Extract keys and names of the selected metrics.
        for (final String selectedMetricData : selectedMetricsData) {
            final StringTokenizer dataTokenizer = new StringTokenizer(selectedMetricData, METRICS_SEPARATOR);
            final String metricKey = dataTokenizer.nextToken() + METRICS_SEPARATOR + dataTokenizer.nextToken();
            final String metricName = dataTokenizer.nextToken();
            selectedMetrics.put(metricKey, metricName);
        }
    }

    /**
     * New report units are available (QC log file changed, or a different root folder and/or date filter was selected).
     * Update the report units in the viewer frame.
     *
     * @param newReportUnits New QC reports
     * @param newPipelineStatus Updated pipeline status
     * @param replaceFlag If true, existing report units will be replaced by new report units.
     *             Else if false, new reports are added to existing reports. GUI will be updated accordingly.
     */
    public void updateReportUnits(final List<ReportUnit> newReportUnits, final String newPipelineStatus,
                                  final Boolean replaceFlag) {
        logger.fine("In updateReportUnits yCoordinate = " + yCoordinate);
        if (replaceFlag) {
            //Replace all existing reports by newReportUnits.
            reportUnits.clear();
            orderedReportUnits.clear();
            reportIsSelected.clear();
            desktopPane.removeAll();
            ticGraphPane.removeAll();
            pack();
            revalidate();
            yCoordinate = 0;
        }
        final int reportIndexOffset = reportUnits.size();
        if (newReportUnits.size() > 0) {
            for (int reportIndex = 0; reportIndex < newReportUnits.size(); reportIndex++) {
                final ReportUnit thisUnit = newReportUnits.get(reportIndex);
                reportUnits.add(thisUnit);
                orderedReportUnits.add(thisUnit);
                reportIsSelected.add(false);
                addChartFrame(thisUnit, reportIndexOffset + reportIndex);
            }
            desktopPane.setPreferredSize(new Dimension(DESKTOP_PANE_WIDTH, reportUnits.size() * REPORT_ROW_HEIGHT));
            //Set first report graph in the Tic Pane. 
            setTicGraphPaneChart(orderedReportUnits.get(0).getReportIndex());
        }
        updatePipelineStatus(newPipelineStatus);
        revalidate();
    }

    /**
     * Update the pipeline status in the report viewer.
     *
     * @param newPipelineStatus as inferred from the qc_status.log file.
     */
    public void updatePipelineStatus(final String newPipelineStatus) {
        //logger.fine("ViewerFrame updatePipelineStatus");
        pipelineStatus = newPipelineStatus;
        statusField.setText(getExtendedPipelineStatus());
    }

    /**
     * Assemble following components of the ViewerFrame.
     * 1) ControlFrame 2) desktopPane 3) ticGraphPane 4) MenuBar
     */
    private void assembleComponents() {
        logger.fine("ViewerFrame assembleComponents");
        //We need two split panes to create 3 regions in the main frame
        
        //Add desktopPane for displaying graphs and other QC Control
        final int totalReports = orderedReportUnits.size();
        if (totalReports != 0) {
            desktopPane.setPreferredSize(new Dimension(DESKTOP_PANE_WIDTH, totalReports * REPORT_ROW_HEIGHT));
            prepareChartsInOrder(true);
            // Set initial tic Graph - specify complete chart in terms of orderedReportUnits.
            setTicGraphPaneChart(orderedReportUnits.get(0).getReportIndex());
        }
        //Display empty desktopPane and ticGraphPane
        splitPane2.add(new JScrollPane(desktopPane), 0);
        ticGraphPane.setPreferredSize(new Dimension(DESKTOP_PANE_WIDTH, 2 * CHART_HEIGHT));
        splitPane2.add(new JScrollPane(ticGraphPane), 1);
        //hide-show feature
        splitPane2.setOneTouchExpandable(true);
        splitPane2.setDividerLocation(SPLIT_PANE_2_DIVIDER_LOCATION);
        //Add static (immovable) Control frame
        final JInternalFrame controlFrame = getControlFrame();
        final JScrollPane controlFrameScrollPane = new JScrollPane(controlFrame);
        controlFrameScrollPane.setPreferredSize(new Dimension(CONTROL_FRAME_WIDTH, CONTROL_FRAME_HEIGHT));
        splitPane1.setLeftComponent(controlFrameScrollPane);
        splitPane1.setRightComponent(splitPane2);
        //hide-show feature
        splitPane1.setOneTouchExpandable(true);
        splitPane1.setDividerLocation(SPLIT_PANE_1_DIVIDER_LOCATION);
        splitPane1.setPreferredSize(new Dimension(SPLIT_PANE_1_WIDTH, SPLIT_PANE_1_HEIGHT));
        getContentPane().add(splitPane1, "Center");
        setJMenuBar(createMenuBar());
    }

    /**
     * Create Menu Bar for settings and about tab.
     *
     * @return the menu bar.
     */
    private JMenuBar createMenuBar() {
        final JMenuBar menuBar = new JMenuBar();
        final JMenu settingsMenu = new JMenu("Settings");
        menuBar.add(settingsMenu);
        final JMenuItem newDirAction = new JMenuItem("Set Root Directory...");
        settingsMenu.add(newDirAction);
        newDirAction.setActionCommand(CHANGE_ROOT_DIRECTORY_COMMAND);
        newDirAction.addActionListener(this);
        final JMenuItem filterAction = new JMenuItem("Set Filter...");
        settingsMenu.add(filterAction);
        filterAction.setActionCommand(SET_FILTER_COMMAND);
        filterAction.addActionListener(this);
        final JMenuItem metricsAction = new JMenuItem("Select Metrics...");
        settingsMenu.add(metricsAction);
        metricsAction.setActionCommand(SELECT_METRICS_COMMAND);
        metricsAction.addActionListener(this);
        final JMenuItem aboutAction = new JMenuItem("About...");
        settingsMenu.add(aboutAction);
        aboutAction.setActionCommand(ABOUT_COMMAND);
        aboutAction.addActionListener(this);
        return menuBar;
    }

    /**
     * Sets the report units to be displayed.
     *
     * @param reportUnits the report units to be displayed.
     */
    private void setReportUnits(final List<ReportUnit> reportUnits) {
        logger.fine("ViewerFrame setReportUnits No. of reportUnits = " + reportUnits.size());
        if (this.reportUnits != null) {
            this.reportUnits.clear();
        }
        this.reportUnits = reportUnits;
        //Initialize reportIsSelected to false
        for (final ReportUnit ignored : reportUnits) {
            reportIsSelected.add(false);
        }
    }

    /**
     * Sets the report units to be displayed.
     * @param reportUnits the report units to be displayed.
     */
    private void setOrderedReportUnits(final List<ReportUnit> reportUnits) {
        logger.fine("ViewerFrame setOrderedReportUnits No. of reportUnits = " + reportUnits.size());
        orderedReportUnits.clear();
        orderedReportUnits.addAll(reportUnits);
        logger.fine("ViewerFrame setOrderedReportUnits No. of ordered reportUnits = " + orderedReportUnits.size());
    }

    /**
     * Prepare and return controlFrame.
     *
     * @return JInternalFrame controlFrame.
     */
    private JInternalFrame getControlFrame() {
        logger.fine("ViewerFrame getControlFrame");
        logger.fine("CTMM TraIT logo file is " + Constants.CTMM_TRAIT_LOGO_FILE_NAME);

        final JInternalFrame controlFrame = new JInternalFrame("Control Panel", true);
        final javax.swing.plaf.InternalFrameUI ifu = controlFrame.getUI();
        ((javax.swing.plaf.basic.BasicInternalFrameUI)ifu).setNorthPane(null);

        //controlFrame now uses Box layout 
        controlFrame.setBorder(null);
        controlFrame.setBackground(Color.WHITE);

        statusField = new JTextField(getExtendedPipelineStatus());
        statusField.setFont(Constants.DEFAULT_FONT);
//        statusField.setBackground(Color.CYAN);
        statusField.setHorizontalAlignment(JTextField.CENTER);
        statusField.setEditable(false);
        final JPanel statusPanel = new JPanel();
        statusPanel.add(statusField);
        statusPanel.setPreferredSize(new Dimension(STATUS_PANEL_WIDTH, STATUS_PANEL_HEIGHT));

        controlFrame.getContentPane().setLayout(new BoxLayout(controlFrame.getContentPane(), BoxLayout.Y_AXIS));
        controlFrame.getContentPane().add(Box.createRigidArea(DIMENSION_5X0));
        controlFrame.getContentPane().add(createControlPanel(), BorderLayout.CENTER);
        controlFrame.getContentPane().add(Box.createRigidArea(DIMENSION_5X0));
        controlFrame.getContentPane().add(statusPanel, BorderLayout.CENTER);
        controlFrame.getContentPane().add(Box.createRigidArea(DIMENSION_5X0));
        controlFrame.pack();
        controlFrame.setLocation(0, 0);
        controlFrame.setResizable(false);
        controlFrame.setVisible(true);
        return controlFrame;
    }

    /**
     * Create the top control panel.
     *
     * @return the top control panel.
     */
    private JPanel createControlPanel() {
        final JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.X_AXIS));
        controlPanel.setBackground(Color.WHITE);
        controlPanel.add(Box.createRigidArea(DIMENSION_10X0));
        controlPanel.add(createLogoLabel(Constants.OPL_LOGO_FILE_NAME), Box.CENTER_ALIGNMENT);
        controlPanel.add(Box.createRigidArea(DIMENSION_10X0));
        controlPanel.add(createZoomPanel(), Box.CENTER_ALIGNMENT);
        controlPanel.add(Box.createRigidArea(DIMENSION_10X0));
        createOrUpdateSortPanel();
        controlPanel.add(sortPanel, Box.CENTER_ALIGNMENT);
        controlPanel.add(Box.createRigidArea(DIMENSION_10X0));
        controlPanel.add(createLogoLabel(Constants.CTMM_TRAIT_LOGO_FILE_NAME), Box.CENTER_ALIGNMENT);
        controlPanel.add(Box.createRigidArea(DIMENSION_10X0));
        controlPanel.setPreferredSize(new Dimension(CONTROL_PANEL_WIDTH, CONTROL_PANEL_HEIGHT));
        return controlPanel;
    }

    /**
     * Create the zoom panel with all zoom controls.
     *
     * @return the zoom panel with all zoom controls.
     */
    private JPanel createZoomPanel() {
        final JPanel zoomPanel = new JPanel();
        zoomPanel.setLayout(new GridLayout(2, 1));
        zoomPanel.setBackground(Color.WHITE);
        zoomPanel.add(createZoomButtonsPanel(), 0);
        zoomPanel.add(createZoomXAxisPanel(), 1);
        zoomPanel.setMinimumSize(new Dimension(ZOOM_PANEL_WIDTH, ZOOM_PANEL_HEIGHT));
        return zoomPanel;
    }

    /**
     * Create the panel with the zoom in, original and out buttons.
     *
     * @return the panel with the zoom in, original and out buttons.
     */
    private JPanel createZoomButtonsPanel() {
        // Zoom all - in, original, out
        final JButton inButton = new JButton(ZOOM_IN_BUTTON_TEXT);
        inButton.setActionCommand(ZOOM_IN_COMMAND);
        inButton.addActionListener(this);
        final JButton originalButton = new JButton(ZOOM_ORIGINAL_BUTTON_TEXT);
        originalButton.setActionCommand(ZOOM_ORIGINAL_COMMAND);
        originalButton.addActionListener(this);
        final JButton outButton = new JButton(ZOOM_OUT_BUTTON_TEXT);
        outButton.setActionCommand(ZOOM_OUT_COMMAND);
        outButton.addActionListener(this);
        final ButtonGroup zoomGroup = new ButtonGroup();
        zoomGroup.add(inButton);
        zoomGroup.add(originalButton);
        zoomGroup.add(outButton);

        final JPanel zoomButtonsPanel = new JPanel();
        zoomButtonsPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
                                                                    "Zoom All TIC Charts"));
        zoomButtonsPanel.setLayout(new BoxLayout(zoomButtonsPanel, BoxLayout.X_AXIS));
        zoomButtonsPanel.add(Box.createRigidArea(DIMENSION_5X0));
        inButton.setMinimumSize(new Dimension(ZOOM_BUTTON_WIDTH, ZOOM_BUTTON_HEIGHT));
        zoomButtonsPanel.add(inButton, Box.CENTER_ALIGNMENT);
        zoomButtonsPanel.add(Box.createRigidArea(DIMENSION_5X0));
        originalButton.setMinimumSize(new Dimension(ZOOM_BUTTON_WIDTH, ZOOM_BUTTON_HEIGHT));
        zoomButtonsPanel.add(originalButton, Box.CENTER_ALIGNMENT);
        zoomButtonsPanel.add(Box.createRigidArea(DIMENSION_5X0));
        outButton.setMinimumSize(new Dimension(ZOOM_BUTTON_WIDTH, ZOOM_BUTTON_HEIGHT));
        zoomButtonsPanel.add(outButton, Box.CENTER_ALIGNMENT);
        zoomButtonsPanel.add(Box.createRigidArea(DIMENSION_5X0));
        zoomButtonsPanel.setBackground(Color.WHITE);
        zoomButtonsPanel.setMinimumSize(new Dimension(ZOOM_PANEL_WIDTH, ZOOM_PANEL_FORM_HEIGHT));
        return zoomButtonsPanel;
    }

    /**
     * Create the panel with the x axis zoom controls.
     *
     * @return the panel with the x axis zoom controls.
     */
    private JPanel createZoomXAxisPanel() {
        final ActionListener zoomMinMaxActionListener = new ActionListener() {
            public void actionPerformed(final ActionEvent e) {
                zoomMinMax();
            }
        };
        minText.setValue(ZOOM_X_AXIS_DEFAULT_START);
        minText.addActionListener(zoomMinMaxActionListener);
        minText.setMaximumSize(new Dimension(TEXT_BOX_WIDTH, TEXT_BOX_HEIGHT));
        maxText.setValue(ZOOM_X_AXIS_DEFAULT_END);
        maxText.addActionListener(zoomMinMaxActionListener);
        maxText.setMaximumSize(new Dimension(TEXT_BOX_WIDTH, TEXT_BOX_HEIGHT));
        final JButton zoomButton = new JButton(ZOOM_X_AXIS_BUTTON_TEXT);
        zoomButton.setActionCommand(ZOOM_MIN_MAX_COMMAND);
        zoomButton.addActionListener(this);
        zoomButton.setMinimumSize(new Dimension(ZOOM_BUTTON_WIDTH, ZOOM_BUTTON_HEIGHT));
        final JPanel zoomXAxisPanel = new JPanel();
        final Border border = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Zoom Along X Axis");
        zoomXAxisPanel.setBorder(border);
        zoomXAxisPanel.setLayout(new BoxLayout(zoomXAxisPanel, BoxLayout.X_AXIS));
        fillZoomXAxisPanel(zoomButton, zoomXAxisPanel);
        zoomXAxisPanel.setBackground(Color.WHITE);
        zoomXAxisPanel.setMinimumSize(new Dimension(ZOOM_PANEL_WIDTH, ZOOM_PANEL_FORM_HEIGHT));
        return zoomXAxisPanel;
    }

    /**
     * Add the proper controls to the zoom x axis panel.
     *
     * @param zoomButton the zoom x axis button.
     * @param zoomXAxisPanel the zoom x axis panel.
     */
    private void fillZoomXAxisPanel(final JButton zoomButton, final JPanel zoomXAxisPanel) {
        zoomXAxisPanel.add(Box.createRigidArea(DIMENSION_10X0));
        zoomXAxisPanel.add(new JLabel("Min: "), Box.CENTER_ALIGNMENT);
        zoomXAxisPanel.add(Box.createRigidArea(DIMENSION_5X0));
        zoomXAxisPanel.add(minText, Box.CENTER_ALIGNMENT);
        zoomXAxisPanel.add(Box.createRigidArea(DIMENSION_5X0));
        zoomXAxisPanel.add(new JLabel("Max: "), Box.CENTER_ALIGNMENT);
        zoomXAxisPanel.add(Box.createRigidArea(DIMENSION_5X0));
        zoomXAxisPanel.add(maxText, Box.CENTER_ALIGNMENT);
        zoomXAxisPanel.add(Box.createRigidArea(DIMENSION_10X0));
        zoomXAxisPanel.add(zoomButton, Box.CENTER_ALIGNMENT);
        zoomXAxisPanel.add(Box.createRigidArea(DIMENSION_5X0));
    }

    /**
     * Create a label with the specified logo image.
     *
     * @param logoFilePath the file path of the logo image.
     * @return the label with the logo.
     */
    private JLabel createLogoLabel(final String logoFilePath) {
        BufferedImage logoImage = null;
        try {
            logoImage = ImageIO.read(new File(FilenameUtils.normalize(logoFilePath)));
        } catch (final IOException e) {
            logger.log(Level.SEVERE, "Something went wrong while reading logo file " + logoFilePath, e);
        }
        final Image scaledLogoImage = Utilities.scaleImage(logoImage, Utilities.SCALE_FIT, LOGO_WIDTH, LOGO_HEIGHT);
        return new JLabel(new ImageIcon(scaledLogoImage));
    }

    /**
     * Create sort panel displaying sort metrics and sort order buttons.
     */
    private void createOrUpdateSortPanel() {
        sortPanel.removeAll();
        final ButtonGroup sortOptionsButtonGroup = new ButtonGroup();
        sortPanel.setLayout(new GridLayout(selectedMetrics.size() / 2 + 1, 2));
        sortPanel.setBackground(Color.WHITE);
        sortPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Sort Options"));
        sortPanel.setPreferredSize(new Dimension(SORT_PANEL_WIDTH, SORT_PANEL_HEIGHT));
        boolean firstSortOption = true;
        for (Map.Entry<String, String> metricEntry : selectedMetrics.entrySet()) {
            sortPanel.add(createSortOptionPanel(metricEntry.getValue(), metricEntry.getKey(), sortOptionsButtonGroup,
                                                firstSortOption));
            firstSortOption = false;
        }
        // Add sorting option for comparing selected reports.
        sortPanel.add(createSortOptionPanel(SORT_ORDER_COMPARE_LABEL, SORT_ORDER_COMPARE, sortOptionsButtonGroup,
                                            false));
    }

    /**
     * Create a panel with the controls for a sort option: label, ascending radio button and descending radio button.
     *
     * @param sortOptionName the name of the sort option, which is used for the label.
     * @param sortOptionKey the key of the sort option, which is used for the action commands.
     * @param sortOptionsButtonGroup the button group all sort radio buttons should be a part of.
     * @param firstSortOption whether this is the first option: in that case select the ascending radio button and set
     *                        the <code>currentSortCriteria</code>.
     * @return the panel with the controls for a sort option.
     */
    private JPanel createSortOptionPanel(final String sortOptionName, final String sortOptionKey,
                                         final ButtonGroup sortOptionsButtonGroup, final boolean firstSortOption) {
        // Create the label that describes this sort option.
        final JLabel sortOptionLabel = new JLabel(sortOptionName + ':');
        sortOptionLabel.setFont(Constants.DEFAULT_FONT);
        sortOptionLabel.setBackground(Color.WHITE);
        sortOptionLabel.setMinimumSize(new Dimension(METRIC_LABEL_WIDTH, METRIC_LABEL_HEIGHT));
        sortOptionLabel.setMaximumSize(new Dimension(METRIC_LABEL_WIDTH, METRIC_LABEL_HEIGHT));
        final String baseAction = SORT_COMMAND_PREFIX + SORT_COMMAND_SEPARATOR + sortOptionKey + SORT_COMMAND_SEPARATOR;
        // Create the sort ascending button.
        final JRadioButton sortAscendingButton = new JRadioButton(SORT_ORDER_ASCENDING_LABEL, false);
        sortAscendingButton.setBackground(Color.WHITE);
        sortAscendingButton.setActionCommand(baseAction + SORT_ORDER_ASCENDING);
        sortAscendingButton.addActionListener(this);
        sortOptionsButtonGroup.add(sortAscendingButton);
        if (firstSortOption) {
            sortAscendingButton.setSelected(true);
            currentSortCriteria = sortAscendingButton.getActionCommand();
        }
        // Create the sort descending button.
        final JRadioButton sortDescendingButton = new JRadioButton(SORT_ORDER_DESCENDING_LABEL, false);
        sortDescendingButton.setBackground(Color.WHITE);
        sortDescendingButton.setActionCommand(baseAction + SORT_ORDER_DESCENDING);
        sortDescendingButton.addActionListener(this);
        sortOptionsButtonGroup.add(sortDescendingButton);
        // Create the sort option panel.
        final JPanel sortOptionPanel = new JPanel();
        sortOptionPanel.setLayout(new BoxLayout(sortOptionPanel, BoxLayout.X_AXIS));
        sortOptionPanel.setBackground(Color.WHITE);
        sortOptionPanel.add(Box.createRigidArea(DIMENSION_5X0));
        sortOptionPanel.add(sortOptionLabel, Box.CENTER_ALIGNMENT);
        sortOptionPanel.add(Box.createRigidArea(DIMENSION_5X0));
        sortOptionPanel.add(sortAscendingButton, Box.CENTER_ALIGNMENT);
        sortOptionPanel.add(Box.createRigidArea(DIMENSION_5X0));
        sortOptionPanel.add(sortDescendingButton, Box.CENTER_ALIGNMENT);
        sortOptionPanel.add(Box.createRigidArea(DIMENSION_5X0));
        return sortOptionPanel;
    }

    /**
     * Get the pipeline status extended with the number of report units.
     *
     * @return the pipeline status extended with the number of report units.
     */
    private String getExtendedPipelineStatus() {
        return pipelineStatus + " | | | | | Number of report units: " + orderedReportUnits.size();
    }

    /**
     * Set ticChart in the ticGraphPane corresponding to given reportNum.
     *
     * @param reportNum Number of QC report for which chart is to be set.
     */
    private void setTicGraphPaneChart(final int reportNum) {
        logger.fine("ViewerFrame setTicGraphPaneChart " + reportNum);
        ticGraphPane.removeAll();
        // Create the visible chart panel.
        final ChartPanel chartPanel = new ChartPanel(reportUnits.get(reportNum).getChartUnit().getTicChart());
        chartPanel.setPreferredSize(new Dimension(DESKTOP_PANE_WIDTH, 2 * CHART_HEIGHT));
        final JInternalFrame chartFrame = new JInternalFrame(CHART_FRAME_TITLE_PREFIX + reportNum, true);
        final javax.swing.plaf.InternalFrameUI ifu = chartFrame.getUI();
        ((javax.swing.plaf.basic.BasicInternalFrameUI)ifu).setNorthPane(null);
        chartFrame.getContentPane().add(chartPanel);
        chartFrame.setPreferredSize(new Dimension(DESKTOP_PANE_WIDTH, 2 * CHART_HEIGHT));
        chartFrame.setBorder(null);
        chartFrame.pack();
        chartFrame.setLocation(0, 0);
        chartFrame.setVisible(true);
        ticGraphPane.add(chartFrame);
        // Finally refresh the frame.
        revalidate();
    }

    /**
     * Zoom all the ticCharts according to min and max zoom values as obtained from controlFrame.
     */
    private void zoomMinMax() {
        // TODO: Make a new user interface for Min Max zoom box.
        int min = -1;
        int max = -1;
        boolean parseError = false;
        try {
            min = Integer.parseInt(minText.getText());
            max = Integer.parseInt(maxText.getText());
        } catch (final NumberFormatException e) {
            logger.log(Level.SEVERE, "Something went wrong while reading min and max zoom values", e);
            parseError = true;
        }
        if (parseError || minAndMaxAreInvalid(min, max)) {
            final String message1 = "Incorrect min or max.";
            final String message2 = "Resetting to " + ZOOM_X_AXIS_DEFAULT_START + " and " + ZOOM_X_AXIS_DEFAULT_END;
            JOptionPane.showMessageDialog(this, message1 + ' ' + message2, "Error", JOptionPane.ERROR_MESSAGE);
            minText.setText(Integer.toString(ZOOM_X_AXIS_DEFAULT_START));
            maxText.setText(Integer.toString(ZOOM_X_AXIS_DEFAULT_END));
            min = ZOOM_X_AXIS_DEFAULT_START;
            max = ZOOM_X_AXIS_DEFAULT_END;
        }
        setDomainRangeCharts(min, max);
    }

    /**
     * Check whether the start and end percentage for zooming along the x axis are invalid.
     *
     * @param min the start percentage.
     * @param max the end percentage.
     * @return whether the start and end percentage are valid.
     */
    private boolean minAndMaxAreInvalid(final int min, final int max) {
        final int maxPercentage = 99;
        return min < 0 || min > maxPercentage || max < 1 || max > maxPercentage || min >= max;
    }

    /**
     * Change the range of the domain axis for all the charts.
     *
     * @param min the start percentage.
     * @param max the end percentage.
     */
    private void setDomainRangeCharts(final int min, final int max) {
        logger.fine("minValue = " + minText.getText() + " maxValue = " + maxText.getText() + " min = " + min
                    + " max = " + max);
        for (final ChartPanel chartPanel : chartPanelList) {
            final XYPlot plot = (XYPlot) chartPanel.getChart().getPlot();
            plot.getDomainAxis().setRange(min, max);
            chartPanel.setRefreshBuffer(true);
            chartPanel.repaint();
        }
    }

    /**
     * Process user input events.
     *
     * @param actionEvent the action event.
     */
    @Override
    public void actionPerformed(final ActionEvent actionEvent) {
        final String actionCommand = actionEvent.getActionCommand();
        logger.fine("Corresponding action command is " + actionCommand + ", event class: " + actionEvent.getClass());
        if (actionCommand.startsWith(DETAILS_ACTION_PREFIX)) {
            //Parse action command to get reportUnit number
            final StringTokenizer commandTokenizer = new StringTokenizer(actionCommand, "-");
            commandTokenizer.nextToken();
            final int reportNum = Integer.parseInt(commandTokenizer.nextToken());
            logger.fine("Details requested for reportNum " + reportNum);
            final ReportUnit reportUnit = reportUnits.get(reportNum - 1);
            final DetailsFrame detailsFrame = new DetailsFrame(metricsParser.getMetricsListing(), reportUnit);
            detailsFrame.setVisible(true);
            detailsFrame.revalidate();
        } else if (actionCommand.toLowerCase().startsWith("zoom")) {
            handleZoomActions(actionCommand);
        } else if (actionCommand.startsWith(SORT_COMMAND_PREFIX)) {
            // Sort chart frame list according to chosen Sort criteria.
            newSortCriteria = actionCommand;
            sortChartFrameList();
        } else {
            handleMenuActions(actionCommand);
        }
    }

    /**
     * Handle the zoom action events.
     *
     * @param actionCommand the action command.
     */
    private void handleZoomActions(final String actionCommand) {
        if (ZOOM_MIN_MAX_COMMAND.equals(actionCommand)) {
            zoomMinMax();
        } else {
            logger.fine("Number of chart panels: " + chartPanelList.size());
            for (final ChartPanel chartPanel : chartPanelList) {
                switch (actionCommand) {
                    case ZOOM_IN_COMMAND:
                        chartPanel.zoomInDomain(0, 0);
                        break;
                    case ZOOM_ORIGINAL_COMMAND:
                        chartPanel.restoreAutoBounds();
                        break;
                    case ZOOM_OUT_COMMAND:
                        chartPanel.zoomOutDomain(0, 0);
                        break;
                    default:
                        logger.warning("Unexpected zoom action: " + actionCommand);
                }
                chartPanel.setRefreshBuffer(true);
                chartPanel.repaint();
            }
        }
    }

    /**
     * Handle the menu action events.
     *
     * @param actionCommand the action command.
     */
    private void handleMenuActions(final String actionCommand) {
        switch (actionCommand) {
            case CHANGE_ROOT_DIRECTORY_COMMAND:
                new DataEntryForm(this, appProperties).displayRootDirectoryChooser();
                break;
            case SET_FILTER_COMMAND:
                new DataEntryForm(this, appProperties).displayDateFilterEntryForm();
                break;
            case SELECT_METRICS_COMMAND:
                // Display ChooseMetricsForm to select metrics to display.
                final JFrame metricsForm = new ChooseMetricsForm(this, metricsParser, selectedMetrics.keySet());
                metricsForm.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                metricsForm.pack();
                RefineryUtilities.centerFrameOnScreen(metricsForm);
                metricsForm.setVisible(true);
                break;
            case ABOUT_COMMAND:
                final AboutFrame aboutFrame = new AboutFrame();
                aboutFrame.setVisible(true);
                aboutFrame.revalidate();
                break;
            default:
                logger.warning("Unexpected action: " + actionCommand);
        }
    }

    /**
     * Sort displayed report units according to user assigned sort criteria.
     */
    private void sortChartFrameList() {
        logger.fine("sortChartFrameList From " + currentSortCriteria + " To " + newSortCriteria);
        final StringTokenizer sortCriteriaTokenizer = new StringTokenizer(newSortCriteria, SORT_COMMAND_SEPARATOR);
        sortCriteriaTokenizer.nextToken();
        //e.g. generic:date
        final String sortKey = sortCriteriaTokenizer.nextToken();
        //e.g. Asc or Des
        final String sortOrder = sortCriteriaTokenizer.nextToken();
        logger.fine("Sort requested according to " + sortKey + " order " + sortOrder);
        //Remove currently ordered report units and recreate them according to sort criteria
        orderedReportUnits.clear();
        // A new chart frame will be given to every report.
        desktopPane.removeAll();
        final boolean ascending = sortOrder.equals(SORT_ORDER_ASCENDING);
        if (!SORT_ORDER_COMPARE.equals(sortKey)) {
            /* TODO: can we use Collections.sort with a custom comparator here? [Freek]
               [Pravin] ReportUnit.java now implements Comparable<ReportUnit> interface.
               Added a comparator in ReportUnit.java to compare report units.
               Removed compareTo(thisUnit, otherUnit) method from ReportUnit.java. 
               ViewerFrame.java now uses Collections.sort() method to sort reports. 
            */
            //Copy reportUnits to orderedReportUnits
            orderedReportUnits.addAll(reportUnits);
            //Sort orderedReportUnits according to sortKey and sort order - ascending/descending
            Collections.sort(orderedReportUnits, ReportUnit.getReportUnitComparator(sortKey, ascending));
            //Create chart frames and add them to the desktop pane.
            prepareChartsInOrder(true);
            // Set first report graph in the Tic Pane. 
            setTicGraphPaneChart(orderedReportUnits.get(0).getReportIndex());
        } else if (SORT_ORDER_COMPARE.equals(sortKey)) {
            //Check checkbox flag status and group those reports together at the beginning of orderedReportUnits
            //Add all selected reports first i refers to original report number
            final ArrayList<ReportUnit> deselectedReports = new ArrayList<>();
            for (int reportIndex = 0; reportIndex < reportIsSelected.size(); reportIndex++) {
                if (reportIsSelected.get(reportIndex)) {
                    logger.fine("Selected report index = " + reportIndex);
                    orderedReportUnits.add(reportUnits.get(reportIndex));
                } else {
                    deselectedReports.add(reportUnits.get(reportIndex));
                }
            }
            // Now add all deselected reports.
            orderedReportUnits.addAll(deselectedReports);
            final int selectedIndex = ascending ? 0 : orderedReportUnits.size() - 1;
            prepareChartsInOrder(ascending);
            // Show the TIC graph of the first or last report in the bottom TIC pane.
            setTicGraphPaneChart(orderedReportUnits.get(selectedIndex).getReportIndex());
        }
        currentSortCriteria = newSortCriteria;
        newSortCriteria = "";
    }

    /**
     * Create chart frames and add them to the desktop pane.
     *
     * @param ascending if <code>true</code>, the charts will be prepared in ascending order; if <code>false</code>, the
     *                  charts will be prepared in descending order.
     */
    private void prepareChartsInOrder(final boolean ascending) {
        logger.fine("ViewerFrame prepareChartsInOrder");
        chartPanelList.clear();
        yCoordinate = 0;
        logger.fine("No. of orderedReportUnits = " + orderedReportUnits.size());
        for (int reportIndex = 0; reportIndex < orderedReportUnits.size(); reportIndex++) {
            final int sortedIndex = ascending ? reportIndex : orderedReportUnits.size() - reportIndex - 1;
            addChartFrame(orderedReportUnits.get(sortedIndex), reportIndex);
        }
    }

    /**
     * Add an internal frame for a report showing the report number, selection check box, details button, metrics
     * panel and the TIC chart. The controls are created in three panels: 1) reportIdPanel, 2) metricsPanel, and
     * 3) chartPanel. The frame is added to the desktop pane.
     *
     * TODO: clarify when we use reportNumber, reportUnit.getReportNum() - 1 and reportUnit.getReportNum(). [Freek]
     * [Pravin] reportUnit.getReportNum() - 1 represents report index. Index is used for array operations. 
     * I have added reportIndex variable and getReportIndex method in ReportUnit to avoid repetitive calls to
     * reportUnit.getReportNum() - 1.
     *
     * @param reportUnit the report unit.
     * @param reportNumber the report number.
     */
    private void addChartFrame(final ReportUnit reportUnit, final int reportNumber) {
        logger.fine("ViewerFrame addChartFrame " + reportNumber + " ");

        final ChartPanel chartPanel = new ChartPanel(reportUnit.getChartUnit().getTicChart());
        chartPanel.addChartMouseListener(this);
        chartPanel.setPreferredSize(new Dimension(CHART_PANEL_WIDTH, ACTUAL_CHART_HEIGHT));
        chartPanelList.add(chartPanel);

        final JPanel reportIdPanel = createReportIdPanel(reportUnit);
        //metricsPanel uses GridLayout
        final JPanel metricsPanel = createOrUpdateMetricsPanel(reportUnit, null);
        reportUnitToMetricsPanel.put(reportUnit, metricsPanel);

        //displayPanel now uses FlowLayout
        final JPanel displayPanel = new JPanel();
        displayPanel.setLayout(new FlowLayout());
        displayPanel.add(reportIdPanel);
        displayPanel.add(metricsPanel);
        displayPanel.add(chartPanel);
        displayPanel.setBorder(null);

        final JInternalFrame chartFrame = new JInternalFrame(CHART_FRAME_TITLE_PREFIX + reportNumber, true);
        chartFrame.setName(Integer.toString(reportUnit.getReportIndex()));
        ((javax.swing.plaf.basic.BasicInternalFrameUI) chartFrame.getUI()).setNorthPane(null);
        chartFrame.getContentPane().add(displayPanel);
        chartFrame.setBorder(BorderFactory.createRaisedBevelBorder());
        chartFrame.pack();
        chartFrame.setLocation(0, yCoordinate);
        chartFrame.setVisible(true);
        chartFrame.addMouseListener(this);
        desktopPane.add(chartFrame);
        logger.fine("yCoordinate = " + yCoordinate);
        yCoordinate += REPORT_ROW_HEIGHT;
    }

    /**
     * Create a panel for a report showing the report number, the selection check box and the metrics details button.
     *
     * @param reportUnit the report unit to show.
     * @return the panel with the relevant information.
     */
    private JPanel createReportIdPanel(final ReportUnit reportUnit) {
        final JLabel reportNumberLabel = new JLabel(Integer.toString(reportUnit.getReportNum()));
        reportNumberLabel.setFont(Constants.REPORT_NUMBER_FONT);

        final JButton detailsButton = new JButton("Details");
        detailsButton.setFont(Constants.DEFAULT_FONT);
        detailsButton.setActionCommand(DETAILS_ACTION_PREFIX + reportUnit.getReportNum());
        detailsButton.addActionListener(this);

        final JCheckBox selectionCheckBox = new JCheckBox(SORT_ORDER_COMPARE_LABEL);
        selectionCheckBox.setFont(Constants.DEFAULT_FONT);
        selectionCheckBox.setBackground(Color.WHITE);
        // TODO: perhaps it's easier to use the report number here as well, since any unique id is ok? [Freek]
        // [Pravin] Using report index instead of report number
        selectionCheckBox.setName(Integer.toString(reportUnit.getReportIndex()));
        // This call to setSelected preserves selection status.
        /** TODO: this call to setSelected is needed after a refresh of the report list? [Freek]
         * [Pravin] Call to setSelected preserves selection status set by the user. 
         * In case of refresh of the report list, we already clear reportIsSelected list using 
         * reportIsSelected.clear(); [line 411]
         * For every new report, selection status is set to false reportIsSelected.add(false); [Line 424]
         * Hence this call setSelected works fine every time (It doesn't need to know difference between 
         * refresh of the report list and reordering report list) 
         */
        selectionCheckBox.setSelected(reportIsSelected.get(reportUnit.getReportIndex()));
        selectionCheckBox.addItemListener(this);

        //reportIDPanel now uses BoxLayout //TODO: issues with center alignment
        final JPanel reportIdPanel = new JPanel();
        reportIdPanel.setLayout(new BoxLayout(reportIdPanel, BoxLayout.PAGE_AXIS));
        reportIdPanel.setFont(Constants.DEFAULT_FONT);
        reportIdPanel.setBackground(Color.WHITE);
        reportIdPanel.setForeground(Color.WHITE);
        reportIdPanel.add(Box.createRigidArea(DIMENSION_0X10));
        reportIdPanel.add(reportNumberLabel, Component.CENTER_ALIGNMENT);
        reportIdPanel.add(Box.createRigidArea(DIMENSION_0X10));
        reportIdPanel.add(selectionCheckBox, Component.CENTER_ALIGNMENT);
        reportIdPanel.add(Box.createRigidArea(DIMENSION_0X10));
        detailsButton.setPreferredSize(new Dimension(ZOOM_BUTTON_WIDTH, ZOOM_BUTTON_HEIGHT));
        reportIdPanel.add(detailsButton, Component.CENTER_ALIGNMENT);
        reportIdPanel.add(Box.createRigidArea(DIMENSION_0X10));
        reportIdPanel.setPreferredSize(new Dimension(CHECK_PANEL_WIDTH, CHART_HEIGHT));

        return reportIdPanel;
    }

    /**
     * Update the selected metrics: parse the data and change the metrics labels.
     *
     * @param selectedMetricsData the data of the selected metrics (keys and names).
     */
    public void updateSelectedMetrics(final List<String> selectedMetricsData) {
      //Update control frame to display newly selected metrics
        logger.fine("In updateSelectedMetrics - refreshing metrics values..");
        parseSelectedMetricsData(selectedMetricsData);
        for (final ReportUnit reportUnit : reportUnits) {
            if (reportUnitToMetricsPanel.containsKey(reportUnit)) {
                createOrUpdateMetricsPanel(reportUnit, reportUnitToMetricsPanel.get(reportUnit));
            }
        }
        createOrUpdateSortPanel();
        revalidate();
    }

    /**
     * Create or update a panel with the metrics for the specified report unit.
     *
     * @param reportUnit the report unit
     * @param existingMetricsPanel <code>null</code> to create a new panel or an existing panel to replace the labels.
     * @return the panel with the metrics.
     */
    private JPanel createOrUpdateMetricsPanel(final ReportUnit reportUnit, final JPanel existingMetricsPanel) {
        // Create a new metrics panel or remove all labels from the existing panel.
        // metricsPanel already uses GridLayout 
        final JPanel metricsPanel = existingMetricsPanel == null ? new JPanel() : existingMetricsPanel;
        if (existingMetricsPanel == null) {
            metricsPanel.setBackground(Color.WHITE);
            metricsPanel.setLayout(new GridLayout(selectedMetrics.size(), 1));
            metricsPanel.setPreferredSize(new Dimension(METRICS_PANEL_WIDTH, CHART_HEIGHT));
        } else {
            metricsPanel.removeAll();
        }
        // Add labels for each of the selected metrics.
        int metricIndex = 0;
        for (Map.Entry<String, String> metricEntry : selectedMetrics.entrySet()) {
            final String metricValue = reportUnit.getMetricsValueFromKey(metricEntry.getKey());
            final Color foregroundColor = LABEL_COLORS.get(metricIndex % LABEL_COLORS.size());
            final JLabel label = new JLabel(metricEntry.getValue() + ": " + metricValue);
            label.setFont(Constants.DEFAULT_FONT);
            label.setForeground(foregroundColor);
            metricsPanel.add(label);
            metricIndex++;
        }
        if (existingMetricsPanel != null) {
            metricsPanel.validate();
            metricsPanel.repaint();
        }
        return metricsPanel;
    }

    @Override
    public void itemStateChanged(final ItemEvent itemEvent) {
        //Find out index of selection, checked-unchecked and update CheckBoxList
        if (itemEvent.getSource() instanceof JCheckBox) {
            final JCheckBox thisCheckBox = (JCheckBox) itemEvent.getSource();
            logger.fine("Check box name = " + thisCheckBox.getName());
            final int checkBoxFlagIndex = Integer.parseInt(thisCheckBox.getName());
            //reportIsSelected will be maintained all the time according to reportIndex
            if (itemEvent.getStateChange() == ItemEvent.SELECTED) {
                logger.fine("Selected");
                reportIsSelected.set(checkBoxFlagIndex, true);
            } else if (itemEvent.getStateChange() == ItemEvent.DESELECTED) {
                logger.fine("DeSelected");
                reportIsSelected.set(checkBoxFlagIndex, false);
            }
        }
    }

    @Override
    public void mouseClicked(final MouseEvent mouseEvent) {
        final Component clickedComponent = mouseEvent.getComponent();
        if (clickedComponent instanceof JInternalFrame) {
            final JInternalFrame clickedFrame = (JInternalFrame) clickedComponent;
            logger.fine("Frame title = " + clickedFrame.getTitle() + ", frame name = " + clickedFrame.getName());
            setTicGraphPaneChart(Integer.parseInt(clickedFrame.getName()));
        }
    }

    @Override
    public void mouseEntered(final MouseEvent mouseEvent) {
    }

    @Override
    public void mouseExited(final MouseEvent mouseEvent) {
    }

    @Override
    public void mousePressed(final MouseEvent mouseEvent) {
    }

    @Override
    public void mouseReleased(final MouseEvent mouseEvent) {
    }

    /**
     * Select the related report when the user clicks a TIC chart.
     *
     * @param chartMouseEvent the chart mouse event.
     */
    @Override
    public void chartMouseClicked(final ChartMouseEvent chartMouseEvent) {
        // Parse chart index from title.
        final StringTokenizer tokenizer = new StringTokenizer(chartMouseEvent.getChart().getTitle().getText(), "= ");
        // Skip first token. Second token is chart index.
        tokenizer.nextToken();
        final int index = Integer.parseInt(tokenizer.nextToken());
        logger.fine("Graph Index from SubTitle = " + index);
        setTicGraphPaneChart(index);
    }

    @Override
    public void chartMouseMoved(final ChartMouseEvent chartMouseEvent) {
        // This event is not used.
    }
}
