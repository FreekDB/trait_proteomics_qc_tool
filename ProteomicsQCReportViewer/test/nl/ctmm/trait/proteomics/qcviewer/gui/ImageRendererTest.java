package nl.ctmm.trait.proteomics.qcviewer.gui;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JTable;
import nl.ctmm.trait.proteomics.qcviewer.utils.Utilities;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

/**
 * Unit tests for the <code>ImageRenderer</code> class.
 *
 * @author <a href="mailto:freek.de.bruijn@nbic.nl">Freek de Bruijn</a>
 */
public class ImageRendererTest {
    /**
     * Check the <code>getTableCellRendererComponent</code> method.
     */
    @Test
    public void testGetTableCellRendererComponent() {
        final ImageRenderer renderer = new ImageRenderer();
        final JTable table = new JTable();
        final BufferedImage image = Utilities.getNotAvailableImage();
        final Component rendererComponent = renderer.getTableCellRendererComponent(table, image, false, false, 0, 0);
        assertTrue(rendererComponent instanceof JLabel);
        final JLabel rendererLabel = (JLabel) rendererComponent;
        assertEquals(JLabel.CENTER, rendererLabel.getHorizontalAlignment());
        assertTrue(imagesEqual(image, iconToImage(rendererLabel.getIcon())));
    }

    // http://stackoverflow.com/questions/11006394/is-there-a-simple-way-to-compare-bufferedimage-instances
    private boolean imagesEqual(final BufferedImage image1, final BufferedImage image2) {
        boolean imagesAreEqual = true;
        if (image1.getWidth() == image2.getWidth() && image1.getHeight() == image2.getHeight()) {
            for (int x = 0; imagesAreEqual && x < image1.getWidth(); x++)
                for (int y = 0; imagesAreEqual && y < image1.getHeight(); y++)
                    if (image1.getRGB(x, y) != image2.getRGB(x, y))
                        imagesAreEqual = false;
        } else
            imagesAreEqual = false;
        return imagesAreEqual;
    }

    // http://stackoverflow.com/questions/12533543/converting-a-imageicon-array-to-a-bufferedimage
    private BufferedImage iconToImage(final Icon icon) {
        final int imageType = BufferedImage.TYPE_INT_RGB;
        final BufferedImage bufferedImage = new BufferedImage(icon.getIconWidth(), icon.getIconHeight(), imageType);
        final Graphics graphics = bufferedImage.createGraphics();
        icon.paintIcon(null, graphics, 0, 0);
        graphics.dispose();
        return bufferedImage;
    }
}
