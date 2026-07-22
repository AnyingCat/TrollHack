/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.client.sound;

import it.unimi.dsi.fastutil.floats.FloatConsumer;
import java.io.IOException;
import java.nio.ByteBuffer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.sound.ChannelList;
import net.minecraft.client.sound.NonRepeatingAudioStream;

@Environment(value=EnvType.CLIENT)
public interface BufferedAudioStream
extends NonRepeatingAudioStream {
    public static final int CHUNK_SIZE = 8192;

    public boolean read(FloatConsumer var1) throws IOException;

    @Override
    default public ByteBuffer read(int size) throws IOException {
        ChannelList channelList = new ChannelList(size + 8192);
        while (this.read(channelList) && channelList.getCurrentBufferSize() < size) {
        }
        return channelList.getBuffer();
    }

    @Override
    default public ByteBuffer readAll() throws IOException {
        ChannelList channelList = new ChannelList(16384);
        while (this.read(channelList)) {
        }
        return channelList.getBuffer();
    }
}

