package nl.ctmm.trait.proteomics.qcviewer.utils;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

import org.apache.commons.io.FilenameUtils;

/**
 * This class contains some utility declarations and methods.
 *
 * @author <a href="mailto:pravin.pawar@nbic.nl">Pravin Pawar</a>
 * @author <a href="mailto:freek.de.bruijn@nbic.nl">Freek de Bruijn</a>
 */
public class Utilities {
    private static final Logger logger = Logger.getLogger(Utilities.class.getName());
    public static final int SCALE_FIT = 0;
    public static final int SCALE_FILL = 1;
    public static final String NOT_AVAILABLE_ICON_NAME = "naIcon";
    public static final String NOT_AVAILABLE_ICON_FILE = FilenameUtils.normalize("images\\na.jpg");

    private static BufferedImage notAvailableImage;

    public static BufferedImage getNotAvailableImage() {
        if (notAvailableImage == null) {
            try {
                notAvailableImage = ImageIO.read(new File(NOT_AVAILABLE_ICON_FILE));
            } catch (IOException e) {
                logger.log(Level.SEVERE, e.getMessage(), e);
            }
        }
        return notAvailableImage;
    }

    public static BufferedImage scaleImage(final BufferedImage image, final int scaleType, final int width,
                                           final int height) {
        final BufferedImage scaledImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        final Graphics2D graphics2D = scaledImage.createGraphics();
        graphics2D.setColor(Color.white);
        graphics2D.fillRect(0, 0, width, height);
        final double imageWidth = image.getWidth();
        final double imageHeight = image.getHeight();
        final double xScale = width / imageWidth;
        final double yScale = height / imageHeight;
        double scale = 1.0;
        switch (scaleType) {
            case SCALE_FIT:
                scale = Math.min(xScale, yScale);
                break;
            case SCALE_FILL:
                scale = Math.max(xScale, yScale);
        }
        final double x = (width - imageWidth * scale) / 2;
        final double y = (height - imageHeight * scale) / 2;
        final AffineTransform affineTransform = AffineTransform.getTranslateInstance(x, y);
        affineTransform.scale(scale, scale);
        graphics2D.drawRenderedImage(image, affineTransform);
        graphics2D.dispose();
        return scaledImage;
    }
}
