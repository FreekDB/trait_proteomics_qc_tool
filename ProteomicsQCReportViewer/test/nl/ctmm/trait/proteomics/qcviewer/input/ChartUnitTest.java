package nl.ctmm.trait.proteomics.qcviewer.input;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.StringTokenizer;

import nl.ctmm.trait.proteomics.qcviewer.utils.Utilities;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.data.xy.XYSeries;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for the <code>ReportUnit</code> class.
 *
 * @author <a href="mailto:freek.de.bruijn@nbic.nl">Freek de Bruijn</a>
 */
public class ChartUnitTest {
    private static final int REPORT_NUMBER = 6;

    private ChartUnit chartUnit1, chartUnit2;
    private XYSeries series; 

    /**
     * Initialize a <code>ChartUnit</code>.
     */
    @Before
    public void setUp() {
    	/*
    	 * public ChartUnit(final String msrunName, final int reportNum, final XYSeries series)
    	 */
    	String msrunName1 = "msrun1";
    	String msrunName2 = "msrun2";
    	String ticMatrixFileName = "QCReports\\ctmm\\2013\\Feb\\QE1_121127_OPL1016_DC_Mefopa_CSF_AA3\\QE1_121127_OPL1016_DC_Mefopa_CSF_AA3 _ticmatrix.csv";
    	File ticMatrixFile = new File(ticMatrixFileName);
    	series = readXYSeries(msrunName1, ticMatrixFile);
    	chartUnit1 = new ChartUnit(msrunName1, 1, series);
    	chartUnit2 = new ChartUnit(msrunName2, 2, null); //initialize with empty series 
    }

    /**
     * Test <code>getMaxTicIntensity</code> from <code>ChartUnit</code> class.
     */
    @SuppressWarnings("deprecation")
	@Test
    public void testgetMaxTicIntensity() {
    	double maxIntensity = 22950831906.0;
        assertEquals(0, chartUnit2.getMaxTicIntensity(), 0);
        assertEquals(maxIntensity, chartUnit1.getMaxTicIntensity(), 0);
    }

    /**
     * Test <code>setGraphDataSeries</code> from <code>ChartUnit</code> class.
     */
    @Test
    public void testSetGraphDataSeries() {
    	double maxIntensity = 22950831906.0;
        chartUnit2.setGraphDataSeries(series);
        assertEquals(maxIntensity, chartUnit2.getMaxTicIntensity(), 0);
    }

    /**
     * Test <code>getTicChart</code> and <code>getDomainAxis</code> from <code>ChartUnit</code> class.
     */
    @Test
    public void testGetTicChartGetDomainAxis() {
        assertEquals("Expecting JFreeChart class.", JFreeChart.class, chartUnit1.getTicChart());
        assertEquals("Expecting JFreeChart class.", JFreeChart.class, chartUnit2.getTicChart());
        assertEquals("Expecting NumberAxis class.", NumberAxis.class, chartUnit1.getDomainAxis());
        assertEquals("Expecting NumberAxis class.", NumberAxis.class, chartUnit2.getDomainAxis());
    }
    
    /**
     * Create XYSeries by reading TIC matrix file that contains X & Y axis values representing TIC graph
     * @param msrunName
     * @param ticMatrixFile
     * @return XYSeries
     */
    private XYSeries readXYSeries(final String msrunName, final File ticMatrixFile) {
        BufferedReader br = null;
    	try {
    		br = new BufferedReader(new FileReader(ticMatrixFile));
    	} catch (FileNotFoundException e) {
    		e.printStackTrace();
    	}
        String line;
        XYSeries series = new XYSeries(msrunName);
        try {
            br.readLine(); //skip first line
    		while ((line = br.readLine()) != null) {
    		    StringTokenizer st = new StringTokenizer(line, ",");
    		    // The first token is the x value.
    		    String xValue = st.nextToken();
    		    // The last token is the y value.
    		    String yValue = st.nextToken();
    		    float x = Float.parseFloat(xValue)/60;
    		    float y = Float.parseFloat(yValue);
    		    series.add(x, y);
    		    //System.out.println("xValue = " + xValue + " x = " + x);
    		}
    	    br.close();
    	} catch (NumberFormatException | IOException e) {
    		e.printStackTrace();
    	}
        return series;
    }
    
}
