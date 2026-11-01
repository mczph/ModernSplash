package gkappa.modernsplash;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SplashLayoutTest {
    @Test
    void swapsTimerAndForgeLogoWithTheirEdgePaddingPreserved() {
        for (float halfWidth : new float[] { 320, 427, 800 }) {
            float left = 320 - halfWidth;
            float right = 320 + halfWidth;
            assertEquals(left + 4, SplashLayout.timerX(halfWidth, 200, false));
            assertEquals(right - 4, SplashLayout.timerX(halfWidth, 200, true) + 200);
            assertEquals(right - 7, SplashLayout.forgeX(halfWidth, 32, 7, false) + 32);
            assertEquals(left + 7, SplashLayout.forgeX(halfWidth, 32, 7, true) - 32);
        }
    }

    @Test
    void remappedForgeSplashCanLinkTheLayoutAndDecoder() throws Exception {
        MethodHandles.publicLookup().findStatic(SplashLayout.class, "timerX",
            MethodType.methodType(float.class, float.class, float.class, boolean.class));
        MethodHandles.publicLookup().findStatic(SplashLayout.class, "forgeX",
            MethodType.methodType(float.class, float.class, float.class, int.class, boolean.class));
        MethodHandles.publicLookup().findStatic(SplashImage.class, "read",
            MethodType.methodType(SplashImage.class, java.io.InputStream.class, boolean.class));
    }
}
