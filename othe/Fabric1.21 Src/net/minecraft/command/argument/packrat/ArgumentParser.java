/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.command.argument.packrat;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.packrat.IdentifierSuggestable;
import net.minecraft.command.argument.packrat.ParseError;
import net.minecraft.command.argument.packrat.ParseErrorList;
import net.minecraft.command.argument.packrat.ParsingRules;
import net.minecraft.command.argument.packrat.ParsingState;
import net.minecraft.command.argument.packrat.ParsingStateImpl;
import net.minecraft.command.argument.packrat.Suggestable;
import net.minecraft.command.argument.packrat.Symbol;

public record ArgumentParser<T>(ParsingRules<StringReader> rules, Symbol<T> top) {
    public Optional<T> startParsing(ParsingState<StringReader> state) {
        return state.startParsing(this.top);
    }

    public T parse(StringReader reader) throws CommandSyntaxException {
        Object r;
        ParseErrorList.Impl<StringReader> impl = new ParseErrorList.Impl<StringReader>();
        ParsingStateImpl parsingStateImpl = new ParsingStateImpl(this.rules(), impl, reader);
        Optional<T> optional = this.startParsing(parsingStateImpl);
        if (optional.isPresent()) {
            return optional.get();
        }
        List list = impl.getErrors().stream().mapMulti((error, consumer) -> {
            Object object = error.reason();
            if (object instanceof Exception) {
                Exception exception = (Exception)object;
                consumer.accept(exception);
            }
        }).toList();
        for (Exception exception : list) {
            if (!(exception instanceof CommandSyntaxException)) continue;
            CommandSyntaxException commandSyntaxException = (CommandSyntaxException)exception;
            throw commandSyntaxException;
        }
        if (list.size() == 1 && (r = list.get(0)) instanceof RuntimeException) {
            RuntimeException runtimeException = (RuntimeException)r;
            throw runtimeException;
        }
        throw new IllegalStateException("Failed to parse: " + impl.getErrors().stream().map(ParseError::toString).collect(Collectors.joining(", ")));
    }

    public CompletableFuture<Suggestions> listSuggestions(SuggestionsBuilder builder) {
        StringReader stringReader = new StringReader(builder.getInput());
        stringReader.setCursor(builder.getStart());
        ParseErrorList.Impl<StringReader> impl = new ParseErrorList.Impl<StringReader>();
        ParsingStateImpl parsingStateImpl = new ParsingStateImpl(this.rules(), impl, stringReader);
        this.startParsing(parsingStateImpl);
        List<ParseError<StringReader>> list = impl.getErrors();
        if (list.isEmpty()) {
            return builder.buildFuture();
        }
        SuggestionsBuilder suggestionsBuilder = builder.createOffset(impl.getCursor());
        for (ParseError<StringReader> parseError : list) {
            Suggestable<StringReader> suggestable = parseError.suggestions();
            if (suggestable instanceof IdentifierSuggestable) {
                IdentifierSuggestable identifierSuggestable = (IdentifierSuggestable)suggestable;
                CommandSource.suggestIdentifiers(identifierSuggestable.possibleIds(), suggestionsBuilder);
                continue;
            }
            CommandSource.suggestMatching(parseError.suggestions().possibleValues(parsingStateImpl), suggestionsBuilder);
        }
        return suggestionsBuilder.buildFuture();
    }
}

