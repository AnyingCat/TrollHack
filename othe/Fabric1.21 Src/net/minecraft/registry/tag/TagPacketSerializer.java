/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.registry.tag;

import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.CombinedDynamicRegistries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.SerializableRegistries;
import net.minecraft.registry.ServerDynamicRegistryType;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

public class TagPacketSerializer {
    public static Map<RegistryKey<? extends Registry<?>>, Serialized> serializeTags(CombinedDynamicRegistries<ServerDynamicRegistryType> dynamicRegistryManager) {
        return SerializableRegistries.streamRegistryManagerEntries(dynamicRegistryManager).map(registry -> Pair.of(registry.key(), TagPacketSerializer.serializeTags(registry.value()))).filter(pair -> ((Serialized)pair.getSecond()).size() > 0).collect(Collectors.toMap(Pair::getFirst, Pair::getSecond));
    }

    private static <T> Serialized serializeTags(Registry<T> registry) {
        HashMap<Identifier, IntList> map = new HashMap<Identifier, IntList>();
        registry.streamTagsAndEntries().forEach(pair -> {
            RegistryEntryList registryEntryList = (RegistryEntryList)pair.getSecond();
            IntArrayList intList = new IntArrayList(registryEntryList.size());
            for (RegistryEntry registryEntry : registryEntryList) {
                if (registryEntry.getType() != RegistryEntry.Type.REFERENCE) {
                    throw new IllegalStateException("Can't serialize unregistered value " + String.valueOf(registryEntry));
                }
                intList.add(registry.getRawId(registryEntry.value()));
            }
            map.put(((TagKey)pair.getFirst()).id(), intList);
        });
        return new Serialized(map);
    }

    static <T> void loadTags(RegistryKey<? extends Registry<T>> registryKey, Registry<T> registry, Serialized serialized, Loader<T> loader) {
        serialized.contents.forEach((tagId, rawIds) -> {
            TagKey tagKey = TagKey.of(registryKey, tagId);
            List list = rawIds.intStream().mapToObj(registry::getEntry).flatMap(Optional::stream).collect(Collectors.toUnmodifiableList());
            loader.accept(tagKey, list);
        });
    }

    public static final class Serialized {
        final Map<Identifier, IntList> contents;

        Serialized(Map<Identifier, IntList> contents) {
            this.contents = contents;
        }

        public void writeBuf(PacketByteBuf buf) {
            buf.writeMap(this.contents, PacketByteBuf::writeIdentifier, PacketByteBuf::writeIntList);
        }

        public static Serialized fromBuf(PacketByteBuf buf) {
            return new Serialized(buf.readMap(PacketByteBuf::readIdentifier, PacketByteBuf::readIntList));
        }

        public int size() {
            return this.contents.size();
        }

        public <T> void loadTo(Registry<T> registry) {
            if (this.size() == 0) {
                return;
            }
            HashMap map = new HashMap(this.size());
            TagPacketSerializer.loadTags(registry.getKey(), registry, this, map::put);
            registry.populateTags(map);
        }
    }

    @FunctionalInterface
    public static interface Loader<T> {
        public void accept(TagKey<T> var1, List<RegistryEntry<T>> var2);
    }
}

