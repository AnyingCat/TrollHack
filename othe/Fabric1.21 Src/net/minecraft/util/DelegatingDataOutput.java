/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.util;

import java.io.DataOutput;
import java.io.IOException;

public class DelegatingDataOutput
implements DataOutput {
    private final DataOutput delegate;

    public DelegatingDataOutput(DataOutput delegate) {
        this.delegate = delegate;
    }

    @Override
    public void write(int v) throws IOException {
        this.delegate.write(v);
    }

    @Override
    public void write(byte[] b) throws IOException {
        this.delegate.write(b);
    }

    @Override
    public void write(byte[] bs, int off, int len) throws IOException {
        this.delegate.write(bs, off, len);
    }

    @Override
    public void writeBoolean(boolean v) throws IOException {
        this.delegate.writeBoolean(v);
    }

    @Override
    public void writeByte(int v) throws IOException {
        this.delegate.writeByte(v);
    }

    @Override
    public void writeShort(int v) throws IOException {
        this.delegate.writeShort(v);
    }

    @Override
    public void writeChar(int v) throws IOException {
        this.delegate.writeChar(v);
    }

    @Override
    public void writeInt(int v) throws IOException {
        this.delegate.writeInt(v);
    }

    @Override
    public void writeLong(long v) throws IOException {
        this.delegate.writeLong(v);
    }

    @Override
    public void writeFloat(float v) throws IOException {
        this.delegate.writeFloat(v);
    }

    @Override
    public void writeDouble(double v) throws IOException {
        this.delegate.writeDouble(v);
    }

    @Override
    public void writeBytes(String s) throws IOException {
        this.delegate.writeBytes(s);
    }

    @Override
    public void writeChars(String s) throws IOException {
        this.delegate.writeChars(s);
    }

    @Override
    public void writeUTF(String s) throws IOException {
        this.delegate.writeUTF(s);
    }
}

