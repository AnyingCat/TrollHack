/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.client.realms.dto;

import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.realms.dto.ValueObject;
import net.minecraft.client.realms.util.JsonUtils;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public class WorldTemplate
extends ValueObject {
    private static final Logger LOGGER = LogUtils.getLogger();
    public String id = "";
    public String name = "";
    public String version = "";
    public String author = "";
    public String link = "";
    @Nullable
    public String image;
    public String trailer = "";
    public String recommendedPlayers = "";
    public WorldTemplateType type = WorldTemplateType.WORLD_TEMPLATE;

    public static WorldTemplate parse(JsonObject node) {
        WorldTemplate worldTemplate = new WorldTemplate();
        try {
            worldTemplate.id = JsonUtils.getNullableStringOr("id", node, "");
            worldTemplate.name = JsonUtils.getNullableStringOr("name", node, "");
            worldTemplate.version = JsonUtils.getNullableStringOr("version", node, "");
            worldTemplate.author = JsonUtils.getNullableStringOr("author", node, "");
            worldTemplate.link = JsonUtils.getNullableStringOr("link", node, "");
            worldTemplate.image = JsonUtils.getNullableStringOr("image", node, null);
            worldTemplate.trailer = JsonUtils.getNullableStringOr("trailer", node, "");
            worldTemplate.recommendedPlayers = JsonUtils.getNullableStringOr("recommendedPlayers", node, "");
            worldTemplate.type = WorldTemplateType.valueOf(JsonUtils.getNullableStringOr("type", node, WorldTemplateType.WORLD_TEMPLATE.name()));
        } catch (Exception exception) {
            LOGGER.error("Could not parse WorldTemplate: {}", (Object)exception.getMessage());
        }
        return worldTemplate;
    }

    @Environment(value=EnvType.CLIENT)
    public static enum WorldTemplateType {
        WORLD_TEMPLATE,
        MINIGAME,
        ADVENTUREMAP,
        EXPERIENCE,
        INSPIRATION;

    }
}

