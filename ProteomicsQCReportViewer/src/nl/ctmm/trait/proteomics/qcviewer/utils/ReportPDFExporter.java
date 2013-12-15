package nl.ctmm.trait.proteomics.qcviewer.utils;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import nl.ctmm.trait.proteomics.qcviewer.input.ReportUnit;

import org.jfree.chart.ChartRenderingInfo;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.title.TextTitle;

import com.itextpdf.text.BadElementException;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chapter;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;

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
    private static final String PDF_PAGE_TITLE = "QC Pipeline Report for Msrun %s Created on: %s";

    /**
     * Title of the TIC graph section in PDF document.
     */
    private static final String TIC_GRAPH_SECTION_TITLE = "TIC Graph for Msrun %s";

    /**
     * Title of the TIC chart.
     */
    private static final String TIC_GRAPH_TITLE = "%s    MaxIntensity = %s";

    /**
     * Title of the Metrics values section in PDF document.
     */
    private static final String METRICS_VALUES_SECTION_TITLE = "QC Metrics Values for Msrun %s";

    /**
     * Error message to be shown in case exception occurs while exporting reports in PDF format.
     */
    private static final String PDF_EXPORT_EXCEPTION_MESSAGE = "Failed exporting report units "
                                                               + "to PDF format. (Multiple) exceptions occurred.";

    /**
     * Error message to be written to the logger in case exception occurs while preparing metrics values table.
     */
    private static final String PDF_TABLE_EXCEPTION_MESSAGE = "DocumentException occurred while creating "
                                                              + "table for report unit %s.";

    /**
     * Page margin of the PDF document.
     */
    private static final int PDF_PAGE_MARGIN = 30;

    /**
     * Width of the TIC chart image.
     */
    private static final int CHART_IMAGE_WIDTH = 750;

    /**
     * Height of the TIC chart image.
     */
    private static final int CHART_IMAGE_HEIGHT = 150;

    /**
     * Spacing before the metrics values table in the PDF document.
     */
    private static final int TABLE_SPACING = 5;

    /**
     * Spacing for paragraphs in the PDF document.
     */
    private static final int PDF_PARAGRAPH_SPACING = 10;

    /**
     * Width of columns in metrics values table.
     */
    private static final float[] COLUMN_WIDTHS = new float[]{80, 210, 80, 80, 210, 80};

    /**
     * Total number of columns in metrics values table.
     */
    private static final int TOTAL_COLUMNS = 6;

    /**
     * The date format for adding creation date/time to the PDF document.
     */
    private static final DateFormat CREATION_DATE_TIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    /**
     * Error message to be written to the logger in case exception occurs while preparing TIC Graph.
     */
    private static final String CLONE_EXCEPTION_MESSAGE = "Clone not supported exception occurred while preparing TIC"
                                                          + " graph for %s.";

    /**
     * Default private constructor.
     */
    private ReportPDFExporter() {
    }

    /**
     * Export a report to a pdf file.
     *
     * @param allMetricsMap         map of all QC metrics - keys and description.
     * @param selectedReports       the report units to be exported in PDF format.
     * @param preferredPDFDirectory the directory the pdf document should be exported to.
     * @return Path of the created PDF document if it is successfully created - otherwise return empty string.
     */
    public static String exportReportUnitInPDFFormat(final Map<String, String> allMetricsMap,
                                                     final ArrayList<ReportUnit> selectedReports,
                                                     final String preferredPDFDirectory) {
        //Obtain current timestamp. 
        final java.util.Date date = new java.util.Date();
        final String timestampString = CREATION_DATE_TIME_FORMAT.format(date);
        //Replace all occurrences of special character ':' from time stamp since ':' is not allowed in filename. 
        final String filenameTimestamp = timestampString.replace(':', '-');
        //Instantiation of document object - landscape format using the rotate() method
        final Document document = new Document(PageSize.A4.rotate(), PDF_PAGE_MARGIN, PDF_PAGE_MARGIN,
                                               PDF_PAGE_MARGIN, PDF_PAGE_MARGIN);
        final String pdfFileName = preferredPDFDirectory + "\\QCReports-" + filenameTimestamp + FILE_TYPE_EXTENSION;
        try {
            //Creation of PdfWriter object
//            PdfWriter writer;
//            writer = PdfWriter.getInstance(document, new FileOutputStream(pdfFileName));
            document.open();
            for (ReportUnit reportUnit : selectedReports) {
                //New page for each report. 
                document.newPage();
                //Creation of chapter object
                final Paragraph pageTitle = new Paragraph(String.format(PDF_PAGE_TITLE, reportUnit.getMsrunName(),
                                                                        timestampString), Constants.PDF_TITLE_FONT);
                pageTitle.setAlignment(Element.ALIGN_CENTER);
                final Chapter chapter1 = new Chapter(pageTitle, 1);
                chapter1.setNumberDepth(0);
                //Creation of TIC graph section object
                final String graphTitle = String.format(TIC_GRAPH_SECTION_TITLE, reportUnit.getMsrunName());
                final Paragraph ticGraphSection = new Paragraph(graphTitle, Constants.PDF_SECTION_FONT);
                ticGraphSection.setSpacingBefore(PDF_PARAGRAPH_SPACING);
                ticGraphSection.add(Chunk.NEWLINE);
                //Insert TIC Graph in ticGraphSection.
                ticGraphSection.add(createTICChartImage(reportUnit));
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
            }
            document.close();
            return pdfFileName;
        } catch (final DocumentException e) {
            // TODO Explain when these exception can occur.
            /* FileNotFoundException will be thrown by the FileInputStream, FileOutputStream, and 
             * RandomAccessFile constructors when a file with the specified path name does not exist.
             * It will also be thrown by these constructors if the file does exist but for some reason 
             * is inaccessible, for example when an attempt is made to open a read-only file for writing.
             * DocumentException Signals that an error has occurred in a Document.
             */
            logger.log(Level.SEVERE, PDF_EXPORT_EXCEPTION_MESSAGE, e);
            return "";
        }
    }

    /**
     * Create image of the TIC Chart.
     *
     * @param reportUnit Report unit for which to create TIC chart image.
     * @return TIC Chart image.
     */
    private static Image createTICChartImage(final ReportUnit reportUnit) {
        /*Reference: http://vangjee.wordpress.com/2010/11/03/how-to-use-and-not-use-itext-and-jfreechart/
         * Apache License, Version 2.0
         */
        JFreeChart ticChart = null;
        try {
            ticChart = (JFreeChart) reportUnit.getChartUnit().getTicChart().clone();
        } catch (final CloneNotSupportedException e) {
            logger.log(Level.SEVERE, String.format(CLONE_EXCEPTION_MESSAGE, reportUnit.getMsrunName()), e);
        }
        final String titleString = String.format(TIC_GRAPH_TITLE, reportUnit.getMsrunName(),
                                                 reportUnit.getChartUnit().getMaxTicIntensityString());
        final TextTitle newTitle = new TextTitle(titleString, Constants.PDF_CHART_TITLE_FONT);
        newTitle.setPaint(Color.red);
        if (ticChart != null) {
            ticChart.setTitle(newTitle);
        }
        Image chartImage = null;
        try {
            final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            if (ticChart != null) {
                ChartUtilities.writeChartAsPNG(
                    byteArrayOutputStream, ticChart, CHART_IMAGE_WIDTH, CHART_IMAGE_HEIGHT,
                    new ChartRenderingInfo());
            }
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
            logger.log(Level.SEVERE, PDF_EXPORT_EXCEPTION_MESSAGE, e);
        }
        if (chartImage != null) {
            chartImage.setAlignment(Element.ALIGN_CENTER);
        }
        return chartImage;
    }

    /**
     * Create the metrics values table for given report unit. This table will be added to the PDF document.
     *
     * @param allMetricsMap map of all QC metrics - keys and description.
     * @param reportUnit    Report unit for which to create the metrics values table.
     * @return PDF table containing metrics values of the report unit.
     */
    private static PdfPTable createMetricsValuesTable(final Map<String, String> allMetricsMap,
                                                      final ReportUnit reportUnit) {
        /*
         * TODO: Column size, font size and spacing of the metrics value table. 
         */
        // Create columns names.
        final String columnNames[] = {Constants.METRICS_ID_COLUMN_NAME, Constants.DESCRIPTION_COLUMN_NAME,
            Constants.VALUE_COLUMN_NAME, Constants.METRICS_ID_COLUMN_NAME, Constants.DESCRIPTION_COLUMN_NAME,
            Constants.VALUE_COLUMN_NAME, };
        //Creation of table object.
        final PdfPTable table = new PdfPTable(columnNames.length);
        try {
            table.setSpacingBefore(TABLE_SPACING);
            //Set the table width. 
            table.setTotalWidth(COLUMN_WIDTHS);
            table.setLockedWidth(true);
            //Add table header. 
            for (int i = 0; i < TOTAL_COLUMNS; ++i) {
                final PdfPCell headerCell = new PdfPCell(new Phrase(columnNames[i], Constants.TABLE_HEADER_FONT));
                headerCell.setBackgroundColor(BaseColor.RED);
                headerCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(headerCell);
            }
            //Read metricsValues corresponding to reportUnit.
            final Map<String, String> metricsValues = reportUnit.getMetricsValues();
            //TODO: Split allMetricsMap in two parts such that sorted rows are properly added in the table.
            //Get all keys  
            final Object[] keyArray = allMetricsMap.keySet().toArray();
            //get all values
            final Object[] valueArray = allMetricsMap.values().toArray();
            //Calculate halfSize
            final int halfSize = keyArray.length / 2;
            for (int i = 0; i < halfSize; ++i) {
                addMetric(keyArray[i].toString(), valueArray[i].toString(), metricsValues, table);
                addMetric(keyArray[i + halfSize].toString(), valueArray[i + halfSize].toString(), metricsValues, table);
            }
        } catch (final DocumentException e) {
            //DocumentException signals that an error has occurred in a Document.
            logger.log(Level.SEVERE, String.format(PDF_TABLE_EXCEPTION_MESSAGE, reportUnit.getMsrunName()), e);
        }
        return table;
    }

    /**
     * Add a metric to the pdf table.
     *
     * @param key           the metric key.
     * @param description   the metric description.
     * @param metricsValues the metric values.
     * @param table         the pdf table.
     */
    private static void addMetric(final String key, final String description, final Map<String, String> metricsValues,
                                  final PdfPTable table) {
        final String value = (metricsValues != null) ? metricsValues.get(key) : Constants.NOT_AVAILABLE_STRING;
        // Populate content in table cell.
        table.addCell(new Phrase(key, Constants.TABLE_CONTENT_FONT));
        table.addCell(new Phrase(description, Constants.TABLE_CONTENT_FONT));
        final PdfPCell valueCell = new PdfPCell(new Phrase(value, Constants.TABLE_CONTENT_FONT));
        valueCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(valueCell);
    }
}
