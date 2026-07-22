/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.server;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.JsonOps;
import java.util.Collection;
import java.util.Map;
import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementEntry;
import net.minecraft.advancement.AdvancementManager;
import net.minecraft.advancement.AdvancementPositioner;
import net.minecraft.advancement.PlacedAdvancement;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryOps;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.resource.JsonDataLoader;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.ErrorReporter;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class ServerAdvancementLoader
extends JsonDataLoader {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Gson GSON = new GsonBuilder().create();
    private Map<Identifier, AdvancementEntry> advancements = Map.of();
    private AdvancementManager manager = new AdvancementManager();
    private final RegistryWrapper.WrapperLookup registryLookup;

    public ServerAdvancementLoader(RegistryWrapper.WrapperLookup registryLookup) {
        super(GSON, RegistryKeys.getPath(RegistryKeys.ADVANCEMENT));
        this.registryLookup = registryLookup;
    }

    @Override
    protected void apply(Map<Identifier, JsonElement> map, ResourceManager resourceManager, Profiler profiler) {
        RegistryOps<JsonElement> registryOps = this.registryLookup.getOps(JsonOps.INSTANCE);
        ImmutableMap.Builder builder = ImmutableMap.builder();
        map.forEach((id, json) -> {
            try {
                Advancement advancement = (Advancement)Advancement.CODEC.parse(registryOps, json).getOrThrow(JsonParseException::new);
                this.validate((Identifier)id, advancement);
                builder.put(id, new AdvancementEntry((Identifier)id, advancement));
            } catch (Exception exception) {
                LOGGER.error("Parsing error loading custom advancement {}: {}", id, (Object)exception.getMessage());
            }
        });
        this.advancements = builder.buildOrThrow();
        AdvancementManager advancementManager = new AdvancementManager();
        advancementManager.addAll(this.advancements.values());
        for (PlacedAdvancement placedAdvancement : advancementManager.getRoots()) {
            if (!placedAdvancement.getAdvancementEntry().value().display().isPresent()) continue;
            AdvancementPositioner.arrangeForTree(placedAdvancement);
        }
        this.manager = advancementManager;
    }

    private void validate(Identifier id, Advancement advancement) {
        ErrorReporter.Impl impl = new ErrorReporter.Impl();
        advancement.validate(impl, this.registryLookup.createRegistryLookup());
        impl.getErrorsAsString().ifPresent(string -> LOGGER.warn("Found validation problems in advancement {}: \n{}", (Object)id, string));
    }

    @Nullable
    public AdvancementEntry get(Identifier id) {
        return this.advancements.get(id);
    }

    public AdvancementManager getManager() {
        return this.manager;
    }

    public Collection<AdvancementEntry> getAdvancements() {
        return this.advancements.values();
    }
}

