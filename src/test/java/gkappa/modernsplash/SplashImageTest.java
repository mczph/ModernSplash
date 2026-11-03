package gkappa.modernsplash;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import javax.imageio.ImageIO;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SplashImageTest {
    private SplashImage gif() throws Exception {
        return SplashImage.read(new ByteArrayInputStream(TestImages.animatedGif()), true);
    }

    @Test
    void composesPartialGifFramesWithBothDisposalModes() throws Exception {
        SplashImage image = gif();
        assertEquals(64, image.width());
        assertEquals(64, image.height());
        assertEquals(4, image.frameCount());
        for (int i = 0; i < image.frameCount(); i++) {
            assertEquals(64, image.frame(i).getWidth());
            assertEquals(64, image.frame(i).getHeight());
        }
        assertEquals(0xFFFF0000, image.frame(0).getRGB(8, 12));
        assertEquals(0xFF00FF00, image.frame(1).getRGB(8, 12));
        assertEquals(0xFFFF0000, image.frame(2).getRGB(8, 12));
        assertEquals(0xFF0000FF, image.frame(2).getRGB(40, 4));
        assertEquals(0, image.frame(3).getRGB(40, 4));
        assertEquals(0xFFFFFF00, image.frame(3).getRGB(0, 56));
    }

    @Test
    void usesPerFrameDelaysAndLoopsAtTheExactBoundary() throws Exception {
        SplashImage image = gif();
        assertEquals(1000, image.durationMillis());
        assertEquals(0, image.frameAt(0));
        assertEquals(0, image.frameAt(99));
        assertEquals(1, image.frameAt(100));
        assertEquals(1, image.frameAt(399));
        assertEquals(2, image.frameAt(400));
        assertEquals(2, image.frameAt(599));
        assertEquals(3, image.frameAt(600));
        assertEquals(3, image.frameAt(999));
        assertEquals(0, image.frameAt(1000));
        assertEquals(1, image.frameAt(1100));
    }

    @Test
    void preservesPortraitTitlesAndSupportsLegacyForgePngStrips() throws Exception {
        BufferedImage source = new BufferedImage(16, 32, BufferedImage.TYPE_INT_ARGB);
        source.setRGB(0, 0, 0xFFFF0000);
        source.setRGB(0, 16, 0xFF00FF00);
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        ImageIO.write(source, "png", bytes);
        SplashImage title = SplashImage.read(new ByteArrayInputStream(bytes.toByteArray()), false);
        assertEquals(16, title.width());
        assertEquals(32, title.height());
        assertEquals(1, title.frameCount());
        SplashImage strip = SplashImage.read(new ByteArrayInputStream(bytes.toByteArray()), true);
        assertEquals(16, strip.height());
        assertEquals(2, strip.frameCount());
        assertEquals(0xFFFF0000, strip.frame(0).getRGB(0, 0));
        assertEquals(0xFF00FF00, strip.frame(1).getRGB(0, 0));
        assertEquals(0, strip.frameAt(49));
        assertEquals(1, strip.frameAt(50));
        assertEquals(0, strip.frameAt(100));
    }

    @Test
    void preservesReplacementTitleColorsAndTransparency() throws Exception {
        BufferedImage source = new BufferedImage(3, 2, BufferedImage.TYPE_INT_ARGB);
        int[] pixels = { 0xFFFF0000, 0xFF00FF00, 0xFF0000FF, 0x8012AB34, 0x00123456, 0xFFFFFFFF };
        source.setRGB(0, 0, 3, 2, pixels, 0, 3);
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        ImageIO.write(source, "png", bytes);
        SplashImage title = SplashImage.read(new ByteArrayInputStream(bytes.toByteArray()), false);
        assertEquals(3, title.width());
        assertEquals(2, title.height());
        assertEquals(1, title.frameCount());
        for (int i = 0; i < pixels.length; i++) {
            assertEquals(pixels[i], title.frame(0).getRGB(i % 3, i / 3));
        }
    }

    @Test
    void rejectsUnsupportedImageData() {
        assertThrows(java.io.IOException.class,
            () -> SplashImage.read(new ByteArrayInputStream(new byte[] { 1, 2, 3 }), true));
    }
}
