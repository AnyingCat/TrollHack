/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.command.argument.packrat;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.command.argument.packrat.ParseError;
import net.minecraft.command.argument.packrat.Suggestable;

public interface ParseErrorList<S> {
    public void add(int var1, Suggestable<S> var2, Object var3);

    default public void add(int cursor, Object reason) {
        this.add(cursor, Suggestable.empty(), reason);
    }

    public void setCursor(int var1);

    public static class Impl<S>
    implements ParseErrorList<S> {
        private final List<ParseError<S>> errors = new ArrayList<ParseError<S>>();
        private int cursor = -1;

        private void moveCursor(int cursor) {
            if (cursor > this.cursor) {
                this.cursor = cursor;
                this.errors.clear();
            }
        }

        @Override
        public void setCursor(int cursor) {
            this.moveCursor(cursor);
        }

        @Override
        public void add(int cursor, Suggestable<S> suggestions, Object reason) {
            this.moveCursor(cursor);
            if (cursor == this.cursor) {
                this.errors.add(new ParseError<S>(cursor, suggestions, reason));
            }
        }

        public List<ParseError<S>> getErrors() {
            return this.errors;
        }

        public int getCursor() {
            return this.cursor;
        }
    }
}

