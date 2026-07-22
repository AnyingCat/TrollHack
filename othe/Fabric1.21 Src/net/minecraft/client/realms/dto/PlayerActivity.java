/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.client.realms.dto;

import com.google.gson.JsonObject;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.realms.dto.ValueObject;
import net.minecraft.client.realms.util.JsonUtils;

@Environment(value=EnvType.CLIENT)
public class PlayerActivity
extends ValueObject {
    public String profileUuid;
    public long joinTime;
    public long leaveTime;

    public static PlayerActivity parse(JsonObject json) {
        PlayerActivity playerActivity = new PlayerActivity();
        try {
            playerActivity.profileUuid = JsonUtils.getNullableStringOr("profileUuid", json, null);
            playerActivity.joinTime = JsonUtils.getLongOr("joinTime", json, Long.MIN_VALUE);
            playerActivity.leaveTime = JsonUtils.getLongOr("leaveTime", json, Long.MIN_VALUE);
        } catch (Exception exception) {
            // empty catch block
        }
        return playerActivity;
    }
}

