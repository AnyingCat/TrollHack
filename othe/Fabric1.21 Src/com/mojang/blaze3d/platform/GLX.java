/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package com.mojang.blaze3d.platform;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.logging.LogUtils;
import java.util.ArrayList;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.function.LongSupplier;
import java.util.function.Supplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gl.GlDebug;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.Window;
import net.minecraft.util.Colors;
import net.minecraft.util.annotation.DeobfuscateClass;
import org.lwjgl.Version;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWErrorCallbackI;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL11;
import org.lwjgl.system.MemoryUtil;
import org.slf4j.Logger;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;

@Environment(value=EnvType.CLIENT)
@DeobfuscateClass
public class GLX {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static String cpuInfo;

    public static String getOpenGLVersionString() {
        RenderSystem.assertOnRenderThread();
        if (GLFW.glfwGetCurrentContext() == 0L) {
            return "NO CONTEXT";
        }
        return GlStateManager._getString(GL11.GL_RENDERER) + " GL version " + GlStateManager._getString(GL11.GL_VERSION) + ", " + GlStateManager._getString(GL11.GL_VENDOR);
    }

    public static int _getRefreshRate(Window window) {
        RenderSystem.assertOnRenderThread();
        long l = GLFW.glfwGetWindowMonitor(window.getHandle());
        if (l == 0L) {
            l = GLFW.glfwGetPrimaryMonitor();
        }
        GLFWVidMode gLFWVidMode = l == 0L ? null : GLFW.glfwGetVideoMode(l);
        return gLFWVidMode == null ? 0 : gLFWVidMode.refreshRate();
    }

    public static String _getLWJGLVersion() {
        return Version.getVersion();
    }

    public static LongSupplier _initGlfw() {
        LongSupplier longSupplier;
        Window.acceptError((code, message) -> {
            throw new IllegalStateException(String.format(Locale.ROOT, "GLFW error before init: [0x%X]%s", code, message));
        });
        ArrayList<String> list = Lists.newArrayList();
        GLFWErrorCallback gLFWErrorCallback = GLFW.glfwSetErrorCallback((code, pointer) -> {
            String string = pointer == 0L ? "" : MemoryUtil.memUTF8(pointer);
            list.add(String.format(Locale.ROOT, "GLFW error during init: [0x%X]%s", code, string));
        });
        if (GLFW.glfwInit()) {
            longSupplier = () -> (long)(GLFW.glfwGetTime() * 1.0E9);
            for (String string : list) {
                LOGGER.error("GLFW error collected during initialization: {}", (Object)string);
            }
        } else {
            throw new IllegalStateException("Failed to initialize GLFW, errors: " + Joiner.on(",").join(list));
        }
        RenderSystem.setErrorCallback(gLFWErrorCallback);
        return longSupplier;
    }

    public static void _setGlfwErrorCallback(GLFWErrorCallbackI callback) {
        GLFWErrorCallback gLFWErrorCallback = GLFW.glfwSetErrorCallback(callback);
        if (gLFWErrorCallback != null) {
            gLFWErrorCallback.free();
        }
    }

    public static boolean _shouldClose(Window window) {
        return GLFW.glfwWindowShouldClose(window.getHandle());
    }

    public static void _init(int debugVerbosity, boolean debugSync) {
        try {
            CentralProcessor centralProcessor = new SystemInfo().getHardware().getProcessor();
            cpuInfo = String.format(Locale.ROOT, "%dx %s", centralProcessor.getLogicalProcessorCount(), centralProcessor.getProcessorIdentifier().getName()).replaceAll("\\s+", " ");
        } catch (Throwable throwable) {
            // empty catch block
        }
        GlDebug.enableDebug(debugVerbosity, debugSync);
    }

    public static String _getCpuInfo() {
        return cpuInfo == null ? "<unknown>" : cpuInfo;
    }

    public static void _renderCrosshair(int size, boolean drawX, boolean drawY, boolean drawZ) {
        if (!(drawX || drawY || drawZ)) {
            return;
        }
        RenderSystem.assertOnRenderThread();
        GlStateManager._depthMask(false);
        GlStateManager._disableCull();
        RenderSystem.setShader(GameRenderer::getRenderTypeLinesProgram);
        Tessellator tessellator = RenderSystem.renderThreadTesselator();
        BufferBuilder bufferBuilder = tessellator.begin(VertexFormat.DrawMode.LINES, VertexFormats.LINES);
        RenderSystem.lineWidth(4.0f);
        if (drawX) {
            bufferBuilder.vertex(0.0f, 0.0f, 0.0f).color(Colors.BLACK).normal(1.0f, 0.0f, 0.0f);
            bufferBuilder.vertex(size, 0.0f, 0.0f).color(Colors.BLACK).normal(1.0f, 0.0f, 0.0f);
        }
        if (drawY) {
            bufferBuilder.vertex(0.0f, 0.0f, 0.0f).color(Colors.BLACK).normal(0.0f, 1.0f, 0.0f);
            bufferBuilder.vertex(0.0f, size, 0.0f).color(Colors.BLACK).normal(0.0f, 1.0f, 0.0f);
        }
        if (drawZ) {
            bufferBuilder.vertex(0.0f, 0.0f, 0.0f).color(Colors.BLACK).normal(0.0f, 0.0f, 1.0f);
            bufferBuilder.vertex(0.0f, 0.0f, size).color(Colors.BLACK).normal(0.0f, 0.0f, 1.0f);
        }
        BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
        RenderSystem.lineWidth(2.0f);
        bufferBuilder = tessellator.begin(VertexFormat.DrawMode.LINES, VertexFormats.LINES);
        if (drawX) {
            bufferBuilder.vertex(0.0f, 0.0f, 0.0f).color(Colors.RED).normal(1.0f, 0.0f, 0.0f);
            bufferBuilder.vertex(size, 0.0f, 0.0f).color(Colors.RED).normal(1.0f, 0.0f, 0.0f);
        }
        if (drawY) {
            bufferBuilder.vertex(0.0f, 0.0f, 0.0f).color(Colors.GREEN).normal(0.0f, 1.0f, 0.0f);
            bufferBuilder.vertex(0.0f, size, 0.0f).color(Colors.GREEN).normal(0.0f, 1.0f, 0.0f);
        }
        if (drawZ) {
            bufferBuilder.vertex(0.0f, 0.0f, 0.0f).color(-8421377).normal(0.0f, 0.0f, 1.0f);
            bufferBuilder.vertex(0.0f, 0.0f, size).color(-8421377).normal(0.0f, 0.0f, 1.0f);
        }
        BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
        RenderSystem.lineWidth(1.0f);
        GlStateManager._enableCull();
        GlStateManager._depthMask(true);
    }

    public static <T> T make(Supplier<T> factory) {
        return factory.get();
    }

    public static <T> T make(T object, Consumer<T> initializer) {
        initializer.accept(object);
        return object;
    }
}

