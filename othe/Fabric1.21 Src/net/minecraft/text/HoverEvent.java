/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.text;

import com.google.gson.JsonParser;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.component.ComponentChanges;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.StringNbtReader;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryOps;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.Text;
import net.minecraft.text.TextCodecs;
import net.minecraft.util.Identifier;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.Uuids;
import org.jetbrains.annotations.Nullable;

public class HoverEvent {
    public static final Codec<HoverEvent> CODEC = Codec.withAlternative(EventData.CODEC.codec(), EventData.LEGACY_CODEC.codec()).xmap(HoverEvent::new, event -> event.data);
    private final EventData<?> data;

    public <T> HoverEvent(Action<T> action, T contents) {
        this(new EventData<T>(action, contents));
    }

    private HoverEvent(EventData<?> data) {
        this.data = data;
    }

    public Action<?> getAction() {
        return this.data.action;
    }

    @Nullable
    public <T> T getValue(Action<T> action) {
        if (this.data.action == action) {
            return action.cast(this.data.value);
        }
        return null;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        return ((HoverEvent)o).data.equals(this.data);
    }

    public String toString() {
        return this.data.toString();
    }

    public int hashCode() {
        return this.data.hashCode();
    }

    record EventData<T>(Action<T> action, T value) {
        public static final MapCodec<EventData<?>> CODEC = Action.CODEC.dispatchMap("action", EventData::action, action -> action.codec);
        public static final MapCodec<EventData<?>> LEGACY_CODEC = Action.CODEC.dispatchMap("action", EventData::action, action -> action.legacyCodec);
    }

    public static class Action<T>
    implements StringIdentifiable {
        public static final Action<Text> SHOW_TEXT = new Action<Text>("show_text", true, TextCodecs.CODEC, (text, ops) -> DataResult.success(text));
        public static final Action<ItemStackContent> SHOW_ITEM = new Action<ItemStackContent>("show_item", true, ItemStackContent.CODEC, ItemStackContent::legacySerializer);
        public static final Action<EntityContent> SHOW_ENTITY = new Action<EntityContent>("show_entity", true, EntityContent.CODEC, EntityContent::legacySerializer);
        public static final Codec<Action<?>> UNVALIDATED_CODEC = StringIdentifiable.createBasicCodec(() -> new Action[]{SHOW_TEXT, SHOW_ITEM, SHOW_ENTITY});
        public static final Codec<Action<?>> CODEC = UNVALIDATED_CODEC.validate(Action::validate);
        private final String name;
        private final boolean parsable;
        final MapCodec<EventData<T>> codec;
        final MapCodec<EventData<T>> legacyCodec;

        public Action(String name, boolean parsable, Codec<T> contentCodec, final LegacySerializer<T> legacySerializer) {
            this.name = name;
            this.parsable = parsable;
            this.codec = contentCodec.xmap(value -> new EventData<Object>(this, value), action -> action.value).fieldOf("contents");
            this.legacyCodec = new Codec<EventData<T>>(){

                @Override
                public <D> DataResult<Pair<EventData<T>, D>> decode(DynamicOps<D> ops, D input) {
                    return TextCodecs.CODEC.decode(ops, input).flatMap((? super R pair) -> {
                        DataResult<Pair> dataResult;
                        if (ops instanceof RegistryOps) {
                            RegistryOps registryOps = (RegistryOps)ops;
                            dataResult = legacySerializer.parse((Text)pair.getFirst(), registryOps);
                        } else {
                            dataResult = legacySerializer.parse((Text)pair.getFirst(), null);
                        }
                        return dataResult.map((? super R value) -> Pair.of(new EventData<Object>(this, value), pair.getSecond()));
                    });
                }

                @Override
                public <D> DataResult<D> encode(EventData<T> eventData, DynamicOps<D> dynamicOps, D object) {
                    return DataResult.error(() -> "Can't encode in legacy format");
                }

                @Override
                public /* synthetic */ DataResult encode(Object input, DynamicOps ops, Object prefix) {
                    return this.encode((EventData)input, ops, prefix);
                }
            }.fieldOf("value");
        }

        public boolean isParsable() {
            return this.parsable;
        }

        @Override
        public String asString() {
            return this.name;
        }

        T cast(Object o) {
            return (T)o;
        }

        public String toString() {
            return "<action " + this.name + ">";
        }

        private static DataResult<Action<?>> validate(@Nullable Action<?> action) {
            if (action == null) {
                return DataResult.error(() -> "Unknown action");
            }
            if (!action.isParsable()) {
                return DataResult.error(() -> "Action not allowed: " + String.valueOf(action));
            }
            return DataResult.success(action, Lifecycle.stable());
        }
    }

    public static interface LegacySerializer<T> {
        public DataResult<T> parse(Text var1, @Nullable RegistryOps<?> var2);
    }

