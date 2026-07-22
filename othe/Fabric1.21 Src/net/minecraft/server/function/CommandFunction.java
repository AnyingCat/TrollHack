/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.server.function;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.context.ContextChain;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.List;
import java.util.Optional;
import net.minecraft.command.SingleCommandAction;
import net.minecraft.command.SourcedCommandAction;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.command.AbstractServerCommandSource;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.function.FunctionBuilder;
import net.minecraft.server.function.MacroException;
import net.minecraft.server.function.Procedure;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public interface CommandFunction<T> {
    public Identifier id();

    public Procedure<T> withMacroReplaced(@Nullable NbtCompound var1, CommandDispatcher<T> var2) throws MacroException;

    private static boolean continuesToNextLine(CharSequence string) {
        int i = string.length();
        return i > 0 && string.charAt(i - 1) == '\\';
    }

    /**
     * Parses a function in the context of {@code source}.
     * 
     * <p>Any syntax errors, such as improper comment lines or unknown commands, will be thrown at this point.
     * 
     * @param lines the raw lines (including comments) read from a function file
     */
    public static <T extends AbstractServerCommandSource<T>> CommandFunction<T> create(Identifier id, CommandDispatcher<T> dispatcher, T source, List<String> lines) {
        FunctionBuilder<T> functionBuilder = new FunctionBuilder<T>();
        for (int i = 0; i < lines.size(); ++i) {
            String string3;
            String string2;
            int j = i + 1;
            String string = lines.get(i).trim();
            if (CommandFunction.continuesToNextLine(string)) {
                StringBuilder stringBuilder = new StringBuilder(string);
                do {
                    if (++i == lines.size()) {
                        throw new IllegalArgumentException("Line continuation at end of file");
                    }
                    stringBuilder.deleteCharAt(stringBuilder.length() - 1);
                    string2 = lines.get(i).trim();
                    stringBuilder.append(string2);
                    CommandFunction.validateCommandLength(stringBuilder);
                } while (CommandFunction.continuesToNextLine(stringBuilder));
                string3 = stringBuilder.toString();
            } else {
                string3 = string;
            }
            CommandFunction.validateCommandLength(string3);
            StringReader stringReader = new StringReader(string3);
            if (!stringReader.canRead() || stringReader.peek() == '#') continue;
            if (stringReader.peek() == '/') {
                stringReader.skip();
                if (stringReader.peek() == '/') {
                    throw new IllegalArgumentException("Unknown or invalid command '" + string3 + "' on line " + j + " (if you intended to make a comment, use '#' not '//')");
                }
                string2 = stringReader.readUnquotedString();
                throw new IllegalArgumentException("Unknown or invalid command '" + string3 + "' on line " + j + " (did you mean '" + string2 + "'? Do not use a preceding forwards slash.)");
            }
            if (stringReader.peek() == '$') {
                functionBuilder.addMacroCommand(string3.substring(1), j, source);
                continue;
            }
            try {
                functionBuilder.addAction(CommandFunction.parse(dispatcher, source, stringReader));
                continue;
            } catch (CommandSyntaxException commandSyntaxException) {
                throw new IllegalArgumentException("Whilst parsing command on line " + j + ": " + commandSyntaxException.getMessage());
            }
        }
        return functionBuilder.toCommandFunction(id);
    }

    public static void validateCommandLength(CharSequence command) {
        if (command.length() > 2000000) {
            CharSequence charSequence = command.subSequence(0, Math.min(512, 2000000));
            throw new IllegalStateException("Command too long: " + command.length() + " characters, contents: " + String.valueOf(charSequence) + "...");
        }
    }

    public static <T extends AbstractServerCommandSource<T>> SourcedCommandAction<T> parse(CommandDispatcher<T> dispatcher, T source, StringReader reader) throws CommandSyntaxException {
        ParseResults<T> parseResults = dispatcher.parse(reader, source);
        CommandManager.throwException(parseResults);
        Optional<ContextChain<T>> optional = ContextChain.tryFlatten(parseResults.getContext().build(reader.getString()));
        if (optional.isEmpty()) {
            throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherUnknownCommand().createWithContext(parseResults.getReader());
        }
        return new SingleCommandAction.Sourced<T>(reader.getString(), optional.get());
    }
}

