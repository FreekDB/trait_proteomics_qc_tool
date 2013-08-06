package nl.ctmm.trait.proteomics.qcviewer.gui;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;
import java.util.logging.ConsoleHandler;
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
import javax.swing.JTextField;

import nl.ctmm.trait.proteomics.qcviewer.Main;
import nl.ctmm.trait.proteomics.qcviewer.utils.Constants;

import org.apache.commons.io.FilenameUtils;
import org.jfree.ui.RefineryUtilities;

import com.toedter.calendar.JDateChooser;

/**
 * Consists of following forms to accept user input: PreferredRootDirectory - from which to read QC reports,
 * Date selection form to select dates for showing QC reports within those dates.
 *
 * @author <a href="mailto:pravin.pawar@nbic.nl">Pravin Pawar</a>
 * @author <a href="mailto:freek.de.bruijn@nbic.nl">Freek de Bruijn</a>
 */

public class DataEntryForm extends JFrame implements ActionListener, Runnable{
    /**
     * The version number for (de)serialization of this class (UID: universal identifier).
     */
    private static final long serialVersionUID = 1;
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
    private static final int INITIAL_DIALOG_WIDTH = 300;

    /**
     * Height of the initial status dialog.
     */
    private static final int INITIAL_DIALOG_HEIGHT = 100;    
    
    /**
     * Dimension object for filler areas of 10x0 pixels for GUI layout.
     */
    private static final Dimension DIMENSION_10X0 = new Dimension(10, 0);
    
    /**
     * Dimension object for filler areas of 25x0 pixels for GUI layout.
     */
    private static final Dimension DIMENSION_25X0 = new Dimension(25, 0);
    
    JTextField inputText; 
    Main parentMain = null; 
    ViewerFrame parentViewerFrame = null; 
    Properties appProperties = null; 
    JDialog initialDialog = null;
    JLabel message1 = null;
    String rootDirectoryName = "";
    
    /**
     * Constructor - whereas parent is Main class
     * @param parent Instance of Main class
     * @param appProperties Application properties
     */
    public DataEntryForm(final Main parent, final Properties appProperties) {
        super("DataEntry Frame");
        this.parentMain = parent; 
        this.appProperties = appProperties; 
    }

	/**
     * Constructor - whereas parent is ViewerFrame class
     * @param parent Instance of ViewerFrame class
     * @param appProperties Application properties
     */
    public DataEntryForm(final ViewerFrame parent, final Properties appProperties) {
        super("DataEntry Frame");
        this.parentViewerFrame = parent; 
        this.appProperties = appProperties;
    }
    
    /**
     * Set preferred root directory to read QC reports from
     * @param rootDirectoryName Name of the preferred root directory 
     */
    public void setRootDirectoryName(String rootDirectoryName) {
        this.rootDirectoryName = rootDirectoryName;
    }
    
    /**
     * Display initial dialog while starting the QC report Viewer
     */
    public void displayInitialDialog() {
        message1 = new JLabel("<html>Reading reports from " + rootDirectoryName + "</html>");
        initialDialog = new JDialog();
        initialDialog.setTitle("Operation in progress");
        initialDialog.getContentPane().add(message1);
        initialDialog.setPreferredSize(new Dimension(INITIAL_DIALOG_WIDTH,INITIAL_DIALOG_HEIGHT));
        RefineryUtilities.centerFrameOnScreen(initialDialog);
        initialDialog.pack();
        initialDialog.setVisible(true);
        initialDialog.revalidate();
        logger.fine("Displaying initial dialog with message " + message1.getText());
    }
    
    /**
     * Dispose initial dialogue
     */
    public void disposeInitialDialog() {
        if (initialDialog != null) {
            initialDialog.dispose();
            initialDialog = null;
        }
    }
    
    /**
     * Display error message in a dialog
     * @param errorMessage Error message to be shown in a dialog
     */
    public void displayErrorMessage (String errorMessage) {
        JOptionPane.showMessageDialog(this, errorMessage,
                  "Error",JOptionPane.ERROR_MESSAGE);
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
            updatePreferredRootDirectory(preferredRootDirectory);
            dispose();
            Main.getInstance().updateReportViewer();
        } 
     }
    
     /**
      * Save selected root directory in the application properties file
      * @param newRootDirectory Root directory selected by user
      */
    private void updatePreferredRootDirectory(String newRootDirectory) {
        logger.fine("Changing root directory to " + newRootDirectory);
        appProperties.setProperty(Constants.PROPERTY_ROOT_FOLDER, newRootDirectory);
        try {
            FileOutputStream out = new FileOutputStream(FilenameUtils.normalize(Constants.PROPERTIES_FILE_NAME));
            appProperties.store(out, null);
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Process user input events for the class DataEntryForm
     */
    @Override
     public void actionPerformed(ActionEvent ae) {
        logger.fine("DataEntryFrame Action command = " + ae.getActionCommand());
    }

    @Override
    public void run() {
        displayInitialDialog();
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
        JButton b2 = new JButton("Cancel");

        JPanel p3 = new JPanel(); 
        p3.setLayout(new BoxLayout(p3, BoxLayout.X_AXIS));
        p3.add(Box.createRigidArea(DIMENSION_25X0));
        b1.setPreferredSize(new Dimension(BUTTON_WIDTH, BUTTON_HEIGHT));
        p3.add(b1, Box.CENTER_ALIGNMENT);
        p3.add(Box.createRigidArea(DIMENSION_10X0));
        b2.setPreferredSize(new Dimension(BUTTON_WIDTH, BUTTON_HEIGHT));
        p3.add(b2, Box.CENTER_ALIGNMENT);
        
        JPanel p4 = new JPanel(new GridLayout(3,1)); 
        p4.add(p1, 0);
        p4.add(p2, 1);
        p4.add(p3, 2);
        p4.setPreferredSize(new Dimension(DATE_FRAME_WIDTH, DATE_FRAME_HEIGHT));
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
                    JOptionPane.showMessageDialog(null, "Press Select to choose proper date", "Error",JOptionPane.ERROR_MESSAGE);
                } else {
                    try {
                        if (sdf.parse(date1).compareTo(sdf.parse(date2))>0) {
                            JOptionPane.showMessageDialog(null, "From date " + date1 + " is > To date " + date2, "Error",JOptionPane.ERROR_MESSAGE);
                        } else {
                            dispose();
                            appProperties.setProperty(Constants.PROPERTY_SHOW_REPORTS_FROM_DATE, date1);
                            appProperties.setProperty(Constants.PROPERTY_SHOW_REPORTS_TILL_DATE, date2);
                            try {
                                FileOutputStream out = new FileOutputStream(FilenameUtils.normalize(Constants.PROPERTIES_FILE_NAME));
                                appProperties.store(out, null);
                                out.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            Main.getInstance().updateReportViewer();
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
