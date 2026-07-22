/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.server.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.logging.LogUtils;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileAttribute;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.ToIntFunction;
import java.util.stream.Stream;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.StructureBlockBlockEntity;
import net.minecraft.command.argument.TestClassArgumentType;
import net.minecraft.command.argument.TestFunctionArgumentType;
import net.minecraft.data.DataWriter;
import net.minecraft.data.dev.NbtProvider;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtIo;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.command.TestFinder;
import net.minecraft.server.network.DebugInfoSender;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.test.BatchListener;
import net.minecraft.test.Batches;
import net.minecraft.test.GameTestBatch;
import net.minecraft.test.GameTestState;
import net.minecraft.test.StructureBlockFinder;
import net.minecraft.test.StructureTestUtil;
import net.minecraft.test.TestAttemptConfig;
import net.minecraft.test.TestFunction;
import net.minecraft.test.TestFunctionFinder;
import net.minecraft.test.TestFunctions;
import net.minecraft.test.TestListener;
import net.minecraft.test.TestManager;
import net.minecraft.test.TestRunContext;
import net.minecraft.test.TestSet;
import net.minecraft.test.TestStructurePlacer;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.Texts;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.PathUtil;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.Heightmap;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.apache.commons.lang3.mutable.MutableInt;
import org.slf4j.Logger;

public class TestCommand {
    public static final int field_33180 = 15;
    public static final int field_33181 = 200;
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int field_33178 = 200;
    private static final int field_33179 = 1024;
    private static final int field_33182 = 3;
    private static final int field_33183 = 10000;
    private static final int field_33184 = 5;
    private static final int field_33185 = 5;
    private static final int field_33186 = 5;
    private static final String BLOCK_ENTITY_NOT_FOUND_TEXT = "Structure block entity could not be found";
    private static final TestFinder.Runners<Runner> RUNNERS = new TestFinder.Runners<Runner>(Runner::new);

    private static ArgumentBuilder<ServerCommandSource, ?> testAttemptConfig(ArgumentBuilder<ServerCommandSource, ?> builder, Function<CommandContext<ServerCommandSource>, Runner> callback, Function<ArgumentBuilder<ServerCommandSource, ?>, ArgumentBuilder<ServerCommandSource, ?>> extraConfigAdder) {
        return ((ArgumentBuilder)builder.executes(context -> ((Runner)callback.apply(context)).runOnce())).then(((RequiredArgumentBuilder)CommandManager.argument("numberOfTimes", IntegerArgumentType.integer(0)).executes(context -> ((Runner)callback.apply(context)).run(new TestAttemptConfig(IntegerArgumentType.getInteger(context, "numberOfTimes"), false)))).then(extraConfigAdder.apply((ArgumentBuilder<ServerCommandSource, ?>)CommandManager.argument("untilFailed", BoolArgumentType.bool()).executes(context -> ((Runner)callback.apply(context)).run(new TestAttemptConfig(IntegerArgumentType.getInteger(context, "numberOfTimes"), BoolArgumentType.getBool(context, "untilFailed")))))));
    }

    private static ArgumentBuilder<ServerCommandSource, ?> testAttemptConfig(ArgumentBuilder<ServerCommandSource, ?> builder, Function<CommandContext<ServerCommandSource>, Runner> callback) {
        return TestCommand.testAttemptConfig(builder, callback, extraConfigAdder -> extraConfigAdder);
    }

