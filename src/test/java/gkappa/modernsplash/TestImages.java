package gkappa.modernsplash;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.IndexColorModel;
import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.ImageOutputStream;

/** Deterministic fixtures, also usable in the real development client. */
public final class TestImages {
    private static final byte[] RED = { 0, -1, 0, 0, -1, 0, 0, -1 };
    private static final byte[] GREEN = { 0, 0, -1, 0, -1, -1, 0, -1 };
    private static final byte[] BLUE = { 0, 0, 0, -1, 0, -1, 0, -1 };

    private TestImages() {}

    static BufferedImage solid(int width, int height, int color) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_INDEXED,
            new IndexColorModel(3, 8, RED, GREEN, BLUE, 0));
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) image.getRaster().setSample(x, y, 0, color);
        }
        return image;
    }

    static byte[] animatedGif() throws Exception {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        ImageWriter writer = ImageIO.getImageWritersByFormatName("gif").next();
        try (ImageOutputStream output = ImageIO.createImageOutputStream(bytes)) {
            writer.setOutput(output);
            IIOMetadata stream = writer.getDefaultStreamMetadata(writer.getDefaultWriteParam());
            IIOMetadataNode root = (IIOMetadataNode) stream.getAsTree(stream.getNativeMetadataFormatName());
            IIOMetadataNode screen = (IIOMetadataNode) root.getElementsByTagName("LogicalScreenDescriptor").item(0);
            screen.setAttribute("logicalScreenWidth", "64");
            screen.setAttribute("logicalScreenHeight", "64");
            stream.setFromTree(stream.getNativeMetadataFormatName(), root);
            writer.prepareWriteSequence(stream);
            int[] sizes = { 64, 24, 16, 8 };
            int[] xs = { 0, 8, 40, 0 };
            int[] ys = { 0, 12, 4, 56 };
            int[] delays = { 10, 30, 20, 40 };
            String[] disposals = { "doNotDispose", "restoreToPrevious", "restoreToBackgroundColor", "doNotDispose" };
            for (int i = 0; i < sizes.length; i++) {
                BufferedImage image = solid(sizes[i], sizes[i], i + 1);
                IIOMetadata metadata = writer.getDefaultImageMetadata(
                    javax.imageio.ImageTypeSpecifier.createFromRenderedImage(image), writer.getDefaultWriteParam());
                IIOMetadataNode tree = (IIOMetadataNode) metadata.getAsTree(metadata.getNativeMetadataFormatName());
                IIOMetadataNode descriptor = (IIOMetadataNode) tree.getElementsByTagName("ImageDescriptor").item(0);
                descriptor.setAttribute("imageLeftPosition", Integer.toString(xs[i]));
                descriptor.setAttribute("imageTopPosition", Integer.toString(ys[i]));
                IIOMetadataNode control = (IIOMetadataNode) tree.getElementsByTagName("GraphicControlExtension").item(0);
                control.setAttribute("disposalMethod", disposals[i]);
                control.setAttribute("delayTime", Integer.toString(delays[i]));
                control.setAttribute("transparentColorFlag", "TRUE");
                control.setAttribute("transparentColorIndex", "0");
                metadata.setFromTree(metadata.getNativeMetadataFormatName(), tree);
                writer.writeToSequence(new IIOImage(image, null, metadata), writer.getDefaultWriteParam());
            }
            writer.endWriteSequence();
        } finally {
            writer.dispose();
        }
        return bytes.toByteArray();
    }

    public static void main(String[] args) throws Exception {
        Path textures = Path.of(args[0], "assets", "custom", "textures");
        Files.createDirectories(textures);
        Files.write(textures.resolve("forge-custom.gif"), animatedGif());
        BufferedImage title = new BufferedImage(300, 80, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = title.createGraphics();
        try {
            graphics.setColor(Color.CYAN);
            graphics.fillRect(0, 0, 300, 80);
            graphics.setColor(Color.BLACK);
            graphics.drawString("CUSTOM TITLE 300 x 80", 60, 45);
        } finally {
            graphics.dispose();
        }
        ImageIO.write(title, "png", textures.resolve("title-custom.png").toFile());
        System.out.println("Wrote PNG title and partial-frame GIF fixtures to " + textures.toAbsolutePath());
    }
}
