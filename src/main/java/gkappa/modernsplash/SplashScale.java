package gkappa.modernsplash;

/** Shared pixel-to-splash conversions for loading and the main-menu fade. */
public final class SplashScale {
    static final float DEFAULT = 1.0f;
    static final float MIN = 0.25f;
    static final float MAX = 4.0f;
    private static final float REFERENCE_WIDTH = 854.0f;
    private static final float REFERENCE_HEIGHT = 480.0f;
    private static final float LOGO_SIZE = 512.0f;

    private SplashScale() {}

    static float normalize(float scale) {
        return Float.isFinite(scale) ? Math.clamp(scale, MIN, MAX) : DEFAULT;
    }

    // The coremod remaps its caller into net.minecraftforge.fml.client.
    public static float forWindow(int width, int height, float uiScale) {
        float windowScale = Math.min(Math.max(1, width) / REFERENCE_WIDTH,
            Math.max(1, height) / REFERENCE_HEIGHT);
        return windowScale * normalize(uiScale);
    }

    public static float halfExtent(int pixels, float scale) {
        return Math.max(1, pixels) / (2.0f * scale);
    }

    static float logoSize(int guiWidth, int displayWidth, int displayHeight, float uiScale) {
        return LOGO_SIZE * forWindow(displayWidth, displayHeight, uiScale)
            * guiWidth / Math.max(1, displayWidth);
    }
}
