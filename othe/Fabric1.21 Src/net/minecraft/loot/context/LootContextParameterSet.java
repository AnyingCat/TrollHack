/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.loot.context;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.Consumer;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.context.LootContextParameter;
import net.minecraft.loot.context.LootContextType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public class LootContextParameterSet {
    private final ServerWorld world;
    private final Map<LootContextParameter<?>, Object> parameters;
    private final Map<Identifier, DynamicDrop> dynamicDrops;
    private final float luck;

    public LootContextParameterSet(ServerWorld world, Map<LootContextParameter<?>, Object> parameters, Map<Identifier, DynamicDrop> dynamicDrops, float luck) {
        this.world = world;
        this.parameters = parameters;
        this.dynamicDrops = dynamicDrops;
        this.luck = luck;
    }

    public ServerWorld getWorld() {
        return this.world;
    }

    public boolean contains(LootContextParameter<?> parameter) {
        return this.parameters.containsKey(parameter);
    }

    public <T> T get(LootContextParameter<T> parameter) {
        Object object = this.parameters.get(parameter);
        if (object == null) {
            throw new NoSuchElementException(parameter.getId().toString());
        }
        return (T)object;
    }

    @Nullable
    public <T> T method_51868(LootContextParameter<T> parameter) {
        return (T)this.parameters.get(parameter);
    }

    @Nullable
    public <T> T getOptional(LootContextParameter<T> parameter) {
        return (T)this.parameters.get(parameter);
    }

    public void addDynamicDrops(Identifier id, Consumer<ItemStack> lootConsumer) {
        DynamicDrop dynamicDrop = this.dynamicDrops.get(id);
        if (dynamicDrop != null) {
            dynamicDrop.add(lootConsumer);
        }
    }

    public float getLuck() {
        return this.luck;
    }

    @FunctionalInterface
    public static interface DynamicDrop {
        public void add(Consumer<ItemStack> var1);
    }

    public static class Builder {
        private final ServerWorld world;
        private final Map<LootContextParameter<?>, Object> parameters = Maps.newIdentityHashMap();
        private final Map<Identifier, DynamicDrop> dynamicDrops = Maps.newHashMap();
        private float luck;

        public Builder(ServerWorld world) {
            this.world = world;
        }

        public ServerWorld getWorld() {
            return this.world;
        }

        public <T> Builder add(LootContextParameter<T> parameter, T value) {
            this.parameters.put(parameter, value);
            return this;
        }

        public <T> Builder addOptional(LootContextParameter<T> parameter, @Nullable T value) {
            if (value == null) {
                this.parameters.remove(parameter);
            } else {
                this.parameters.put(parameter, value);
            }
            return this;
        }

        public <T> T get(LootContextParameter<T> parameter) {
            Object object = this.parameters.get(parameter);
            if (object == null) {
                throw new NoSuchElementException(parameter.getId().toString());
            }
            return (T)object;
        }

        @Nullable
        public <T> T getOptional(LootContextParameter<T> parameter) {
            return (T)this.parameters.get(parameter);
        }

        public Builder addDynamicDrop(Identifier id, DynamicDrop dynamicDrop) {
            DynamicDrop dynamicDrop2 = this.dynamicDrops.put(id, dynamicDrop);
            if (dynamicDrop2 != null) {
                throw new IllegalStateException("Duplicated dynamic drop '" + String.valueOf(this.dynamicDrops) + "'");
            }
            return this;
        }

        public Builder luck(float luck) {
            this.luck = luck;
            return this;
        }

        public LootContextParameterSet build(LootContextType contextType) {
            Sets.SetView<LootContextParameter<?>> set = Sets.difference(this.parameters.keySet(), contextType.getAllowed());
            if (!set.isEmpty()) {
                throw new IllegalArgumentException("Parameters not allowed in this parameter set: " + String.valueOf(set));
            }
            Sets.SetView<LootContextParameter<?>> set2 = Sets.difference(contextType.getRequired(), this.parameters.keySet());
            if (!set2.isEmpty()) {
                throw new IllegalArgumentException("Missing required parameters: " + String.valueOf(set2));
            }
            return new LootContextParameterSet(this.world, this.parameters, this.dynamicDrops, this.luck);
        }
    }
}

