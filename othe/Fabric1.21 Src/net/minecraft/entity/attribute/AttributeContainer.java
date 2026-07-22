/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.entity.attribute;

import com.google.common.collect.Multimap;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class AttributeContainer {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final Map<RegistryEntry<EntityAttribute>, EntityAttributeInstance> custom = new Object2ObjectOpenHashMap<RegistryEntry<EntityAttribute>, EntityAttributeInstance>();
    private final Set<EntityAttributeInstance> tracked = new ObjectOpenHashSet<EntityAttributeInstance>();
    private final Set<EntityAttributeInstance> pendingUpdate = new ObjectOpenHashSet<EntityAttributeInstance>();
    private final DefaultAttributeContainer fallback;

    public AttributeContainer(DefaultAttributeContainer defaultAttributes) {
        this.fallback = defaultAttributes;
    }

    private void updateTrackedStatus(EntityAttributeInstance instance) {
        this.pendingUpdate.add(instance);
        if (instance.getAttribute().value().isTracked()) {
            this.tracked.add(instance);
        }
    }

    public Set<EntityAttributeInstance> getTracked() {
        return this.tracked;
    }

    public Set<EntityAttributeInstance> getPendingUpdate() {
        return this.pendingUpdate;
    }

    public Collection<EntityAttributeInstance> getAttributesToSend() {
        return this.custom.values().stream().filter(attribute -> attribute.getAttribute().value().isTracked()).collect(Collectors.toList());
    }

    @Nullable
    public EntityAttributeInstance getCustomInstance(RegistryEntry<EntityAttribute> attribute2) {
        return this.custom.computeIfAbsent(attribute2, attribute -> this.fallback.createOverride(this::updateTrackedStatus, (RegistryEntry<EntityAttribute>)attribute));
    }

    public boolean hasAttribute(RegistryEntry<EntityAttribute> attribute) {
        return this.custom.get(attribute) != null || this.fallback.has(attribute);
    }

    public boolean hasModifierForAttribute(RegistryEntry<EntityAttribute> attribute, Identifier id) {
        EntityAttributeInstance entityAttributeInstance = this.custom.get(attribute);
        return entityAttributeInstance != null ? entityAttributeInstance.getModifier(id) != null : this.fallback.hasModifier(attribute, id);
    }

    public double getValue(RegistryEntry<EntityAttribute> attribute) {
        EntityAttributeInstance entityAttributeInstance = this.custom.get(attribute);
        return entityAttributeInstance != null ? entityAttributeInstance.getValue() : this.fallback.getValue(attribute);
    }

    public double getBaseValue(RegistryEntry<EntityAttribute> attribute) {
        EntityAttributeInstance entityAttributeInstance = this.custom.get(attribute);
        return entityAttributeInstance != null ? entityAttributeInstance.getBaseValue() : this.fallback.getBaseValue(attribute);
    }

    public double getModifierValue(RegistryEntry<EntityAttribute> attribute, Identifier id) {
        EntityAttributeInstance entityAttributeInstance = this.custom.get(attribute);
        return entityAttributeInstance != null ? entityAttributeInstance.getModifier(id).value() : this.fallback.getModifierValue(attribute, id);
    }

    public void addTemporaryModifiers(Multimap<RegistryEntry<EntityAttribute>, EntityAttributeModifier> modifiersMap) {
        modifiersMap.forEach((attribute, modifier) -> {
            EntityAttributeInstance entityAttributeInstance = this.getCustomInstance((RegistryEntry<EntityAttribute>)attribute);
            if (entityAttributeInstance != null) {
                entityAttributeInstance.removeModifier(modifier.id());
                entityAttributeInstance.addTemporaryModifier((EntityAttributeModifier)modifier);
            }
        });
    }

    public void removeModifiers(Multimap<RegistryEntry<EntityAttribute>, EntityAttributeModifier> modifiersMap) {
        modifiersMap.asMap().forEach((attribute, modifiers) -> {
            EntityAttributeInstance entityAttributeInstance = this.custom.get(attribute);
            if (entityAttributeInstance != null) {
                modifiers.forEach(modifier -> entityAttributeInstance.removeModifier(modifier.id()));
            }
        });
    }

    public void setFrom(AttributeContainer other) {
        other.custom.values().forEach(attributeInstance -> {
            EntityAttributeInstance entityAttributeInstance = this.getCustomInstance(attributeInstance.getAttribute());
            if (entityAttributeInstance != null) {
                entityAttributeInstance.setFrom((EntityAttributeInstance)attributeInstance);
            }
        });
    }

    public void setBaseFrom(AttributeContainer other) {
        other.custom.values().forEach(attributeInstance -> {
            EntityAttributeInstance entityAttributeInstance = this.getCustomInstance(attributeInstance.getAttribute());
            if (entityAttributeInstance != null) {
                entityAttributeInstance.setBaseValue(attributeInstance.getBaseValue());
            }
        });
    }

    public NbtList toNbt() {
        NbtList nbtList = new NbtList();
        for (EntityAttributeInstance entityAttributeInstance : this.custom.values()) {
            nbtList.add(entityAttributeInstance.toNbt());
        }
        return nbtList;
    }

    public void readNbt(NbtList nbt) {
        for (int i = 0; i < nbt.size(); ++i) {
            NbtCompound nbtCompound = nbt.getCompound(i);
            String string = nbtCompound.getString("id");
            Identifier identifier = Identifier.tryParse(string);
            if (identifier != null) {
                Util.ifPresentOrElse(Registries.ATTRIBUTE.getEntry(identifier), attribute -> {
                    EntityAttributeInstance entityAttributeInstance = this.getCustomInstance((RegistryEntry<EntityAttribute>)attribute);
                    if (entityAttributeInstance != null) {
                        entityAttributeInstance.readNbt(nbtCompound);
                    }
                }, () -> LOGGER.warn("Ignoring unknown attribute '{}'", (Object)identifier));
                continue;
            }
            LOGGER.warn("Ignoring malformed attribute '{}'", (Object)string);
        }
    }
}

