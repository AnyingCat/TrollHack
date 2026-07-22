/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.datafixer.fix;

import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import java.util.function.Function;
import java.util.stream.Stream;
import net.minecraft.datafixer.fix.PointOfInterestFix;

public class PointOfInterestRenameFix
extends PointOfInterestFix {
    private final Function<String, String> renamer;

    public PointOfInterestRenameFix(Schema outputSchema, String name, Function<String, String> renamer) {
        super(outputSchema, name);
        this.renamer = renamer;
    }

    @Override
    protected <T> Stream<Dynamic<T>> update(Stream<Dynamic<T>> dynamics) {
        return dynamics.map(dynamic2 -> dynamic2.update("type", dynamic -> DataFixUtils.orElse(dynamic.asString().map(this.renamer).map(dynamic::createString).result(), dynamic)));
    }
}

