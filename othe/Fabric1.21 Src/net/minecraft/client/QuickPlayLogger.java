/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.time.Instant;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.Util;
import net.minecraft.util.dynamic.Codecs;
import net.minecraft.world.GameMode;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public class QuickPlayLogger {
    private static final QuickPlayLogger NOOP = new QuickPlayLogger(""){

        @Override
        public void save(MinecraftClient client) {
        }

        @Override
        public void setWorld(WorldType worldType, String id, String name) {
        }
    };
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Gson GSON = new GsonBuilder().create();
    private final Path path;
    @Nullable
    private QuickPlayWorld world;

    QuickPlayLogger(String relativePath) {
        this.path = MinecraftClient.getInstance().runDirectory.toPath().resolve(relativePath);
    }

    public static QuickPlayLogger create(@Nullable String relativePath) {
        if (relativePath == null) {
            return NOOP;
        }
        return new QuickPlayLogger(relativePath);
    }

    public void setWorld(WorldType worldType, String id, String name) {
        this.world = new QuickPlayWorld(worldType, id, name);
    }

    public void save(MinecraftClient client) {
        if (client.interactionManager == null || this.world == null) {
            LOGGER.error("Failed to log session for quickplay. Missing world data or gamemode");
            return;
        }
        Util.getIoWorkerExecutor().execute(() -> {
            try {
                Files.deleteIfExists(this.path);
            } catch (IOException iOException) {
                LOGGER.error("Failed to delete quickplay log file {}", (Object)this.path, (Object)iOException);
            }
            Log log = new Log(this.world, Instant.now(), minecraftClient.interactionManager.getCurrentGameMode());
            Codec.list(Log.CODEC).encodeStart(JsonOps.INSTANCE, List.of(log)).resultOrPartial(Util.addPrefix("Quick Play: ", LOGGER::error)).ifPresent(json -> {
                try {
                    Files.createDirectories(this.path.getParent(), new FileAttribute[0]);
                    Files.writeString(this.path, (CharSequence)GSON.toJson((JsonElement)json), new OpenOption[0]);
                } catch (IOException iOException) {
                    LOGGER.error("Failed to write to quickplay log file {}", (Object)this.path, (Object)iOException);
                }
            });
        });
    }

    @Environment(value=EnvType.CLIENT)
    record QuickPlayWorld(WorldType type, String id, String name) {
        public static final MapCodec<QuickPlayWorld> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(((MapCodec)WorldType.CODEC.fieldOf("type")).forGetter(QuickPlayWorld::type), ((MapCodec)Codecs.ESCAPED_STRING.fieldOf("id")).forGetter(QuickPlayWorld::id), ((MapCodec)Codec.STRING.fieldOf("name")).forGetter(QuickPlayWorld::name)).apply((Applicative<QuickPlayWorld, ?>)instance, QuickPlayWorld::new));
    }

    @Environment(value=EnvType.CLIENT)
    public static enum WorldType implements StringIdentifiable
    {
        SINGLEPLAYER("singleplayer"),
        MULTIPLAYER("multiplayer"),
        REALMS("realms");

        static final Codec<WorldType> CODEC;
        private final String id;

        private WorldType(String id) {
            this.id = id;
        }

        @Override
        public String asString() {
            return this.id;
        }

        static {
            CODEC = StringIdentifiable.createCodec(WorldType::values);
        }
    }

    @Environment(value=EnvType.CLIENT)
    record Log(QuickPlayWorld quickPlayWorld, Instant lastPlayedTime, GameMode gameMode) {
        public static final Codec<Log> CODEC = RecordCodecBuilder.create(instance -> instance.group(QuickPlayWorld.CODEC.forGetter(Log::quickPlayWorld), ((MapCodec)Codecs.INSTANT.fieldOf("lastPlayedTime")).forGetter(Log::lastPlayedTime), ((MapCodec)GameMode.CODEC.fieldOf("gamemode")).forGetter(Log::gameMode)).apply((Applicative<Log, ?>)instance, Log::new));
    }
}

