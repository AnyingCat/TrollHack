/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.network.message;

import com.mojang.logging.LogUtils;
import java.time.Instant;
import java.util.UUID;
import java.util.function.BooleanSupplier;
import net.minecraft.network.encryption.PlayerPublicKey;
import net.minecraft.network.encryption.SignatureVerifier;
import net.minecraft.network.encryption.Signer;
import net.minecraft.network.message.FilterMask;
import net.minecraft.network.message.MessageBody;
import net.minecraft.network.message.MessageLink;
import net.minecraft.network.message.MessageSignatureData;
import net.minecraft.network.message.SignedMessage;
import net.minecraft.text.Text;
import net.minecraft.util.TextifiedException;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

/**
 * A class for handling the "message chain".
 * 
 * <p>A message chain (since 1.19.3) is implemented using an integer that is incremented
 * for each message, called "index". {@link MessageLink} represents the link that a
 * particular message has.
 * 
 * <p>Clients signing a message with its preceding message's index is called
 * "packing", and the server creating a signed message with its preceding message's
 * index is called "unpacking". Unpacked messages can then be verified to check the
 * chain's legitimacy.
 * 
 * @see MessageLink
 */
public class MessageChain {
    static final Logger LOGGER = LogUtils.getLogger();
    @Nullable
    MessageLink link;
    Instant lastTimestamp = Instant.EPOCH;

    public MessageChain(UUID sender, UUID sessionId) {
        this.link = MessageLink.of(sender, sessionId);
    }

    public Packer getPacker(Signer signer) {
        return body -> {
            MessageLink messageLink = this.link;
            if (messageLink == null) {
                return null;
            }
            this.link = messageLink.next();
            return new MessageSignatureData(signer.sign(updatable -> SignedMessage.update(updatable, messageLink, body)));
        };
    }

    public Unpacker getUnpacker(final PlayerPublicKey playerPublicKey) {
        final SignatureVerifier signatureVerifier = playerPublicKey.createSignatureInstance();
        return new Unpacker(){

            @Override
            public SignedMessage unpack(@Nullable MessageSignatureData messageSignatureData, MessageBody messageBody) throws MessageChainException {
                if (messageSignatureData == null) {
                    throw new MessageChainException(MessageChainException.MISSING_PROFILE_KEY_EXCEPTION);
                }
                if (playerPublicKey.data().isExpired()) {
                    throw new MessageChainException(MessageChainException.EXPIRED_PROFILE_KEY_EXCEPTION);
                }
                MessageLink messageLink = MessageChain.this.link;
                if (messageLink == null) {
                    throw new MessageChainException(MessageChainException.CHAIN_BROKEN_EXCEPTION);
                }
                if (messageBody.timestamp().isBefore(MessageChain.this.lastTimestamp)) {
                    this.setChainBroken();
                    throw new MessageChainException(MessageChainException.OUT_OF_ORDER_CHAT_EXCEPTION);
                }
                MessageChain.this.lastTimestamp = messageBody.timestamp();
                SignedMessage signedMessage = new SignedMessage(messageLink, messageSignatureData, messageBody, null, FilterMask.PASS_THROUGH);
                if (!signedMessage.verify(signatureVerifier)) {
                    this.setChainBroken();
                    throw new MessageChainException(MessageChainException.INVALID_SIGNATURE_EXCEPTION);
                }
                if (signedMessage.isExpiredOnServer(Instant.now())) {
                    LOGGER.warn("Received expired chat: '{}'. Is the client/server system time unsynchronized?", (Object)messageBody.content());
                }
                MessageChain.this.link = messageLink.next();
                return signedMessage;
            }

            @Override
            public void setChainBroken() {
                MessageChain.this.link = null;
            }
        };
    }

    @FunctionalInterface
    public static interface Packer {
        public static final Packer NONE = body -> null;

        @Nullable
        public MessageSignatureData pack(MessageBody var1);
    }

    public static class MessageChainException
    extends TextifiedException {
        static final Text MISSING_PROFILE_KEY_EXCEPTION = Text.translatable("chat.disabled.missingProfileKey");
        static final Text CHAIN_BROKEN_EXCEPTION = Text.translatable("chat.disabled.chain_broken");
        static final Text EXPIRED_PROFILE_KEY_EXCEPTION = Text.translatable("chat.disabled.expiredProfileKey");
        static final Text INVALID_SIGNATURE_EXCEPTION = Text.translatable("chat.disabled.invalid_signature");
        static final Text OUT_OF_ORDER_CHAT_EXCEPTION = Text.translatable("chat.disabled.out_of_order_chat");

        public MessageChainException(Text message) {
            super(message);
        }
    }

    @FunctionalInterface
    public static interface Unpacker {
        public static Unpacker unsigned(UUID sender, BooleanSupplier secureProfileEnforced) {
            return (signature, body) -> {
                if (secureProfileEnforced.getAsBoolean()) {
                    throw new MessageChainException(MessageChainException.MISSING_PROFILE_KEY_EXCEPTION);
                }
                return SignedMessage.ofUnsigned(sender, body.content());
            };
        }

        public SignedMessage unpack(@Nullable MessageSignatureData var1, MessageBody var2) throws MessageChainException;

        default public void setChainBroken() {
        }
    }
}

