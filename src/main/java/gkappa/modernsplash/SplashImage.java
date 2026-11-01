package gkappa.modernsplash;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.stream.ImageInputStream;

import org.w3c.dom.Node;

/** Decodes textures once; frame selection uses the GIF's own delays without allocating. */
public final class SplashImage {
    private final BufferedImage[] frames;
    private final long[] frameEnds;

    private SplashImage(BufferedImage[] frames, long[] frameEnds) {
        this.frames = frames;
        this.frameEnds = frameEnds;
    }

    public static SplashImage read(InputStream input, boolean allowStrip) throws IOException {
        try (ImageInputStream stream = ImageIO.createImageInputStream(input)) {
            if (stream == null) throw new IOException("Could not open image stream");
            Iterator<ImageReader> readers = ImageIO.getImageReaders(stream);
            if (!readers.hasNext()) throw new IOException("Unsupported image format");
            ImageReader reader = readers.next();
            try {
                reader.setInput(stream);
                if ("gif".equalsIgnoreCase(reader.getFormatName())) return readGif(reader);
                BufferedImage first = reader.read(0);
                int count = allowStrip && first.getHeight() > first.getWidth()
                    && first.getHeight() % first.getWidth() == 0 ? first.getHeight() / first.getWidth() : 1;
                BufferedImage[] frames = new BufferedImage[count];
                long[] ends = new long[count];
                for (int i = 0; i < count; i++) {
                    frames[i] = count == 1 ? first
                        : first.getSubimage(0, i * first.getWidth(), first.getWidth(), first.getWidth());
                    ends[i] = (i + 1L) * 50;
                }
                return new SplashImage(frames, ends);
            } finally {
                reader.dispose();
            }
        }
    }

    private static SplashImage readGif(ImageReader reader) throws IOException {
        // Java Image I/O GIF metadata defines logical size, frame offsets, disposal and 1/100 s delays.
        Node stream = tree(reader.getStreamMetadata());
        Node screen = child(stream, "LogicalScreenDescriptor");
        int width = number(screen, "logicalScreenWidth", reader.getWidth(0));
        int height = number(screen, "logicalScreenHeight", reader.getHeight(0));
        Node palette = child(stream, "GlobalColorTable");
        int backgroundIndex = number(palette, "backgroundColorIndex", 0);
        int background = 0;
        if (palette != null) {
            for (Node entry = palette.getFirstChild(); entry != null; entry = entry.getNextSibling()) {
                if (number(entry, "index", -1) == backgroundIndex) {
                    background = 0xFF000000 | number(entry, "red", 0) << 16
                        | number(entry, "green", 0) << 8 | number(entry, "blue", 0);
                }
            }
        }
        int count = reader.getNumImages(true);
        if (count < 1) throw new IOException("GIF has no frames");
        BufferedImage[] frames = new BufferedImage[count];
        long[] ends = new long[count];
        BufferedImage canvas = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        long duration = 0;
        for (int i = 0; i < count; i++) {
            Node metadata = tree(reader.getImageMetadata(i));
            Node descriptor = child(metadata, "ImageDescriptor");
            Node control = child(metadata, "GraphicControlExtension");
            int clearColor = "TRUE".equals(attribute(control, "transparentColorFlag", "FALSE"))
                && number(control, "transparentColorIndex", -1) == backgroundIndex ? 0 : background;
            if (i == 0) fill(canvas, 0, 0, width, height, clearColor);
            int x = number(descriptor, "imageLeftPosition", 0);
            int y = number(descriptor, "imageTopPosition", 0);
            String disposal = attribute(control, "disposalMethod", "none");
            BufferedImage previous = "restoreToPrevious".equals(disposal) ? copy(canvas) : null;
            BufferedImage frame = reader.read(i);
            Graphics2D graphics = canvas.createGraphics();
            try {
                graphics.drawImage(frame, x, y, null);
            } finally {
                graphics.dispose();
            }
            frames[i] = copy(canvas);
            int delay = number(control, "delayTime", 0);
            duration += delay > 0 ? delay * 10L : 100L;
            ends[i] = duration;
            if ("restoreToBackgroundColor".equals(disposal)) {
                fill(canvas, x, y, frame.getWidth(), frame.getHeight(), clearColor);
            } else if (previous != null) {
                canvas = previous;
            }
        }
        return new SplashImage(frames, ends);
    }

    private static Node tree(IIOMetadata metadata) {
        return metadata == null ? null : metadata.getAsTree(metadata.getNativeMetadataFormatName());
    }

    private static Node child(Node parent, String name) {
        if (parent != null) {
            for (Node node = parent.getFirstChild(); node != null; node = node.getNextSibling()) {
                if (name.equals(node.getNodeName())) return node;
            }
        }
        return null;
    }

    private static String attribute(Node node, String name, String fallback) {
        Node value = node == null || node.getAttributes() == null ? null : node.getAttributes().getNamedItem(name);
        return value == null ? fallback : value.getNodeValue();
    }

    private static int number(Node node, String name, int fallback) {
        return Integer.parseInt(attribute(node, name, Integer.toString(fallback)));
    }

    private static void fill(BufferedImage image, int x, int y, int width, int height, int color) {
        Graphics2D graphics = image.createGraphics();
        try {
            graphics.setComposite(AlphaComposite.Src);
            graphics.setColor(new Color(color, true));
            graphics.fillRect(x, y, width, height);
        } finally {
            graphics.dispose();
        }
    }

    private static BufferedImage copy(BufferedImage image) {
        BufferedImage copy = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = copy.createGraphics();
        try {
            graphics.setComposite(AlphaComposite.Src);
            graphics.drawImage(image, 0, 0, null);
        } finally {
            graphics.dispose();
        }
        return copy;
    }

    public int frameCount() { return frames.length; }
    public int width() { return frames[0].getWidth(); }
    public int height() { return frames[0].getHeight(); }
    public BufferedImage frame(int index) { return frames[index]; }
    public long durationMillis() { return frameEnds[frameEnds.length - 1]; }

    public int frameAt(long elapsedMillis) {
        long position = Math.floorMod(elapsedMillis, durationMillis());
        int low = 0;
        int high = frameEnds.length - 1;
        while (low < high) {
            int middle = (low + high) >>> 1;
            if (position < frameEnds[middle]) high = middle;
            else low = middle + 1;
        }
        return low;
    }
}
