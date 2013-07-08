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
    	//    public ReportUnit(String msrunName, int reportNum) 
        return new ReportUnit("msrun", reportNumber);
    }
}
