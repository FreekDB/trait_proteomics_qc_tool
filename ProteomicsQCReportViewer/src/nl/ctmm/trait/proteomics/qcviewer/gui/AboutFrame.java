package nl.ctmm.trait.proteomics.qcviewer.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import nl.ctmm.trait.proteomics.qcviewer.utils.Constants;
import nl.ctmm.trait.proteomics.qcviewer.utils.Utilities;

import org.jfree.ui.RefineryUtilities;

/**
 * The class for displaying About information of the Report Viewer.
 *
 * @author <a href="mailto:pravin.pawar@nbic.nl">Pravin Pawar</a>
 */

public class AboutFrame extends JFrame implements ActionListener {

    int style = Font.PLAIN;
    Font font = new Font ("Garamond", style , 11);
    
    /**
     * Constructor
     */
    public AboutFrame() {
        super ("About MSQC Report Viewer");
        JPanel mainPanel = new JPanel();
        mainPanel.setPreferredSize(new Dimension(500, 700));
        JTextArea descText = getDescriptionJTextArea();
        JScrollPane areaScrollPane = new JScrollPane(descText);
        areaScrollPane.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "Proteomics Quality Control"));
        areaScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        areaScrollPane.setHorizontalScrollBarPolicy(
                        JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        areaScrollPane.setPreferredSize(new Dimension(500, 200));
        areaScrollPane.setBackground(Color.WHITE);
        mainPanel.add(areaScrollPane, 0);
        JTextArea refsText = getReferencesTextArea();
        areaScrollPane = new JScrollPane(refsText);
        areaScrollPane.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "References"));
        areaScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        areaScrollPane.setHorizontalScrollBarPolicy(
                        JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        areaScrollPane.setPreferredSize(new Dimension(500, 150));
        areaScrollPane.setBackground(Color.WHITE);
        mainPanel.add(areaScrollPane, 1);
        JTextArea detailsText = getDetailsTextArea();
        areaScrollPane = new JScrollPane(detailsText);
        areaScrollPane.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "Download & Contact Details"));
        areaScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        areaScrollPane.setHorizontalScrollBarPolicy(
                        JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        areaScrollPane.setPreferredSize(new Dimension(500, 100));
        areaScrollPane.setBackground(Color.WHITE);
        mainPanel.add(areaScrollPane, 2);
        JLabel oplLabel = createAcknowledgementLabel("http://www.oncoproteomics.nl", Constants.PROPERTY_OPL_LOGO_FILE);
        JLabel nistLabel = createAcknowledgementLabel("http://www.nist.gov", Constants.PROPERTY_NIST_LOGO_FILE);
        JLabel ctmmLabel = createAcknowledgementLabel("http://www.ctmm.nl", Constants.PROPERTY_CTMM_LOGO_FILE);
        JLabel projectLabel = createAcknowledgementLabel("http://www.ctmm-trait.nl", Constants.PROPERTY_PROJECT_LOGO_FILE);
        JLabel nbicLabel = createAcknowledgementLabel("http://www.nbic.nl", Constants.PROPERTY_NBIC_LOGO_FILE);
        JPanel labelPanel = new JPanel();
        labelPanel.setLayout(new GridLayout(5, 1));
        labelPanel.add(oplLabel, 0);
        labelPanel.add(nistLabel, 1);
        labelPanel.add(ctmmLabel, 2);
        labelPanel.add(projectLabel, 3);
        labelPanel.add(nbicLabel, 4);
        labelPanel.setBackground(Color.WHITE);
        areaScrollPane = new JScrollPane(labelPanel);
        areaScrollPane.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "Acknowledgements"));
        areaScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        areaScrollPane.setHorizontalScrollBarPolicy(
                        JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        areaScrollPane.setPreferredSize(new Dimension(500, 200));
        areaScrollPane.setBackground(Color.WHITE);
        mainPanel.add(areaScrollPane, 3);
        JButton SUBMIT = new JButton("OK");
        SUBMIT.setSize(new Dimension(50, 30));
            SUBMIT.addActionListener(this);
            SUBMIT.setActionCommand("OK");
          mainPanel.add(SUBMIT);
        getContentPane().add(mainPanel);
        setBounds(0, 0, 520, 740);
        RefineryUtilities.centerFrameOnScreen(this);
    }
    
    /**
     * Get description text 
     * @return JTextArea Text area for the description text
     */
    private JTextArea getDescriptionJTextArea() {
        //Create a text area.
        JTextArea textArea = new JTextArea(
                "Proteomics deals with the large-scale study of proteins and has become a powerful technology in " + 
                "discovery of drug targets and biomarkers. Modern day proteomics is largely based on nano-liquid " +
                "chromatography coupled to high-resolution tandem mass spectrometry (nanoLC-MS/MS). However, the " + 
                "reproducibility of MS results is considered by many as insufficient. In part, inadequate quality " + 
                "control (QC) might be responsible for the lack of reproducibility [1].\n " + 
                "The importance of well-established QC has become a focal point in the field of proteomics [2]. The " + 
                "National Institute of Standards (NIST) has developed a QC pipeline for evaluating analytical " + 
                "performance of a common discovery-based proteomics platform by monitoring selected output from a " + 
                "LC-MS/MS system. This pipeline is known as NIST MSQC Pipeline [3]. The 46 QC metrics for monitoring " + 
                "chromatographic performance, electrospray source stability, MS1 and MS2 signals, dynamic sampling of " + 
                "ions for MS/MS, and peptide identification are described in [4]. Application of these metrics enables " + 
                "rational, quantitative quality assessment for proteomics and other LC-MS/MS analytical applications.\n " + 
                "The Netherlands Bioinformatics Institute (NBIC) has adapted the NIST MSQC Pipeline to suit the " + 
                "proteomics QC requirements of the OncoProteomics Laboratory (OPL). The adapted pipeline is named as " + 
                "NBIC-NIST MSQC Pipeline. Compared to the NIST MSQC Pipeline, the adapted version triggers pipeline " + 
                "processing on the availability of a new RAW data file in a fully automatic fashion– and creates a " + 
                "web-based QC report combining the NIST metrics with a number of custom metrics.  The custom-made MSQC " + 
                "Report Viewer aggregates and shows all available QC reports generated by the MSQC pipeline report " + 
                "directory. It includes functionality of sorting, filtering, zooming and comparing these reports as " + 
                "per the need of proteomics researchers." 
        );
        textArea.setFont(font);
        textArea.setEditable(false);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        return textArea;
    }
    
    /**
     * Get references text 
     * @return JTextArea Text area for the references text
     */
    private JTextArea getReferencesTextArea() {
        //Create a text area.
        JTextArea textArea = new JTextArea(
                "[1] Köcher, T., Pichler, P., Swart, R. and Mechtler, K., Quality control in LC-MS/MS, Wiley " + 
                "Proteomics, 11(6), pp. 1026-1030, Feb 2011.\n" + 
                "[2] PRIME-XS-Deliverable 13.1, Deliverable 13.1 - Suite of tools that allow automatic quality control " +
                "on the acquired data, 16 Feb 2012.\n" + 
                "[3] NIST LC-MS/MS Metrics for Monitoring Variability in Proteomics Experiments, National Institute of " +  
                "Standards and Technology - United States Department of Commerce, June 2011, available online: " +
                "http://peptide.nist.gov/metrics/, last accessed: 22 Jan 2013.\n" +  
                "[4] Rudnick P. A. et. al., Performance Metrics for Liquid Chromatography-Tandem Mass Spectrometry " + 
                "Systems in Proteomics Analyses, Molecular and Cell Proteomics, 9(2), pp. 225–241, Feb 2010."
        );
        textArea.setFont(font);
        textArea.setEditable(false);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        return textArea;
    }
    
    /**
     * Get details text 
     * @return JTextArea Text area for the project and contact details text
     */
    
    private JTextArea getDetailsTextArea() {
        //Create a text area.
        JTextArea textArea = new JTextArea(
                "Project homepage: https://trac.nbic.nl/svn/proteomics/ProteomicsQCReportViewer\n" + 
                "Github Repository: https://github.com/ppawar/TrialOfTraitProteomicsQCTool/\n" + 
                "Version: 1.6.5\n" +
                "Contact: Dr. Thang V. Pham (t.pham@vumc.nl)"
        );
        textArea.setFont(font);
        textArea.setEditable(false);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        return textArea;
    }
    
    /**
     * Create acknowledgement label
     * @param labelText Text of the acknowledgement label
     * @param iconPath Logo of the institution 
     * @return Label and logo in JLabel format
     */
    private JLabel createAcknowledgementLabel(String labelText, String iconPath) {
        //Add opllogo to control frame
        BufferedImage logo = null;
        try {
            logo = ImageIO.read(new File(iconPath));
        } catch (IOException e) {
            e.printStackTrace();
        }
        logo = Utilities.scaleImage(logo, 0, 100, 100);
        JLabel oplLabel = new JLabel(labelText, new ImageIcon(logo), JLabel.LEFT);
        return oplLabel;
    }
    
    /**
     * Event handler for user actions such as pressing the OK button
     */
    @Override
    public void actionPerformed(ActionEvent ae) {
        if (ae.getActionCommand().equals("OK")) {
            dispose();
        }
    }

}
