/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.client.session.telemetry;

import java.util.function.Consumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.session.telemetry.PropertyMap;
import net.minecraft.client.session.telemetry.TelemetryEventType;

@FunctionalInterface
@Environment(value=EnvType.CLIENT)
public interface TelemetrySender {
    public static final TelemetrySender NOOP = (eventType, propertyAdder) -> {};

    default public TelemetrySender decorate(Consumer<PropertyMap.Builder> decorationAdder) {
        return (eventType, propertyAdder) -> this.send(eventType, builder -> {
            propertyAdder.accept(builder);
            decorationAdder.accept((PropertyMap.Builder)builder);
        });
    }

    public void send(TelemetryEventType var1, Consumer<PropertyMap.Builder> var2);
}

