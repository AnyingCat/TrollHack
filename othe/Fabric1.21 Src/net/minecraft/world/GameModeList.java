/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.world;

import com.mojang.serialization.Codec;
import java.util.Arrays;
import java.util.List;
import net.minecraft.world.GameMode;

public record GameModeList(List<GameMode> gameModes) {
    public static final GameModeList ALL = GameModeList.of(GameMode.values());
    public static final GameModeList SURVIVAL_LIKE = GameModeList.of(GameMode.SURVIVAL, GameMode.ADVENTURE);
    public static final Codec<GameModeList> CODEC = GameMode.CODEC.listOf().xmap(GameModeList::new, GameModeList::gameModes);

    public static GameModeList of(GameMode ... gameModes) {
        return new GameModeList(Arrays.stream(gameModes).toList());
    }

    public boolean contains(GameMode gameMode) {
        return this.gameModes.contains(gameMode);
    }
}

