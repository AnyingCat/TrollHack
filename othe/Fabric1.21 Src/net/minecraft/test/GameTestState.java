/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.test;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;
import net.minecraft.block.entity.StructureBlockBlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.test.StructureTestUtil;
import net.minecraft.test.TestAttemptConfig;
import net.minecraft.test.TestContext;
import net.minecraft.test.TestFunction;
import net.minecraft.test.TestListener;
import net.minecraft.test.TestRunContext;
import net.minecraft.test.TickLimitExceededException;
import net.minecraft.test.TimedTaskRunner;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.tick.WorldTickScheduler;
import org.jetbrains.annotations.Nullable;

public class GameTestState {
    private final TestFunction testFunction;
    @Nullable
    private BlockPos pos;
    @Nullable
    private BlockPos boxMinPos;
    private final ServerWorld world;
    private final Collection<TestListener> listeners = Lists.newArrayList();
    private final int tickLimit;
    private final Collection<TimedTaskRunner> timedTaskRunners = Lists.newCopyOnWriteArrayList();
    private final Object2LongMap<Runnable> ticksByRunnables = new Object2LongOpenHashMap<Runnable>();
    private long startTime;
    private int initialDelay = 20;
    private boolean initialized;
    private boolean tickedOnce;
    private long tick;
    private boolean started;
    private final TestAttemptConfig testAttemptConfig;
    private final Stopwatch stopwatch = Stopwatch.createUnstarted();
    private boolean completed;
    private final BlockRotation rotation;
    @Nullable
    private Throwable throwable;
    @Nullable
    private StructureBlockBlockEntity structureBlockEntity;

    public GameTestState(TestFunction testFunction, BlockRotation rotation, ServerWorld world, TestAttemptConfig testAttemptConfig) {
        this.testFunction = testFunction;
        this.world = world;
        this.testAttemptConfig = testAttemptConfig;
        this.tickLimit = testFunction.tickLimit();
        this.rotation = testFunction.rotation().rotate(rotation);
    }

    void setPos(BlockPos pos) {
        this.pos = pos;
    }

    public GameTestState startCountdown(int additionalExpectedStopTime) {
        this.startTime = this.world.getTime() + this.testFunction.setupTicks() + (long)additionalExpectedStopTime;
        this.stopwatch.start();
        return this;
    }

    public GameTestState initializeImmediately() {
        if (this.initialized) {
            return this;
        }
        this.initialDelay = 0;
        this.initialized = true;
        StructureBlockBlockEntity structureBlockBlockEntity = this.getStructureBlockBlockEntity();
        structureBlockBlockEntity.loadAndPlaceStructure(this.world);
        BlockBox blockBox = StructureTestUtil.getStructureBlockBox(structureBlockBlockEntity);
        ((WorldTickScheduler)this.world.getBlockTickScheduler()).clearNextTicks(blockBox);
        this.world.clearUpdatesInArea(blockBox);
        return this;
    }

    private boolean initialize() {
        if (this.initialized) {
            return true;
        }
        if (this.initialDelay > 0) {
            --this.initialDelay;
            return false;
        }
        this.initializeImmediately().startCountdown(0);
        return true;
    }

    public void tick(TestRunContext context) {
        if (this.isCompleted()) {
            return;
        }
        if (this.structureBlockEntity == null) {
            this.fail(new IllegalStateException("Running test without structure block entity"));
        }
        if (!this.tickedOnce && !StructureTestUtil.getStructureBlockBox(this.structureBlockEntity).streamChunkPos().allMatch(chunkPos -> this.world.shouldTickEntity(chunkPos.getStartPos()))) {
            return;
        }
        this.tickedOnce = true;
        if (!this.initialize()) {
            return;
        }
        this.tickTests();
        if (this.isCompleted()) {
            if (this.throwable != null) {
                this.listeners.forEach(listener -> listener.onFailed(this, context));
            } else {
                this.listeners.forEach(listener -> listener.onPassed(this, context));
            }
        }
    }

    private void tickTests() {
        this.tick = this.world.getTime() - this.startTime;
        if (this.tick < 0L) {
            return;
        }
        if (!this.started) {
            this.start();
        }
        Iterator objectIterator = this.ticksByRunnables.object2LongEntrySet().iterator();
        while (objectIterator.hasNext()) {
            Object2LongMap.Entry entry = (Object2LongMap.Entry)objectIterator.next();
            if (entry.getLongValue() > this.tick) continue;
            try {
                ((Runnable)entry.getKey()).run();
            } catch (Exception exception) {
                this.fail(exception);
            }
            objectIterator.remove();
        }
        if (this.tick > (long)this.tickLimit) {
            if (this.timedTaskRunners.isEmpty()) {
                this.fail(new TickLimitExceededException("Didn't succeed or fail within " + this.testFunction.tickLimit() + " ticks"));
            } else {
                this.timedTaskRunners.forEach(runner -> runner.runReported(this.tick));
                if (this.throwable == null) {
                    this.fail(new TickLimitExceededException("No sequences finished"));
                }
            }
        } else {
            this.timedTaskRunners.forEach(runner -> runner.runSilently(this.tick));
        }
    }

