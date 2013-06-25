package nl.ctmm.trait.proteomics.qcviewer.gui;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.StringTokenizer;

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

    private ChartUnit chartUnit1, chartUnit2, chartUnit3;
    private XYSeries series1, series2; 

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
    	String msrunName3 = "msrun3";
    	String ticMatrixFileName1 = "QCReports\\2013\\Jun\\data01QE2_130409_OPL1013_CvA_Bonemarrow_TiOx_H1\\data01QE2_130409_OPL1013_CvA_Bonemarrow_TiOx_H1_ticmatrix.csv";
    	File ticMatrixFile1 = new File(ticMatrixFileName1);
    	series1 = readXYSeries(msrunName1, ticMatrixFile1);
    	String ticMatrixFileName2 = "QCReports\\2013\\Jun\\QE2_130124_OPL0000_jurkat2ug_01\\QE2_130124_OPL0000_jurkat2ug_01_ticmatrix.csv";
    	File ticMatrixFile2 = new File(ticMatrixFileName2);
    	series2 = readXYSeries(msrunName1, ticMatrixFile2);
    	chartUnit1 = new ChartUnit(msrunName1, 1, series1);
    	chartUnit2 = new ChartUnit(msrunName2, 2, null); //initialize with empty series
    	chartUnit3 = new ChartUnit(msrunName3, 3, series2);
    }

    /**
     * Test <code>getMaxTicIntensity</code> from <code>ChartUnit</code> class.
     */
    @SuppressWarnings("deprecation")
	@Test
    public void testgetMaxTicIntensity() {
    	double maxIntensity1 = 8.98197E9;
    	double maxIntensity2 = 1.03135E10;
        System.out.println("maxIntensity1 = " + maxIntensity1 + " Obtained = " + chartUnit1.getMaxTicIntensity());
        System.out.println("maxIntensity2 = " + maxIntensity2 + " Obtained = " + chartUnit3.getMaxTicIntensity());
        assertEquals(0, chartUnit2.getMaxTicIntensity(), 0);
        assertEquals(maxIntensity1, chartUnit1.getMaxTicIntensity(), 10000);
        assertEquals(maxIntensity2, chartUnit3.getMaxTicIntensity(), 10000);
    }

    /**
     * Test <code>setGraphDataSeries</code> from <code>ChartUnit</code> class.
     */
    @Test
    public void testSetGraphDataSeries() {
    	double maxIntensity = 8.98197E9;
        chartUnit2.setGraphDataSeries(series1);
        assertEquals(maxIntensity, chartUnit2.getMaxTicIntensity(), 10000);
    }

    /**
     * Test <code>getTicChart</code> and <code>getDomainAxis</code> from <code>ChartUnit</code> class.
     */
    @Test
    public void testGetTicChartGetDomainAxis() {
        assertEquals("Expecting JFreeChart class.", JFreeChart.class, chartUnit1.getTicChart().getClass());
        assertEquals("Expecting JFreeChart class.", JFreeChart.class, chartUnit2.getTicChart().getClass());
        assertEquals("Expecting NumberAxis class.", NumberAxis.class, chartUnit1.getDomainAxis().getClass());
        assertEquals("Expecting NumberAxis class.", NumberAxis.class, chartUnit2.getDomainAxis().getClass());
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
