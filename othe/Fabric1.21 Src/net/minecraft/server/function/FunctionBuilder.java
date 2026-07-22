/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.server.function;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.command.MacroInvocation;
import net.minecraft.command.SourcedCommandAction;
import net.minecraft.server.command.AbstractServerCommandSource;
import net.minecraft.server.function.CommandFunction;
import net.minecraft.server.function.ExpandedMacro;
import net.minecraft.server.function.Macro;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

class FunctionBuilder<T extends AbstractServerCommandSource<T>> {
    @Nullable
    private List<SourcedCommandAction<T>> actions = new ArrayList<SourcedCommandAction<T>>();
    @Nullable
    private List<Macro.Line<T>> macroLines;
    private final List<String> usedVariables = new ArrayList<String>();

    FunctionBuilder() {
    }

    public void addAction(SourcedCommandAction<T> action) {
        if (this.macroLines != null) {
            this.macroLines.add(new Macro.FixedLine<T>(action));
        } else {
            this.actions.add(action);
        }
    }

    private int indexOfVariable(String variable) {
        int i = this.usedVariables.indexOf(variable);
        if (i == -1) {
            i = this.usedVariables.size();
            this.usedVariables.add(variable);
        }
        return i;
    }

    private IntList indicesOfVariables(List<String> variables) {
        IntArrayList intArrayList = new IntArrayList(variables.size());
        for (String string : variables) {
            intArrayList.add(this.indexOfVariable(string));
        }
        return intArrayList;
    }

    public void addMacroCommand(String command, int lineNum, T source) {
        MacroInvocation macroInvocation = MacroInvocation.parse(command, lineNum);
        if (this.actions != null) {
            this.macroLines = new ArrayList<Macro.Line<T>>(this.actions.size() + 1);
            for (SourcedCommandAction<T> sourcedCommandAction : this.actions) {
                this.macroLines.add(new Macro.FixedLine<T>(sourcedCommandAction));
            }
            this.actions = null;
        }
        this.macroLines.add(new Macro.VariableLine<T>(macroInvocation, this.indicesOfVariables(macroInvocation.variables()), source));
    }

    public CommandFunction<T> toCommandFunction(Identifier id) {
        if (this.macroLines != null) {
            return new Macro<T>(id, this.macroLines, this.usedVariables);
        }
        return new ExpandedMacro<T>(id, this.actions);
    }
}