    public static class ItemStackContent {
        public static final Codec<ItemStackContent> ITEM_STACK_CODEC = ItemStack.CODEC.xmap(ItemStackContent::new, ItemStackContent::asStack);
        private static final Codec<ItemStackContent> ENTRY_BASED_CODEC = ItemStack.REGISTRY_ENTRY_CODEC.xmap(ItemStackContent::new, ItemStackContent::asStack);
        public static final Codec<ItemStackContent> CODEC = Codec.withAlternative(ITEM_STACK_CODEC, ENTRY_BASED_CODEC);
        private final RegistryEntry<Item> item;
        private final int count;
        private final ComponentChanges changes;
        @Nullable
        private ItemStack stack;

        ItemStackContent(RegistryEntry<Item> item, int count, ComponentChanges changes) {
            this.item = item;
            this.count = count;
            this.changes = changes;
        }

        public ItemStackContent(ItemStack stack) {
            this(stack.getRegistryEntry(), stack.getCount(), stack.getComponentChanges());
        }

        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || this.getClass() != o.getClass()) {
                return false;
            }
            ItemStackContent itemStackContent = (ItemStackContent)o;
            return this.count == itemStackContent.count && this.item.equals(itemStackContent.item) && this.changes.equals(itemStackContent.changes);
        }

        public int hashCode() {
            int i = this.item.hashCode();
            i = 31 * i + this.count;
            i = 31 * i + this.changes.hashCode();
            return i;
        }

        public ItemStack asStack() {
            if (this.stack == null) {
                this.stack = new ItemStack(this.item, this.count, this.changes);
            }
            return this.stack;
        }

        private static DataResult<ItemStackContent> legacySerializer(Text text, @Nullable RegistryOps<?> ops) {
            try {
                NbtCompound nbtCompound = StringNbtReader.parse(text.getString());
                NbtOps dynamicOps = ops != null ? ops.withDelegate(NbtOps.INSTANCE) : NbtOps.INSTANCE;
                return ItemStack.CODEC.parse(dynamicOps, nbtCompound).map(ItemStackContent::new);
            } catch (CommandSyntaxException commandSyntaxException) {
                return DataResult.error(() -> "Failed to parse item tag: " + commandSyntaxException.getMessage());
            }
        }
    }

    public static class EntityContent {
        public static final Codec<EntityContent> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)Registries.ENTITY_TYPE.getCodec().fieldOf("type")).forGetter(content -> content.entityType), ((MapCodec)Uuids.STRICT_CODEC.fieldOf("id")).forGetter(content -> content.uuid), TextCodecs.CODEC.lenientOptionalFieldOf("name").forGetter(content -> content.name)).apply((Applicative<EntityContent, ?>)instance, EntityContent::new));
        public final EntityType<?> entityType;
        public final UUID uuid;
        public final Optional<Text> name;
        @Nullable
        private List<Text> tooltip;

        public EntityContent(EntityType<?> entityType, UUID uuid, @Nullable Text name) {
            this(entityType, uuid, Optional.ofNullable(name));
        }

        public EntityContent(EntityType<?> entityType, UUID uuid, Optional<Text> name) {
            this.entityType = entityType;
            this.uuid = uuid;
            this.name = name;
        }

        public static DataResult<EntityContent> legacySerializer(Text text2, @Nullable RegistryOps<?> ops) {
            try {
                NbtCompound nbtCompound = StringNbtReader.parse(text2.getString());
                JsonOps dynamicOps = ops != null ? ops.withDelegate(JsonOps.INSTANCE) : JsonOps.INSTANCE;
                DataResult dataResult = TextCodecs.CODEC.parse(dynamicOps, JsonParser.parseString(nbtCompound.getString("name")));
                EntityType<?> entityType = Registries.ENTITY_TYPE.get(Identifier.of(nbtCompound.getString("type")));
                UUID uUID = UUID.fromString(nbtCompound.getString("id"));
                return dataResult.map(text -> new EntityContent(entityType, uUID, (Text)text));
            } catch (Exception exception) {
                return DataResult.error(() -> "Failed to parse tooltip: " + exception.getMessage());
            }
        }

        public List<Text> asTooltip() {
            if (this.tooltip == null) {
                this.tooltip = new ArrayList<Text>();
                this.name.ifPresent(this.tooltip::add);
                this.tooltip.add(Text.translatable("gui.entity_tooltip.type", this.entityType.getName()));
                this.tooltip.add(Text.literal(this.uuid.toString()));
            }
            return this.tooltip;
        }

        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || this.getClass() != o.getClass()) {
                return false;
            }
            EntityContent entityContent = (EntityContent)o;
            return this.entityType.equals(entityContent.entityType) && this.uuid.equals(entityContent.uuid) && this.name.equals(entityContent.name);
        }

        public int hashCode() {
            int i = this.entityType.hashCode();
            i = 31 * i + this.uuid.hashCode();
            i = 31 * i + this.name.hashCode();
            return i;
        }
    }
}

