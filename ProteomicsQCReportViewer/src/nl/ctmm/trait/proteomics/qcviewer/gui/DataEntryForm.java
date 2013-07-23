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
import nl.ctmm.trait.proteomics.qcviewer.utils.DatePicker;

import org.apache.commons.io.FilenameUtils;
import org.jfree.ui.RefineryUtilities;

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
        prepareLogger();
        this.parentMain = parent; 
        this.appProperties = appProperties; 
    }

	/**
     * Prepare the logger for this class
     * Set ConsoleHandler as handler
     * Set logging level to ALL 
     */
    private void prepareLogger() {
    	//Set logger and handler levels to Level.ALL
    	logger.setLevel(Level.ALL);
        ConsoleHandler handler = new ConsoleHandler();
        handler.setLevel(Level.ALL);
        logger.addHandler(handler);
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
            if (parentViewerFrame != null) {
                logger.fine("Cleaning everything and restarting..");
                parentViewerFrame.clean();
                parentViewerFrame.dispose();
            }
            dispose();
            Main.getInstance().runReportViewer();
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
        JLabel label1 = new JLabel("From Date:");
        final JTextField text1 = new JTextField(10);
        text1.setEnabled(false);
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat(
                Constants.SIMPLE_DATE_FORMAT_STRING);
        //Set current date - 2 weeks in fromDate
        Calendar now = Calendar.getInstance();
        now.add(Calendar.DATE, -14); 
        Date fromDate = now.getTime();
        text1.setText(sdf.format(fromDate));
        JButton b1 = new JButton("select");
        JPanel p1 = new JPanel();
        p1.add(label1);
        p1.add(text1);
        p1.add(b1);
        
        JLabel label2 = new JLabel("Till Date:");
        final JTextField text2 = new JTextField(10);
        text2.setEnabled(false);
        //Set current date in text2 field
        
        Date tillDate = new Date(); 
        text2.setText(sdf.format(tillDate));
        JButton b2 = new JButton("select");
        JPanel p2 = new JPanel();
        p2.add(label2);
        p2.add(text2);
        p2.add(b2);
        
        JButton b3 = new JButton("Submit");
        JButton b4 = new JButton("Cancel");

        JPanel p3 = new JPanel(new GridLayout(1,2));
        p3.add(b3);
        p3.add(b4);
        
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
                text1.setText(new DatePicker().setPickedDate());
            }
        });
        b2.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                text2.setText(new DatePicker().setPickedDate());
            }
        });
        b3.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                String date1 = text1.getText();
                String date2 = text2.getText();
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat(
                        Constants.SIMPLE_DATE_FORMAT_STRING);
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
                            if (parentViewerFrame != null) {
                                logger.fine("Invoke parentViewerFrame methods");
                                parentViewerFrame.clean();
                                parentViewerFrame.dispose();
                            }
                            Main.getInstance().runReportViewer();
                        }
                    } catch (ParseException e) {
                    	logger.log(Level.SEVERE, "Something went wrong while parsing fromDate and tillDate.", e);
                    } 
                }
            }
        });
        b4.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                dispose();
            }
        });
    }
}
