package nl.ctmm.trait.proteomics.qcviewer.gui;

import java.awt.Dimension;
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
        initialDialog.setPreferredSize(new Dimension(300,100));
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
            FileOutputStream out = new FileOutputStream(Constants.PROPERTIES_FILE_NAME);
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
        if (ae.getActionCommand().equals("SUBMITDIR")) {
            String preferredRootDirectory = FilenameUtils.normalize(inputText.getText());
            if (!preferredRootDirectory.trim().equals("")) { //appProperty not empty
                dispose();
                logger.fine("Preferred root directory = " + preferredRootDirectory);
                if (parentMain != null) {
                    updatePreferredRootDirectory(preferredRootDirectory);
                    parentMain.runReportViewer();
                } else if (parentViewerFrame != null) {
                    logger.fine("Invoke parentViewerFrame methods");
                    parentViewerFrame.clean();
                    parentViewerFrame.dispose();
                    updatePreferredRootDirectory(preferredRootDirectory);
                    Main.getInstance().runReportViewer();
                }
            } else displayErrorMessage ("Enter valid root directory.");
        } else if (ae.getActionCommand().startsWith("CANCEL")) {
            if (parentViewerFrame != null) {
                logger.fine("Invoke parentViewerFrame methods");
                parentViewerFrame.clean();
            }
            dispose();
        }
    }

    @Override
    public void run() {
        displayInitialDialog();
    }
    
    /**
     * Display date filter form to select From Date and Till Date for displaying QC reports
     */
    public void displayDateFilterEntryForm() {
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat(
                Constants.SIMPLE_DATE_FORMAT_STRING);
        JLabel label1 = new JLabel("From Date:");
        JPanel p1 = new JPanel();
        p1.setMinimumSize(new Dimension(250, 20));
        p1.add(label1);
        final JDateChooser fromDateChooser = new JDateChooser();
        fromDateChooser.setDateFormatString(Constants.SIMPLE_DATE_FORMAT_STRING);
        //Set current date - 2 weeks in fromDate
        Calendar now = Calendar.getInstance();
        now.add(Calendar.DATE, -14); 
        Date fromDate = now.getTime();
        fromDateChooser.setDate(fromDate);
        fromDateChooser.getDateEditor().setEnabled(false);
        fromDateChooser.setPreferredSize(new Dimension(100, 20));
        p1.add(fromDateChooser);
        fromDateChooser.requestFocusInWindow(); 
        JLabel label2 = new JLabel("    Till Date:");
        JPanel p2 = new JPanel();
        p2.add(label2);
        final JDateChooser tillDateChooser = new JDateChooser();
        tillDateChooser.setDateFormatString(Constants.SIMPLE_DATE_FORMAT_STRING);
        //Set current date
        tillDateChooser.setDate(new Date());
        tillDateChooser.getDateEditor().setEnabled(false);
        tillDateChooser.setPreferredSize(new Dimension(100, 20));
        p2.add(tillDateChooser);
        tillDateChooser.requestFocusInWindow(); 

        JButton b1 = new JButton("Submit");
        JButton b2 = new JButton("Cancel");

        JPanel p3 = new JPanel(new GridLayout(1,2));
        p3.add(b1);
        p3.add(b2);
        
        JPanel p4 = new JPanel(new GridLayout(3,1));
        p4.add(p1, 0);
        p4.add(p2, 1);
        p4.add(p3, 2);
        
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
                                FileOutputStream out = new FileOutputStream(Constants.PROPERTIES_FILE_NAME);
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
