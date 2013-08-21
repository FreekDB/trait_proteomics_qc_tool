package nl.ctmm.trait.proteomics.qcviewer.utils;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.logging.Logger;

import javax.swing.JFileChooser;

import nl.ctmm.trait.proteomics.qcviewer.input.ReportUnit;

import org.apache.commons.io.FilenameUtils;

import com.itextpdf.text.Chapter;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;

/**
 * This class exports report unit data in PDF format. 
 *
 * @author <a href="mailto:pravin.pawar@nbic.nl">Pravin Pawar</a>
 * @author <a href="mailto:freek.de.bruijn@nbic.nl">Freek de Bruijn</a>
 */
public class ReportPDFExporter {

    
    /**
     * The logger for this class.
     */
    private static final Logger logger = Logger.getLogger(ReportPDFExporter.class.getName());

    /**
     * PDF File type extension.  
     */
    private static final String FILE_TYPE_EXTENSION = ".pdf";
    
    /**
     * Title of the PDF document.  
     */
    private static final String PDF_DOCUMENT_TITLE = "QC Pipeline Report for Msrun %s";
    
    /**
     * Title of the TIC graph section in PDF document.  
     */
    private static final String TIC_GRAPH_SECTION_TITLE = "TIC Graph for Msrun %s";

    /**
     * Title of the Metrics values section in PDF document.  
     */
    private static final String METRICS_VALUES_SECTION_TITLE = "QC Metrics Values for Msrun %s";

    /**
     * Default private constructor.
     */
    private ReportPDFExporter() {
        
    }
    
    /**
     * @param selectedReports List of report units to be exported in PDF format
     * @throws DocumentException
     * @throws IOException
     * @throws MalformedURLException
     */
    public static void exportReportUnitInPDFFormat(final ReportUnit selectedReport, final String preferredPDFDirectory) 
            throws DocumentException, MalformedURLException, IOException {
        /*
         * TODO:  
         * Display PDF directory chooser form. 
         * Obtain directory location to save the reports.
         * For every selected report unit, follow following steps:  
         * 1) Name the PDF file -> msrunname.pdf.
         * 2) Create PDF document.
         * 3) Add header to the PDF document.
         * 4) Obtain TIC Chart of the Report Unit.   
         * 5) Add TIC Chart to the PDF document.
         * 6) Obtain metrics listing and values table of the report unit. 
         * 7) Add metrics listing and values table to the PDF document. 
         * 8) Save PDF document to specified directory location. 
         * Notify user about saving PDF reports/handle error conditions. 
         */

        //Instantiation of document object
        final Document document = new Document(PageSize.A4, 50, 50, 50, 50);
        final String pdfFileName = preferredPDFDirectory + "\\" + selectedReport.getMsrunName() + FILE_TYPE_EXTENSION;
        //Creation of PdfWriter object
        PdfWriter.getInstance(document, new FileOutputStream(pdfFileName));
        document.open();
        //Creation of chapter object
        final Paragraph documentTitle = new Paragraph(String.format(PDF_DOCUMENT_TITLE, selectedReport.getMsrunName()), 
                                        Constants.PDF_TITLE_FONT);
        final Chapter chapter1 = new Chapter(documentTitle, 1);
        chapter1.setNumberDepth(0);
        //Creation of TIC graph section object
        final Paragraph ticGraphSection = new Paragraph(String.format(TIC_GRAPH_SECTION_TITLE, selectedReport.getMsrunName()),
                                        Constants.PDF_SECTION_FONT);
        //TODO: Insert TIC Graph in ticGraphSection
        //Reference: viralpatel.net/blogs/generate-pie-chart-bar-graph-in-pdf-using-itext-jfreechart/
        chapter1.addSection(ticGraphSection);
        final Paragraph metricsValuesSection = new Paragraph(String.format(METRICS_VALUES_SECTION_TITLE, selectedReport.getMsrunName()),
                                        Constants.PDF_SECTION_FONT);
        //TODO: Insert metrics values table in metricsValuesSection
        //Reference: http://www.java-connect.com/itext/add-table-in-PDF-document-using-java-iText-library.html
        chapter1.addSection(metricsValuesSection);
        //Addition of a chapter to the main document
        document.add(chapter1);
        document.close();
    }
    


}
