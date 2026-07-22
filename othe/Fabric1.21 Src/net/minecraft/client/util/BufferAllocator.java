/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.client.util;

import com.mojang.logging.LogUtils;
import java.nio.ByteBuffer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.system.MemoryUtil;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public class BufferAllocator
implements AutoCloseable {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final MemoryUtil.MemoryAllocator allocator = MemoryUtil.getAllocator(false);
    private static final int MIN_GROWTH = 0x200000;
    private static final int CLOSED = -1;
    long pointer;
    private int size;
    private int offset;
    private int prevOffset;
    private int refCount;
    private int clearCount;

    public BufferAllocator(int size) {
        this.size = size;
        this.pointer = allocator.malloc(size);
        if (this.pointer == 0L) {
            throw new OutOfMemoryError("Failed to allocate " + size + " bytes");
        }
    }

    public long allocate(int size) {
        int i = this.offset;
        int j = i + size;
        this.growIfNecessary(j);
        this.offset = j;
        return this.pointer + (long)i;
    }

    private void growIfNecessary(int targetSize) {
        if (targetSize > this.size) {
            int i = Math.min(this.size, 0x200000);
            int j = Math.max(this.size + i, targetSize);
            this.grow(j);
        }
    }

    private void grow(int targetSize) {
        this.pointer = allocator.realloc(this.pointer, targetSize);
        LOGGER.debug("Needed to grow BufferBuilder buffer: Old size {} bytes, new size {} bytes.", (Object)this.size, (Object)targetSize);
        if (this.pointer == 0L) {
            throw new OutOfMemoryError("Failed to resize buffer from " + this.size + " bytes to " + targetSize + " bytes");
        }
        this.size = targetSize;
    }

    @Nullable
    public CloseableBuffer getAllocated() {
        this.ensureNotFreed();
        int i = this.prevOffset;
        int j = this.offset - i;
        if (j == 0) {
            return null;
        }
        this.prevOffset = this.offset;
        ++this.refCount;
        return new CloseableBuffer(i, j, this.clearCount);
    }

    public void clear() {
        if (this.refCount > 0) {
            LOGGER.warn("Clearing BufferBuilder with unused batches");
        }
        this.reset();
    }

    public void reset() {
        this.ensureNotFreed();
        if (this.refCount > 0) {
            this.forceClear();
            this.refCount = 0;
        }
    }

    boolean clearCountEquals(int clearCount) {
        return clearCount == this.clearCount;
    }

    void clearIfUnreferenced() {
        if (--this.refCount <= 0) {
            this.forceClear();
        }
    }

    private void forceClear() {
        int i = this.offset - this.prevOffset;
        if (i > 0) {
            MemoryUtil.memCopy(this.pointer + (long)this.prevOffset, this.pointer, i);
        }
        this.offset = i;
        this.prevOffset = 0;
        ++this.clearCount;
    }

    @Override
    public void close() {
        if (this.pointer != 0L) {
            allocator.free(this.pointer);
            this.pointer = 0L;
            this.clearCount = -1;
        }
    }

    private void ensureNotFreed() {
        if (this.pointer == 0L) {
            throw new IllegalStateException("Buffer has been freed");
        }
    }

    @Environment(value=EnvType.CLIENT)
    public class CloseableBuffer
    implements AutoCloseable {
        private final int offset;
        private final int size;
        private final int clearCount;
        private boolean closed;

        CloseableBuffer(int offset, int size, int clearCount) {
            this.offset = offset;
            this.size = size;
            this.clearCount = clearCount;
        }

        public ByteBuffer getBuffer() {
            if (!BufferAllocator.this.clearCountEquals(this.clearCount)) {
                throw new IllegalStateException("Buffer is no longer valid");
            }
            return MemoryUtil.memByteBuffer(BufferAllocator.this.pointer + (long)this.offset, this.size);
        }

        @Override
        public void close() {
            if (this.closed) {
                return;
            }
            this.closed = true;
            if (BufferAllocator.this.clearCountEquals(this.clearCount)) {
                BufferAllocator.this.clearIfUnreferenced();
            }
        }
    }
}

