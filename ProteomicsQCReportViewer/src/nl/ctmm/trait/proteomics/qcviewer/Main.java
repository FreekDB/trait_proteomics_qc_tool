package nl.ctmm.trait.proteomics.qcviewer;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.WindowConstants;
import nl.ctmm.trait.proteomics.qcviewer.gui.ViewerFrame;
import nl.ctmm.trait.proteomics.qcviewer.gui.ViewerPanel;
import nl.ctmm.trait.proteomics.qcviewer.input.DataEntryForm;
import nl.ctmm.trait.proteomics.qcviewer.input.ReportReader;
import nl.ctmm.trait.proteomics.qcviewer.input.ReportUnit;
import nl.ctmm.trait.proteomics.qcviewer.utils.Constants;
import org.jfree.ui.RefineryUtilities;

/**
 * The class that starts the QC Report Viewer.
 *
 * @author <a href="mailto:pravin.pawar@nbic.nl">Pravin Pawar</a>
 * @author <a href="mailto:freek.de.bruijn@nbic.nl">Freek de Bruijn</a>
 */
public class Main{
	private Properties applicationProperties = null; 
	/**
     * The logger for this class.
     */
    private static final Logger logger = Logger.getLogger(Main.class.getName());

    /**
     * The starting point for the QC Report Viewer.
     *
     * @param arguments the command-line arguments, which are currently not used.
     */
    public static void main(final String[] arguments) {
        new Main().runReportViewer();
    }

    /**
     * Start the QC Report Viewer.
     */
    public void runReportViewer() {
        applicationProperties = loadProperties();
       	String preferredRootDirectory = applicationProperties.getProperty(Constants.PROPERTY_ROOT_FOLDER);
        System.out.println("in Main preferredRootDirectory = " + preferredRootDirectory);
        final List<ReportUnit> reportUnits = getReportUnits(preferredRootDirectory);
        if (reportUnits.size() == 0) { //There exist no reports in current root directory
        	//Get new location to read reports from
        	DataEntryForm deForm = new DataEntryForm(this, applicationProperties);
        	deForm.displayErrorMessage("No Reports found in " + preferredRootDirectory);
        	deForm.displayRootDirectoryEntryForm();
        } else {
        	final int GUIversion = Integer.parseInt(applicationProperties.getProperty(Constants.PROPERTY_GUI_VERSION));
        	if (GUIversion == 1) {
        		startGuiVersion1(applicationProperties, reportUnits);
        	} else startGuiVersion2(applicationProperties, reportUnits); 
        }
    }
    
    /**
     * Writes QC Report Summary to a CSV file 
     * @param reportUnits
     */
    private void saveReportSummary(List<ReportUnit> reportUnits) {
    	try {
        	//Save reportUnit values to .csv file
            FileWriter fWriter = new FileWriter("QCReports\\allReports.csv");
            BufferedWriter bWriter = new BufferedWriter(fWriter);
			bWriter.write("Num,msrunName,fileSize,MS1Spectra,MS2Spectra,Measured,Runtime");
			for(int i = 0; i < reportUnits.size(); ++i) {
				ReportUnit thisReport = (ReportUnit) reportUnits.get(i);
				bWriter.write("\n" + thisReport.getReportNum() + "," + thisReport.getMsrunName()
						+ "," + thisReport.getFileSizeString()
						+ "," + thisReport.getMs1Spectra() + "," + thisReport.getMs2Spectra()
						+ "," + thisReport.getMeasured() + "," + thisReport.getRuntime());
			}
			bWriter.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

    /**
     * Load the application properties from the properties file.
     *
     * @return the application properties.
     */
    private Properties loadProperties() {
        final Properties appProperties = new Properties();
        // Set default properties.
        appProperties.setProperty(Constants.PROPERTY_ROOT_FOLDER, Constants.DEFAULT_ROOT_FOLDER);
        appProperties.setProperty(Constants.PROPERTY_TOP_COLUMN_NAMESV1, Constants.DEFAULT_TOP_COLUMN_NAMES);
        appProperties.setProperty(Constants.PROPERTY_TOP_COLUMN_NAMESV2, Constants.DEFAULT_TOP_COLUMN_NAMES);
        appProperties.setProperty(Constants.PROPERTY_BOTTOM_COLUMN_NAMES, Constants.DEFAULT_BOTTOM_COLUMN_NAMES);
        // Load actual properties from file.
        try {
            final FileInputStream fileInputStream = new FileInputStream(Constants.PROPERTIES_FILE_NAME);
            appProperties.load(fileInputStream);
            fileInputStream.close();
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Loading of application properties failed.", e);
        }
        return appProperties;
    }

    /**
     * Get the report units from the directory structure below the root directory.
     *
     * @param rootDirectoryName the root directory to search in.
     * @return the list with report units.
     */
    private List<ReportUnit> getReportUnits(final String rootDirectoryName) {
        return new ReportReader().retrieveReports(rootDirectoryName);
    }

    /**
     * Create and start the GUI - Version 1 of QC Report Viewer.
     *
     * @param appProperties the application properties.
     * @param reportUnits   the report units to be displayed.
     */
    private void startGuiVersion1(final Properties appProperties, final List<ReportUnit> reportUnits) {
        // First show the frame.
        final JFrame frame = new JFrame(Constants.APPLICATION_NAME + " " + Constants.APPLICATION_VERSION);
        //frame.setBounds(200, 40, 1024, 678);
        frame.setSize(new Dimension(1366, 768));
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        // Next put the components in the frame.
        final List<String> mainColumnNames = getColumnNames(appProperties, Constants.PROPERTY_TOP_COLUMN_NAMESV1);
        final List<String> imagesColumnNames = getColumnNames(appProperties, Constants.PROPERTY_BOTTOM_COLUMN_NAMES);
        final ViewerPanel viewerPanel = new ViewerPanel(mainColumnNames, imagesColumnNames);
        viewerPanel.setReportUnits(reportUnits);
        frame.getContentPane().add(viewerPanel);
        frame.setVisible(true);
        // Finally refresh the frame.
        frame.getContentPane().validate();
        frame.getContentPane().repaint();
    }

    /**
     * Create and start the GUI - Version 2 of QC Report Viewer.
     *
     * @param appProperties the application properties.
     * @param reportUnits   the report units to be displayed.
     */
    private void startGuiVersion2(final Properties appProperties, final List<ReportUnit> reportUnits) {
    	System.out.println("Main startGuiVersion2");
    	final List<String> qcParamNames = getColumnNames(appProperties, Constants.PROPERTY_TOP_COLUMN_NAMESV2);
    	//Create ViewerFrame and set it visible
        final ViewerFrame frame = new ViewerFrame(appProperties, Constants.APPLICATION_NAME + " " + Constants.APPLICATION_VERSION, reportUnits, qcParamNames);
        frame.pack();
        RefineryUtilities.centerFrameOnScreen(frame);
        frame.setVisible(true);
    }
    
    /**
     * Get a property with a comma-separated string with column names and convert it into a list of strings.
     *
     * @param applicationProperties the application properties.
     * @param propertyName          the name of the property that contains the comma-separated string with column names.
     * @return the column names in a list of strings.
     */
    private List<String> getColumnNames(final Properties applicationProperties, final String propertyName) {
        return Arrays.asList(applicationProperties.getProperty(propertyName).split(","));
    }
}
