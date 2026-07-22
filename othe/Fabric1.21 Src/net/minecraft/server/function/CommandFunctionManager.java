/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.server.function;

import com.google.common.collect.ImmutableList;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.logging.LogUtils;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import net.minecraft.command.CommandExecutionContext;
import net.minecraft.command.ReturnValueConsumer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.function.CommandFunction;
import net.minecraft.server.function.FunctionLoader;
import net.minecraft.server.function.MacroException;
import net.minecraft.server.function.Procedure;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;
import org.slf4j.Logger;

/**
 * The command function manager implements execution of functions, like that from
 * the {@code function} command.
 */
public class CommandFunctionManager {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Identifier TICK_TAG_ID = Identifier.ofVanilla("tick");
    private static final Identifier LOAD_TAG_ID = Identifier.ofVanilla("load");
    private final MinecraftServer server;
    /**
     * A list of {@code minecraft:tick} tag functions to run on every tick. Set up on
     * load, this is more efficient than polling the tag from the {@link #loader}
     * every tick.
     */
    private List<CommandFunction<ServerCommandSource>> tickFunctions = ImmutableList.of();
    /**
     * Whether this command function manager has just {@linkplain #load(FunctionLoader)
     * loaded} and should run all functions in the {@code minecraft:load} function tag.
     */
    private boolean justLoaded;
    /**
     * The source of functions for this command function manager.
     */
    private FunctionLoader loader;

    public CommandFunctionManager(MinecraftServer server, FunctionLoader loader) {
        this.server = server;
        this.loader = loader;
        this.load(loader);
    }

    public CommandDispatcher<ServerCommandSource> getDispatcher() {
        return this.server.getCommandManager().getDispatcher();
    }

    public void tick() {
        if (!this.server.getTickManager().shouldTick()) {
            return;
        }
        if (this.justLoaded) {
            this.justLoaded = false;
            Collection<CommandFunction<ServerCommandSource>> collection = this.loader.getTagOrEmpty(LOAD_TAG_ID);
            this.executeAll(collection, LOAD_TAG_ID);
        }
        this.executeAll(this.tickFunctions, TICK_TAG_ID);
    }

    private void executeAll(Collection<CommandFunction<ServerCommandSource>> functions, Identifier label) {
        this.server.getProfiler().push(label::toString);
        for (CommandFunction<ServerCommandSource> commandFunction : functions) {
            this.execute(commandFunction, this.getScheduledCommandSource());
        }
        this.server.getProfiler().pop();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    /**
     * Executes a function.
     * 
     * @param function the function
     */
    public void execute(CommandFunction<ServerCommandSource> function, ServerCommandSource source) {
        Profiler profiler = this.server.getProfiler();
        profiler.push(() -> "function " + String.valueOf(function.id()));
        try {
            Procedure<ServerCommandSource> procedure = function.withMacroReplaced(null, this.getDispatcher());
            CommandManager.callWithContext(source, context -> CommandExecutionContext.enqueueProcedureCall(context, procedure, source, ReturnValueConsumer.EMPTY));
        } catch (MacroException procedure) {
        } catch (Exception exception) {
            LOGGER.warn("Failed to execute function {}", (Object)function.id(), (Object)exception);
        } finally {
            profiler.pop();
        }
    }

    /**
     * Sets the functions that this command function manager will use in executions.
     * 
     * @param loader the new loader functions will be taken from
     */
    public void setFunctions(FunctionLoader loader) {
        this.loader = loader;
        this.load(loader);
    }

    private void load(FunctionLoader loader) {
        this.tickFunctions = ImmutableList.copyOf(loader.getTagOrEmpty(TICK_TAG_ID));
        this.justLoaded = true;
    }

    /**
     * {@return the command source to execute scheduled functions} Scheduled functions
     * are those from the {@code /schedule} command and those from the {@code
     * minecraft:tick} tag.
     * 
     * <p>This command source {@linkplain ServerCommandSource#hasPermissionLevel(int)
     * has permission level 2} and is {@linkplain ServerCommandSource#withSilent()
     * silent} compared to the server's {@linkplain MinecraftServer#getCommandSource()
     * command source}.
     */
    public ServerCommandSource getScheduledCommandSource() {
        return this.server.getCommandSource().withLevel(2).withSilent();
    }

    public Optional<CommandFunction<ServerCommandSource>> getFunction(Identifier id) {
        return this.loader.get(id);
    }

    public Collection<CommandFunction<ServerCommandSource>> getTag(Identifier id) {
        return this.loader.getTagOrEmpty(id);
    }

    public Iterable<Identifier> getAllFunctions() {
        return this.loader.getFunctions().keySet();
    }

    public Iterable<Identifier> getFunctionTags() {
        return this.loader.getTags();
    }
}

