/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.server.function;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntLists;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.minecraft.command.MacroInvocation;
import net.minecraft.command.SourcedCommandAction;
import net.minecraft.nbt.NbtByte;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtDouble;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtFloat;
import net.minecraft.nbt.NbtLong;
import net.minecraft.nbt.NbtShort;
import net.minecraft.server.command.AbstractServerCommandSource;
import net.minecraft.server.function.CommandFunction;
import net.minecraft.server.function.ExpandedMacro;
import net.minecraft.server.function.MacroException;
import net.minecraft.server.function.Procedure;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import org.jetbrains.annotations.Nullable;

public class Macro<T extends AbstractServerCommandSource<T>>
implements CommandFunction<T> {
    private static final DecimalFormat DECIMAL_FORMAT = Util.make(new DecimalFormat("#"), format -> {
        format.setMaximumFractionDigits(15);
        format.setDecimalFormatSymbols(DecimalFormatSymbols.getInstance(Locale.US));
    });
    private static final int CACHE_SIZE = 8;
    private final List<String> varNames;
    private final Object2ObjectLinkedOpenHashMap<List<String>, Procedure<T>> cache = new Object2ObjectLinkedOpenHashMap(8, 0.25f);
    private final Identifier id;
    private final List<Line<T>> lines;

    public Macro(Identifier id, List<Line<T>> lines, List<String> varNames) {
        this.id = id;
        this.lines = lines;
        this.varNames = varNames;
    }

    @Override
    public Identifier id() {
        return this.id;
    }

    @Override
    public Procedure<T> withMacroReplaced(@Nullable NbtCompound arguments, CommandDispatcher<T> dispatcher) throws MacroException {
        if (arguments == null) {
            throw new MacroException(Text.translatable("commands.function.error.missing_arguments", Text.of(this.id())));
        }
        ArrayList<String> list = new ArrayList<String>(this.varNames.size());
        for (String string : this.varNames) {
            NbtElement nbtElement = arguments.get(string);
            if (nbtElement == null) {
                throw new MacroException(Text.translatable("commands.function.error.missing_argument", Text.of(this.id()), string));
            }
            list.add(Macro.toString(nbtElement));
        }
        Procedure<T> procedure = this.cache.getAndMoveToLast(list);
        if (procedure != null) {
            return procedure;
        }
        if (this.cache.size() >= 8) {
            this.cache.removeFirst();
        }
        Procedure<T> procedure2 = this.withMacroReplaced(this.varNames, list, dispatcher);
        this.cache.put(list, procedure2);
        return procedure2;
    }

    private static String toString(NbtElement nbt) {
        if (nbt instanceof NbtFloat) {
            NbtFloat nbtFloat = (NbtFloat)nbt;
            return DECIMAL_FORMAT.format(nbtFloat.floatValue());
        }
        if (nbt instanceof NbtDouble) {
            NbtDouble nbtDouble = (NbtDouble)nbt;
            return DECIMAL_FORMAT.format(nbtDouble.doubleValue());
        }
        if (nbt instanceof NbtByte) {
            NbtByte nbtByte = (NbtByte)nbt;
            return String.valueOf(nbtByte.byteValue());
        }
        if (nbt instanceof NbtShort) {
            NbtShort nbtShort = (NbtShort)nbt;
            return String.valueOf(nbtShort.shortValue());
        }
        if (nbt instanceof NbtLong) {
            NbtLong nbtLong = (NbtLong)nbt;
            return String.valueOf(nbtLong.longValue());
        }
        return nbt.asString();
    }

    private static void addArgumentsByIndices(List<String> arguments, IntList indices, List<String> out) {
        out.clear();
        indices.forEach(index -> out.add((String)arguments.get(index)));
    }

    private Procedure<T> withMacroReplaced(List<String> varNames, List<String> arguments, CommandDispatcher<T> dispatcher) throws MacroException {
        ArrayList list = new ArrayList(this.lines.size());
        ArrayList<String> list2 = new ArrayList<String>(arguments.size());
        for (Line<T> line : this.lines) {
            Macro.addArgumentsByIndices(arguments, line.getDependentVariables(), list2);
            list.add(line.instantiate(list2, dispatcher, this.id));
        }
        return new ExpandedMacro(this.id().withPath(path -> path + "/" + varNames.hashCode()), list);
    }

    static interface Line<T> {
        public IntList getDependentVariables();

        public SourcedCommandAction<T> instantiate(List<String> var1, CommandDispatcher<T> var2, Identifier var3) throws MacroException;
    }

    static class VariableLine<T extends AbstractServerCommandSource<T>>
    implements Line<T> {
        private final MacroInvocation invocation;
        private final IntList variableIndices;
        private final T source;

        public VariableLine(MacroInvocation invocation, IntList variableIndices, T source) {
            this.invocation = invocation;
            this.variableIndices = variableIndices;
            this.source = source;
        }

        @Override
        public IntList getDependentVariables() {
            return this.variableIndices;
        }

        @Override
        public SourcedCommandAction<T> instantiate(List<String> args, CommandDispatcher<T> dispatcher, Identifier id) throws MacroException {
            String string = this.invocation.apply(args);
            try {
                return CommandFunction.parse(dispatcher, this.source, new StringReader(string));
            } catch (CommandSyntaxException commandSyntaxException) {
                throw new MacroException(Text.translatable("commands.function.error.parse", Text.of(id), string, commandSyntaxException.getMessage()));
            }
        }
    }

    static class FixedLine<T>
    implements Line<T> {
        private final SourcedCommandAction<T> action;

        public FixedLine(SourcedCommandAction<T> action) {
            this.action = action;
        }

        @Override
        public IntList getDependentVariables() {
            return IntLists.emptyList();
        }

        @Override
        public SourcedCommandAction<T> instantiate(List<String> args, CommandDispatcher<T> dispatcher, Identifier id) {
            return this.action;
        }
    }
}

