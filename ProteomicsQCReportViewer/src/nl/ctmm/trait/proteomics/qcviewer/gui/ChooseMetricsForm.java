package nl.ctmm.trait.proteomics.qcviewer.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DropMode;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.TransferHandler;

import nl.ctmm.trait.proteomics.qcviewer.input.MetricsParser;
import nl.ctmm.trait.proteomics.qcviewer.utils.Constants;
import nl.ctmm.trait.proteomics.qcviewer.utils.PropertyFileWriter;

/**
 * The class for displaying the metrics selection form with a drag and drop user interface. This class uses BorderLayout
 * as its layout manager (BorderLayout is the default layout manager for the Frame class).
 *
 * @author <a href="mailto:pravin.pawar@nbic.nl">Pravin Pawar</a>
 * @author <a href="mailto:freek.de.bruijn@nbic.nl">Freek de Bruijn</a>
 */
public class ChooseMetricsForm extends JFrame implements ActionListener {
    /**
     * The logger for this class.
     */
    private static final Logger logger = Logger.getLogger(ChooseMetricsForm.class.getName());

    /**
     * The version number for (de)serialization of this class (UID: universal identifier).
     */
    private static final long serialVersionUID = 1;

    /**
     * The maximum number of metrics that can be selected.
     */
    private static final int MAX_SELECTED_METRICS = 6;

    /**
     * Drag-and-drop target identifier for the list with selected metrics.
     */
    private static final String ID_SELECTED_METRICS = "selectedMetricsList";

    /**
     * Drag-and-drop target identifier for the list with available metrics.
     */
    private static final String ID_AVAILABLE_METRICS = "availableMetricsList";

    /**
     * Width of an individual button.
     */
    private static final int BUTTON_WIDTH = 80;

    /**
     * Height of an individual zoom button.
     */
    private static final int BUTTON_HEIGHT = 25;

    /**
     * Width of the choose metrics form.
     */
    private static final int METRICS_FORM_WIDTH = 830;

    /**
     * Height of the choose metrics form.
     */
    private static final int METRICS_FORM_HEIGHT = 340;
    
    /**
     * The GUI list with the selected metrics.
     */
    private JList<String> selectedMetricsList;

    /**
     * The list model with the selected metrics (as strings).
     */
    private SortedListModel selectedMetricsListModel;

    /**
     * The GUI list with the available (not yet selected) metrics.
     */
    private JList<String> availableMetricsList;

    /**
     * The list model with the available (not yet selected) metrics (as strings).
     */
    private SortedListModel availableMetricsListModel;

    /**
     * The parent viewer frame (to pass the changes in the selected metrics to).
     */
    private ViewerFrame viewerFrame;

    /**
     * Constructor of the metrics selection form.
     *
     * @param viewerFrame the parent viewer frame to pass the changes to.
     * @param metricsParser the metrics parser to get the selected metrics from and pass the changes to.
     * @param selectedMetricsKeys the list of the keys of the currently selected metrics.
     */
    public ChooseMetricsForm(final ViewerFrame viewerFrame, final MetricsParser metricsParser,
                             final Collection<String> selectedMetricsKeys) {
        super("Select QC-Full Metrics for MSQC Report Viewer");
        this.viewerFrame = viewerFrame;
        // Create the list models for both selected metrics and available (not yet selected) metrics.
        createListModels(metricsParser, selectedMetricsKeys);
        // Create the list with selected metrics on the right side.
        this.selectedMetricsList = new JList<>(this.selectedMetricsListModel);
        final String titleSelected = "Drag n Drop: Metrics to Show (max " + MAX_SELECTED_METRICS + "):";
        add(createMetricsPanel(this.selectedMetricsList, ID_SELECTED_METRICS, titleSelected), BorderLayout.CENTER);
        // Create the list with available (not yet selected) metrics on the left side.
        this.availableMetricsList = new JList<>(this.availableMetricsListModel);
        final String titleAvailable = "Drag n Drop: Metrics to Hide:";
        add(createMetricsPanel(this.availableMetricsList, ID_AVAILABLE_METRICS, titleAvailable), BorderLayout.WEST);
        // Add the panel with OK and Cancel buttons.
        addButtonPanel();
        // Set content pane properties.
        ((JPanel) getContentPane()).setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
        getContentPane().setPreferredSize(new Dimension(METRICS_FORM_WIDTH, METRICS_FORM_HEIGHT));
    }

