package nl.ctmm.trait.proteomics.qcviewer.gui;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.data.xy.XYSeries;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Unit tests for the <code>ChartUnit</code> class.
 *
 * @author <a href="mailto:pravin.pawar@nbic.nl">Pravin Pawar</a>
 * @author <a href="mailto:freek.de.bruijn@nbic.nl">Freek de Bruijn</a>
 */
public class ChartUnitTest {
    /**
     * Base directory for the TIC files.
     */
    private static final String TIC_DIRECTORY = "QCReports\\2013\\Jul\\";

    private ChartUnit chartUnit1, chartUnit2, chartUnit3;
    private XYSeries xySeries;

    /**
     * Initialize an <code>XYSeries</code> object and three <code>ChartUnit</code> objects.
     */
    @Before
    public void setUp() {
    	/*
    	 * public ChartUnit(final String msrunName, final int reportNum, final XYSeries series)
    	 */
    	final String msrunName1 = "msrun1";
    	final String msrunName2 = "msrun2";
    	final String msrunName3 = "msrun3";
    	final String directory1 = TIC_DIRECTORY + "QE1_130108_OPL1005_YL_lysRIVM_NKI_BRCA_H1\\QE1_130108_OPL1005_YL_lysRIVM_NKI_BRCA_H1_ticmatrix.csv";
    	File ticMatrixFile1 = new File(directory1);
    	xySeries = readXYSeries(msrunName1, ticMatrixFile1);
    	final String directory2 = TIC_DIRECTORY + "QE2_120822_OPL0000_jurkat2ug_02\\QE2_120822_OPL0000_jurkat2ug_02_ticmatrix.csv";
    	File ticMatrixFile2 = new File(directory2);
    	chartUnit1 = new ChartUnit(msrunName1, 1, xySeries);
    	chartUnit2 = new ChartUnit(msrunName2, 2, null); //initialize with empty series
    	chartUnit3 = new ChartUnit(msrunName3, 3, readXYSeries(msrunName1, ticMatrixFile2));
    }

    /**
     * Test the <code>getMaxTicIntensity</code> method.
     */
    
	@Test
    public void testgetMaxTicIntensity() {
    	double maxIntensity1 = 5.99102E9;
    	double maxIntensity2 = 0.0;
    	double maxIntensity3 = 2.77233E10;
        assertEquals(0, chartUnit2.getMaxTicIntensity(), 0);
        assertEquals(maxIntensity1, chartUnit1.getMaxTicIntensity(), 10);
        assertEquals(maxIntensity2, chartUnit2.getMaxTicIntensity(), 0);
        assertEquals(maxIntensity3, chartUnit3.getMaxTicIntensity(), 10);
    }

    /**
     * Test the <code>setGraphDataSeries</code> method.
     * This test currently fails: we need to decide whether setGraphDataSeries should set maxIntensity.
     */
    @Test
    @Ignore
    public void testSetGraphDataSeries() {
    	double maxIntensity = 5.99102E9;
        chartUnit2.setGraphDataSeries(xySeries);
        assertEquals(maxIntensity, chartUnit2.getMaxTicIntensity(), 10);
    }

    /**
     * Test the <code>getTicChart</code> and <code>getDomainAxis</code> methods.
     */
    @Test
    public void testGetTicChartGetDomainAxis() {
        assertEquals("Expecting JFreeChart class.", JFreeChart.class, chartUnit1.getTicChart().getClass());
        assertEquals("Expecting JFreeChart class.", JFreeChart.class, chartUnit2.getTicChart().getClass());
        assertEquals("Expecting NumberAxis class.", NumberAxis.class, chartUnit1.getDomainAxis().getClass());
        assertEquals("Expecting NumberAxis class.", NumberAxis.class, chartUnit2.getDomainAxis().getClass());
    }

    /**
     * Create an <code>XYSeries</code> object by reading the TIC matrix file that contains rt and ions values
     * representing a TIC graph.
     *
     * @param msrunName the name of the msrun.
     * @param ticMatrixFile the file with the TIC values.
     * @return The <code>XYSeries</code> object with the TIC graph.
     */
    private XYSeries readXYSeries(final String msrunName, final File ticMatrixFile) {
        final XYSeries xySeries = new XYSeries(msrunName);
        try {
            final BufferedReader bufferedReader = new BufferedReader(new FileReader(ticMatrixFile));
            // Skip the header line ("rt","ions").
            bufferedReader.readLine();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                final String[] values = line.split(",");
                xySeries.add(Double.parseDouble(values[0]) / 60.0, Double.parseDouble(values[1]));
            }
            bufferedReader.close();
        } catch (NumberFormatException | IOException e) {
            fail(e.getMessage());
            e.printStackTrace();
        }
        return xySeries;
    }
}
