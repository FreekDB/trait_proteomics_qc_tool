package nl.ctmm.trait.proteomics.qcviewer.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
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
 * The class for displaying information about the Report Viewer.
 *
 * @author <a href="mailto:pravin.pawar@nbic.nl">Pravin Pawar</a>
 * @author <a href="mailto:freek.de.bruijn@nbic.nl">Freek de Bruijn</a>
 */
public class AboutFrame extends JFrame implements ActionListener {
    /**
     * The logger for this class.
     */
    private static final Logger logger = Logger.getLogger(AboutFrame.class.getName());

    /**
     * The version number for (de)serialization of this class (UID: universal identifier).
     */
    private static final long serialVersionUID = 1;

    /**
     * The description for the application.
     */
    private static final String DESCRIPTION =
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
            "The Netherlands Bioinformatics Centre (NBIC) has adapted the NIST MSQC Pipeline to suit the " +
            "proteomics QC requirements of the OncoProteomics Laboratory (OPL). The adapted pipeline is named as " +
            "NBIC-NIST MSQC Pipeline. Compared to the NIST MSQC Pipeline, the adapted version triggers pipeline " +
            "processing on the availability of a new RAW data file in a fully automatic fashion� and creates a " +
            "web-based QC report combining the NIST metrics with a number of custom metrics.  The custom-made MSQC " +
            "Report Viewer aggregates and shows all available QC reports generated by the MSQC pipeline report " +
            "directory. It includes functionality of sorting, filtering, zooming and comparing these reports as " +
            "per the need of proteomics researchers.";

    /**
     * The references for the application.
     */
    private static final String REFERENCES =
            "[1] K�cher, T., Pichler, P., Swart, R. and Mechtler, K., Quality control in LC-MS/MS, Wiley " +
            "Proteomics, 11(6), pp. 1026-1030, Feb 2011.\n" +
            "[2] PRIME-XS-Deliverable 13.1, Deliverable 13.1 - Suite of tools that allow automatic quality control " +
            "on the acquired data, 16 Feb 2012.\n" +
            "[3] NIST LC-MS/MS Metrics for Monitoring Variability in Proteomics Experiments, National Institute of " +
            "Standards and Technology - United States Department of Commerce, June 2011, available online: " +
            "http://peptide.nist.gov/metrics/, last accessed: 22 Jan 2013.\n" +
            "[4] Rudnick P. A. et. al., Performance Metrics for Liquid Chromatography-Tandem Mass Spectrometry " +
            "Systems in Proteomics Analyses, Molecular and Cell Proteomics, 9(2), pp. 225�241, Feb 2010.";

    /**
     * The information for getting more details about the application.
     */
    private static final String DETAILS =
            "GitHub repository: https://github.com/CTMM-TraIT/trait_proteomics_qc_tool\n" +
            "Version: " + Constants.APPLICATION_VERSION + "\n" +
            "Contact: Dr. Thang V. Pham (t.pham@vumc.nl)";

    /**
     * Constructor of the AboutFrame.
     */
    public AboutFrame() {
        super("About " + Constants.APPLICATION_NAME);
        // Create the main panel with the contents of this frame.
        final JPanel mainPanel = new JPanel();
        mainPanel.setPreferredSize(new Dimension(500, 700));
        mainPanel.add(createAboutScrollPane(createAboutTextArea(DESCRIPTION), "Proteomics Quality Control", 200));
        mainPanel.add(createAboutScrollPane(createAboutTextArea(REFERENCES), "References", 150));
        mainPanel.add(createAboutScrollPane(createAboutTextArea(DETAILS), "Download & Contact Details", 100));
        mainPanel.add(createAboutScrollPane(createAcknowledgementsPanel(), "Acknowledgements", 200));
        final JButton okButton = new JButton("OK");
        okButton.setSize(new Dimension(50, 30));
        okButton.addActionListener(this);
        okButton.setActionCommand("OK");
        mainPanel.add(okButton);
        // Add the main panel to the frame and resize it.
        getContentPane().add(mainPanel);
        setBounds(0, 0, 520, 740);
        RefineryUtilities.centerFrameOnScreen(this);
    }

    /**
     * Create a scroll pane with a view component inside of it.
     *
     * @param view the view to put inside the scroll pane.
     * @param title the title of the scroll pane.
     * @param height the height of the scroll pane.
     * @return the new scroll pane.
     */
    private JScrollPane createAboutScrollPane(final Component view, final String title, final int height) {
        final JScrollPane descriptionScrollPane = new JScrollPane(view);
        descriptionScrollPane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), title));
        descriptionScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        descriptionScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        descriptionScrollPane.setPreferredSize(new Dimension(500, height));
        descriptionScrollPane.setBackground(Color.WHITE);
        return descriptionScrollPane;
    }

    /**
     * Create a text area with the specified text inside of it.
     *
     * @return the text area with the text.
     */
    private JTextArea createAboutTextArea(final String text) {
        final JTextArea textArea = new JTextArea(text);
        textArea.setFont(Constants.PLAIN_FONT);
        textArea.setEditable(false);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        return textArea;
    }

    /**
     * Create the panel with the acknowledgements: logo's and links for all the partners involved in this project.
     *
     * @return the panel with the acknowledgements.
     */
    private JPanel createAcknowledgementsPanel() {
        final String[][] acknowledgementsData = {
                {"http://www.oncoproteomics.nl", Constants.PROPERTY_OPL_LOGO_FILE},
                {"http://www.nist.gov", Constants.PROPERTY_NIST_LOGO_FILE},
                {"http://www.ctmm.nl", Constants.PROPERTY_CTMM_LOGO_FILE},
                {"http://www.ctmm-trait.nl", Constants.PROPERTY_PROJECT_LOGO_FILE},
                {"http://www.nbic.nl", Constants.PROPERTY_NBIC_LOGO_FILE}
        };
        final JPanel acknowledgementsPanel = new JPanel();
        acknowledgementsPanel.setLayout(new GridLayout(5, 1));
        acknowledgementsPanel.setBackground(Color.WHITE);
        for (final String[] acknowledgementData : acknowledgementsData) {
            acknowledgementsPanel.add(createAcknowledgementLabel(acknowledgementData[0], acknowledgementData[1]));
        }
        return acknowledgementsPanel;
    }

    /**
     * Create acknowledgement label with logo and link.
     *
     * @param link text of the acknowledgement label.
     * @param logoPath path to the logo.
     * @return a new label with logo and link.
     */
    private JLabel createAcknowledgementLabel(final String link, final String logoPath) {
        final JLabel label = new JLabel(link, JLabel.LEFT);
        try {
            label.setIcon(new ImageIcon(Utilities.scaleImage(ImageIO.read(new File(logoPath)), 0, 100, 100)));
        } catch (final IOException e) {
            logger.log(Level.WARNING, "Logo in file " + logoPath + " not found.", e);
        }
        return label;
    }
    
    /**
     * Event handler for user actions such as pressing the OK button.
     */
    @Override
    public void actionPerformed(final ActionEvent actionEvent) {
        if (actionEvent.getActionCommand().equals("OK")) {
            dispose();
        }
    }
}
