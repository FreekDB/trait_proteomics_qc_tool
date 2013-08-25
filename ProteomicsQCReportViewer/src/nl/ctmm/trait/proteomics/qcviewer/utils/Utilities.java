package nl.ctmm.trait.proteomics.qcviewer.utils;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
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
    /**
     * Scale an image by making it fit within a width by height rectangle (and possibly not fill it in one dimension).
     */
    public static final int SCALE_FIT = 0;

    /**
     * Scale an image by making it a width by height rectangle (and possibly go outside it in one dimension).
     */
    public static final int SCALE_FILL = 1;

    /**
     * The file name of the image to be shown when an actual image is not available.
     */
    public static final String NOT_AVAILABLE_ICON_FILE = FilenameUtils.normalize("images\\na.jpg");

    /**
     * The logger for this class.
     */
    private static final Logger logger = Logger.getLogger(Utilities.class.getName());

    /**
     * Cached reference to the image to be shown when an actual image is not available.
     */
    private static BufferedImage notAvailableImage;

    /**
     * Hidden constructor.
     */
    private Utilities() {
    }

    /**
     * TODO: Remove Utilities.getNotAvailableImage()??  [Pravin]
     * The function Utilities.getNotAvailableImage() is not used in v2. It was in use for v1 of the GUI.
     */

    /**
     * Get the placeholder image to be shown when the actual image is not available.
     *
     * @return the placeholder image.
     */
    public static BufferedImage getNotAvailableImage() {
        if (notAvailableImage == null) {
            try {
                notAvailableImage = ImageIO.read(new File(FilenameUtils.normalize(NOT_AVAILABLE_ICON_FILE)));
            } catch (final IOException e) {
                logger.log(Level.SEVERE, e.getMessage(), e);
            }
        }
        return notAvailableImage;
    }

    /**
     * Scale the supplied image to the specified width and height. The scale type is either {@link Utilities#SCALE_FIT}
     * to make the scaled image fit within the width by height rectangle or {@link Utilities#SCALE_FILL} to make the
     * scaled image fill the entire rectangle (and possibly go outside it in one dimension).
     *
     * @param image     the image to be scaled.
     * @param scaleType {@link Utilities#SCALE_FIT} or {@link Utilities#SCALE_FILL}.
     * @param width     the preferred width.
     * @param height    the preferred height.
     * @return the scaled image.
     */
    public static BufferedImage scaleImage(final BufferedImage image, final int scaleType, final int width,
                                           final int height) {
        logger.fine("scaleImage: width: " + width + " height: " + height);

        /* TODO: can we do the scaling once and save the images of the right size? [Freek]
         * This is a good idea. [Pravin]  
         * 
         * TODO: are there classes in the standard Java libraries or third party libraries that do this scaling? [Freek]
         * return image.getScaledInstance(width, height, Image.SCALE_SMOOTH);
         * This article describes use of Greaphics2D.drawImage() [Pravin]
         * http://www.mkyong.com/java/how-to-resize-an-image-in-java/
         * imgscalr is Java image scaling library available under Apache 2 License. 
         * http://www.thebuzzmedia.com/software/imgscalr-java-image-scaling-library/
         */
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
                break;
            default:
                logger.warning(String.format("Unexpected scale type: %d.", scaleType));
                break;
        }
        final double x = (width - imageWidth * scale) / 2;
        final double y = (height - imageHeight * scale) / 2;
        final AffineTransform affineTransform = AffineTransform.getTranslateInstance(x, y);
        affineTransform.scale(scale, scale);
        graphics2D.drawRenderedImage(image, affineTransform);
        graphics2D.dispose();
        return scaledImage;
    }

    /**
     * Set the specified font for the component and if it is a container then do it for all its child components too.
     *
     * @param component the component for which the font will be set (and for its children if there are any).
     * @param font the font to set.
     */
    public static void setFontContainer(final Component component, final Font font) {
        component.setFont(font);
        if (component instanceof Container) {
            for (final Component child : ((Container) component).getComponents()) {
                setFontContainer(child, font);
            }
        }
    }

    /**
     * Create a date format with the pattern "dd/MM/yyyy".
     *
     * @return the date format.
     */
    public static SimpleDateFormat createDateFormat() {
        final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        dateFormat.setLenient(false);
        return dateFormat;
    }
}
