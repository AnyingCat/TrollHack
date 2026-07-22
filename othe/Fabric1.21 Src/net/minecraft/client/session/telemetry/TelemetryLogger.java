/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.client.session.telemetry;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.session.telemetry.SentTelemetryEvent;

@Environment(value=EnvType.CLIENT)
public interface TelemetryLogger {
    public void log(SentTelemetryEvent var1);
}

