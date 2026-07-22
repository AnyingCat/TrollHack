/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.network.packet.c2s.handshake;

public enum ConnectionIntent {
    STATUS,
    LOGIN,
    TRANSFER;

    private static final int STATUS_ID = 1;
    private static final int LOGIN_ID = 2;
    private static final int TRANSFER_ID = 3;

    public static ConnectionIntent byId(int id) {
        return switch (id) {
            case 1 -> STATUS;
            case 2 -> LOGIN;
            case 3 -> TRANSFER;
            default -> throw new IllegalArgumentException("Unknown connection intent: " + id);
        };
    }

    public int getId() {
        return switch (this.ordinal()) {
            default -> throw new MatchException(null, null);
            case 0 -> 1;
            case 1 -> 2;
            case 2 -> 3;
        };
    }
}

