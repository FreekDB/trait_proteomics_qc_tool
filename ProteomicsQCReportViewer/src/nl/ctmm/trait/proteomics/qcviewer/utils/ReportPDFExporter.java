package nl.ctmm.trait.proteomics.qcviewer.utils;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import nl.ctmm.trait.proteomics.qcviewer.input.ReportUnit;

import org.jfree.chart.ChartRenderingInfo;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;

import com.itextpdf.text.BadElementException;
import com.itextpdf.text.Chapter;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

/**
 * This class exports report unit data in PDF format. 
 *
 * @author <a href="mailto:pravin.pawar@nbic.nl">Pravin Pawar</a>
 * @author <a href="mailto:freek.de.bruijn@nbic.nl">Freek de Bruijn</a>
 */
@SuppressWarnings("unused")
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
     * Error message to be shown in case exception occurs while exporting reports in PDF format.  
     */
    private static final String PDF_EXPORT_EXCEPTION_MESSAGE = "Failed exporting report unit %s "
            + "to PDF format. (Multiple) exceptions occured.";

    /**
     * Page margin of the PDF document.  
     */
    private static final int PDF_PAGE_MARGIN = 50; 
    
    /**
     * Width of the TIC chart image.  
     */
    private static final int CHART_IMAGE_WIDTH = 500; 
    
    /**
     * Height of the TIC chart image.  
     */
    private static final int CHART_IMAGE_HEIGHT = 200; 
    
    /**
     * Spacing for the metrics values table in the PDF document. 
     */
    private static final int TABLE_SPACING = 25; 
    
    /**
     * Spacing for paragraphs in the PDF document. 
     */
    private static final int PDF_PARAGRAPH_SPACING = 25; 
    
    /**
     * Default private constructor.
     */
    private ReportPDFExporter() {
        
    }
    
    /**
     * Export a report to a pdf file.
     * @param allMetricsMap map of all QC metrics - keys and description. 
     * @param reportUnit the report unit to be exported in PDF format.
     * @param preferredPDFDirectory the directory the pdf document should be exported to.
     * @return true if document is successfully created - otherwise return false. 
     */
    public static boolean exportReportUnitInPDFFormat(final Map<String, String> allMetricsMap, 
            final ReportUnit reportUnit, final String preferredPDFDirectory) {
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
        final Document document = new Document(PageSize.A4, PDF_PAGE_MARGIN, PDF_PAGE_MARGIN, PDF_PAGE_MARGIN, PDF_PAGE_MARGIN);
        final String pdfFileName = preferredPDFDirectory + "\\" + reportUnit.getMsrunName() + FILE_TYPE_EXTENSION;
        try {
          //Creation of PdfWriter object
            PdfWriter writer;
            writer = PdfWriter.getInstance(document, new FileOutputStream(pdfFileName));
            document.open();
            //Creation of chapter object
            final Paragraph documentTitle = new Paragraph(String.format(PDF_DOCUMENT_TITLE, reportUnit.getMsrunName()), 
                                        Constants.PDF_TITLE_FONT);
            final Chapter chapter1 = new Chapter(documentTitle, 1);
            chapter1.setNumberDepth(0);
            //Creation of TIC graph section object
            final String graphTitle = String.format(TIC_GRAPH_SECTION_TITLE, reportUnit.getMsrunName());
            final Paragraph ticGraphSection = new Paragraph(graphTitle, Constants.PDF_SECTION_FONT);
            ticGraphSection.setSpacingBefore(PDF_PARAGRAPH_SPACING);
            ticGraphSection.add(Chunk.NEWLINE);
            ticGraphSection.add(Chunk.NEWLINE);
            //Insert TIC Graph in ticGraphSection.
            ticGraphSection.add(createTICChartImage(writer, reportUnit));
            chapter1.addSection(ticGraphSection);
            final String metricsTitle = String.format(METRICS_VALUES_SECTION_TITLE, reportUnit.getMsrunName());
            final Paragraph metricsValuesSection = new Paragraph(metricsTitle, Constants.PDF_SECTION_FONT);
            metricsValuesSection.setSpacingBefore(PDF_PARAGRAPH_SPACING);
            //Reference: http://www.java-connect.com/itext/add-table-in-PDF-document-using-java-iText-library.html
            //TODO: Insert metrics values table in metricsValuesSection
            metricsValuesSection.add(createMetricsValuesTable(allMetricsMap, reportUnit)); 
            chapter1.addSection(metricsValuesSection);
            //Addition of a chapter to the main document
            document.add(chapter1);
            document.close();
            return true; 
        } catch (final FileNotFoundException | DocumentException e) {
            // TODO Explain when these exception can occur.
            /* FileNotFoundException will be thrown by the FileInputStream, FileOutputStream, and 
             * RandomAccessFile constructors when a file with the specified pathname does not exist. 
             * It will also be thrown by these constructors if the file does exist but for some reason 
             * is inaccessible, for example when an attempt is made to open a read-only file for writing.
             * DocumentException Signals that an error has occurred in a Document.
             */
            logger.log(Level.SEVERE, String.format(PDF_EXPORT_EXCEPTION_MESSAGE, reportUnit.getMsrunName()), e);
            return false;
        }
    }
    
    /**
     * Create image of the TIC Chart.
     * @param writer PDFWriter object for the PDF document. 
     * @param reportUnit Report unit for which to create TIC chart image. 
     * @return TIC Chart image.
     */
    private static Image createTICChartImage(final PdfWriter writer, final ReportUnit reportUnit) {
        /*Reference: http://vangjee.wordpress.com/2010/11/03/how-to-use-and-not-use-itext-and-jfreechart/
         * Apache License, Version 2.0
         */
        final JFreeChart ticChart = reportUnit.getChartUnit().getTicChart();
        Image chartImage = null;
        ByteArrayOutputStream byteArrayOutputStream = null;
        try {
            byteArrayOutputStream = new ByteArrayOutputStream();
            ChartUtilities.writeChartAsPNG(
                    byteArrayOutputStream, ticChart, CHART_IMAGE_WIDTH, CHART_IMAGE_HEIGHT, 
                    new ChartRenderingInfo());
            chartImage = Image.getInstance(byteArrayOutputStream.toByteArray());
            byteArrayOutputStream.close();
        } catch (final BadElementException | IOException e) {
            // TODO Explain when these exception can occur.
            /*
             * IOException occurs in case PDF file can not be created. 
             * e.g. PDF file with same name exist in the PDF directory and it is open. 
             * In this case, the PDF file can not be overwritten. 
             * BadElementException Signals an attempt to create an Element that hasn't got the right form. 
             */
            logger.log(Level.SEVERE, String.format(PDF_EXPORT_EXCEPTION_MESSAGE, reportUnit.getMsrunName()), e);
        } 
        return chartImage; 
    }

    /**
     * Create the metrics values table for given report unit. This table will be added to the PDF document.
     * @param allMetricsMap map of all QC metrics - keys and description.
     * @param reportUnit Report unit for which to create the metrics values table. 
     * @return PDF table containing metrics values of the report unit. 
     * @throws BadElementException
     */
    private static PdfPTable createMetricsValuesTable(final Map<String, String> allMetricsMap, final ReportUnit reportUnit) {
        /*
         * TODO: Column size, font size and spacing of the metrics value table 
         */
        // Create columns names.
        final String columnNames[] = {Constants.METRICS_ID_COLUMN_NAME, Constants.DESCRIPTION_COLUMN_NAME, Constants.VALUE_COLUMN_NAME};
        //Creation of table object
        final PdfPTable table = new PdfPTable(columnNames.length);
        table.setSpacingBefore(TABLE_SPACING);
        final PdfPCell c1 = new PdfPCell(new Phrase(columnNames[0]));
        table.addCell(c1);
        final PdfPCell c2 = new PdfPCell(new Phrase(columnNames[1]));
        table.addCell(c2);
        final PdfPCell c3 = new PdfPCell(new Phrase(columnNames[2]));
        table.addCell(c3);
        
        // Read metricsValues corresponding to reportUnit.
        final Map<String, String> metricsValues = reportUnit.getMetricsValues();
        for (final Map.Entry<String, String> metricsData : allMetricsMap.entrySet()) {
            final String[] row = new String[columnNames.length];
            row[0] = metricsData.getKey();
            row[1] = metricsData.getValue();
            row[2] = (metricsValues != null) ? metricsValues.get(metricsData.getKey()) : Constants.NOT_AVAILABLE_STRING;
            table.addCell(row[0]);
            table.addCell(row[1]);
            table.addCell(row[2]);
        }
        return table; 
    }
}
