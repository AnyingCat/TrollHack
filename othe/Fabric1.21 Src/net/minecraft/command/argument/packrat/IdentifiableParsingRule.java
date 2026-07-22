/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.command.argument.packrat;

import com.mojang.brigadier.ImmutableStringReader;
import com.mojang.brigadier.StringReader;
import java.util.Optional;
import net.minecraft.command.argument.packrat.IdentifierSuggestable;
import net.minecraft.command.argument.packrat.ParsingRule;
import net.minecraft.command.argument.packrat.ParsingState;
import net.minecraft.command.argument.packrat.Symbol;
import net.minecraft.util.Identifier;

public abstract class IdentifiableParsingRule<C, V>
implements ParsingRule<StringReader, V>,
IdentifierSuggestable {
    private final Symbol<Identifier> symbol;
    protected final C callbacks;

    protected IdentifiableParsingRule(Symbol<Identifier> symbol, C callbacks) {
        this.symbol = symbol;
        this.callbacks = callbacks;
    }

    @Override
    public Optional<V> parse(ParsingState<StringReader> state) {
        state.getReader().skipWhitespace();
        int i = state.getCursor();
        Optional<Identifier> optional = state.parse(this.symbol);
        if (optional.isPresent()) {
            try {
                return Optional.of(this.parse(state.getReader(), optional.get()));
            } catch (Exception exception) {
                state.getErrors().add(i, this, exception);
                return Optional.empty();
            }
        }
        state.getErrors().add(i, this, Identifier.COMMAND_EXCEPTION.createWithContext(state.getReader()));
        return Optional.empty();
    }

    protected abstract V parse(ImmutableStringReader var1, Identifier var2) throws Exception;
}

