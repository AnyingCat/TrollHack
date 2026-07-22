/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.test;

import net.minecraft.test.GameTestState;
import net.minecraft.test.TestRunContext;

public interface TestListener {
    public void onStarted(GameTestState var1);

    public void onPassed(GameTestState var1, TestRunContext var2);

    public void onFailed(GameTestState var1, TestRunContext var2);

    public void onRetry(GameTestState var1, GameTestState var2, TestRunContext var3);
}

