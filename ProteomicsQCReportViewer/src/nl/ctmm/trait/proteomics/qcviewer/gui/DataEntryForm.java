package nl.ctmm.trait.proteomics.qcviewer.gui;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Date;
import java.util.logging.Logger;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import nl.ctmm.trait.proteomics.qcviewer.Main;
import nl.ctmm.trait.proteomics.qcviewer.utils.Constants;
import nl.ctmm.trait.proteomics.qcviewer.utils.PropertyFileWriter;

import org.apache.commons.io.FilenameUtils;
import org.jfree.ui.RefineryUtilities;

import com.toedter.calendar.JDateChooser;

/**
 * This class is responsible for several dialogs:
 * - the one that is shown while the reports are read during initialization;
 * - the form that is used to change the top directory from which the QC reports are read;
 * - the form that is used to select dates for filtering the QC reports to be shown.
 *
 * TODO: can we use JDialog instead of JFrame (that way we can set the owner to the viewer frame)? [Freek]
 * TODO: "extending JDialog is as bad as extending JFrame" see
 *       http://stackoverflow.com/questions/15429653/how-to-set-the-jframe-as-a-parent-to-the-jdialog [Freek]
 *
 * @author <a href="mailto:pravin.pawar@nbic.nl">Pravin Pawar</a>
 * @author <a href="mailto:freek.de.bruijn@nbic.nl">Freek de Bruijn</a>
 */
public class DataEntryForm extends JFrame {
    /**
     * The version number for (de)serialization of this class (UID: universal identifier).
     */
    private static final long serialVersionUID = 1;
    
    /**
     * The logger for this class.
     */
    private static final Logger logger = Logger.getLogger(DataEntryForm.class.getName());

    /**
     * Width of an individual button.
     */
    private static final int BUTTON_WIDTH = 80;

    /**
     * Height of an individual zoom button.
     */
    private static final int BUTTON_HEIGHT = 25;
    
    /**
     * Width of the date chooser component.
     */
    private static final int DATE_CHOOSER_WIDTH = 100;

    /**
     * Height of the date chooser component.
     */
    private static final int DATE_CHOOSER_HEIGHT = 20;

    /**
     * Width of the from and till date panels.
     */
    private static final int DATE_PANEL_WIDTH = 180;

    /**
     * Height of the from and till date panels.
     */
    private static final int DATE_PANEL_HEIGHT = 20;

    /**
     * Width of the from and till date panels.
     */
    private static final int DATE_LABEL_WIDTH = 80;

    /**
     * Height of the from and till date panels.
     */
    private static final int DATE_LABEL_HEIGHT = DATE_PANEL_HEIGHT;

    /**
     * Width of the Data Entry Form for selecting dates.
     */
    private static final int DATE_FRAME_WIDTH = 200;

    /**
     * Height of the Data Entry Form for selecting dates.
     */
    private static final int DATE_FRAME_HEIGHT = 120;
    
    /**
     * Width of the initial status dialog.
     */
    private static final int INITIAL_DIALOG_WIDTH = 1000;

    /**
     * Height of the initial status dialog.
     */
    private static final int INITIAL_DIALOG_HEIGHT = 100;    
    
    /**
     * Width of the initial status dialog.
     */
    private static final int NO_REPORTS_FRAME_WIDTH = 300;

    /**
     * Height of the initial status dialog.
     */
    private static final int NO_REPORTS_FRAME_HEIGHT = 90;    

    /**
     * Width of the initial status dialog.
     */
    private static final int NO_REPORTS_TEXT_WIDTH = 300;

    /**
     * Height of the initial status dialog.
     */
    private static final int NO_REPORTS_TEXT_HEIGHT = 50;    
    
    /**
     * The title shown when an error has occurred.
     */
    private static final String ERROR_TITLE = "Error";

    /**
     * Message shown to the user when no reports are found.
     */
    private static final String NO_REPORTS_MESSAGE = "No reports found in ";
    
    /**
     * Message asking user's input to select another root directory.
     */
    private static final String ROOT_DIRECTORY_USER_INPUT_MESSAGE = "Would you like to select another root directory?"; 
    
    /**
     * The dialog that is shown while the reports are read during initialization.
     */
    private JDialog initialDialog;

    /**
     * The root directory under which the QC pipeline writes the QC reports.
     */
    private String rootDirectoryName;
    
    /**
     * Construct a data entry form.
     */
    public DataEntryForm() {
        super("DataEntry Frame");
    }

    /**
     * Set preferred root directory to read QC reports from.
     *
     * TODO: pass rootDirectoryName as a parameter to displayInitialDialog. [Freek]
     *
     * @param rootDirectoryName Name of the preferred root directory 
     */
    public void setRootDirectoryName(final String rootDirectoryName) {
        this.rootDirectoryName = rootDirectoryName;
    }
    
