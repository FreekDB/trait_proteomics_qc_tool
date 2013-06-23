package nl.ctmm.trait.proteomics.qcviewer.gui;

import nl.ctmm.trait.proteomics.qcviewer.input.ReportUnit;

/**
 * This class contains a utility method for the unit tests in the gui package.
 *
 * @author <a href="mailto:freek.de.bruijn@nbic.nl">Freek de Bruijn</a>
 */
public class GuiTestUtils {
    /**
     * Create a report unit for testing.
     *
     * @param reportNumber the unique report number.
     * @return the new report unit.
     */
    public static ReportUnit createReportUnit(final int reportNumber) {
    	// public ReportUnit(final String msrunName, final int reportNum, final String fileSizeString,
        //                   final String ms1Spectra, final String ms2Spectra, final String measured,
        //                   final String runtime, final BufferedImage heatmap, final BufferedImage ioncount,
        //                   final String heatmapName, final String ioncountName)
        return new ReportUnit("msrun", reportNumber, reportNumber + "1", reportNumber + "2", reportNumber + "3",
                              reportNumber + "4", reportNumber + "5", null, null, reportNumber + "heatmap.png",
                              reportNumber + "ions.png");
    }
}
