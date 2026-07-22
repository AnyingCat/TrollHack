/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.scoreboard.number;

import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.scoreboard.number.NumberFormat;
import net.minecraft.scoreboard.number.NumberFormatType;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

public class BlankNumberFormat
implements NumberFormat {
    public static final BlankNumberFormat INSTANCE = new BlankNumberFormat();
    public static final NumberFormatType<BlankNumberFormat> TYPE = new NumberFormatType<BlankNumberFormat>(){
        private static final MapCodec<BlankNumberFormat> CODEC = MapCodec.unit(INSTANCE);
        private static final PacketCodec<RegistryByteBuf, BlankNumberFormat> PACKET_CODEC = PacketCodec.unit(INSTANCE);

        @Override
        public MapCodec<BlankNumberFormat> getCodec() {
            return CODEC;
        }

        @Override
        public PacketCodec<RegistryByteBuf, BlankNumberFormat> getPacketCodec() {
            return PACKET_CODEC;
        }
    };

    @Override
    public MutableText format(int number) {
        return Text.empty();
    }

    public NumberFormatType<BlankNumberFormat> getType() {
        return TYPE;
    }
}

