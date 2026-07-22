/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.network.packet.c2s.play;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.listener.ServerPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.PacketType;
import net.minecraft.network.packet.PlayPackets;

public abstract class PlayerMoveC2SPacket
implements Packet<ServerPlayPacketListener> {
    protected final double x;
    protected final double y;
    protected final double z;
    protected final float yaw;
    protected final float pitch;
    protected final boolean onGround;
    protected final boolean changePosition;
    protected final boolean changeLook;

    protected PlayerMoveC2SPacket(double x, double y, double z, float yaw, float pitch, boolean onGround, boolean changePosition, boolean changeLook) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
        this.onGround = onGround;
        this.changePosition = changePosition;
        this.changeLook = changeLook;
    }

    @Override
    public abstract PacketType<? extends PlayerMoveC2SPacket> getPacketId();

    @Override
    public void apply(ServerPlayPacketListener serverPlayPacketListener) {
        serverPlayPacketListener.onPlayerMove(this);
    }

    public double getX(double currentX) {
        return this.changePosition ? this.x : currentX;
    }

    public double getY(double currentY) {
        return this.changePosition ? this.y : currentY;
    }

    public double getZ(double currentZ) {
        return this.changePosition ? this.z : currentZ;
    }

    public float getYaw(float currentYaw) {
        return this.changeLook ? this.yaw : currentYaw;
    }

    public float getPitch(float currentPitch) {
        return this.changeLook ? this.pitch : currentPitch;
    }

    public boolean isOnGround() {
        return this.onGround;
    }

    public boolean changesPosition() {
        return this.changePosition;
    }

    public boolean changesLook() {
        return this.changeLook;
    }

    public static class OnGroundOnly
    extends PlayerMoveC2SPacket {
        public static final PacketCodec<PacketByteBuf, OnGroundOnly> CODEC = Packet.createCodec(OnGroundOnly::write, OnGroundOnly::read);

        public OnGroundOnly(boolean onGround) {
            super(0.0, 0.0, 0.0, 0.0f, 0.0f, onGround, false, false);
        }

        private static OnGroundOnly read(PacketByteBuf buf) {
            boolean bl = buf.readUnsignedByte() != 0;
            return new OnGroundOnly(bl);
        }

        private void write(PacketByteBuf buf) {
            buf.writeByte(this.onGround ? 1 : 0);
        }

        @Override
        public PacketType<OnGroundOnly> getPacketId() {
            return PlayPackets.MOVE_PLAYER_STATUS_ONLY;
        }
    }

    public static class LookAndOnGround
    extends PlayerMoveC2SPacket {
        public static final PacketCodec<PacketByteBuf, LookAndOnGround> CODEC = Packet.createCodec(LookAndOnGround::write, LookAndOnGround::read);

        public LookAndOnGround(float yaw, float pitch, boolean onGround) {
            super(0.0, 0.0, 0.0, yaw, pitch, onGround, false, true);
        }

        private static LookAndOnGround read(PacketByteBuf buf) {
            float f = buf.readFloat();
            float g = buf.readFloat();
            boolean bl = buf.readUnsignedByte() != 0;
            return new LookAndOnGround(f, g, bl);
        }

        private void write(PacketByteBuf buf) {
            buf.writeFloat(this.yaw);
            buf.writeFloat(this.pitch);
            buf.writeByte(this.onGround ? 1 : 0);
        }

        @Override
        public PacketType<LookAndOnGround> getPacketId() {
            return PlayPackets.MOVE_PLAYER_ROT;
        }
    }

    public static class PositionAndOnGround
    extends PlayerMoveC2SPacket {
        public static final PacketCodec<PacketByteBuf, PositionAndOnGround> CODEC = Packet.createCodec(PositionAndOnGround::write, PositionAndOnGround::read);

        public PositionAndOnGround(double x, double y, double z, boolean onGround) {
            super(x, y, z, 0.0f, 0.0f, onGround, true, false);
        }

        private static PositionAndOnGround read(PacketByteBuf buf) {
            double d = buf.readDouble();
            double e = buf.readDouble();
            double f = buf.readDouble();
            boolean bl = buf.readUnsignedByte() != 0;
            return new PositionAndOnGround(d, e, f, bl);
        }

        private void write(PacketByteBuf buf) {
            buf.writeDouble(this.x);
            buf.writeDouble(this.y);
            buf.writeDouble(this.z);
            buf.writeByte(this.onGround ? 1 : 0);
        }

        @Override
        public PacketType<PositionAndOnGround> getPacketId() {
            return PlayPackets.MOVE_PLAYER_POS;
        }
    }

    public static class Full
    extends PlayerMoveC2SPacket {
        public static final PacketCodec<PacketByteBuf, Full> CODEC = Packet.createCodec(Full::write, Full::read);

        public Full(double x, double y, double z, float yaw, float pitch, boolean onGround) {
            super(x, y, z, yaw, pitch, onGround, true, true);
        }

        private static Full read(PacketByteBuf buf) {
            double d = buf.readDouble();
            double e = buf.readDouble();
            double f = buf.readDouble();
            float g = buf.readFloat();
            float h = buf.readFloat();
            boolean bl = buf.readUnsignedByte() != 0;
            return new Full(d, e, f, g, h, bl);
        }

        private void write(PacketByteBuf buf) {
            buf.writeDouble(this.x);
            buf.writeDouble(this.y);
            buf.writeDouble(this.z);
            buf.writeFloat(this.yaw);
            buf.writeFloat(this.pitch);
            buf.writeByte(this.onGround ? 1 : 0);
        }

        @Override
        public PacketType<Full> getPacketId() {
            return PlayPackets.MOVE_PLAYER_POS_ROT;
        }
    }
}