    /**
     * Create the list models for the lists with selected and available metrics.
     *
     * @param metricsParser the metrics parser providing access to all possible metrics.
     * @param selectedMetricsKeys the list of currently selected metric keys.
     */
    private void createListModels(final MetricsParser metricsParser, final Collection<String> selectedMetricsKeys) {
        selectedMetricsListModel = new SortedListModel();
        availableMetricsListModel = new SortedListModel();
        final Map<String, String> metricsMap = metricsParser.getMetricsListing();
        for (final String metricKey : metricsMap.keySet()) {
            final boolean selected = selectedMetricsKeys.contains(metricKey);
            final SortedListModel targetListModel = selected ? selectedMetricsListModel : availableMetricsListModel;
            final String metricName = metricsMap.get(metricKey);
            targetListModel.add(metricKey + Constants.METRICS_SEPARATOR + metricName);
        }
    }

    /**
     * Create a panel for selected or available metrics.
     *
     * @param metricsList the GUI list with selected or available metrics.
     * @param metricsTransferId the drag-and-drop transfer identifier to use for this GUI list.
     * @param title the title to show in a label above the list.
     * @return the panel with the label and the GUI list inside of a scroll pane.
     */
    private JPanel createMetricsPanel(final JList<String> metricsList, final String metricsTransferId,
                                      final String title) {
        final JPanel metricsPanel = new JPanel(); 
        metricsPanel.setLayout(new BoxLayout(metricsPanel, BoxLayout.Y_AXIS));
        metricsList.setTransferHandler(new MetricTransferHandler(metricsTransferId));
        metricsList.setDragEnabled(true);
        metricsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        metricsList.setDropMode(DropMode.INSERT);
        final JLabel metricsTitleLabel = new JLabel(title);
        metricsTitleLabel.setAlignmentX(0f);
        metricsPanel.add(metricsTitleLabel);
        final JScrollPane scrollPane = new JScrollPane(metricsList);
        scrollPane.setAlignmentX(0f);
        metricsPanel.add(scrollPane);
        metricsPanel.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 0));
        return metricsPanel;
    }

    /**
     * Add the panel with OK and Cancel buttons.
     */
    private void addButtonPanel() {
        final JPanel buttonPanel = new JPanel(); 
        final JButton okButton = new JButton(Constants.OK_BUTTON_TEXT);
        okButton.addActionListener(this);
        okButton.setActionCommand(Constants.OK_BUTTON_TEXT);
        final JButton cancelButton = new JButton(Constants.CANCEL_BUTTON_TEXT);
        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent actionEvent) {
                dispose();
            }
        });
        okButton.setPreferredSize(new Dimension(BUTTON_WIDTH, BUTTON_HEIGHT));
        buttonPanel.add(okButton);
        buttonPanel.add(Box.createRigidArea(Constants.DIMENSION_10X0));
        cancelButton.setPreferredSize(new Dimension(BUTTON_WIDTH, BUTTON_HEIGHT));
        buttonPanel.add(cancelButton);
        buttonPanel.setBackground(Color.WHITE);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    /**
     * The user pressed the OK button: save the selected metrics to the application properties file, update the selected
     * metrics in the viewer frame and close this metrics form.
     *
     * @param actionEvent the data related to this event.
     */
    @Override
    public void actionPerformed(final ActionEvent actionEvent) {
        logger.fine("DataEntryFrame Action command = " + actionEvent.getActionCommand());
        if (actionEvent.getActionCommand().equals(Constants.OK_BUTTON_TEXT)) {
            PropertyFileWriter.updateMetricsSelection(selectedMetricsListModel);
            viewerFrame.updateSelectedMetrics(new ArrayList<>(selectedMetricsListModel.getModel()));
            dispose();
        }
    }


    /**
     * This class represents a metric being transferred from one GUI list to another in a drag-and-drop action.
     */
    private class MetricTransferHandler extends TransferHandler {
        /**
         * The version number for (de)serialization of this class (UID: universal identifier).
         */
        private static final long serialVersionUID = 1;

        /**
         * The origin of the metric being transferred. This should be either <code>ID_AVAILABLE_METRICS</code> or
         * <code>ID_SELECTED_METRICS</code>.
         */
        private String origin;

        /**
         * The index of the selected metric in a GUI list.
         */
        private int selectedIndex;

        /**
         * Create a metric transfer handler.
         *
         * @param origin the origin of the metric being transferred. This should be either
         *               <code>ID_AVAILABLE_METRICS</code> or <code>ID_SELECTED_METRICS</code>.
         */
        public MetricTransferHandler(final String origin) {
            this.origin = origin;
        }

        @Override
        public int getSourceActions(final JComponent component) {
            return MOVE;
        }

        @Override
        public boolean canImport(final TransferHandler.TransferSupport support) {
            boolean importPossible = false;
            if (support.isDrop()) {
                if (selectedMetricsListModel.getSize() >= MAX_SELECTED_METRICS
                    && origin.equalsIgnoreCase(ID_SELECTED_METRICS)) {
                    logger.fine("Limit of selected list exceeded. Max " + MAX_SELECTED_METRICS + ". Origin: " + origin);
                } else if (support.isDataFlavorSupported(DataFlavor.stringFlavor)) {
                    importPossible = (support.getSourceDropActions() & TransferHandler.MOVE) == TransferHandler.MOVE;
                    if (importPossible) {
                        support.setDropAction(TransferHandler.MOVE);
                    }
                }
            }
            return importPossible;
        }

        @Override
        public Transferable createTransferable(final JComponent component) {
            String selection = null;
            if (origin.equalsIgnoreCase(ID_AVAILABLE_METRICS)) {
                selection = availableMetricsList.getSelectedValue();
                selectedIndex = availableMetricsList.getSelectedIndex();
            } else if (origin.equalsIgnoreCase(ID_SELECTED_METRICS)) {
                selection = selectedMetricsList.getSelectedValue();
                selectedIndex = selectedMetricsList.getSelectedIndex();
            }
            return selection != null ? new StringSelection(selection) : null;
        }

        @Override
        public void exportDone(final JComponent comp, final Transferable trans, final int action) {
            if (action == MOVE) {
                if (origin.equalsIgnoreCase(ID_AVAILABLE_METRICS)) {
                    availableMetricsListModel.removeElementAt(selectedIndex);
                } else if (origin.equalsIgnoreCase(ID_SELECTED_METRICS)) {
                    selectedMetricsListModel.removeElementAt(selectedIndex);
                }
            }
        }

        @Override
        public boolean importData(final TransferHandler.TransferSupport support) {
            boolean result = false;
            if (canImport(support)) {
                try {
                    final JList list = (JList) support.getComponent();
                    final SortedListModel model = (SortedListModel) list.getModel();
                    model.add((String) support.getTransferable().getTransferData(DataFlavor.stringFlavor));
                    final int index = ((JList.DropLocation) support.getDropLocation()).getIndex();
                    list.scrollRectToVisible(list.getCellBounds(index, index));
                    list.setSelectedIndex(index);
                    list.requestFocusInWindow();
                    result = true;
                } catch (final UnsupportedFlavorException | IOException e) {
                    logger.log(Level.SEVERE, "Something went wrong while selecting metrics (with drag-and-drop)", e);
                }
            }
            return result;
        }  
    } 
}
