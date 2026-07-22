/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.server;

import com.mojang.datafixers.util.Either;
import io.netty.buffer.ByteBuf;
import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.function.IntFunction;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.text.Text;
import net.minecraft.text.TextCodecs;
import net.minecraft.util.function.ValueLists;

public record ServerLinks(List<Entry> entries) {
    public static final ServerLinks EMPTY = new ServerLinks(List.of());
    public static final PacketCodec<ByteBuf, Either<Known, Text>> TYPE_CODEC = PacketCodecs.either(Known.CODEC, TextCodecs.PACKET_CODEC);
    public static final PacketCodec<ByteBuf, List<StringifiedEntry>> LIST_CODEC = StringifiedEntry.CODEC.collect(PacketCodecs.toList());

    public boolean isEmpty() {
        return this.entries.isEmpty();
    }

    public Optional<Entry> getEntryFor(Known known) {
        return this.entries.stream().filter(entry -> entry.type.map(type -> type == known, text -> false)).findFirst();
    }

    public List<StringifiedEntry> getLinks() {
        return this.entries.stream().map(entry -> new StringifiedEntry(entry.type, entry.link.toString())).toList();
    }

    public static enum Known {
        BUG_REPORT(0, "report_bug"),
        COMMUNITY_GUIDELINES(1, "community_guidelines"),
        SUPPORT(2, "support"),
        STATUS(3, "status"),
        FEEDBACK(4, "feedback"),
        COMMUNITY(5, "community"),
        WEBSITE(6, "website"),
        FORUMS(7, "forums"),
        NEWS(8, "news"),
        ANNOUNCEMENTS(9, "announcements");

        private static final IntFunction<Known> FROM_ID;
        public static final PacketCodec<ByteBuf, Known> CODEC;
        private final int id;
        private final String name;

        private Known(int id, String name) {
            this.id = id;
            this.name = name;
        }

        private Text getText() {
            return Text.translatable("known_server_link." + this.name);
        }

        public Entry createEntry(URI link) {
            return Entry.create(this, link);
        }

        static {
            FROM_ID = ValueLists.createIdToValueFunction(known -> known.id, Known.values(), ValueLists.OutOfBoundsHandling.ZERO);
            CODEC = PacketCodecs.indexed(FROM_ID, known -> known.id);
        }
    }

    public record StringifiedEntry(Either<Known, Text> type, String link) {
        public static final PacketCodec<ByteBuf, StringifiedEntry> CODEC = PacketCodec.tuple(TYPE_CODEC, StringifiedEntry::type, PacketCodecs.STRING, StringifiedEntry::link, StringifiedEntry::new);
    }

    public record Entry(Either<Known, Text> type, URI link) {
        public static Entry create(Known known, URI link) {
            return new Entry(Either.left(known), link);
        }

        public static Entry create(Text name, URI link) {
            return new Entry(Either.right(name), link);
        }

        public Text getText() {
            return this.type.map(Known::getText, text -> text);
        }
    }
}