    /**
     * Display the initial dialog while starting the QC report viewer.
     */
    public void displayInitialDialog() {
        final String absolutePath = new File(rootDirectoryName).getAbsolutePath();
        final String message = "  Reading QC reports from the QC pipeline (under the " + absolutePath + " directory).";
        initialDialog = new JDialog((Frame) null, "Reading QC reports in progress");
        initialDialog.getContentPane().add(new JLabel(message));
        initialDialog.setPreferredSize(new Dimension(INITIAL_DIALOG_WIDTH, INITIAL_DIALOG_HEIGHT));
        initialDialog.pack();
        RefineryUtilities.centerFrameOnScreen(initialDialog);
        initialDialog.setVisible(true);
        initialDialog.revalidate();
        logger.fine("Displaying initial dialog with message " + message);
    }
    
    /**
     * Dispose the initial dialogue.
     */
    public void disposeInitialDialog() {
        if (initialDialog != null) {
            initialDialog.dispose();
            initialDialog = null;
        }
    }
    
    /**
     * Display error message in a dialog.
     *
     * @param errorMessage Error message to be shown in a dialog
     */
    public void displayErrorMessage(final String errorMessage) {
        JOptionPane.showMessageDialog(this, errorMessage, ERROR_TITLE, JOptionPane.ERROR_MESSAGE);
    }
    
