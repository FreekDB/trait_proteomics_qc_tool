package nl.ctmm.trait.proteomics.qcviewer.gui;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.Level;
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
     * Display chooser form to select preferred root directory
     */
     public void displayRootDirectoryChooser () {
     JFileChooser chooser = new JFileChooser();
         chooser.setName("Select Preferred Root Directory");
        chooser.setFileSelectionMode( JFileChooser.DIRECTORIES_ONLY);
        int returnVal = chooser.showOpenDialog(null);
        String preferredRootDirectory = null;
        if(returnVal == JFileChooser.APPROVE_OPTION) {
            preferredRootDirectory = FilenameUtils.normalize(chooser.getSelectedFile().getAbsolutePath());
           logger.fine("You chose to open this folder: " +
        		   FilenameUtils.normalize(chooser.getSelectedFile().getAbsolutePath()));
            PropertyFileWriter.updatePreferredRootDirectory(preferredRootDirectory);
            dispose();
            Main.getInstance().updateReportViewer(true);
        } 
     }
    
    /**
     * Display date filter form to select From Date and Till Date for displaying QC reports
     */
    public void displayDateFilterEntryForm() {
        JLabel label1 = new JLabel("From Date:");
        JPanel p1 = new JPanel(new FlowLayout()); 
        p1.setPreferredSize(new Dimension(DATE_PANEL_WIDTH, DATE_PANEL_HEIGHT));
        p1.add(label1);
        final JDateChooser fromDateChooser = new JDateChooser();
        fromDateChooser.setDateFormatString(Constants.SIMPLE_DATE_FORMAT_STRING);
        //Set current date - 2 weeks in fromDate
        Calendar now = Calendar.getInstance();
        now.add(Calendar.DATE, -14); 
        Date fromDate = now.getTime();
        fromDateChooser.setDate(fromDate);
        fromDateChooser.getDateEditor().setEnabled(false);
        fromDateChooser.setPreferredSize(new Dimension(DATE_CHOOSER_WIDTH, DATE_CHOOSER_HEIGHT));
        p1.add(fromDateChooser);
        fromDateChooser.requestFocusInWindow(); 
        JLabel label2 = new JLabel("    Till Date:");
        JPanel p2 = new JPanel(new FlowLayout()); 
        p2.add(label2);
        final JDateChooser tillDateChooser = new JDateChooser();
        tillDateChooser.setDateFormatString(Constants.SIMPLE_DATE_FORMAT_STRING);
        //Set current date
        tillDateChooser.setDate(new Date());
        tillDateChooser.getDateEditor().setEnabled(false);
        tillDateChooser.setPreferredSize(new Dimension(DATE_CHOOSER_WIDTH, DATE_CHOOSER_HEIGHT));
        p2.add(tillDateChooser);
        tillDateChooser.requestFocusInWindow(); 

        JButton b1 = new JButton("Submit");
        JButton b2 = new JButton(Constants.CANCEL_BUTTON_TEXT);

        JPanel p3 = new JPanel(); 
        p3.setLayout(new BoxLayout(p3, BoxLayout.X_AXIS));
        p3.add(Box.createRigidArea(Constants.DIMENSION_25X0));
        b1.setPreferredSize(new Dimension(BUTTON_WIDTH, BUTTON_HEIGHT));
        p3.add(b1, Box.CENTER_ALIGNMENT);
        p3.add(Box.createRigidArea(Constants.DIMENSION_10X0));
        b2.setPreferredSize(new Dimension(BUTTON_WIDTH, BUTTON_HEIGHT));
        p3.add(b2, Box.CENTER_ALIGNMENT);
        
        JPanel p4 = new JPanel(new GridLayout(3,1)); 
        p4.add(p1, 0);
        p4.add(p2, 1);
        p4.add(p3, 2);
        p4.setPreferredSize(new Dimension(DATE_FRAME_WIDTH, DATE_FRAME_HEIGHT));

        setTitle("Date Filter");
        getContentPane().add(p4);
        pack();
        RefineryUtilities.centerFrameOnScreen(this);
        setVisible(true);
        b1.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat(
                        Constants.SIMPLE_DATE_FORMAT_STRING);
            	String date1 = sdf.format(fromDateChooser.getDate());
                String date2 = sdf.format(tillDateChooser.getDate());

                if (date1.equals("") || date2.equals("")) {
                    JOptionPane.showMessageDialog(null, "Press Select to choose proper date", ERROR_TITLE,
                                                  JOptionPane.ERROR_MESSAGE);
                } else {
                    try {
                        if (sdf.parse(date1).compareTo(sdf.parse(date2))>0) {
                            JOptionPane.showMessageDialog(null, "From date " + date1 + " is > To date " + date2,
                                                          ERROR_TITLE, JOptionPane.ERROR_MESSAGE);
                        } else {
                            PropertyFileWriter.updateFromAndTillDates(date1, date2);
                            dispose();               
                            Main.getInstance().updateReportViewer(false);
                        }
                    } catch (ParseException e) {
                    	logger.log(Level.SEVERE, "Something went wrong while parsing fromDate and tillDate.", e);
                    } 
                }
            }
        });
        b2.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                dispose();
            }
        });
        setResizable(false);
    }
}