    private void start() {
        if (this.started) {
            return;
        }
        this.started = true;
        try {
            this.testFunction.start(new TestContext(this));
        } catch (Exception exception) {
            this.fail(exception);
        }
    }

    public void runAtTick(long tick, Runnable runnable) {
        this.ticksByRunnables.put(runnable, tick);
    }

    public String getTemplatePath() {
        return this.testFunction.templatePath();
    }

    @Nullable
    public BlockPos getPos() {
        return this.pos;
    }

    public Box getBoundingBox() {
        StructureBlockBlockEntity structureBlockBlockEntity = this.getStructureBlockBlockEntity();
        return StructureTestUtil.getStructureBoundingBox(structureBlockBlockEntity);
    }

    public StructureBlockBlockEntity getStructureBlockBlockEntity() {
        if (this.structureBlockEntity == null) {
            if (this.pos == null) {
                throw new IllegalStateException("Could not find a structureBlockEntity for this GameTestInfo");
            }
            this.structureBlockEntity = (StructureBlockBlockEntity)this.world.getBlockEntity(this.pos);
            if (this.structureBlockEntity == null) {
                throw new IllegalStateException("Could not find a structureBlockEntity at the given coordinate " + String.valueOf(this.pos));
            }
        }
        return this.structureBlockEntity;
    }

    public ServerWorld getWorld() {
        return this.world;
    }

    public boolean isPassed() {
        return this.completed && this.throwable == null;
    }

    public boolean isFailed() {
        return this.throwable != null;
    }

    public boolean isStarted() {
        return this.started;
    }

    public boolean isCompleted() {
        return this.completed;
    }

    public long getElapsedMilliseconds() {
        return this.stopwatch.elapsed(TimeUnit.MILLISECONDS);
    }

    private void complete() {
        if (!this.completed) {
            this.completed = true;
            if (this.stopwatch.isRunning()) {
                this.stopwatch.stop();
            }
        }
    }

    public void completeIfSuccessful() {
        if (this.throwable == null) {
            this.complete();
            Box box = this.getBoundingBox();
            List<Entity> list = this.getWorld().getEntitiesByClass(Entity.class, box.expand(1.0), entity -> !(entity instanceof PlayerEntity));
            list.forEach(entity -> entity.remove(Entity.RemovalReason.DISCARDED));
        }
    }

    public void fail(Throwable throwable) {
        this.throwable = throwable;
        this.complete();
    }

    @Nullable
    public Throwable getThrowable() {
        return this.throwable;
    }

    public String toString() {
        return this.getTemplatePath();
    }

    public void addListener(TestListener listener) {
        this.listeners.add(listener);
    }

    public GameTestState init() {
        BlockPos blockPos = this.getBoxMinPos();
        this.structureBlockEntity = StructureTestUtil.initStructure(this, blockPos, this.getRotation(), this.world);
        this.pos = this.structureBlockEntity.getPos();
        StructureTestUtil.placeStartButton(this.pos, new BlockPos(1, 0, -1), this.getRotation(), this.world);
        StructureTestUtil.placeBarrierBox(this.getBoundingBox(), this.world, !this.testFunction.skyAccess());
        this.listeners.forEach(listener -> listener.onStarted(this));
        return this;
    }

    long getTick() {
        return this.tick;
    }

    TimedTaskRunner createTimedTaskRunner() {
        TimedTaskRunner timedTaskRunner = new TimedTaskRunner(this);
        this.timedTaskRunners.add(timedTaskRunner);
        return timedTaskRunner;
    }

    public boolean isRequired() {
        return this.testFunction.required();
    }

    public boolean isOptional() {
        return !this.testFunction.required();
    }

    public String getTemplateName() {
        return this.testFunction.templateName();
    }

    public BlockRotation getRotation() {
        return this.rotation;
    }

    public TestFunction getTestFunction() {
        return this.testFunction;
    }

    public int getTickLimit() {
        return this.tickLimit;
    }

    public boolean isFlaky() {
        return this.testFunction.isFlaky();
    }

    public int getMaxAttempts() {
        return this.testFunction.maxAttempts();
    }

    public int getRequiredSuccesses() {
        return this.testFunction.requiredSuccesses();
    }

    public TestAttemptConfig getTestAttemptConfig() {
        return this.testAttemptConfig;
    }

    public Stream<TestListener> streamListeners() {
        return this.listeners.stream();
    }

    public GameTestState copy() {
        GameTestState gameTestState = new GameTestState(this.testFunction, this.rotation, this.world, this.getTestAttemptConfig());
        if (this.boxMinPos != null) {
            gameTestState.setBoxMinPos(this.boxMinPos);
        }
        if (this.pos != null) {
            gameTestState.setPos(this.pos);
        }
        return gameTestState;
    }

    private BlockPos getBoxMinPos() {
        if (this.boxMinPos == null) {
            BlockBox blockBox = StructureTestUtil.getStructureBlockBox(this.getStructureBlockBlockEntity());
            this.boxMinPos = new BlockPos(blockBox.getMinX(), blockBox.getMinY(), blockBox.getMinZ());
        }
        return this.boxMinPos;
    }

    public void setBoxMinPos(BlockPos boxMinPos) {
        this.boxMinPos = boxMinPos;
    }
}

