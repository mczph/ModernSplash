package gkappa.modernsplash;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DarkPaletteTest {
    @Test
    void upgradesOldGrayDefaultsAndPreservesCustomColors() {
        assertEquals(DarkPalette.BACKGROUND,
            DarkPalette.upgradeLegacyColor(0x202020, 0x202020, DarkPalette.BACKGROUND));
        assertEquals(DarkPalette.FONT,
            DarkPalette.upgradeLegacyColor(0x606060, 0x606060, DarkPalette.FONT));
        assertEquals(DarkPalette.LOGO,
            DarkPalette.upgradeLegacyColor(0x999999, 0x999999, DarkPalette.LOGO));
        assertEquals(DarkPalette.BAR,
            DarkPalette.upgradeLegacyColor(0x4E4E4E, 0x4E4E4E, DarkPalette.BAR));
        assertEquals(0x12AB34, DarkPalette.upgradeLegacyColor(0x12AB34, 0x606060, DarkPalette.FONT));
        assertEquals(DarkPalette.FONT,
            DarkPalette.upgradeLegacyColor(DarkPalette.FONT, 0x606060, DarkPalette.FONT));
    }

    @Test
    void readableTextAndStatusAccentsStandOutAgainstTheGreenBackground() {
        double background = luminance(DarkPalette.BACKGROUND);
        // The logo is decorative and its tint can intentionally be dark or black.
        for (int color : new int[] { DarkPalette.FONT, DarkPalette.BAR_BORDER,
                DarkPalette.BAR, DarkPalette.MEMORY_GOOD, DarkPalette.MEMORY_WARN, DarkPalette.MEMORY_LOW }) {
            assertTrue((luminance(color) + 0.05) / (background + 0.05) > 7.0);
        }
    }

    private double luminance(int color) {
        return 0.2126 * channel(color >> 16 & 255) + 0.7152 * channel(color >> 8 & 255)
            + 0.0722 * channel(color & 255);
    }

    private double channel(int value) {
        double srgb = value / 255.0;
        return srgb <= 0.04045 ? srgb / 12.92 : Math.pow((srgb + 0.055) / 1.055, 2.4);
    }
}
