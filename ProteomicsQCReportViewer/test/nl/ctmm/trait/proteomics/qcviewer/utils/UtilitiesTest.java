package nl.ctmm.trait.proteomics.qcviewer.utils;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.mockito.Matchers.anyString;
import static org.powermock.api.mockito.PowerMockito.whenNew;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

/**
 * Unit tests for the <code>Utilities</code> class.
 *
 * @author <a href="mailto:freek.de.bruijn@nbic.nl">Freek de Bruijn</a>
 */
@RunWith(PowerMockRunner.class)
public class UtilitiesTest {
    /**
     * Test the <code>getNotAvailableImage</code> method in a normal situation.
     */
    @Test
    public void testGetNotAvailableImageNormal() throws Exception {
        final BufferedImage notAvailableIcon1 = Utilities.getNotAvailableImage();
        final BufferedImage notAvailableIcon2 = Utilities.getNotAvailableImage();
        assertNotNull("The icon should not be equal to null.", notAvailableIcon1);
        assertEquals("The icon references should be the same.", notAvailableIcon1, notAvailableIcon2);
    }

    /**
     * TODO: mvn package command gives following error message:  
     * Tests in error: testGetNotAvailableImageException(nl.ctmm.trait.proteomics.qcviewer.utils.UtilitiesTest): 
     * Inconsistent stackmap frames at branch target 2627 in method nl.ctmm.trait.proteomics.qcviewer.utils.Utilities.scaleImage
     * (Ljava/awt/image/BufferedImage;III)Ljava/awt/image/BufferedImage; at offset 2617
     * Can this test be removed? 
     * The function Utilities.getNotAvailableImage() is not used in v2. It was in use for v1 of the GUI. 
     */
    
    /**
     * Test the <code>getNotAvailableImage</code> method with an exception.
     */
    @PrepareForTest(Utilities.class)
    @Test
    public void testGetNotAvailableImageException() throws Exception {
        whenNew(File.class).withArguments(anyString()).thenThrow(new IOException("Test with an exception."));
        Logger.getLogger(Utilities.class.getName()).setLevel(Level.OFF);
        assertNull(Utilities.getNotAvailableImage());
    }
}
