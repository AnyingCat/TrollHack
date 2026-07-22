/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.network.packet.s2c.play;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Multimap;
import com.mojang.authlib.GameProfile;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.encryption.PublicPlayerSession;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.PacketType;
import net.minecraft.network.packet.PlayPackets;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.text.TextCodecs;
import net.minecraft.util.Nullables;
import net.minecraft.world.GameMode;
import org.jetbrains.annotations.Nullable;

public class PlayerListS2CPacket
implements Packet<ClientPlayPacketListener> {
    public static final PacketCodec<RegistryByteBuf, PlayerListS2CPacket> CODEC = Packet.createCodec(PlayerListS2CPacket::write, PlayerListS2CPacket::new);
    private final EnumSet<Action> actions;
    private final List<Entry> entries;

    public PlayerListS2CPacket(EnumSet<Action> actions, Collection<ServerPlayerEntity> players) {
        this.actions = actions;
        this.entries = players.stream().map(Entry::new).toList();
    }

    public PlayerListS2CPacket(Action action, ServerPlayerEntity player) {
        this.actions = EnumSet.of(action);
        this.entries = List.of(new Entry(player));
    }

    public static PlayerListS2CPacket entryFromPlayer(Collection<ServerPlayerEntity> players) {
        EnumSet<Action[]> enumSet = EnumSet.of(Action.ADD_PLAYER, new Action[]{Action.INITIALIZE_CHAT, Action.UPDATE_GAME_MODE, Action.UPDATE_LISTED, Action.UPDATE_LATENCY, Action.UPDATE_DISPLAY_NAME});
        return new PlayerListS2CPacket(enumSet, players);
    }

    private PlayerListS2CPacket(RegistryByteBuf buf) {
        this.actions = buf.readEnumSet(Action.class);
        this.entries = buf.readList(buf2 -> {
            Serialized serialized = new Serialized(buf2.readUuid());
            for (Action action : this.actions) {
                action.reader.read(serialized, (RegistryByteBuf)buf2);
            }
            return serialized.toEntry();
        });
    }

    private void write(RegistryByteBuf buf) {
        buf.writeEnumSet(this.actions, Action.class);
        buf.writeCollection(this.entries, (buf2, entry) -> {
            buf2.writeUuid(entry.profileId());
            for (Action action : this.actions) {
                action.writer.write((RegistryByteBuf)buf2, (Entry)entry);
            }
        });
    }

    @Override
    public PacketType<PlayerListS2CPacket> getPacketId() {
        return PlayPackets.PLAYER_INFO_UPDATE;
    }

    @Override
    public void apply(ClientPlayPacketListener clientPlayPacketListener) {
        clientPlayPacketListener.onPlayerList(this);
    }

    public EnumSet<Action> getActions() {
        return this.actions;
    }

    public List<Entry> getEntries() {
        return this.entries;
    }

    public List<Entry> getPlayerAdditionEntries() {
        return this.actions.contains((Object)Action.ADD_PLAYER) ? this.entries : List.of();
    }

    public String toString() {
        return MoreObjects.toStringHelper(this).add("actions", this.actions).add("entries", this.entries).toString();
    }

    public record Entry(UUID profileId, @Nullable GameProfile profile, boolean listed, int latency, GameMode gameMode, @Nullable Text displayName, @Nullable PublicPlayerSession.Serialized chatSession) {
        Entry(ServerPlayerEntity player) {
            this(player.getUuid(), player.getGameProfile(), true, player.networkHandler.getLatency(), player.interactionManager.getGameMode(), player.getPlayerListName(), Nullables.map(player.getSession(), PublicPlayerSession::toSerialized));
        }

        @Nullable
        public GameProfile profile() {
            return this.profile;
        }

        @Nullable
        public Text displayName() {
            return this.displayName;
        }

        @Nullable
        public PublicPlayerSession.Serialized chatSession() {
            return this.chatSession;
        }
    }

    public static enum Action {
        ADD_PLAYER((serialized, buf) -> {
            GameProfile gameProfile = new GameProfile(serialized.profileId, buf.readString(16));
            gameProfile.getProperties().putAll((Multimap)PacketCodecs.PROPERTY_MAP.decode(buf));
            serialized.gameProfile = gameProfile;
        }, (buf, entry) -> {
            GameProfile gameProfile = Objects.requireNonNull(entry.profile());
            buf.writeString(gameProfile.getName(), 16);
            PacketCodecs.PROPERTY_MAP.encode(buf, gameProfile.getProperties());
        }),
        INITIALIZE_CHAT((serialized, buf) -> {
            serialized.session = buf.readNullable(PublicPlayerSession.Serialized::fromBuf);
        }, (buf, entry) -> buf.writeNullable(entry.chatSession, PublicPlayerSession.Serialized::write)),
        UPDATE_GAME_MODE((serialized, buf) -> {
            serialized.gameMode = GameMode.byId(buf.readVarInt());
        }, (buf, entry) -> buf.writeVarInt(entry.gameMode().getId())),
        UPDATE_LISTED((serialized, buf) -> {
            serialized.listed = buf.readBoolean();
        }, (buf, entry) -> buf.writeBoolean(entry.listed())),
        UPDATE_LATENCY((serialized, buf) -> {
            serialized.latency = buf.readVarInt();
        }, (buf, entry) -> buf.writeVarInt(entry.latency())),
        UPDATE_DISPLAY_NAME((serialized, buf) -> {
            serialized.displayName = PacketByteBuf.readNullable(buf, TextCodecs.UNLIMITED_REGISTRY_PACKET_CODEC);
        }, (buf, entry) -> PacketByteBuf.writeNullable(buf, entry.displayName(), TextCodecs.UNLIMITED_REGISTRY_PACKET_CODEC));

        final Reader reader;
        final Writer writer;

        private Action(Reader reader, Writer writer) {
            this.reader = reader;
            this.writer = writer;
        }

        public static interface Reader {
            public void read(Serialized var1, RegistryByteBuf var2);
        }

        public static interface Writer {
            public void write(RegistryByteBuf var1, Entry var2);
        }
    }

    static class Serialized {
        final UUID profileId;
        @Nullable
        GameProfile gameProfile;
        boolean listed;
        int latency;
        GameMode gameMode = GameMode.DEFAULT;
        @Nullable
        Text displayName;
        @Nullable
        PublicPlayerSession.Serialized session;

        Serialized(UUID profileId) {
            this.profileId = profileId;
        }

        Entry toEntry() {
            return new Entry(this.profileId, this.gameProfile, this.listed, this.latency, this.gameMode, this.displayName, this.session);
        }
    }
}

