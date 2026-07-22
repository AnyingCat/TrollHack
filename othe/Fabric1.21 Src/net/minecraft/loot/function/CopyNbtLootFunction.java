/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.loot.function;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import net.minecraft.command.argument.NbtPathArgumentType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameter;
import net.minecraft.loot.function.ConditionalLootFunction;
import net.minecraft.loot.function.LootFunction;
import net.minecraft.loot.function.LootFunctionType;
import net.minecraft.loot.function.LootFunctionTypes;
import net.minecraft.loot.provider.nbt.ContextLootNbtProvider;
import net.minecraft.loot.provider.nbt.LootNbtProvider;
import net.minecraft.loot.provider.nbt.LootNbtProviderTypes;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.StringIdentifiable;
import org.apache.commons.lang3.mutable.MutableObject;

public class CopyNbtLootFunction
extends ConditionalLootFunction {
    public static final MapCodec<CopyNbtLootFunction> CODEC = RecordCodecBuilder.mapCodec(instance -> CopyNbtLootFunction.addConditionsField(instance).and(instance.group(((MapCodec)LootNbtProviderTypes.CODEC.fieldOf("source")).forGetter(function -> function.source), ((MapCodec)Operation.CODEC.listOf().fieldOf("ops")).forGetter(function -> function.operations))).apply((Applicative<CopyNbtLootFunction, ?>)instance, CopyNbtLootFunction::new));
    private final LootNbtProvider source;
    private final List<Operation> operations;

    CopyNbtLootFunction(List<LootCondition> conditions, LootNbtProvider source, List<Operation> operations) {
        super(conditions);
        this.source = source;
        this.operations = List.copyOf(operations);
    }

    public LootFunctionType<CopyNbtLootFunction> getType() {
        return LootFunctionTypes.COPY_CUSTOM_DATA;
    }

    @Override
    public Set<LootContextParameter<?>> getRequiredParameters() {
        return this.source.getRequiredParameters();
    }

    @Override
    public ItemStack process(ItemStack stack, LootContext context) {
        NbtElement nbtElement = this.source.getNbt(context);
        if (nbtElement == null) {
            return stack;
        }
        MutableObject mutableObject = new MutableObject();
        Supplier<NbtElement> supplier = () -> {
            if (mutableObject.getValue() == null) {
                mutableObject.setValue(stack.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT).copyNbt());
            }
            return (NbtElement)mutableObject.getValue();
        };
        this.operations.forEach(operation -> operation.execute(supplier, nbtElement));
        NbtCompound nbtCompound = (NbtCompound)mutableObject.getValue();
        if (nbtCompound != null) {
            NbtComponent.set(DataComponentTypes.CUSTOM_DATA, stack, nbtCompound);
        }
        return stack;
    }

    @Deprecated
    public static Builder builder(LootNbtProvider source) {
        return new Builder(source);
    }

    public static Builder builder(LootContext.EntityTarget target) {
        return new Builder(ContextLootNbtProvider.fromTarget(target));
    }

    public static class Builder
    extends ConditionalLootFunction.Builder<Builder> {
        private final LootNbtProvider source;
        private final List<Operation> operations = Lists.newArrayList();

        Builder(LootNbtProvider source) {
            this.source = source;
        }

        public Builder withOperation(String source, String target, Operator operator) {
            try {
                this.operations.add(new Operation(NbtPathArgumentType.NbtPath.parse(source), NbtPathArgumentType.NbtPath.parse(target), operator));
            } catch (CommandSyntaxException commandSyntaxException) {
                throw new IllegalArgumentException(commandSyntaxException);
            }
            return this;
        }

        public Builder withOperation(String source, String target) {
            return this.withOperation(source, target, Operator.REPLACE);
        }

        @Override
        protected Builder getThisBuilder() {
            return this;
        }

        @Override
        public LootFunction build() {
            return new CopyNbtLootFunction(this.getConditions(), this.source, this.operations);
        }

        @Override
        protected /* synthetic */ ConditionalLootFunction.Builder getThisBuilder() {
            return this.getThisBuilder();
        }
    }

    record Operation(NbtPathArgumentType.NbtPath parsedSourcePath, NbtPathArgumentType.NbtPath parsedTargetPath, Operator operator) {
        public static final Codec<Operation> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)NbtPathArgumentType.NbtPath.CODEC.fieldOf("source")).forGetter(Operation::parsedSourcePath), ((MapCodec)NbtPathArgumentType.NbtPath.CODEC.fieldOf("target")).forGetter(Operation::parsedTargetPath), ((MapCodec)Operator.CODEC.fieldOf("op")).forGetter(Operation::operator)).apply((Applicative<Operation, ?>)instance, Operation::new));

        public void execute(Supplier<NbtElement> itemNbtGetter, NbtElement sourceEntityNbt) {
            try {
                List<NbtElement> list = this.parsedSourcePath.get(sourceEntityNbt);
                if (!list.isEmpty()) {
                    this.operator.merge(itemNbtGetter.get(), this.parsedTargetPath, list);
                }
            } catch (CommandSyntaxException commandSyntaxException) {
                // empty catch block
            }
        }
    }

    public static enum Operator implements StringIdentifiable
    {
        REPLACE("replace"){

            @Override
            public void merge(NbtElement itemNbt, NbtPathArgumentType.NbtPath targetPath, List<NbtElement> sourceNbts) throws CommandSyntaxException {
                targetPath.put(itemNbt, Iterables.getLast(sourceNbts));
            }
        }
        ,
        APPEND("append"){

            @Override
            public void merge(NbtElement itemNbt, NbtPathArgumentType.NbtPath targetPath, List<NbtElement> sourceNbts) throws CommandSyntaxException {
                List<NbtElement> list = targetPath.getOrInit(itemNbt, NbtList::new);
                list.forEach(foundNbt -> {
                    if (foundNbt instanceof NbtList) {
                        sourceNbts.forEach(sourceNbt -> ((NbtList)foundNbt).add(sourceNbt.copy()));
                    }
                });
            }
        }
        ,
        MERGE("merge"){

            @Override
            public void merge(NbtElement itemNbt, NbtPathArgumentType.NbtPath targetPath, List<NbtElement> sourceNbts) throws CommandSyntaxException {
                List<NbtElement> list = targetPath.getOrInit(itemNbt, NbtCompound::new);
                list.forEach(foundNbt -> {
                    if (foundNbt instanceof NbtCompound) {
                        sourceNbts.forEach(sourceNbt -> {
                            if (sourceNbt instanceof NbtCompound) {
                                ((NbtCompound)foundNbt).copyFrom((NbtCompound)sourceNbt);
                            }
                        });
                    }
                });
            }
        };

        public static final Codec<Operator> CODEC;
        private final String name;

        public abstract void merge(NbtElement var1, NbtPathArgumentType.NbtPath var2, List<NbtElement> var3) throws CommandSyntaxException;

        Operator(String name) {
            this.name = name;
        }

        @Override
        public String asString() {
            return this.name;
        }

        static {
            CODEC = StringIdentifiable.createCodec(Operator::values);
        }
    }
}

