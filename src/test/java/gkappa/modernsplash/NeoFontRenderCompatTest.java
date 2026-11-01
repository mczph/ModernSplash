package gkappa.modernsplash;

import java.io.InputStream;
import java.net.URLClassLoader;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.ClassRemapper;
import org.objectweb.asm.commons.Remapper;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.analysis.Analyzer;
import org.objectweb.asm.tree.analysis.BasicInterpreter;
import org.objectweb.asm.tree.analysis.BasicValue;
import org.objectweb.asm.tree.analysis.BasicVerifier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

class NeoFontRenderCompatTest {
    private byte[] splashThread() throws Exception {
        try (InputStream input = getClass().getResourceAsStream("/gkappa/modernsplash/CustomSplash$2.class")) {
            return input.readAllBytes();
        }
    }

    private ClassNode parse(byte[] bytes) {
        ClassNode node = new ClassNode();
        new ClassReader(bytes).accept(node, 0);
        return node;
    }

    private MethodNode run(ClassNode node) {
        return node.methods.stream().filter(method -> method.name.equals("run") && method.desc.equals("()V"))
            .findFirst().orElseThrow();
    }

    @Test
    void tipOverlayCanStillReadAlphaFromLegacyLocalSlotThree() throws Exception {
        ClassNode node = parse(splashThread());
        MethodNode run = run(node);
        var frames = new Analyzer<>(new BasicInterpreter()).analyze(node.name, run);
        boolean found = false;
        for (int i = 0; i < run.instructions.size(); i++) {
            if (run.instructions.get(i) instanceof MethodInsnNode call
                    && call.owner.equals("org/lwjgl/opengl/Display") && call.name.equals("update")) {
                // NeoFontRender 0.8.0 injects FLOAD 3 immediately before this call.
                assertEquals(BasicValue.FLOAT_VALUE, frames[i].getLocal(3));
                found = true;
            }
        }
        assertTrue(found);
    }

    @Test
    void restoresPixelCoordinatesAndNeutralModelViewBeforeLegacyOverlays() throws Exception {
        ClassNode node = parse(splashThread());
        MethodNode run = run(node);
        boolean prepared = false;
        for (var instruction : run.instructions) {
            if (instruction instanceof MethodInsnNode call) {
                if (call.name.equals("prepareOverlays")) prepared = true;
                if (call.owner.equals("org/lwjgl/opengl/Display") && call.name.equals("update")) {
                    assertTrue(prepared, "Injected tips need their original pixel projection before update");
                }
            }
        }
        MethodNode prepare = node.methods.stream().filter(method -> method.name.equals("prepareOverlays"))
            .findFirst().orElseThrow();
        long resets = java.util.Arrays.stream(prepare.instructions.toArray()).filter(instruction ->
            instruction instanceof MethodInsnNode call && call.name.equals("glLoadIdentity")).count();
        assertEquals(2, resets, "Reset both projection and model-view, including rotated Forge icons");
        assertTrue(java.util.Arrays.stream(prepare.instructions.toArray()).anyMatch(instruction ->
            instruction instanceof MethodInsnNode call && call.owner.equals("gkappa/modernsplash/SplashScale")
                && call.name.equals("forWindow")), "Tips must use the same adaptive scale and uiScale as the timer");
        assertTrue(java.util.Arrays.stream(prepare.instructions.toArray()).anyMatch(instruction ->
            instruction instanceof MethodInsnNode call && call.name.equals("glScalef")),
            "Apply the adaptive font scale before the real NeoFontRender injection");
    }

    @Test
    void actualNeoFontRenderTransformerProducesVerifiableBytecode() throws Exception {
        String jar = System.getenv("NEOFONT_COMPAT_JAR");
        assumeTrue(jar != null, "Set NEOFONT_COMPAT_JAR to test an installed NeoFontRender build");
        ClassWriter writer = new ClassWriter(0);
        new ClassReader(splashThread()).accept(new ClassRemapper(writer, new Remapper(Opcodes.ASM9) {
            @Override
            public String map(String name) {
                String source = "gkappa/modernsplash/CustomSplash";
                return name.startsWith(source)
                    ? "net/minecraftforge/fml/client/SplashProgress" + name.substring(source.length()) : name;
            }
        }), 0);
        try (URLClassLoader loader = new URLClassLoader(new java.net.URL[] { Path.of(jar).toUri().toURL() },
                getClass().getClassLoader())) {
            Class<?> transformer = loader.loadClass("neofontrender.splash.SplashProgressTransformer");
            var patch = transformer.getDeclaredMethod("patchRenderThread", byte[].class);
            patch.setAccessible(true);
            byte[] patched = (byte[]) patch.invoke(transformer.getConstructor().newInstance(), writer.toByteArray());
            ClassNode result = parse(patched);
            MethodNode run = run(result);
            assertTrue(java.util.Arrays.stream(run.instructions.toArray()).anyMatch(instruction ->
                instruction instanceof MethodInsnNode call && call.owner.equals("neofontrender/splash/SplashTipsRenderer")
                    && call.name.equals("render")), "The real third-party injection must be present");
            boolean prepared = false;
            for (var instruction : run.instructions) {
                if (instruction instanceof MethodInsnNode call) {
                    if (call.name.equals("prepareOverlays")) prepared = true;
                    if (call.owner.equals("neofontrender/splash/SplashTipsRenderer") && call.name.equals("render")) {
                        assertTrue(prepared, "Prepare the legacy viewport before the actual NeoFontRender injection");
                    }
                }
            }
            new Analyzer<>(new BasicVerifier()).analyze(result.name, run);
        }
    }
}
