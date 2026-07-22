/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.client.realms.util;

import com.google.gson.annotations.SerializedName;
import com.mojang.logging.LogUtils;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.realms.CheckedGson;
import net.minecraft.client.realms.RealmsSerializable;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public class RealmsPersistence {
    private static final String FILE_NAME = "realms_persistence.json";
    private static final CheckedGson CHECKED_GSON = new CheckedGson();
    private static final Logger LOGGER = LogUtils.getLogger();

    public RealmsPersistenceData load() {
        return RealmsPersistence.readFile();
    }

    public void save(RealmsPersistenceData data) {
        RealmsPersistence.writeFile(data);
    }

    public static RealmsPersistenceData readFile() {
        Path path = RealmsPersistence.getFile();
        try {
            String string = Files.readString(path, StandardCharsets.UTF_8);
            RealmsPersistenceData realmsPersistenceData = CHECKED_GSON.fromJson(string, RealmsPersistenceData.class);
            if (realmsPersistenceData != null) {
                return realmsPersistenceData;
            }
        } catch (NoSuchFileException string) {
        } catch (Exception exception) {
            LOGGER.warn("Failed to read Realms storage {}", (Object)path, (Object)exception);
        }
        return new RealmsPersistenceData();
    }

    public static void writeFile(RealmsPersistenceData data) {
        Path path = RealmsPersistence.getFile();
        try {
            Files.writeString(path, (CharSequence)CHECKED_GSON.toJson(data), StandardCharsets.UTF_8, new OpenOption[0]);
        } catch (Exception exception) {
            // empty catch block
        }
    }

    private static Path getFile() {
        return MinecraftClient.getInstance().runDirectory.toPath().resolve(FILE_NAME);
    }

    @Environment(value=EnvType.CLIENT)
    public static class RealmsPersistenceData
    implements RealmsSerializable {
        @SerializedName(value="newsLink")
        public String newsLink;
        @SerializedName(value="hasUnreadNews")
        public boolean hasUnreadNews;
    }
}

