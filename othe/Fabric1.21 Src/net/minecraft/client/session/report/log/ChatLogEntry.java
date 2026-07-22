/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.client.session.report.log;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import java.util.function.Supplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.session.report.log.ReceivedMessage;
import net.minecraft.util.StringIdentifiable;

/**
 * An entry logged to {@link ChatLog}.
 */
@Environment(value=EnvType.CLIENT)
public interface ChatLogEntry {
    public static final Codec<ChatLogEntry> CODEC = StringIdentifiable.createCodec(Type::values).dispatch(ChatLogEntry::getType, Type::getCodec);

    public Type getType();

    @Environment(value=EnvType.CLIENT)
    public static enum Type implements StringIdentifiable
    {
        PLAYER("player", () -> ReceivedMessage.ChatMessage.CHAT_MESSAGE_CODEC),
        SYSTEM("system", () -> ReceivedMessage.GameMessage.GAME_MESSAGE_CODEC);

        private final String id;
        private final Supplier<MapCodec<? extends ChatLogEntry>> codecSupplier;

        private Type(String id, Supplier<MapCodec<? extends ChatLogEntry>> codecSupplier) {
            this.id = id;
            this.codecSupplier = codecSupplier;
        }

        private MapCodec<? extends ChatLogEntry> getCodec() {
            return this.codecSupplier.get();
        }

        @Override
        public String asString() {
            return this.id;
        }
    }
}

