/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.server.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.ResultConsumer;
import com.mojang.brigadier.exceptions.CommandExceptionType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.ReturnValueConsumer;
import net.minecraft.server.function.Tracer;
import org.jetbrains.annotations.Nullable;

public interface AbstractServerCommandSource<T extends AbstractServerCommandSource<T>> {
    public boolean hasPermissionLevel(int var1);

    public T withReturnValueConsumer(ReturnValueConsumer var1);

    public ReturnValueConsumer getReturnValueConsumer();

    default public T withDummyReturnValueConsumer() {
        return this.withReturnValueConsumer(ReturnValueConsumer.EMPTY);
    }

    public CommandDispatcher<T> getDispatcher();

    public void handleException(CommandExceptionType var1, Message var2, boolean var3, @Nullable Tracer var4);

    public boolean isSilent();

    default public void handleException(CommandSyntaxException exception, boolean silent, @Nullable Tracer tracer) {
        this.handleException(exception.getType(), exception.getRawMessage(), silent, tracer);
    }

    public static <T extends AbstractServerCommandSource<T>> ResultConsumer<T> asResultConsumer() {
        return (context, success, result) -> ((AbstractServerCommandSource)context.getSource()).getReturnValueConsumer().onResult(success, result);
    }
}

