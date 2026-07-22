/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.command.argument.packrat;

import com.mojang.brigadier.StringReader;
import net.minecraft.command.argument.packrat.ParseErrorList;
import net.minecraft.command.argument.packrat.ParsingRules;
import net.minecraft.command.argument.packrat.ParsingState;

public class ParsingStateImpl
extends ParsingState<StringReader> {
    private final StringReader reader;

    public ParsingStateImpl(ParsingRules<StringReader> rules, ParseErrorList<StringReader> errors, StringReader reader) {
        super(rules, errors);
        this.reader = reader;
    }

    @Override
    public StringReader getReader() {
        return this.reader;
    }

    @Override
    public int getCursor() {
        return this.reader.getCursor();
    }

    @Override
    public void setCursor(int cursor) {
        this.reader.setCursor(cursor);
    }

    @Override
    public /* synthetic */ Object getReader() {
        return this.getReader();
    }
}