    /**
     * Show a message to the user that no QC reports have been found and ask whether the user wants to select another
     * root directory.
     *
     * TODO: can we use JOptionPane.showConfirmDialog(null, message, "title", JOptionPane.YES_NO_OPTION)? [Freek]
     *
     * @param preferredRootDirectory the directory to which the QC pipeline writes the QC reports.
     */
    public void displayNoReportsFoundDialogue(final String preferredRootDirectory) {
        final String message = NO_REPORTS_MESSAGE + preferredRootDirectory + ". " + ROOT_DIRECTORY_USER_INPUT_MESSAGE;
        final String htmlMessage = Constants.HTML_OPENING_TAG + message + Constants.HTML_CLOSING_TAG;
        final JLabel messageLabel = new JLabel(htmlMessage, JLabel.CENTER);
        messageLabel.setFont(Constants.DEFAULT_FONT);
        messageLabel.setPreferredSize(new Dimension(NO_REPORTS_TEXT_WIDTH, NO_REPORTS_TEXT_HEIGHT));

        final JButton yesButton = new JButton(Constants.YES_BUTTON_TEXT);
        yesButton.setPreferredSize(new Dimension(BUTTON_WIDTH, BUTTON_HEIGHT));
        yesButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent actionEvent) {
                dispose();
                displayRootDirectoryChooser();
            }
        });

        final JButton noButton = new JButton(Constants.NO_BUTTON_TEXT);
        noButton.setPreferredSize(new Dimension(BUTTON_WIDTH, BUTTON_HEIGHT));
        noButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent actionEvent) {
                dispose();
            }
        });

        final JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
        buttonPanel.add(Box.createRigidArea(Constants.DIMENSION_25X0));
        buttonPanel.add(yesButton, Box.CENTER_ALIGNMENT);
        buttonPanel.add(Box.createRigidArea(Constants.DIMENSION_25X0));
        buttonPanel.add(noButton, Box.CENTER_ALIGNMENT);
        
        final JPanel mainPanel = new JPanel(new FlowLayout());
        mainPanel.setPreferredSize(new Dimension(NO_REPORTS_FRAME_WIDTH, NO_REPORTS_FRAME_HEIGHT));
        mainPanel.add(messageLabel);
        mainPanel.add(buttonPanel);

        setTitle(ERROR_TITLE);
        getContentPane().add(mainPanel);
        setResizable(false);
        pack();
        RefineryUtilities.centerFrameOnScreen(this);
        setVisible(true);
    }

    /**
     * Display chooser form to select the preferred root directory.
     */
    public void displayRootDirectoryChooser() {
        final JFileChooser chooser = new JFileChooser();
        chooser.setName("Select Preferred Root Directory");
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            logger.fine("You chose to open this folder: "
                        + FilenameUtils.normalize(chooser.getSelectedFile().getAbsolutePath()));
            final String preferredRootDirectory = FilenameUtils.normalize(chooser.getSelectedFile().getAbsolutePath());
            PropertyFileWriter.updatePreferredRootDirectory(preferredRootDirectory);
            Main.getInstance().updateReportViewer(true);
            dispose();
        }
    }
    
    /**
     * Display the date filter form to select the from and till date for filtering QC reports.
     */
    public void displayDateFilterEntryForm() {
        setTitle("Date Filter");
        final JDateChooser fromDateChooser = createDateChooser(Main.getInstance().getFromDate());
        final JDateChooser tillDateChooser = createDateChooser(Main.getInstance().getTillDate());

        final JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.add(Box.createRigidArea(Constants.DIMENSION_0X5));
        mainPanel.add(createDatePanel("From Date:", fromDateChooser));
        mainPanel.add(Box.createRigidArea(Constants.DIMENSION_0X5));
        mainPanel.add(createDatePanel("Till Date:", tillDateChooser));
        mainPanel.add(Box.createRigidArea(Constants.DIMENSION_0X10));
        mainPanel.add(createButtonPanel(fromDateChooser, tillDateChooser));
        mainPanel.add(Box.createRigidArea(Constants.DIMENSION_0X5));

        mainPanel.setPreferredSize(new Dimension(DATE_FRAME_WIDTH, DATE_FRAME_HEIGHT));
        getContentPane().add(mainPanel);
        pack();
        RefineryUtilities.centerFrameOnScreen(this);
        setVisible(true);
    }

    /**
     * Create a date chooser and set the right properties.
     *
     * @param initialDate the initial date to be displayed.
     * @return the date chooser.
     */
    private JDateChooser createDateChooser(final Date initialDate) {
        final JDateChooser dateChooser = new JDateChooser();
        dateChooser.setDateFormatString(Constants.SIMPLE_DATE_FORMAT_STRING);
        dateChooser.setDate(initialDate);
        dateChooser.getDateEditor().setEnabled(false);
        dateChooser.setPreferredSize(new Dimension(DATE_CHOOSER_WIDTH, DATE_CHOOSER_HEIGHT));
        return dateChooser;
    }

    /**
     * Create a date panel with a label and a date chooser.
     *
     * @param labelText the text for the label.
     * @param dateChooser the date chooser to add.
     * @return the date panel.
     */
    private JPanel createDatePanel(final String labelText, final JDateChooser dateChooser) {
        final JPanel datePanel = new JPanel(new FlowLayout());
        datePanel.setPreferredSize(new Dimension(DATE_PANEL_WIDTH, DATE_PANEL_HEIGHT));
        final JLabel dateLabel = new JLabel(labelText);
        dateLabel.setPreferredSize(new Dimension(DATE_LABEL_WIDTH, DATE_LABEL_HEIGHT));
        dateLabel.setFont(Constants.DEFAULT_FONT);
        datePanel.add(dateLabel);
        datePanel.add(dateChooser);
        dateChooser.requestFocusInWindow();
        return datePanel;
    }

    /**
     * Create the panel with the OK & Cancel buttons and create the action listeners for both buttons.
     *
     * @param fromDateChooser the from date chooser.
     * @param tillDateChooser the till date chooser.
     * @return the button panel.
     */
    private JPanel createButtonPanel(final JDateChooser fromDateChooser, final JDateChooser tillDateChooser) {
        final JButton okButton = new JButton(Constants.OK_BUTTON_TEXT);
        okButton.setFont(Constants.DEFAULT_FONT);
        okButton.setPreferredSize(new Dimension(BUTTON_WIDTH, BUTTON_HEIGHT));
        okButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent actionEvent) {
                final String fromDate = Constants.DATE_FORMAT.format(fromDateChooser.getDate());
                final String tillDate = Constants.DATE_FORMAT.format(tillDateChooser.getDate());
                if (!"".equals(fromDate) && !"".equals(tillDate)) {
                    if (!fromDateChooser.getDate().after(tillDateChooser.getDate())) {
                        PropertyFileWriter.updateFromAndTillDates(fromDate, tillDate);
                        Main.getInstance().updateReportViewer(false);
                        dispose();
                    } else {
                        showErrorMessage("From date " + fromDate + " is after till date " + tillDate);
                    }
                } else {
                    showErrorMessage("Please fill in both dates");
                }
            }
        });

        final JButton cancelButton = new JButton(Constants.CANCEL_BUTTON_TEXT);
        cancelButton.setFont(Constants.DEFAULT_FONT);
        cancelButton.setPreferredSize(new Dimension(BUTTON_WIDTH, BUTTON_HEIGHT));
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent actionEvent) {
                dispose();
            }
        });

        final JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
        buttonPanel.add(Box.createRigidArea(Constants.DIMENSION_25X0));
        buttonPanel.add(okButton, Box.CENTER_ALIGNMENT);
        buttonPanel.add(Box.createRigidArea(Constants.DIMENSION_10X0));
        buttonPanel.add(cancelButton, Box.CENTER_ALIGNMENT);
        return buttonPanel;
    }

    /**
     * Show an error message to the user.
     *
     * @param errorMessage the error message to show.
     */
    private void showErrorMessage(final String errorMessage) {
        JOptionPane.showMessageDialog(this, errorMessage, ERROR_TITLE, JOptionPane.ERROR_MESSAGE);
    }
}
