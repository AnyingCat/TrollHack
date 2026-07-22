/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.stat;

import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Map;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.Registry;
import net.minecraft.stat.Stat;
import net.minecraft.stat.StatFormatter;
import net.minecraft.text.Text;

public class StatType<T>
implements Iterable<Stat<T>> {
    private final Registry<T> registry;
    private final Map<T, Stat<T>> stats = new IdentityHashMap<T, Stat<T>>();
    private final Text name;
    private final PacketCodec<RegistryByteBuf, Stat<T>> packetCodec;

    public StatType(Registry<T> registry, Text name) {
        this.registry = registry;
        this.name = name;
        this.packetCodec = PacketCodecs.registryValue(registry.getKey()).xmap(this::getOrCreateStat, Stat::getValue);
    }

    public PacketCodec<RegistryByteBuf, Stat<T>> getPacketCodec() {
        return this.packetCodec;
    }

    public boolean hasStat(T key) {
        return this.stats.containsKey(key);
    }

    public Stat<T> getOrCreateStat(T key, StatFormatter formatter) {
        return this.stats.computeIfAbsent(key, value -> new Stat<Object>(this, value, formatter));
    }

    public Registry<T> getRegistry() {
        return this.registry;
    }

    @Override
    public Iterator<Stat<T>> iterator() {
        return this.stats.values().iterator();
    }

    public Stat<T> getOrCreateStat(T key) {
        return this.getOrCreateStat(key, StatFormatter.DEFAULT);
    }

    public Text getName() {
        return this.name;
    }
}

