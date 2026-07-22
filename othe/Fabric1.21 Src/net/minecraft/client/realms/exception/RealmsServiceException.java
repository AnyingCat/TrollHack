/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.client.realms.exception;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.realms.RealmsError;

@Environment(value=EnvType.CLIENT)
public class RealmsServiceException
extends Exception {
    public final RealmsError error;

    public RealmsServiceException(RealmsError error) {
        this.error = error;
    }

    @Override
    public String getMessage() {
        return this.error.getErrorMessage();
    }
}

