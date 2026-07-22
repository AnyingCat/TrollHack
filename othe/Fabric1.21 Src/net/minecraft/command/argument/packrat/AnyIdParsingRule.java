/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.command.argument.packrat;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.Optional;
import net.minecraft.command.argument.packrat.ParsingRule;
import net.minecraft.command.argument.packrat.ParsingState;
import net.minecraft.util.Identifier;

public class AnyIdParsingRule
implements ParsingRule<StringReader, Identifier> {
    public static final ParsingRule<StringReader, Identifier> INSTANCE = new AnyIdParsingRule();

    private AnyIdParsingRule() {
    }

    @Override
    public Optional<Identifier> parse(ParsingState<StringReader> state) {
        state.getReader().skipWhitespace();
        try {
            return Optional.of(Identifier.fromCommandInputNonEmpty(state.getReader()));
        } catch (CommandSyntaxException commandSyntaxException) {
            return Optional.empty();
        }
    }
}

