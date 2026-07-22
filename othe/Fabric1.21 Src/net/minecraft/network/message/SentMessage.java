/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.network.message;

import net.minecraft.network.message.MessageType;
import net.minecraft.network.message.SignedMessage;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

/**
 * A class wrapping {@link SignedMessage} on the server to allow custom behavior for
 * sending messages.
 */
public interface SentMessage {
    public Text content();

    public void send(ServerPlayerEntity var1, boolean var2, MessageType.Parameters var3);

    /**
     * {@return the wrapped {@code message}}
     */
    public static SentMessage of(SignedMessage message) {
        if (message.isSenderMissing()) {
            return new Profileless(message.getContent());
        }
        return new Chat(message);
    }

    public record Profileless(Text content) implements SentMessage
    {
        @Override
        public void send(ServerPlayerEntity sender, boolean filterMaskEnabled, MessageType.Parameters params) {
            sender.networkHandler.sendProfilelessChatMessage(this.content, params);
        }
    }

    public record Chat(SignedMessage message) implements SentMessage
    {
        @Override
        public Text content() {
            return this.message.getContent();
        }

        @Override
        public void send(ServerPlayerEntity sender, boolean filterMaskEnabled, MessageType.Parameters params) {
            SignedMessage signedMessage = this.message.withFilterMaskEnabled(filterMaskEnabled);
            if (!signedMessage.isFullyFiltered()) {
                sender.networkHandler.sendChatMessage(signedMessage, params);
            }
        }
    }
}

