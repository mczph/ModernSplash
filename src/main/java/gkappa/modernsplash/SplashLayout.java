package gkappa.modernsplash;

/** Edge anchors shared by the remapped Forge splash and its tests. */
public final class SplashLayout {
    private SplashLayout() {}

    public static float timerX(float halfWidth, float textWidth, boolean swapBottomCorners) {
        return swapBottomCorners ? 320 + halfWidth - 4 - textWidth : 320 - halfWidth + 4;
    }

    public static float forgeX(float halfWidth, float extent, int offset, boolean swapBottomCorners) {
        return swapBottomCorners ? 320 - halfWidth + extent + offset : 320 + halfWidth - extent - offset;
    }
}
