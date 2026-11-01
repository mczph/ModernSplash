package gkappa.modernsplash;

import org.junit.jupiter.api.Test;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SplashScaleTest {
    @Test
    void remappedForgeSplashCanLinkTheProjectionHelper() throws Throwable {
        var halfExtent = MethodHandles.publicLookup().findStatic(SplashScale.class, "halfExtent",
            MethodType.methodType(float.class, int.class, float.class));
        assertEquals(320.0f, (float) halfExtent.invokeExact(640, 1.0f));
        var forWindow = MethodHandles.publicLookup().findStatic(SplashScale.class, "forWindow",
            MethodType.methodType(float.class, int.class, int.class, float.class));
        assertEquals(1.0f, (float) forWindow.invokeExact(854, 480, 1.0f));
    }

    @Test
    void invalidValuesCannotBreakTheProjection() {
        for (float value : new float[] { Float.NaN, Float.NEGATIVE_INFINITY, Float.POSITIVE_INFINITY }) {
            assertEquals(1.0f, SplashScale.normalize(value));
            float scale = SplashScale.forWindow(854, 480, value);
            assertTrue(Float.isFinite(SplashScale.halfExtent(854, scale)));
        }
        assertEquals(0.25f, SplashScale.normalize(0.0f));
        assertEquals(0.25f, SplashScale.normalize(-1.0f));
        assertEquals(4.0f, SplashScale.normalize(100.0f));
    }

    @Test
    void originalSizeAndOddWindowDimensionsStayCentered() {
        assertEquals(320.0f, SplashScale.halfExtent(640, 1.0f));
        assertEquals(240.0f, SplashScale.halfExtent(480, 1.0f));
        assertEquals(427.5f, SplashScale.halfExtent(855, 1.0f));
        assertEquals(240.5f, SplashScale.halfExtent(481, 1.0f));
    }

    @Test
    void scalesWithTheWindowAndThenAppliesTheConfiguredMultiplier() {
        assertEquals(1.0f, SplashScale.forWindow(854, 480, 1.0f));
        assertEquals(0.5f, SplashScale.forWindow(427, 240, 1.0f));
        assertEquals(2.0f, SplashScale.forWindow(1708, 960, 1.0f));
        assertEquals(1.0f, SplashScale.forWindow(1708, 960, 0.5f));
        assertEquals(4.0f, SplashScale.forWindow(1708, 960, 2.0f));
        // The derived scale must not be clamped to the multiplier's bounds.
        float smallScale = SplashScale.forWindow(85, 48, 0.25f);
        assertTrue(smallScale < 0.25f);
        assertTrue(SplashScale.forWindow(7680, 4320, 4.0f) > 4.0f);
        assertEquals(85 / (2 * smallScale), SplashScale.halfExtent(85, smallScale));
    }

    @Test
    void fitsBothAxesForWideTallAndMinimizedWindows() {
        for (int[] window : new int[][] { { 3440, 1440 }, { 480, 854 }, { 855, 481 },
                { 320, 200 }, { 0, 0 }, { -1, 480 } }) {
            int width = Math.max(1, window[0]);
            int height = Math.max(1, window[1]);
            float scale = SplashScale.forWindow(window[0], window[1], 1.0f);
            assertTrue(Float.isFinite(scale) && scale > 0.0f);
            assertTrue(854 * scale <= width + 0.001f);
            assertTrue(480 * scale <= height + 0.001f);
            assertTrue(Float.isFinite(SplashScale.halfExtent(window[0], scale)));
            assertTrue(Float.isFinite(SplashScale.halfExtent(window[1], scale)));
        }
    }

    @Test
    void splashAndFadeHaveTheSamePhysicalLogoSizeAtEveryGuiScale() {
        for (float multiplier : new float[] { 0.25f, 0.5f, 1.0f, 1.5f, 2.0f, 4.0f }) {
            for (int[] window : new int[][] { { 381, 214 }, { 854, 480 }, { 855, 481 },
                    { 1920, 1080 }, { 3840, 2160 }, { 480, 854 }, { 3440, 1440 } }) {
                int displayWidth = window[0];
                int displayHeight = window[1];
                float scale = SplashScale.forWindow(displayWidth, displayHeight, multiplier);
                float splashPixels = 512.0f * displayWidth / (2 * SplashScale.halfExtent(displayWidth, scale));
                for (int guiScale = 1; guiScale <= 4; guiScale++) {
                    int guiWidth = (displayWidth + guiScale - 1) / guiScale;
                    float fadePixels = SplashScale.logoSize(guiWidth, displayWidth, displayHeight, multiplier)
                        * displayWidth / guiWidth;
                    assertEquals(splashPixels, fadePixels, 0.01f);
                }
            }
        }
    }
}
