package nl.ctmm.trait.proteomics.qcviewer.input;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import nl.ctmm.trait.proteomics.qcviewer.Main;
import nl.ctmm.trait.proteomics.qcviewer.gui.ViewerFrame;
import nl.ctmm.trait.proteomics.qcviewer.utils.Constants;

import org.jfree.ui.RefineryUtilities;

public class DataEntryForm extends JFrame implements ActionListener{

	JTextField inputText; 
	Main parentMain = null; 
	ViewerFrame parentViewerFrame = null; 
	Properties appProperties = null; 
	
	public DataEntryForm(final Main parent, final Properties appProperties) {
		super("DataEntry Frame");
		this.parentMain = parent; 
		this.appProperties = appProperties; 
	}
	
	public DataEntryForm(final ViewerFrame parent, final Properties appProperties) {
		super("DataEntry Frame");
		this.parentViewerFrame = parent; 
		this.appProperties = appProperties;
	}
	
	public void displayErrorMessage (String errorMessage) {
    	JOptionPane.showMessageDialog(this, errorMessage,
				  "Error",JOptionPane.ERROR_MESSAGE);
	}
	
    public void displayRootDirectoryEntryForm () {
    	JLabel instruction = new JLabel();
    	instruction.setText("Enter new report folder location:");
    	JLabel label = new JLabel();
    	label.setText("Root folder:");
    	inputText = new JTextField(100);
    	JButton SUBMIT = new JButton("SUBMIT");
    	SUBMIT.setPreferredSize(new Dimension(50, 20));
    	JButton CANCEL = new JButton("CANCEL"); 
    	CANCEL.setPreferredSize(new Dimension(50, 20));
  	  	SUBMIT.addActionListener(this);
  	  	SUBMIT.setActionCommand("SUBMITDIR");
  	  	CANCEL.addActionListener(this);
  	  	CANCEL.setActionCommand("CANCELDIR");
    	JPanel warningPanel = new JPanel(new GridLayout(1,1));
    	warningPanel.add(instruction);
    	JPanel inputPanel = new JPanel(new GridLayout(2,2));
    	inputPanel.setPreferredSize(new Dimension(120, 40));
    	inputPanel.add(label);
    	inputPanel.add(inputText);
    	inputPanel.add(SUBMIT);
    	inputPanel.add(CANCEL);
    	JPanel displayPanel = new JPanel(new GridLayout(2, 1)); 
    	displayPanel.add(warningPanel, 0);
    	displayPanel.add(inputPanel, 1);
    	displayPanel.setPreferredSize(new Dimension(220, 80));
    	add(displayPanel);
    	setSize(new Dimension(300, 150));
    	RefineryUtilities.centerFrameOnScreen(this);
    	setVisible(true);
    }
    
    public void displayPreferredServerEntryForm () {
    	JLabel instruction = new JLabel();
    	instruction.setText("Enter server IP address:");
    	JLabel label = new JLabel();
    	label.setText("Server IP:");
    	inputText = new JTextField(15);
    	JButton SUBMIT = new JButton("SUBMIT");
    	SUBMIT.setPreferredSize(new Dimension(50, 20));
    	JButton CANCEL = new JButton("CANCEL"); 
    	CANCEL.setPreferredSize(new Dimension(50, 20));
  	  	SUBMIT.addActionListener(this);
  	  	SUBMIT.setActionCommand("SUBMITSER");
  	  	CANCEL.addActionListener(this);
  	  	CANCEL.setActionCommand("CANCELSER");
    	JPanel warningPanel = new JPanel(new GridLayout(1,1));
    	warningPanel.add(instruction);
    	JPanel inputPanel = new JPanel(new GridLayout(2,2));
    	inputPanel.setPreferredSize(new Dimension(120, 40));
    	inputPanel.add(label);
    	inputPanel.add(inputText);
    	inputPanel.add(SUBMIT);
    	inputPanel.add(CANCEL);
    	JPanel displayPanel = new JPanel(new GridLayout(2, 1)); 
    	displayPanel.add(warningPanel, 0);
    	displayPanel.add(inputPanel, 1);
    	displayPanel.setPreferredSize(new Dimension(220, 80));
    	add(displayPanel);
    	setSize(new Dimension(300, 150));
    	RefineryUtilities.centerFrameOnScreen(this);
    	setVisible(true);
    }
    
    /**
     * Sets the preferredRootDirectory 
     */
    public void updatePreferredRootDirectory(String newRootDirectory) {
    	System.out.println("Changing root directory to " + newRootDirectory);
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
     * Sets the preferredServer 
     */
    public void updatePreferredServer(String newServer) {
    	System.out.println("Changing server to " + newServer);
    	appProperties.setProperty(Constants.PROPERTY_PREFERRED_SERVER, newServer);
		try {
			FileOutputStream out = new FileOutputStream(Constants.PROPERTIES_FILE_NAME);
			appProperties.store(out, null);
	    	out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
    
	@Override
	 public void actionPerformed(ActionEvent ae) {
		if (ae.getActionCommand().equals("SUBMITDIR")) {
			String preferredRootDirectory = inputText.getText();
			if (!preferredRootDirectory.trim().equals("")) { //appProperty not empty
				System.out.println("Preferred root directory = " + preferredRootDirectory);
				if (parentMain != null) {
					updatePreferredRootDirectory(preferredRootDirectory);
					parentMain.runReportViewer();
				} else if (parentViewerFrame != null) {
					System.out.println("Invoke parentViewerFrame methods");
					parentViewerFrame.dispose();
					updatePreferredRootDirectory(preferredRootDirectory);
					new Main().runReportViewer();
				}
				dispose();
			} else displayErrorMessage ("Enter valid root directory.");
		} else if (ae.getActionCommand().equals("SUBMITSER")) {
			String preferredServer = inputText.getText();
			if (!preferredServer.trim().equals("")) { //appProperty not empty
				System.out.println("Preferred root directory = " + preferredServer);
				if (parentMain != null) {
					updatePreferredServer(preferredServer);
					parentMain.runReportViewer();
				} else if (parentViewerFrame != null) {
					System.out.println("Invoke parentViewerFrame methods");
					parentViewerFrame.dispose();
					updatePreferredServer(preferredServer);
					new Main().runReportViewer();
				}
				dispose();
			}
		} else if (ae.getActionCommand().startsWith("CANCEL")) {
			dispose();
		}
	}
}
