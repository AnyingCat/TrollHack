/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.loot.function;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.UnaryOperator;
import net.minecraft.component.ComponentType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameter;
import net.minecraft.loot.function.ConditionalLootFunction;
import net.minecraft.loot.function.LootFunctionType;
import net.minecraft.loot.function.LootFunctionTypes;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.text.TextCodecs;
import net.minecraft.text.Texts;
import net.minecraft.util.StringIdentifiable;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class SetNameLootFunction
extends ConditionalLootFunction {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final MapCodec<SetNameLootFunction> CODEC = RecordCodecBuilder.mapCodec(instance -> SetNameLootFunction.addConditionsField(instance).and(instance.group(TextCodecs.CODEC.optionalFieldOf("name").forGetter(function -> function.name), LootContext.EntityTarget.CODEC.optionalFieldOf("entity").forGetter(function -> function.entity), Target.CODEC.optionalFieldOf("target", Target.CUSTOM_NAME).forGetter(function -> function.target))).apply((Applicative<SetNameLootFunction, ?>)instance, SetNameLootFunction::new));
    private final Optional<Text> name;
    private final Optional<LootContext.EntityTarget> entity;
    private final Target target;

    private SetNameLootFunction(List<LootCondition> conditions, Optional<Text> name, Optional<LootContext.EntityTarget> entity, Target target) {
        super(conditions);
        this.name = name;
        this.entity = entity;
        this.target = target;
    }

    public LootFunctionType<SetNameLootFunction> getType() {
        return LootFunctionTypes.SET_NAME;
    }

    @Override
    public Set<LootContextParameter<?>> getRequiredParameters() {
        return this.entity.map(entity -> Set.of(entity.getParameter())).orElse(Set.of());
    }

    public static UnaryOperator<Text> applySourceEntity(LootContext context, @Nullable LootContext.EntityTarget sourceEntity) {
        Entity entity;
        if (sourceEntity != null && (entity = context.get(sourceEntity.getParameter())) != null) {
            ServerCommandSource serverCommandSource = entity.getCommandSource().withLevel(2);
            return textComponent -> {
                try {
                    return Texts.parse(serverCommandSource, textComponent, entity, 0);
                } catch (CommandSyntaxException commandSyntaxException) {
                    LOGGER.warn("Failed to resolve text component", commandSyntaxException);
                    return textComponent;
                }
            };
        }
        return textComponent -> textComponent;
    }

    @Override
    public ItemStack process(ItemStack stack, LootContext context) {
        this.name.ifPresent(name -> stack.set(this.target.getComponentType(), (Text)SetNameLootFunction.applySourceEntity(context, this.entity.orElse(null)).apply((Text)name)));
        return stack;
    }

    public static ConditionalLootFunction.Builder<?> builder(Text name, Target target) {
        return SetNameLootFunction.builder(conditions -> new SetNameLootFunction((List<LootCondition>)conditions, Optional.of(name), Optional.empty(), target));
    }

    public static ConditionalLootFunction.Builder<?> builder(Text name, Target target, LootContext.EntityTarget entity) {
        return SetNameLootFunction.builder(conditions -> new SetNameLootFunction((List<LootCondition>)conditions, Optional.of(name), Optional.of(entity), target));
    }

    public static enum Target implements StringIdentifiable
    {
        CUSTOM_NAME("custom_name"),
        ITEM_NAME("item_name");

        public static final Codec<Target> CODEC;
        private final String id;

        private Target(String id) {
            this.id = id;
        }

        @Override
        public String asString() {
            return this.id;
        }

        public ComponentType<Text> getComponentType() {
            return switch (this.ordinal()) {
                default -> throw new MatchException(null, null);
                case 1 -> DataComponentTypes.ITEM_NAME;
                case 0 -> DataComponentTypes.CUSTOM_NAME;
            };
        }

        static {
            CODEC = StringIdentifiable.createCodec(Target::values);
        }
    }
}

