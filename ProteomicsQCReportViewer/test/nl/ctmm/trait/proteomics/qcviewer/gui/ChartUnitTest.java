package nl.ctmm.trait.proteomics.qcviewer.gui;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.data.xy.XYSeries;
import org.junit.Before;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.StringTokenizer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Unit tests for the <code>ChartUnit</code> class.
 *
 * @author <a href="mailto:pravin.pawar@nbic.nl">Pravin Pawar</a>
 * @author <a href="mailto:freek.de.bruijn@nbic.nl">Freek de Bruijn</a>
 */
public class ChartUnitTest {
    private static final String TIC_DIRECTORY = "QCReports\\2013\\Jun\\";

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
        final String ticMatrixFileName1 = TIC_DIRECTORY + "QE2_130603_OPL0000_mq_04_Arnold_04\\";
    	final File ticDirectory1 = new File(ticMatrixFileName1 + "QE2_130603_OPL0000_mq_04_Arnold_04_ticmatrix.csv");
    	series1 = readXYSeries(msrunName1, ticDirectory1);
        String ticMatrixFileName2 = "QCReports\\2013\\Jun\\QE2_130603_OPL0000_mq_05_Arnold_05\\QE2_130603_OPL0000_mq_05_Arnold_05_ticmatrix.csv";
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
    public void testGetMaxTicIntensity() {
    	double maxIntensity1 = 22950831906.0;
    	double maxIntensity2 = 12888332635.0;
        assertEquals(0, chartUnit2.getMaxTicIntensity(), 0);
        assertEquals(maxIntensity1, chartUnit1.getMaxTicIntensity(), 0);
        assertEquals(maxIntensity2, chartUnit3.getMaxTicIntensity(), 0);
    }

    /**
     * Test <code>setGraphDataSeries</code> from <code>ChartUnit</code> class.
     */
    @Test
    public void testSetGraphDataSeries() {
    	double maxIntensity = 22950831906.0;
        chartUnit2.setGraphDataSeries(series1);
        assertEquals(maxIntensity, chartUnit2.getMaxTicIntensity(), 0);
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
        final XYSeries xySeries = new XYSeries(msrunName);
        try {
            final BufferedReader bufferedReader = new BufferedReader(new FileReader(ticMatrixFile));
            bufferedReader.readLine(); //skip first line
            String line;
    		while ((line = bufferedReader.readLine()) != null) {
    		    final StringTokenizer lineTokenizer = new StringTokenizer(line, ",");
    		    // The first token is the x value.
    		    final float x = Float.parseFloat(lineTokenizer.nextToken()) / 60;
                // The second token is the y value.
                final float y = Float.parseFloat(lineTokenizer.nextToken());
    		    xySeries.add(x, y);
    		}
    	    bufferedReader.close();
    	} catch (NumberFormatException | IOException e) {
            fail(e.getMessage());
    		e.printStackTrace();
    	}
        return xySeries;
    }
}
