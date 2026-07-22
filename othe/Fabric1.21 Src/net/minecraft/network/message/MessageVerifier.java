/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.network.message;

import com.mojang.logging.LogUtils;
import java.util.function.BooleanSupplier;
import net.minecraft.network.encryption.SignatureVerifier;
import net.minecraft.network.message.SignedMessage;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

/**
 * Verifies incoming messages' signature and the message chain.
 * 
 * <p>Methods in this interface must be called in the order of the message's reception,
 * as it affects the verification result.
 */
@FunctionalInterface
public interface MessageVerifier {
    public static final Logger LOGGER = LogUtils.getLogger();
    public static final MessageVerifier NO_SIGNATURE = SignedMessage::stripSignature;
    public static final MessageVerifier UNVERIFIED = message -> {
        LOGGER.error("Received chat message from {}, but they have no chat session initialized and secure chat is enforced", (Object)message.getSender());
        return null;
    };

    @Nullable
    public SignedMessage ensureVerified(SignedMessage var1);

    public static class Impl
    implements MessageVerifier {
        private final SignatureVerifier signatureVerifier;
        private final BooleanSupplier expirationChecker;
        @Nullable
        private SignedMessage lastVerifiedMessage;
        private boolean lastMessageVerified = true;

        public Impl(SignatureVerifier signatureVerifier, BooleanSupplier expirationChecker) {
            this.signatureVerifier = signatureVerifier;
            this.expirationChecker = expirationChecker;
        }

        private boolean verifyPrecedingSignature(SignedMessage message) {
            if (message.equals(this.lastVerifiedMessage)) {
                return true;
            }
            if (this.lastVerifiedMessage != null && !message.link().linksTo(this.lastVerifiedMessage.link())) {
                LOGGER.error("Received out-of-order chat message from {}: expected index > {} for session {}, but was {} for session {}", message.getSender(), this.lastVerifiedMessage.link().index(), this.lastVerifiedMessage.link().sessionId(), message.link().index(), message.link().sessionId());
                return false;
            }
            return true;
        }

        private boolean verify(SignedMessage message) {
            if (this.expirationChecker.getAsBoolean()) {
                LOGGER.error("Received message from player with expired profile public key: {}", (Object)message);
                return false;
            }
            if (!message.verify(this.signatureVerifier)) {
                LOGGER.error("Received message with invalid signature from {}", (Object)message.getSender());
                return false;
            }
            return this.verifyPrecedingSignature(message);
        }

        @Override
        @Nullable
        public SignedMessage ensureVerified(SignedMessage message) {
            boolean bl = this.lastMessageVerified = this.lastMessageVerified && this.verify(message);
            if (!this.lastMessageVerified) {
                return null;
            }
            this.lastVerifiedMessage = message;
            return message;
        }
    }
}

