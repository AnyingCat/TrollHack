/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.test;

import com.google.common.collect.Lists;
import java.util.Collection;
import net.minecraft.test.GameTestState;
import net.minecraft.test.TestRunContext;
import net.minecraft.util.Util;
import org.jetbrains.annotations.Nullable;

public class TestManager {
    public static final TestManager INSTANCE = new TestManager();
    private final Collection<GameTestState> tests = Lists.newCopyOnWriteArrayList();
    @Nullable
    private TestRunContext runContext;

    private TestManager() {
    }

    public void start(GameTestState test) {
        this.tests.add(test);
    }

    public void clear() {
        this.tests.clear();
        if (this.runContext != null) {
            this.runContext.clear();
            this.runContext = null;
        }
    }

    public void setRunContext(TestRunContext runContext) {
        if (this.runContext != null) {
            Util.error("The runner was already set in GameTestTicker");
        }
        this.runContext = runContext;
    }

    public void tick() {
        if (this.runContext == null) {
            return;
        }
        this.tests.forEach(test -> test.tick(this.runContext));
        this.tests.removeIf(GameTestState::isCompleted);
    }
}

