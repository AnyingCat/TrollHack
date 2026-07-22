/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.advancement;

import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import net.minecraft.advancement.AdvancementEntry;
import net.minecraft.advancement.PlacedAdvancement;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class AdvancementManager {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final Map<Identifier, PlacedAdvancement> advancements = new Object2ObjectOpenHashMap<Identifier, PlacedAdvancement>();
    private final Set<PlacedAdvancement> roots = new ObjectLinkedOpenHashSet<PlacedAdvancement>();
    private final Set<PlacedAdvancement> dependents = new ObjectLinkedOpenHashSet<PlacedAdvancement>();
    @Nullable
    private Listener listener;

    private void remove(PlacedAdvancement advancement) {
        for (PlacedAdvancement placedAdvancement : advancement.getChildren()) {
            this.remove(placedAdvancement);
        }
        LOGGER.info("Forgot about advancement {}", (Object)advancement.getAdvancementEntry());
        this.advancements.remove(advancement.getAdvancementEntry().id());
        if (advancement.getParent() == null) {
            this.roots.remove(advancement);
            if (this.listener != null) {
                this.listener.onRootRemoved(advancement);
            }
        } else {
            this.dependents.remove(advancement);
            if (this.listener != null) {
                this.listener.onDependentRemoved(advancement);
            }
        }
    }

    public void removeAll(Set<Identifier> advancements) {
        for (Identifier identifier : advancements) {
            PlacedAdvancement placedAdvancement = this.advancements.get(identifier);
            if (placedAdvancement == null) {
                LOGGER.warn("Told to remove advancement {} but I don't know what that is", (Object)identifier);
                continue;
            }
            this.remove(placedAdvancement);
        }
    }

    public void addAll(Collection<AdvancementEntry> advancements) {
        ArrayList<AdvancementEntry> list = new ArrayList<AdvancementEntry>(advancements);
        while (!list.isEmpty()) {
            if (list.removeIf(this::tryAdd)) continue;
            LOGGER.error("Couldn't load advancements: {}", (Object)list);
            break;
        }
        LOGGER.info("Loaded {} advancements", (Object)this.advancements.size());
    }

    private boolean tryAdd(AdvancementEntry advancement) {
        Optional<Identifier> optional = advancement.value().parent();
        PlacedAdvancement placedAdvancement = optional.map(this.advancements::get).orElse(null);
        if (placedAdvancement == null && optional.isPresent()) {
            return false;
        }
        PlacedAdvancement placedAdvancement2 = new PlacedAdvancement(advancement, placedAdvancement);
        if (placedAdvancement != null) {
            placedAdvancement.addChild(placedAdvancement2);
        }
        this.advancements.put(advancement.id(), placedAdvancement2);
        if (placedAdvancement == null) {
            this.roots.add(placedAdvancement2);
            if (this.listener != null) {
                this.listener.onRootAdded(placedAdvancement2);
            }
        } else {
            this.dependents.add(placedAdvancement2);
            if (this.listener != null) {
                this.listener.onDependentAdded(placedAdvancement2);
            }
        }
        return true;
    }

    public void clear() {
        this.advancements.clear();
        this.roots.clear();
        this.dependents.clear();
        if (this.listener != null) {
            this.listener.onClear();
        }
    }

    public Iterable<PlacedAdvancement> getRoots() {
        return this.roots;
    }

    public Collection<PlacedAdvancement> getAdvancements() {
        return this.advancements.values();
    }

    @Nullable
    public PlacedAdvancement get(Identifier id) {
        return this.advancements.get(id);
    }

    @Nullable
    public PlacedAdvancement get(AdvancementEntry advancement) {
        return this.advancements.get(advancement.id());
    }

    public void setListener(@Nullable Listener listener) {
        this.listener = listener;
        if (listener != null) {
            for (PlacedAdvancement placedAdvancement : this.roots) {
                listener.onRootAdded(placedAdvancement);
            }
            for (PlacedAdvancement placedAdvancement : this.dependents) {
                listener.onDependentAdded(placedAdvancement);
            }
        }
    }

    public static interface Listener {
        public void onRootAdded(PlacedAdvancement var1);

        public void onRootRemoved(PlacedAdvancement var1);

        public void onDependentAdded(PlacedAdvancement var1);

        public void onDependentRemoved(PlacedAdvancement var1);

        public void onClear();
    }
}

