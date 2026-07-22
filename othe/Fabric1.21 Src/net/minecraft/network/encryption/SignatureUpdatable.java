/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.network.encryption;

import java.security.SignatureException;

@FunctionalInterface
public interface SignatureUpdatable {
    public void update(SignatureUpdater var1) throws SignatureException;

    @FunctionalInterface
    public static interface SignatureUpdater {
        public void update(byte[] var1) throws SignatureException;
    }
}

