/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.test;

import java.util.stream.Stream;
import net.minecraft.test.TestFunction;

@FunctionalInterface
public interface TestFunctionFinder {
    public Stream<TestFunction> findTestFunctions();
}

