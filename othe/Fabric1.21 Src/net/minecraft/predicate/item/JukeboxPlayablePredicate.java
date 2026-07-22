/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.predicate.item;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.block.jukebox.JukeboxSong;
import net.minecraft.component.ComponentType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.JukeboxPlayableComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.predicate.item.ComponentSubPredicate;
import net.minecraft.registry.RegistryCodecs;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryList;

public record JukeboxPlayablePredicate(Optional<RegistryEntryList<JukeboxSong>> song) implements ComponentSubPredicate<JukeboxPlayableComponent>
{
    public static final Codec<JukeboxPlayablePredicate> CODEC = RecordCodecBuilder.create(instance -> instance.group(RegistryCodecs.entryList(RegistryKeys.JUKEBOX_SONG).optionalFieldOf("song").forGetter(JukeboxPlayablePredicate::song)).apply((Applicative<JukeboxPlayablePredicate, ?>)instance, JukeboxPlayablePredicate::new));

    @Override
    public ComponentType<JukeboxPlayableComponent> getComponentType() {
        return DataComponentTypes.JUKEBOX_PLAYABLE;
    }

    @Override
    public boolean test(ItemStack itemStack, JukeboxPlayableComponent jukeboxPlayableComponent) {
        if (this.song.isPresent()) {
            boolean bl = false;
            for (RegistryEntry registryEntry : this.song.get()) {
                Optional optional = registryEntry.getKey();
                if (optional.isEmpty() || optional.get() != jukeboxPlayableComponent.song().key()) continue;
                bl = true;
                break;
            }
            return bl;
        }
        return true;
    }

    public static JukeboxPlayablePredicate empty() {
        return new JukeboxPlayablePredicate(Optional.empty());
    }
}

