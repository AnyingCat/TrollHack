package github.trollhack.utils.render.font.nanovg;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.lwjgl.nanovg.NanoVG.*;

public final class FontManager {
    private static final Map<String, Integer> LOADED_FONTS = new ConcurrentHashMap<>();
    private static final Map<String, ByteBuffer> FONT_DATA = new ConcurrentHashMap<>();

    private FontManager() {}

    public static int getOrCreate(String fontName, float size) {
        return LOADED_FONTS.computeIfAbsent(fontName + "@" + size, k -> createFont(fontName, size));
    }

    private static int createFont(String fontName, float size) {
        ByteBuffer fontBuffer = FONT_DATA.computeIfAbsent(fontName, name -> {
            String resourcePath = "/assets/troll/fonts/" + name;
            try (InputStream stream = FontManager.class.getResourceAsStream(resourcePath)) {
                if (stream == null) {
                    System.err.println("Font resource not found: " + resourcePath);
                    return null;
                }
                byte[] rawBytes = stream.readAllBytes();
                ByteBuffer buffer = ByteBuffer.allocateDirect(rawBytes.length);
                buffer.put(rawBytes);
                buffer.flip();
                return buffer;
            } catch (IOException e) {
                System.err.println("Error reading font: " + name);
                e.printStackTrace();
                return null;
            }
        });
        if (fontBuffer == null) {
            throw new IllegalStateException("Cannot load font: " + fontName);
        }

        long context = NanoVGRenderer.INSTANCE.getContext();
        int fontId = nvgCreateFontMem(context, fontName, fontBuffer, false);
        if (fontId == -1) {
            throw new IllegalStateException("Failed to create NanoVG font: " + fontName);
        }

        nvgFontSize(context, size);
        return fontId;
    }

    public static void clearCache() {
        LOADED_FONTS.clear();
        FONT_DATA.clear();
    }
}
