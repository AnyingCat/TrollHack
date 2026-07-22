/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.client.render.chunk;

import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;
import java.util.List;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.util.BufferAllocator;
import net.minecraft.util.Util;

@Environment(value=EnvType.CLIENT)
public class BlockBufferAllocatorStorage
implements AutoCloseable {
    private static final List<RenderLayer> BLOCK_LAYERS = RenderLayer.getBlockLayers();
    public static final int EXPECTED_TOTAL_SIZE = BLOCK_LAYERS.stream().mapToInt(RenderLayer::getExpectedBufferSize).sum();
    private final Map<RenderLayer, BufferAllocator> allocators = Util.make(new Reference2ObjectArrayMap(BLOCK_LAYERS.size()), map -> {
        for (RenderLayer renderLayer : BLOCK_LAYERS) {
            map.put(renderLayer, new BufferAllocator(renderLayer.getExpectedBufferSize()));
        }
    });

    public BufferAllocator get(RenderLayer layer) {
        return this.allocators.get(layer);
    }

    public void clear() {
        this.allocators.values().forEach(BufferAllocator::clear);
    }

    public void reset() {
        this.allocators.values().forEach(BufferAllocator::reset);
    }

    @Override
    public void close() {
        this.allocators.values().forEach(BufferAllocator::close);
    }
}

