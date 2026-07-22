/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.server.command;

import com.mojang.brigadier.context.CommandContext;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;
import net.minecraft.command.argument.TestFunctionArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.test.StructureBlockFinder;
import net.minecraft.test.StructureTestUtil;
import net.minecraft.test.TestFunction;
import net.minecraft.test.TestFunctionFinder;
import net.minecraft.test.TestFunctions;
import net.minecraft.util.math.BlockPos;

public class TestFinder<T>
implements StructureBlockFinder,
TestFunctionFinder {
    static final TestFunctionFinder NOOP_TEST_FUNCTION_FINDER = Stream::empty;
    static final StructureBlockFinder NOOP_STRUCTURE_BLOCK_FINDER = Stream::empty;
    private final TestFunctionFinder testFunctionFinder;
    private final StructureBlockFinder structureBlockPosFinder;
    private final ServerCommandSource commandSource;
    private final Function<TestFinder<T>, T> runnerFactory;

    @Override
    public Stream<BlockPos> findStructureBlockPos() {
        return this.structureBlockPosFinder.findStructureBlockPos();
    }

    TestFinder(ServerCommandSource commandSource, Function<TestFinder<T>, T> runnerFactory, TestFunctionFinder testFunctionFinder, StructureBlockFinder structureBlockPosFinder) {
        this.commandSource = commandSource;
        this.runnerFactory = runnerFactory;
        this.testFunctionFinder = testFunctionFinder;
        this.structureBlockPosFinder = structureBlockPosFinder;
    }

    T createRunner() {
        return this.runnerFactory.apply(this);
    }

    public ServerCommandSource getCommandSource() {
        return this.commandSource;
    }

    @Override
    public Stream<TestFunction> findTestFunctions() {
        return this.testFunctionFinder.findTestFunctions();
    }

    public static class Runners<T> {
        private final Function<TestFinder<T>, T> runnerFactory;
        private final UnaryOperator<Supplier<Stream<TestFunction>>> testFunctionsSupplierMapper;
        private final UnaryOperator<Supplier<Stream<BlockPos>>> structurePosSupplierMapper;

        public Runners(Function<TestFinder<T>, T> runnerFactory) {
            this.runnerFactory = runnerFactory;
            this.testFunctionsSupplierMapper = testFunctionsSupplier -> testFunctionsSupplier;
            this.structurePosSupplierMapper = structurePosSupplier -> structurePosSupplier;
        }

        private Runners(Function<TestFinder<T>, T> runnerFactory, UnaryOperator<Supplier<Stream<TestFunction>>> testFunctionsSupplierMapper, UnaryOperator<Supplier<Stream<BlockPos>>> structurePosSupplierMapper) {
            this.runnerFactory = runnerFactory;
            this.testFunctionsSupplierMapper = testFunctionsSupplierMapper;
            this.structurePosSupplierMapper = structurePosSupplierMapper;
        }

        public Runners<T> repeat(int count) {
            return new Runners<T>(this.runnerFactory, Runners.repeating(count), Runners.repeating(count));
        }

        private static <Q> UnaryOperator<Supplier<Stream<Q>>> repeating(int count) {
            return supplier -> {
                LinkedList list = new LinkedList();
                List list2 = ((Stream)supplier.get()).toList();
                for (int j = 0; j < count; ++j) {
                    list.addAll(list2);
                }
                return list::stream;
            };
        }

        private T createRunner(ServerCommandSource source, TestFunctionFinder testFunctionFinder, StructureBlockFinder structureBlockFinder) {
            return new TestFinder<T>(source, this.runnerFactory, ((Supplier)((Supplier)this.testFunctionsSupplierMapper.apply(testFunctionFinder::findTestFunctions)))::get, ((Supplier)((Supplier)this.structurePosSupplierMapper.apply(structureBlockFinder::findStructureBlockPos)))::get).createRunner();
        }

        public T surface(CommandContext<ServerCommandSource> context, int radius) {
            ServerCommandSource serverCommandSource = context.getSource();
            BlockPos blockPos = BlockPos.ofFloored(serverCommandSource.getPosition());
            return this.createRunner(serverCommandSource, NOOP_TEST_FUNCTION_FINDER, () -> StructureTestUtil.findStructureBlocks(blockPos, radius, serverCommandSource.getWorld()));
        }

        public T nearest(CommandContext<ServerCommandSource> context) {
            ServerCommandSource serverCommandSource = context.getSource();
            BlockPos blockPos = BlockPos.ofFloored(serverCommandSource.getPosition());
            return this.createRunner(serverCommandSource, NOOP_TEST_FUNCTION_FINDER, () -> StructureTestUtil.findNearestStructureBlock(blockPos, 15, serverCommandSource.getWorld()).stream());
        }

        public T allStructures(CommandContext<ServerCommandSource> context) {
            ServerCommandSource serverCommandSource = context.getSource();
            BlockPos blockPos = BlockPos.ofFloored(serverCommandSource.getPosition());
            return this.createRunner(serverCommandSource, NOOP_TEST_FUNCTION_FINDER, () -> StructureTestUtil.findStructureBlocks(blockPos, 200, serverCommandSource.getWorld()));
        }

        public T targeted(CommandContext<ServerCommandSource> context) {
            ServerCommandSource serverCommandSource = context.getSource();
            return this.createRunner(serverCommandSource, NOOP_TEST_FUNCTION_FINDER, () -> StructureTestUtil.findTargetedStructureBlock(BlockPos.ofFloored(serverCommandSource.getPosition()), serverCommandSource.getPlayer().getCameraEntity(), serverCommandSource.getWorld()));
        }

        public T allTestFunctions(CommandContext<ServerCommandSource> context) {
            return this.createRunner(context.getSource(), () -> TestFunctions.getTestFunctions().stream().filter(testFunction -> !testFunction.manualOnly()), NOOP_STRUCTURE_BLOCK_FINDER);
        }

        public T in(CommandContext<ServerCommandSource> context, String testClass) {
            return this.createRunner(context.getSource(), () -> TestFunctions.getTestFunctions(testClass).filter(testFunction -> !testFunction.manualOnly()), NOOP_STRUCTURE_BLOCK_FINDER);
        }

        public T failed(CommandContext<ServerCommandSource> context, boolean onlyRequired) {
            return this.createRunner(context.getSource(), () -> TestFunctions.getFailedTestFunctions().filter(function -> !onlyRequired || function.required()), NOOP_STRUCTURE_BLOCK_FINDER);
        }

        public T functionNamed(CommandContext<ServerCommandSource> context, String name) {
            return this.createRunner(context.getSource(), () -> Stream.of(TestFunctionArgumentType.getFunction(context, name)), NOOP_STRUCTURE_BLOCK_FINDER);
        }

        public T structureNamed(CommandContext<ServerCommandSource> context, String name) {
            ServerCommandSource serverCommandSource = context.getSource();
            BlockPos blockPos = BlockPos.ofFloored(serverCommandSource.getPosition());
            return this.createRunner(serverCommandSource, NOOP_TEST_FUNCTION_FINDER, () -> StructureTestUtil.findStructureBlocks(blockPos, 1024, serverCommandSource.getWorld(), name));
        }

        public T failed(CommandContext<ServerCommandSource> context) {
            return this.failed(context, false);
        }
    }
}

