/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.test;

import com.google.common.collect.Lists;
import com.google.common.collect.Streams;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.test.GameTestBatch;
import net.minecraft.test.GameTestState;
import net.minecraft.test.StructureTestUtil;
import net.minecraft.test.TestAttemptConfig;
import net.minecraft.test.TestFunction;
import net.minecraft.test.TestFunctions;
import net.minecraft.test.TestRunContext;

public class Batches {
    private static final int BATCH_SIZE = 50;

    public static Collection<GameTestBatch> createBatches(Collection<TestFunction> testFunctions, ServerWorld world) {
        Map<String, List<TestFunction>> map = testFunctions.stream().collect(Collectors.groupingBy(TestFunction::batchId));
        return map.entrySet().stream().flatMap(entry -> {
            String string = (String)entry.getKey();
            List list = (List)entry.getValue();
            return Streams.mapWithIndex(Lists.partition(list, 50).stream(), (states, index) -> Batches.create(states.stream().map(testFunction -> Batches.createState(testFunction, 0, world)).toList(), string, index));
        }).toList();
    }

    public static GameTestState createState(TestFunction testFunction, int rotationSteps, ServerWorld world) {
        return new GameTestState(testFunction, StructureTestUtil.getRotation(rotationSteps), world, TestAttemptConfig.once());
    }

    public static TestRunContext.Batcher defaultBatcher() {
        return Batches.batcher(50);
    }

    public static TestRunContext.Batcher batcher(int batchSize) {
        return states -> {
            Map<String, List<GameTestState>> map = states.stream().filter(Objects::nonNull).collect(Collectors.groupingBy(state -> state.getTestFunction().batchId()));
            return map.entrySet().stream().flatMap(entry -> {
                String string = (String)entry.getKey();
                List list = (List)entry.getValue();
                return Streams.mapWithIndex(Lists.partition(list, batchSize).stream(), (states, index) -> Batches.create(List.copyOf(states), string, index));
            }).toList();
        };
    }

    public static GameTestBatch create(Collection<GameTestState> states, String batchId, long index) {
        Consumer<ServerWorld> consumer = TestFunctions.getBeforeBatchConsumer(batchId);
        Consumer<ServerWorld> consumer2 = TestFunctions.getAfterBatchConsumer(batchId);
        return new GameTestBatch(batchId + ":" + index, states, consumer, consumer2);
    }
}

