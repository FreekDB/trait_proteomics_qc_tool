package nl.ctmm.trait.proteomics.qcviewer.gui;

import java.awt.Color;
import java.awt.Font;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.List;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYBarPainter;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

/**
 * The object of this class represents tic chart and corresponding plot, domainAxis, rangeAxis and 
 * renderer of single msreading.
 *
 * @author <a href="mailto:pravin.pawar@nbic.nl">Pravin Pawar</a>
 * @author <a href="mailto:freek.de.bruijn@nbic.nl">Freek de Bruijn</a>
 */

public class ChartUnit {
    private String chartName = "";
    private XYSeries graphDataSeries = null;
    private int reportNum; 
    private JFreeChart ticChart = null; 
    private NumberAxis domainAxis = null;
    private NumberAxis rangeAxis = null;
    private XYBarRenderer renderer = null;
    private XYPlot plot = null;
    private double maxIntensity = 0; 
    private String maxIntensityString = "N/A";
    private static final List<Color> GRAPH_COLORS = Arrays.asList(
           Color.BLUE, Color.DARK_GRAY, Color.GRAY, Color.MAGENTA, Color.ORANGE, 
           Color.PINK, Color.LIGHT_GRAY, Color.RED, Color.GREEN);
    
    /**
     * Constructor of the ChartUnit
     * @param msrunName The name of MS RAW file processed by the QC pipeline 
     * @param reportNum Number of report unit to which the ChartUnit belongs
     * @param series XYseries points for the TIC graph
     */
    public ChartUnit(final String msrunName, final int reportNum, final XYSeries series) {
        this.chartName = msrunName; 
        this.reportNum = reportNum;
        this.graphDataSeries = series;
        
        if (series != null) {
            maxIntensity = series.getMaxY();
            NumberFormat formatter = new DecimalFormat("0.0000E0");
            maxIntensityString = formatter.format(maxIntensity);
        }
        domainAxis = new NumberAxis(null);
        rangeAxis = new NumberAxis(null);
        renderer = new XYBarRenderer();
        //Sets the percentage amount by which the bars are trimmed
        renderer.setMargin(0.98); //Default renderer margin is 0.0
        renderer.setDrawBarOutline(false); 
        renderer.setShadowVisible(false);
        Color currentColor = GRAPH_COLORS.get(reportNum%GRAPH_COLORS.size());
        renderer.setSeriesPaint(0, currentColor);
        renderer.setGradientPaintTransformer(null);
        renderer.setSeriesOutlinePaint(0, currentColor);
        renderer.setFillPaint(currentColor);
        renderer.setPaint(currentColor);
        renderer.setBarPainter(new StandardXYBarPainter());
        renderer.setSeriesStroke(0, null);
        renderer.setBasePaint(Color.WHITE);
        XYBarRenderer.setDefaultShadowsVisible(false);
        //Add support for toolTip
        StandardXYToolTipGenerator toolTipGenerator = new StandardXYToolTipGenerator();
        renderer.setBaseToolTipGenerator(toolTipGenerator);
        renderer.setDrawBarOutline(false);
        XYSeriesCollection xyDataset = new XYSeriesCollection(series);
        //Prepare chart using plot - this is the best option to control domain and range axes
        plot = new XYPlot(xyDataset, domainAxis, rangeAxis, renderer);
        rangeAxis.setNumberFormatOverride(new DecimalFormat("0E00"));
        int style = Font.BOLD;
        Font font = new Font ("Garamond", style , 13);
        ticChart = new JFreeChart(msrunName + "     MaxIntensity = " + maxIntensityString, font, plot, false);
        // performance
        ticChart.setAntiAlias(false);
    }
    
    /**
     * Get max intensity of TIC graph
     * @return Max intensity of TIC graph
     */
    public double getMaxTicIntensity() {
        if (graphDataSeries == null) {
            return 0;
        } else return maxIntensity;
    }
    
    
    /**
     * Get max intensity of TIC graph in String format
     * @return Max intensity of TIC graph in String format
     */
    public String getMaxTicIntensityString() {
        return maxIntensityString;
    }
    
    /**
     * Set the value of parameter graphDataSeries
     * @param series
     */
    public void setGraphDataSeries(XYSeries series) {
        this.graphDataSeries = series;
    }
    
    /**
     * Get corresponding tiChart
     * @return ticChart
     */
    public JFreeChart getTicChart() {
        return ticChart;
    }
    
    /**
     * Get corresponding domainAxis
     * @return domainAxis
     */
    public NumberAxis getDomainAxis() {
        return domainAxis;
    }
}
