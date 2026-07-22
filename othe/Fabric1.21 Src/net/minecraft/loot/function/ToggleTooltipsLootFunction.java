/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.loot.function;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.component.ComponentType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.component.type.DyedColorComponent;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.component.type.JukeboxPlayableComponent;
import net.minecraft.component.type.UnbreakableComponent;
import net.minecraft.item.BlockPredicatesChecker;
import net.minecraft.item.ItemStack;
import net.minecraft.item.trim.ArmorTrim;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.function.ConditionalLootFunction;
import net.minecraft.loot.function.LootFunctionType;
import net.minecraft.loot.function.LootFunctionTypes;
import net.minecraft.registry.Registries;

public class ToggleTooltipsLootFunction
extends ConditionalLootFunction {
    private static final Map<ComponentType<?>, Toggle<?>> TOGGLES = Stream.of(new Toggle<ArmorTrim>(DataComponentTypes.TRIM, ArmorTrim::withShowInTooltip), new Toggle<DyedColorComponent>(DataComponentTypes.DYED_COLOR, DyedColorComponent::withShowInTooltip), new Toggle<ItemEnchantmentsComponent>(DataComponentTypes.ENCHANTMENTS, ItemEnchantmentsComponent::withShowInTooltip), new Toggle<ItemEnchantmentsComponent>(DataComponentTypes.STORED_ENCHANTMENTS, ItemEnchantmentsComponent::withShowInTooltip), new Toggle<UnbreakableComponent>(DataComponentTypes.UNBREAKABLE, UnbreakableComponent::withShowInTooltip), new Toggle<BlockPredicatesChecker>(DataComponentTypes.CAN_BREAK, BlockPredicatesChecker::withShowInTooltip), new Toggle<BlockPredicatesChecker>(DataComponentTypes.CAN_PLACE_ON, BlockPredicatesChecker::withShowInTooltip), new Toggle<AttributeModifiersComponent>(DataComponentTypes.ATTRIBUTE_MODIFIERS, AttributeModifiersComponent::withShowInTooltip), new Toggle<JukeboxPlayableComponent>(DataComponentTypes.JUKEBOX_PLAYABLE, JukeboxPlayableComponent::withShowInTooltip)).collect(Collectors.toMap(Toggle::type, toggle -> toggle));
    private static final Codec<Toggle<?>> TOGGLE_CODEC = Registries.DATA_COMPONENT_TYPE.getCodec().comapFlatMap(componentType -> {
        Toggle<?> toggle = TOGGLES.get(componentType);
        return toggle != null ? DataResult.success(toggle) : DataResult.error(() -> "Can't toggle tooltip visiblity for " + String.valueOf(Registries.DATA_COMPONENT_TYPE.getId((ComponentType<?>)componentType)));
    }, Toggle::type);
    public static final MapCodec<ToggleTooltipsLootFunction> CODEC = RecordCodecBuilder.mapCodec(instance -> ToggleTooltipsLootFunction.addConditionsField(instance).and(((MapCodec)Codec.unboundedMap(TOGGLE_CODEC, Codec.BOOL).fieldOf("toggles")).forGetter(lootFunction -> lootFunction.toggles)).apply((Applicative<ToggleTooltipsLootFunction, ?>)instance, ToggleTooltipsLootFunction::new));
    private final Map<Toggle<?>, Boolean> toggles;

    private ToggleTooltipsLootFunction(List<LootCondition> conditions, Map<Toggle<?>, Boolean> toggles) {
        super(conditions);
        this.toggles = toggles;
    }

    @Override
    protected ItemStack process(ItemStack stack, LootContext context) {
        this.toggles.forEach((componentType, showInTooltip) -> componentType.apply(stack, (boolean)showInTooltip));
        return stack;
    }

    public LootFunctionType<ToggleTooltipsLootFunction> getType() {
        return LootFunctionTypes.TOGGLE_TOOLTIPS;
    }

    record Toggle<T>(ComponentType<T> type, TooltipSetter<T> setter) {
        public void apply(ItemStack stack, boolean showInTooltip) {
            T object = stack.get(this.type);
            if (object != null) {
                stack.set(this.type, this.setter.withTooltip(object, showInTooltip));
            }
        }
    }

    @FunctionalInterface
    static interface TooltipSetter<T> {
        public T withTooltip(T var1, boolean var2);
    }
}

