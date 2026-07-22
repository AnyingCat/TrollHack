/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.network.message;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.ArrayDeque;
import java.util.List;
import net.minecraft.network.message.MessageBody;
import net.minecraft.network.message.MessageSignatureData;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.VisibleForTesting;

/**
 * Collects message signatures on the server to make a message chain.
 */
public class MessageSignatureStorage {
    public static final int MISSING = -1;
    private static final int MAX_ENTRIES = 128;
    private final MessageSignatureData[] signatures;

    public MessageSignatureStorage(int maxEntries) {
        this.signatures = new MessageSignatureData[maxEntries];
    }

    public static MessageSignatureStorage create() {
        return new MessageSignatureStorage(128);
    }

    public int indexOf(MessageSignatureData signature) {
        for (int i = 0; i < this.signatures.length; ++i) {
            if (!signature.equals(this.signatures[i])) continue;
            return i;
        }
        return -1;
    }

    @Nullable
    public MessageSignatureData get(int index) {
        return this.signatures[index];
    }

    public void add(MessageBody body, @Nullable MessageSignatureData signature) {
        List<MessageSignatureData> list = body.lastSeenMessages().entries();
        ArrayDeque<MessageSignatureData> arrayDeque = new ArrayDeque<MessageSignatureData>(list.size() + 1);
        arrayDeque.addAll(list);
        if (signature != null) {
            arrayDeque.add(signature);
        }
        this.addFrom(arrayDeque);
    }

    @VisibleForTesting
    void addFrom(List<MessageSignatureData> signatures) {
        this.addFrom(new ArrayDeque<MessageSignatureData>(signatures));
    }

    private void addFrom(ArrayDeque<MessageSignatureData> deque) {
        ObjectOpenHashSet<MessageSignatureData> set = new ObjectOpenHashSet<MessageSignatureData>(deque);
        for (int i = 0; !deque.isEmpty() && i < this.signatures.length; ++i) {
            MessageSignatureData messageSignatureData = this.signatures[i];
            this.signatures[i] = deque.removeLast();
            if (messageSignatureData == null || set.contains(messageSignatureData)) continue;
            deque.addFirst(messageSignatureData);
        }
    }
}