    private static ArgumentBuilder<ServerCommandSource, ?> testAttemptAndPlacementConfig(ArgumentBuilder<ServerCommandSource, ?> builder, Function<CommandContext<ServerCommandSource>, Runner> callback) {
        return TestCommand.testAttemptConfig(builder, callback, extraConfigAdder -> extraConfigAdder.then(((RequiredArgumentBuilder)CommandManager.argument("rotationSteps", IntegerArgumentType.integer()).executes(context -> ((Runner)callback.apply(context)).run(new TestAttemptConfig(IntegerArgumentType.getInteger(context, "numberOfTimes"), BoolArgumentType.getBool(context, "untilFailed")), IntegerArgumentType.getInteger(context, "rotationSteps")))).then(CommandManager.argument("testsPerRow", IntegerArgumentType.integer()).executes(context -> ((Runner)callback.apply(context)).start(new TestAttemptConfig(IntegerArgumentType.getInteger(context, "numberOfTimes"), BoolArgumentType.getBool(context, "untilFailed")), IntegerArgumentType.getInteger(context, "rotationSteps"), IntegerArgumentType.getInteger(context, "testsPerRow"))))));
    }

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        ArgumentBuilder<ServerCommandSource, ?> argumentBuilder = TestCommand.testAttemptAndPlacementConfig(CommandManager.argument("onlyRequiredTests", BoolArgumentType.bool()), context -> RUNNERS.failed((CommandContext<ServerCommandSource>)context, BoolArgumentType.getBool(context, "onlyRequiredTests")));
        ArgumentBuilder<ServerCommandSource, ?> argumentBuilder2 = TestCommand.testAttemptAndPlacementConfig(CommandManager.argument("testClassName", TestClassArgumentType.testClass()), context -> RUNNERS.in((CommandContext<ServerCommandSource>)context, TestClassArgumentType.getTestClass(context, "testClassName")));
        dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.literal("test").then((ArgumentBuilder<ServerCommandSource, ?>)CommandManager.literal("run").then(TestCommand.testAttemptAndPlacementConfig(CommandManager.argument("testName", TestFunctionArgumentType.testFunction()), context -> RUNNERS.functionNamed((CommandContext<ServerCommandSource>)context, "testName"))))).then(CommandManager.literal("runmultiple").then((ArgumentBuilder<ServerCommandSource, ?>)((RequiredArgumentBuilder)CommandManager.argument("testName", TestFunctionArgumentType.testFunction()).executes(context -> RUNNERS.functionNamed(context, "testName").runOnce())).then(CommandManager.argument("amount", IntegerArgumentType.integer()).executes(context -> RUNNERS.repeat(IntegerArgumentType.getInteger(context, "amount")).functionNamed(context, "testName").runOnce()))))).then(TestCommand.testAttemptAndPlacementConfig(CommandManager.literal("runall").then(argumentBuilder2), RUNNERS::allTestFunctions))).then(TestCommand.testAttemptConfig(CommandManager.literal("runthese"), RUNNERS::allStructures))).then(TestCommand.testAttemptConfig(CommandManager.literal("runclosest"), RUNNERS::nearest))).then(TestCommand.testAttemptConfig(CommandManager.literal("runthat"), RUNNERS::targeted))).then(TestCommand.testAttemptAndPlacementConfig(CommandManager.literal("runfailed").then(argumentBuilder), RUNNERS::failed))).then(CommandManager.literal("verify").then((ArgumentBuilder<ServerCommandSource, ?>)CommandManager.argument("testName", TestFunctionArgumentType.testFunction()).executes(context -> RUNNERS.functionNamed(context, "testName").verify())))).then(CommandManager.literal("verifyclass").then((ArgumentBuilder<ServerCommandSource, ?>)CommandManager.argument("testClassName", TestClassArgumentType.testClass()).executes(context -> RUNNERS.in(context, TestClassArgumentType.getTestClass(context, "testClassName")).verify())))).then(CommandManager.literal("locate").then((ArgumentBuilder<ServerCommandSource, ?>)CommandManager.argument("testName", TestFunctionArgumentType.testFunction()).executes(context -> RUNNERS.structureNamed(context, "minecraft:" + TestFunctionArgumentType.getFunction(context, "testName").templateName()).locate())))).then(CommandManager.literal("resetclosest").executes(context -> RUNNERS.nearest(context).reset()))).then(CommandManager.literal("resetthese").executes(context -> RUNNERS.allStructures(context).reset()))).then(CommandManager.literal("resetthat").executes(context -> RUNNERS.targeted(context).reset()))).then(CommandManager.literal("export").then((ArgumentBuilder<ServerCommandSource, ?>)CommandManager.argument("testName", StringArgumentType.word()).executes(context -> TestCommand.executeExport((ServerCommandSource)context.getSource(), "minecraft:" + StringArgumentType.getString(context, "testName")))))).then(CommandManager.literal("exportclosest").executes(context -> RUNNERS.nearest(context).export()))).then(CommandManager.literal("exportthese").executes(context -> RUNNERS.allStructures(context).export()))).then(CommandManager.literal("exportthat").executes(context -> RUNNERS.targeted(context).export()))).then(CommandManager.literal("clearthat").executes(context -> RUNNERS.targeted(context).clear()))).then(CommandManager.literal("clearthese").executes(context -> RUNNERS.allStructures(context).clear()))).then(((LiteralArgumentBuilder)CommandManager.literal("clearall").executes(context -> RUNNERS.surface(context, 200).clear())).then(CommandManager.argument("radius", IntegerArgumentType.integer()).executes(context -> RUNNERS.surface(context, MathHelper.clamp(IntegerArgumentType.getInteger(context, "radius"), 0, 1024)).clear())))).then(CommandManager.literal("import").then((ArgumentBuilder<ServerCommandSource, ?>)CommandManager.argument("testName", StringArgumentType.word()).executes(context -> TestCommand.executeImport((ServerCommandSource)context.getSource(), StringArgumentType.getString(context, "testName")))))).then(CommandManager.literal("stop").executes(context -> TestCommand.stop()))).then(((LiteralArgumentBuilder)CommandManager.literal("pos").executes(context -> TestCommand.executePos((ServerCommandSource)context.getSource(), "pos"))).then(CommandManager.argument("var", StringArgumentType.word()).executes(context -> TestCommand.executePos((ServerCommandSource)context.getSource(), StringArgumentType.getString(context, "var")))))).then(CommandManager.literal("create").then((ArgumentBuilder<ServerCommandSource, ?>)((RequiredArgumentBuilder)CommandManager.argument("testName", StringArgumentType.word()).suggests(TestFunctionArgumentType::suggestTestNames).executes(context -> TestCommand.executeCreate((ServerCommandSource)context.getSource(), StringArgumentType.getString(context, "testName"), 5, 5, 5))).then(((RequiredArgumentBuilder)CommandManager.argument("width", IntegerArgumentType.integer()).executes(context -> TestCommand.executeCreate((ServerCommandSource)context.getSource(), StringArgumentType.getString(context, "testName"), IntegerArgumentType.getInteger(context, "width"), IntegerArgumentType.getInteger(context, "width"), IntegerArgumentType.getInteger(context, "width")))).then(CommandManager.argument("height", IntegerArgumentType.integer()).then((ArgumentBuilder<ServerCommandSource, ?>)CommandManager.argument("depth", IntegerArgumentType.integer()).executes(context -> TestCommand.executeCreate((ServerCommandSource)context.getSource(), StringArgumentType.getString(context, "testName"), IntegerArgumentType.getInteger(context, "width"), IntegerArgumentType.getInteger(context, "height"), IntegerArgumentType.getInteger(context, "depth")))))))));
    }

    private static int reset(GameTestState state) {
        state.getWorld().getOtherEntities(null, state.getBoundingBox()).stream().forEach(entity -> entity.remove(Entity.RemovalReason.DISCARDED));
        state.getStructureBlockBlockEntity().loadAndPlaceStructure(state.getWorld());
        StructureTestUtil.clearBarrierBox(state.getBoundingBox(), state.getWorld());
        TestCommand.sendMessage(state.getWorld(), "Reset succeded for: " + state.getTemplatePath(), Formatting.GREEN);
        return 1;
    }

    static Stream<GameTestState> stream(ServerCommandSource source, TestAttemptConfig config, StructureBlockFinder finder) {
        return finder.findStructureBlockPos().map(pos -> TestCommand.find(pos, source.getWorld(), config)).flatMap(Optional::stream);
    }

    static Stream<GameTestState> stream(ServerCommandSource source, TestAttemptConfig config, TestFunctionFinder finder, int rotationSteps) {
        return finder.findTestFunctions().filter(testFunction -> TestCommand.checkStructure(source.getWorld(), testFunction.templateName())).map(testFunction -> new GameTestState((TestFunction)testFunction, StructureTestUtil.getRotation(rotationSteps), source.getWorld(), config));
    }

    private static Optional<GameTestState> find(BlockPos pos, ServerWorld world, TestAttemptConfig config) {
        StructureBlockBlockEntity structureBlockBlockEntity = (StructureBlockBlockEntity)world.getBlockEntity(pos);
        if (structureBlockBlockEntity == null) {
            TestCommand.sendMessage(world, BLOCK_ENTITY_NOT_FOUND_TEXT, Formatting.RED);
            return Optional.empty();
        }
        String string = structureBlockBlockEntity.getMetadata();
        Optional<TestFunction> optional = TestFunctions.getTestFunction(string);
        if (optional.isEmpty()) {
            TestCommand.sendMessage(world, "Test function for test " + string + " could not be found", Formatting.RED);
            return Optional.empty();
        }
        TestFunction testFunction = optional.get();
        GameTestState gameTestState = new GameTestState(testFunction, structureBlockBlockEntity.getRotation(), world, config);
        gameTestState.setPos(pos);
        if (!TestCommand.checkStructure(world, gameTestState.getTemplateName())) {
            return Optional.empty();
        }
        return Optional.of(gameTestState);
    }

    private static int executeCreate(ServerCommandSource source, String testName, int x, int y, int z) {
        if (x > 48 || y > 48 || z > 48) {
            throw new IllegalArgumentException("The structure must be less than 48 blocks big in each axis");
        }
        ServerWorld serverWorld = source.getWorld();
        BlockPos blockPos = TestCommand.getStructurePos(source).down();
        StructureTestUtil.createTestArea(testName.toLowerCase(), blockPos, new Vec3i(x, y, z), BlockRotation.NONE, serverWorld);
        BlockPos blockPos2 = blockPos.up();
        BlockPos blockPos3 = blockPos2.add(x - 1, 0, z - 1);
        BlockPos.stream(blockPos2, blockPos3).forEach(pos -> serverWorld.setBlockState((BlockPos)pos, Blocks.BEDROCK.getDefaultState()));
        StructureTestUtil.placeStartButton(blockPos, new BlockPos(1, 0, -1), BlockRotation.NONE, serverWorld);
        return 0;
    }

    private static int executePos(ServerCommandSource source, String variableName) throws CommandSyntaxException {
        ServerWorld serverWorld;
        BlockHitResult blockHitResult = (BlockHitResult)source.getPlayerOrThrow().raycast(10.0, 1.0f, false);
        BlockPos blockPos = blockHitResult.getBlockPos();
        Optional<BlockPos> optional = StructureTestUtil.findContainingStructureBlock(blockPos, 15, serverWorld = source.getWorld());
        if (optional.isEmpty()) {
            optional = StructureTestUtil.findContainingStructureBlock(blockPos, 200, serverWorld);
        }
        if (optional.isEmpty()) {
            source.sendError(Text.literal("Can't find a structure block that contains the targeted pos " + String.valueOf(blockPos)));
            return 0;
        }
        StructureBlockBlockEntity structureBlockBlockEntity = (StructureBlockBlockEntity)serverWorld.getBlockEntity(optional.get());
        if (structureBlockBlockEntity == null) {
            TestCommand.sendMessage(serverWorld, BLOCK_ENTITY_NOT_FOUND_TEXT, Formatting.RED);
            return 0;
        }
        BlockPos blockPos2 = blockPos.subtract(optional.get());
        String string = blockPos2.getX() + ", " + blockPos2.getY() + ", " + blockPos2.getZ();
        String string2 = structureBlockBlockEntity.getMetadata();
        MutableText text = Text.literal(string).setStyle(Style.EMPTY.withBold(true).withColor(Formatting.GREEN).withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("Click to copy to clipboard"))).withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, "final BlockPos " + variableName + " = new BlockPos(" + string + ");")));
        source.sendFeedback(() -> Text.literal("Position relative to " + string2 + ": ").append(text), false);
        DebugInfoSender.addGameTestMarker(serverWorld, new BlockPos(blockPos), string, -2147418368, 10000);
        return 1;
    }

    static int stop() {
        TestManager.INSTANCE.clear();
        return 1;
    }

    static int start(ServerCommandSource source, ServerWorld world, TestRunContext context) {
        context.addBatchListener(new ReportingBatchListener(source));
        TestSet testSet = new TestSet(context.getStates());
        testSet.addListener(new Listener(world, testSet));
        testSet.addListener(state -> TestFunctions.addFailedTestFunction(state.getTestFunction()));
        context.start();
        return 1;
    }

    static int export(ServerCommandSource source, StructureBlockBlockEntity blockEntity) {
        String string = blockEntity.getTemplateName();
        if (!blockEntity.saveStructure(true)) {
            TestCommand.sendMessage(source, "Failed to save structure " + string);
        }
        return TestCommand.executeExport(source, string);
    }

    private static int executeExport(ServerCommandSource source, String testName) {
        Path path = Paths.get(StructureTestUtil.testStructuresDirectoryName, new String[0]);
        Identifier identifier = Identifier.of(testName);
        Path path2 = source.getWorld().getStructureTemplateManager().getTemplatePath(identifier, ".nbt");
        Path path3 = NbtProvider.convertNbtToSnbt(DataWriter.UNCACHED, path2, identifier.getPath(), path);
        if (path3 == null) {
            TestCommand.sendMessage(source, "Failed to export " + String.valueOf(path2));
            return 1;
        }
        try {
            PathUtil.createDirectories(path3.getParent());
        } catch (IOException iOException) {
            TestCommand.sendMessage(source, "Could not create folder " + String.valueOf(path3.getParent()));
            LOGGER.error("Could not create export folder", iOException);
            return 1;
        }
        TestCommand.sendMessage(source, "Exported " + testName + " to " + String.valueOf(path3.toAbsolutePath()));
        return 0;
    }

    private static boolean checkStructure(ServerWorld world, String templateId) {
        if (world.getStructureTemplateManager().getTemplate(Identifier.of(templateId)).isEmpty()) {
            TestCommand.sendMessage(world, "Test structure " + templateId + " could not be found", Formatting.RED);
            return false;
        }
        return true;
    }

    static BlockPos getStructurePos(ServerCommandSource source) {
        BlockPos blockPos = BlockPos.ofFloored(source.getPosition());
        int i = source.getWorld().getTopPosition(Heightmap.Type.WORLD_SURFACE, blockPos).getY();
        return new BlockPos(blockPos.getX(), i + 1, blockPos.getZ() + 3);
    }

    static void sendMessage(ServerCommandSource source, String message) {
        source.sendFeedback(() -> Text.literal(message), false);
    }

    private static int executeImport(ServerCommandSource source, String testName) {
        Path path = Paths.get(StructureTestUtil.testStructuresDirectoryName, testName + ".snbt");
        Identifier identifier = Identifier.ofVanilla(testName);
        Path path2 = source.getWorld().getStructureTemplateManager().getTemplatePath(identifier, ".nbt");
        try {
            BufferedReader bufferedReader = Files.newBufferedReader(path);
            String string = IOUtils.toString(bufferedReader);
            Files.createDirectories(path2.getParent(), new FileAttribute[0]);
            try (OutputStream outputStream = Files.newOutputStream(path2, new OpenOption[0]);){
                NbtIo.writeCompressed(NbtHelper.fromNbtProviderString(string), outputStream);
            }
            source.getWorld().getStructureTemplateManager().unloadTemplate(identifier);
            TestCommand.sendMessage(source, "Imported to " + String.valueOf(path2.toAbsolutePath()));
            return 0;
        } catch (CommandSyntaxException | IOException exception) {
            LOGGER.error("Failed to load structure {}", (Object)testName, (Object)exception);
            return 1;
        }
    }

    static void sendMessage(ServerWorld world, String message, Formatting formatting) {
        world.getPlayers(player -> true).forEach(player -> player.sendMessage(Text.literal(message).formatted(formatting)));
    }

    record ReportingBatchListener(ServerCommandSource source) implements BatchListener
    {
        @Override
        public void onStarted(GameTestBatch batch) {
            TestCommand.sendMessage(this.source, "Starting batch: " + batch.id());
        }

        @Override
        public void onFinished(GameTestBatch batch) {
        }
    }

    public record Listener(ServerWorld world, TestSet tests) implements TestListener
    {
        @Override
        public void onStarted(GameTestState test) {
        }

        @Override
        public void onPassed(GameTestState test, TestRunContext context) {
            Listener.onFinished(this.world, this.tests);
        }

        @Override
        public void onFailed(GameTestState test, TestRunContext context) {
            Listener.onFinished(this.world, this.tests);
        }

        @Override
        public void onRetry(GameTestState prevState, GameTestState nextState, TestRunContext context) {
            this.tests.add(nextState);
        }

        private static void onFinished(ServerWorld world, TestSet tests) {
            if (tests.isDone()) {
                TestCommand.sendMessage(world, "GameTest done! " + tests.getTestCount() + " tests were run", Formatting.WHITE);
                if (tests.failed()) {
                    TestCommand.sendMessage(world, tests.getFailedRequiredTestCount() + " required tests failed :(", Formatting.RED);
                } else {
                    TestCommand.sendMessage(world, "All required tests passed :)", Formatting.GREEN);
                }
                if (tests.hasFailedOptionalTests()) {
                    TestCommand.sendMessage(world, tests.getFailedOptionalTestCount() + " optional tests failed", Formatting.GRAY);
                }
            }
        }
    }

    public static class Runner {
        private final TestFinder<Runner> finder;

        public Runner(TestFinder<Runner> finder) {
            this.finder = finder;
        }

        public int reset() {
            TestCommand.stop();
            return TestCommand.stream(this.finder.getCommandSource(), TestAttemptConfig.once(), this.finder).map(TestCommand::reset).toList().isEmpty() ? 0 : 1;
        }

        private <T> void forEach(Stream<T> finder, ToIntFunction<T> consumer, Runnable emptyCallback, Consumer<Integer> finishCallback) {
            int i = finder.mapToInt(consumer).sum();
            if (i == 0) {
                emptyCallback.run();
            } else {
                finishCallback.accept(i);
            }
        }

        public int clear() {
            TestCommand.stop();
            ServerCommandSource serverCommandSource = this.finder.getCommandSource();
            ServerWorld serverWorld = serverCommandSource.getWorld();
            TestRunContext.clearDebugMarkers(serverWorld);
            this.forEach(this.finder.findStructureBlockPos(), pos -> {
                StructureBlockBlockEntity structureBlockBlockEntity = (StructureBlockBlockEntity)serverWorld.getBlockEntity((BlockPos)pos);
                if (structureBlockBlockEntity == null) {
                    return 0;
                }
                BlockBox blockBox = StructureTestUtil.getStructureBlockBox(structureBlockBlockEntity);
                StructureTestUtil.clearArea(blockBox, serverWorld);
                return 1;
            }, () -> TestCommand.sendMessage(serverWorld, "Could not find any structures to clear", Formatting.RED), count -> TestCommand.sendMessage(serverCommandSource, "Cleared " + count + " structures"));
            return 1;
        }

        public int export() {
            MutableBoolean mutableBoolean = new MutableBoolean(true);
            ServerCommandSource serverCommandSource = this.finder.getCommandSource();
            ServerWorld serverWorld = serverCommandSource.getWorld();
            this.forEach(this.finder.findStructureBlockPos(), pos -> {
                StructureBlockBlockEntity structureBlockBlockEntity = (StructureBlockBlockEntity)serverWorld.getBlockEntity((BlockPos)pos);
                if (structureBlockBlockEntity == null) {
                    TestCommand.sendMessage(serverWorld, TestCommand.BLOCK_ENTITY_NOT_FOUND_TEXT, Formatting.RED);
                    mutableBoolean.setFalse();
                    return 0;
                }
                if (TestCommand.export(serverCommandSource, structureBlockBlockEntity) != 0) {
                    mutableBoolean.setFalse();
                }
                return 1;
            }, () -> TestCommand.sendMessage(serverWorld, "Could not find any structures to export", Formatting.RED), count -> TestCommand.sendMessage(serverCommandSource, "Exported " + count + " structures"));
            return mutableBoolean.getValue() != false ? 0 : 1;
        }

        int verify() {
            TestCommand.stop();
            ServerCommandSource serverCommandSource = this.finder.getCommandSource();
            ServerWorld serverWorld = serverCommandSource.getWorld();
            BlockPos blockPos = TestCommand.getStructurePos(serverCommandSource);
            List<GameTestState> collection = Stream.concat(TestCommand.stream(serverCommandSource, TestAttemptConfig.once(), this.finder), TestCommand.stream(serverCommandSource, TestAttemptConfig.once(), this.finder, 0)).toList();
            int i = 10;
            TestRunContext.clearDebugMarkers(serverWorld);
            TestFunctions.clearFailedTestFunctions();
            ArrayList<GameTestBatch> collection2 = new ArrayList<GameTestBatch>();
            for (GameTestState gameTestState : collection) {
                for (BlockRotation blockRotation : BlockRotation.values()) {
                    ArrayList<GameTestState> collection3 = new ArrayList<GameTestState>();
                    for (int j = 0; j < 100; ++j) {
                        GameTestState gameTestState2 = new GameTestState(gameTestState.getTestFunction(), blockRotation, serverWorld, new TestAttemptConfig(1, true));
                        collection3.add(gameTestState2);
                    }
                    GameTestBatch gameTestBatch = Batches.create(collection3, gameTestState.getTestFunction().batchId(), blockRotation.ordinal());
                    collection2.add(gameTestBatch);
                }
            }
            TestStructurePlacer testStructurePlacer = new TestStructurePlacer(blockPos, 10, true);
            TestRunContext testRunContext = TestRunContext.Builder.of(collection2, serverWorld).batcher(Batches.batcher(100)).initialSpawner(testStructurePlacer).reuseSpawner(testStructurePlacer).stopAfterFailure(true).build();
            return TestCommand.start(serverCommandSource, serverWorld, testRunContext);
        }

        public int start(TestAttemptConfig config, int rotationSteps, int testsPerRow) {
            TestCommand.stop();
            ServerCommandSource serverCommandSource = this.finder.getCommandSource();
            ServerWorld serverWorld = serverCommandSource.getWorld();
            BlockPos blockPos = TestCommand.getStructurePos(serverCommandSource);
            List<GameTestState> collection = Stream.concat(TestCommand.stream(serverCommandSource, config, this.finder), TestCommand.stream(serverCommandSource, config, this.finder, rotationSteps)).toList();
            if (collection.isEmpty()) {
                TestCommand.sendMessage(serverCommandSource, "No tests found");
                return 0;
            }
            TestRunContext.clearDebugMarkers(serverWorld);
            TestFunctions.clearFailedTestFunctions();
            TestCommand.sendMessage(serverCommandSource, "Running " + collection.size() + " tests...");
            TestRunContext testRunContext = TestRunContext.Builder.ofStates(collection, serverWorld).initialSpawner(new TestStructurePlacer(blockPos, testsPerRow, false)).build();
            return TestCommand.start(serverCommandSource, serverWorld, testRunContext);
        }

        public int runOnce(int rotationSteps, int testsPerRow) {
            return this.start(TestAttemptConfig.once(), rotationSteps, testsPerRow);
        }

        public int runOnce(int rotationSteps) {
            return this.start(TestAttemptConfig.once(), rotationSteps, 8);
        }

        public int run(TestAttemptConfig config, int rotationSteps) {
            return this.start(config, rotationSteps, 8);
        }

        public int run(TestAttemptConfig config) {
            return this.start(config, 0, 8);
        }

        public int runOnce() {
            return this.run(TestAttemptConfig.once());
        }

        public int locate() {
            TestCommand.sendMessage(this.finder.getCommandSource(), "Started locating test structures, this might take a while..");
            MutableInt mutableInt = new MutableInt(0);
            BlockPos blockPos = BlockPos.ofFloored(this.finder.getCommandSource().getPosition());
            this.finder.findStructureBlockPos().forEach(pos -> {
                StructureBlockBlockEntity structureBlockBlockEntity = (StructureBlockBlockEntity)this.finder.getCommandSource().getWorld().getBlockEntity((BlockPos)pos);
                if (structureBlockBlockEntity == null) {
                    return;
                }
                Direction direction = structureBlockBlockEntity.getRotation().rotate(Direction.NORTH);
                BlockPos blockPos2 = structureBlockBlockEntity.getPos().offset(direction, 2);
                int i = (int)direction.getOpposite().asRotation();
                String string = String.format("/tp @s %d %d %d %d 0", blockPos2.getX(), blockPos2.getY(), blockPos2.getZ(), i);
                int j = blockPos.getX() - pos.getX();
                int k = blockPos.getZ() - pos.getZ();
                int l = MathHelper.floor(MathHelper.sqrt(j * j + k * k));
                MutableText text = Texts.bracketed(Text.translatable("chat.coordinates", pos.getX(), pos.getY(), pos.getZ())).styled(style -> style.withColor(Formatting.GREEN).withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, string)).withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.translatable("chat.coordinates.tooltip"))));
                MutableText text2 = Text.literal("Found structure at: ").append(text).append(" (distance: " + l + ")");
                this.finder.getCommandSource().sendFeedback(() -> text2, false);
                mutableInt.increment();
            });
            int i = mutableInt.intValue();
            if (i == 0) {
                TestCommand.sendMessage(this.finder.getCommandSource().getWorld(), "No such test structure found", Formatting.RED);
                return 0;
            }
            TestCommand.sendMessage(this.finder.getCommandSource().getWorld(), "Finished locating, found " + i + " structure(s)", Formatting.GREEN);
            return 1;
        }
    }
}

