/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.client.sound;

import java.io.IOException;
import java.nio.ByteBuffer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.sound.AudioStream;

@Environment(value=EnvType.CLIENT)
public interface NonRepeatingAudioStream
extends AudioStream {
    public ByteBuffer readAll() throws IOException;
}

