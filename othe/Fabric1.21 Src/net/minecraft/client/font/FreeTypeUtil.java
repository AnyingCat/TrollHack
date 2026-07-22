/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.client.font;

import com.mojang.logging.LogUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.freetype.FT_Vector;
import org.lwjgl.util.freetype.FreeType;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public class FreeTypeUtil {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final Object LOCK = new Object();
    private static long freeType = 0L;

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static long initialize() {
        Object object = LOCK;
        synchronized (object) {
            if (freeType == 0L) {
                try (MemoryStack memoryStack = MemoryStack.stackPush();){
                    PointerBuffer pointerBuffer = memoryStack.mallocPointer(1);
                    FreeTypeUtil.checkFatalError(FreeType.FT_Init_FreeType(pointerBuffer), "Initializing FreeType library");
                    freeType = pointerBuffer.get();
                }
            }
            return freeType;
        }
    }

    public static void checkFatalError(int code, String description) {
        if (code != 0) {
            throw new IllegalStateException("FreeType error: " + FreeTypeUtil.getErrorMessage(code) + " (" + description + ")");
        }
    }

    public static boolean checkError(int code, String description) {
        if (code != 0) {
            LOGGER.error("FreeType error: {} ({})", (Object)FreeTypeUtil.getErrorMessage(code), (Object)description);
            return true;
        }
        return false;
    }

    private static String getErrorMessage(int code) {
        String string = FreeType.FT_Error_String(code);
        if (string != null) {
            return string;
        }
        return "Unrecognized error: 0x" + Integer.toHexString(code);
    }

    public static FT_Vector set(FT_Vector vec, float x, float y) {
        long l = Math.round(x * 64.0f);
        long m = Math.round(y * 64.0f);
        return vec.set(l, m);
    }

    public static float getX(FT_Vector vec) {
        return (float)vec.x() / 64.0f;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void release() {
        Object object = LOCK;
        synchronized (object) {
            if (freeType != 0L) {
                FreeType.FT_Done_Library(freeType);
                freeType = 0L;
            }
        }
    }
}

