/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.command.argument;

import com.google.common.annotations.VisibleForTesting;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.serialization.Codec;
import java.util.Collection;
import java.util.List;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.function.LootFunction;
import net.minecraft.loot.function.LootFunctionTypes;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.NbtString;
import net.minecraft.nbt.StringNbtReader;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryOps;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public class RegistryEntryArgumentType<T>
implements ArgumentType<RegistryEntry<T>> {
    private static final Collection<String> EXAMPLES = List.of("foo", "foo:bar", "012", "{}", "true");
    public static final DynamicCommandExceptionType FAILED_TO_PARSE_EXCEPTION = new DynamicCommandExceptionType(argument -> Text.stringifiedTranslatable("argument.resource_or_id.failed_to_parse", argument));
    private static final SimpleCommandExceptionType INVALID_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("argument.resource_or_id.invalid"));
    private final RegistryWrapper.WrapperLookup registryLookup;
    private final boolean canLookupRegistry;
    private final Codec<RegistryEntry<T>> entryCodec;

    protected RegistryEntryArgumentType(CommandRegistryAccess registryAccess, RegistryKey<Registry<T>> registry, Codec<RegistryEntry<T>> entryCodec) {
        this.registryLookup = registryAccess;
        this.canLookupRegistry = registryAccess.getOptionalWrapper(registry).isPresent();
        this.entryCodec = entryCodec;
    }

    public static LootTableArgumentType lootTable(CommandRegistryAccess registryAccess) {
        return new LootTableArgumentType(registryAccess);
    }

    public static RegistryEntry<LootTable> getLootTable(CommandContext<ServerCommandSource> context, String argument) throws CommandSyntaxException {
        return RegistryEntryArgumentType.getArgument(context, argument);
    }

    public static LootFunctionArgumentType lootFunction(CommandRegistryAccess registryAccess) {
        return new LootFunctionArgumentType(registryAccess);
    }

    public static RegistryEntry<LootFunction> getLootFunction(CommandContext<ServerCommandSource> context, String argument) {
        return RegistryEntryArgumentType.getArgument(context, argument);
    }

    public static LootConditionArgumentType lootCondition(CommandRegistryAccess registryAccess) {
        return new LootConditionArgumentType(registryAccess);
    }

    public static RegistryEntry<LootCondition> getLootCondition(CommandContext<ServerCommandSource> context, String argument) {
        return RegistryEntryArgumentType.getArgument(context, argument);
    }

    private static <T> RegistryEntry<T> getArgument(CommandContext<ServerCommandSource> context, String argument) {
        return context.getArgument(argument, RegistryEntry.class);
    }

    @Override
    @Nullable
    public RegistryEntry<T> parse(StringReader stringReader) throws CommandSyntaxException {
        NbtElement nbtElement = RegistryEntryArgumentType.parseAsNbt(stringReader);
        if (!this.canLookupRegistry) {
            return null;
        }
        RegistryOps<NbtElement> registryOps = this.registryLookup.getOps(NbtOps.INSTANCE);
        return (RegistryEntry)this.entryCodec.parse(registryOps, nbtElement).getOrThrow(argument -> FAILED_TO_PARSE_EXCEPTION.createWithContext(stringReader, argument));
    }

    @VisibleForTesting
    static NbtElement parseAsNbt(StringReader stringReader) throws CommandSyntaxException {
        int i = stringReader.getCursor();
        NbtElement nbtElement = new StringNbtReader(stringReader).parseElement();
        if (RegistryEntryArgumentType.hasFinishedReading(stringReader)) {
            return nbtElement;
        }
        stringReader.setCursor(i);
        Identifier identifier = Identifier.fromCommandInput(stringReader);
        if (RegistryEntryArgumentType.hasFinishedReading(stringReader)) {
            return NbtString.of(identifier.toString());
        }
        stringReader.setCursor(i);
        throw INVALID_EXCEPTION.createWithContext(stringReader);
    }

    private static boolean hasFinishedReading(StringReader stringReader) {
        return !stringReader.canRead() || stringReader.peek() == ' ';
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }

    @Override
    @Nullable
    public /* synthetic */ Object parse(StringReader stringReader) throws CommandSyntaxException {
        return this.parse(stringReader);
    }

    public static class LootTableArgumentType
    extends RegistryEntryArgumentType<LootTable> {
        protected LootTableArgumentType(CommandRegistryAccess registryAccess) {
            super(registryAccess, RegistryKeys.LOOT_TABLE, LootTable.ENTRY_CODEC);
        }

        @Override
        @Nullable
        public /* synthetic */ Object parse(StringReader stringReader) throws CommandSyntaxException {
            return super.parse(stringReader);
        }
    }

    public static class LootFunctionArgumentType
    extends RegistryEntryArgumentType<LootFunction> {
        protected LootFunctionArgumentType(CommandRegistryAccess registryAccess) {
            super(registryAccess, RegistryKeys.ITEM_MODIFIER, LootFunctionTypes.ENTRY_CODEC);
        }

        @Override
        @Nullable
        public /* synthetic */ Object parse(StringReader stringReader) throws CommandSyntaxException {
            return super.parse(stringReader);
        }
    }

    public static class LootConditionArgumentType
    extends RegistryEntryArgumentType<LootCondition> {
        protected LootConditionArgumentType(CommandRegistryAccess registryAccess) {
            super(registryAccess, RegistryKeys.PREDICATE, LootCondition.ENTRY_CODEC);
        }

        @Override
        @Nullable
        public /* synthetic */ Object parse(StringReader stringReader) throws CommandSyntaxException {
            return super.parse(stringReader);
        }
    }
}

