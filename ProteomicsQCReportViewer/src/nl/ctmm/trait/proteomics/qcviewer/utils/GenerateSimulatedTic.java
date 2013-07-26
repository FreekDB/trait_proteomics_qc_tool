package nl.ctmm.trait.proteomics.qcviewer.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

/**
 * Very simple tool to generate a TIC (total ion chromatogram) file for testing purposes.
 *
 * @author <a href="mailto:freek.de.bruijn@nbic.nl">Freek de Bruijn</a>
 */
public class GenerateSimulatedTic {
    /**
     * Seed number for generating reproducible random numbers for the TIC file.
     */
    private static final int RANDOM_GENERATOR_SEED = 123456;

    /**
     * Number of lines to generate in the TIC file.
     */
    private static final int NUMBER_OF_LINES = 5000;

    /**
     * Minimum value of rt.
     */
    private static final double MIN_RT = 300.0;

    /**
     * Maximum value of rt.
     */
    private static final double MAX_RT = 5400.0;

    /**
     * Value of 100%.
     */
    private static final double MAX_PERCENTAGE = 100.0;

    /**
     * Percentage of x axis where higher TIC values start.
     */
    private static final double START_PERCENTAGE_HIGH = 20.0;

    /**
     * Percentage of x axis where higher TIC values end.
     */
    private static final double END_PERCENTAGE_HIGH = 40.0;

    /**
     * Maximum TIC value in higher area.
     */
    private static final double MAX_TIC_HIGH = 9876543210.0;

    /**
     * Maximum TIC value in lower area.
     */
    private static final double MAX_TIC_LOW = 4876543210.0;

    /**
     * Minimum (base) value for TIC values.
     */
    private static final double MIN_TIC = 87654321.0;

    /**
     * Hidden constructor.
     */
    private GenerateSimulatedTic() {
    }

    /**
     * Generate a simulated TIC file in the specified location. This method can be called when more generated TIC files
     * are needed. Changing the constants above can give some variation in the TIC file characteristics.
     *
     * @param args command-line arguments (which are not used).
     */
    // CHECKSTYLE_OFF: UncommentedMain
    public static void main(final String[] args) {
        final String baseName = "simulated_tic_130707_d";
        new GenerateSimulatedTic().generateTicFile("C:\\Freek\\NBIC\\Proteomics\\trait_proteomics_qc_tool\\"
                                                   + "ProteomicsQCReportViewer\\QCReports\\2013\\Jul\\"
                                                   + baseName + "\\" + baseName + "_ticmatrix.csv");
    }
    // CHECKSTYLE_ON: UncommentedMain

    /**
     * Generate a simulated TIC (total ion chromatogram) file using the provided file path. For more information, please
     * see https://en.wikipedia.org/wiki/Mass_chromatogram#Total_ion_current_.28TIC.29_chromatogram.
     *
     * @param filePath the location and file name used for the simulated TIC file.
     */
    private void generateTicFile(final String filePath) {
        try {
            final File parentDirectory = new File(filePath).getParentFile();
            if (parentDirectory.exists() || parentDirectory.mkdirs()) {
                final Random randomGenerator = new Random(RANDOM_GENERATOR_SEED);
                final BufferedWriter ticWriter = new BufferedWriter(new FileWriter(filePath));
                ticWriter.write("\"rt\",\"ions\"");
                ticWriter.newLine();
                final int lineCount = NUMBER_OF_LINES;
                for (int lineIndex = 0; lineIndex < lineCount; lineIndex++) {
                    final double rt = MIN_RT + (MAX_RT - MIN_RT) * lineIndex / lineCount + randomGenerator.nextDouble();
                    final double percentage = (lineIndex * MAX_PERCENTAGE) / lineCount;
                    final boolean high = START_PERCENTAGE_HIGH <= percentage && percentage <= END_PERCENTAGE_HIGH;
                    final double maxTic = high ? MAX_TIC_HIGH : MAX_TIC_LOW;
                    final double tic = MIN_TIC + randomGenerator.nextDouble() * maxTic;
                    ticWriter.write(rt + "," + tic);
                    ticWriter.newLine();
                }
                ticWriter.close();
            } else {
                System.err.println("Creating one or more necessary directories failed: " + parentDirectory.getName());
            }
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }
}
