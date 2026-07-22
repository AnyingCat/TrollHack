/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.data;

import com.google.common.hash.HashCode;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import net.minecraft.util.PathUtil;

public interface DataWriter {
    public static final DataWriter UNCACHED = (path, data, hashCode) -> {
        PathUtil.createDirectories(path.getParent());
        Files.write(path, data, new OpenOption[0]);
    };

    public void write(Path var1, byte[] var2, HashCode var3) throws IOException;
}

