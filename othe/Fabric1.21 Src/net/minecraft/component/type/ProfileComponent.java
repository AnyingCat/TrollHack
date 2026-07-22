/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.component.type;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.PropertyMap;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import net.minecraft.block.entity.SkullBlockEntity;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.util.Util;
import net.minecraft.util.Uuids;
import net.minecraft.util.dynamic.Codecs;

public record ProfileComponent(Optional<String> name, Optional<UUID> id, PropertyMap properties, GameProfile gameProfile) {
    private static final Codec<ProfileComponent> BASE_CODEC = RecordCodecBuilder.create(instance -> instance.group(Codecs.PLAYER_NAME.optionalFieldOf("name").forGetter(ProfileComponent::name), Uuids.INT_STREAM_CODEC.optionalFieldOf("id").forGetter(ProfileComponent::id), Codecs.GAME_PROFILE_PROPERTY_MAP.optionalFieldOf("properties", new PropertyMap()).forGetter(ProfileComponent::properties)).apply((Applicative<ProfileComponent, ?>)instance, ProfileComponent::new));
    public static final Codec<ProfileComponent> CODEC = Codec.withAlternative(BASE_CODEC, Codecs.PLAYER_NAME, name -> new ProfileComponent(Optional.of(name), Optional.empty(), new PropertyMap()));
    public static final PacketCodec<ByteBuf, ProfileComponent> PACKET_CODEC = PacketCodec.tuple(PacketCodecs.string(16).collect(PacketCodecs::optional), ProfileComponent::name, Uuids.PACKET_CODEC.collect(PacketCodecs::optional), ProfileComponent::id, PacketCodecs.PROPERTY_MAP, ProfileComponent::properties, ProfileComponent::new);

    public ProfileComponent(Optional<String> name, Optional<UUID> id, PropertyMap properties) {
        this(name, id, properties, ProfileComponent.createProfile(name, id, properties));
    }

    public ProfileComponent(GameProfile gameProfile) {
        this(Optional.of(gameProfile.getName()), Optional.of(gameProfile.getId()), gameProfile.getProperties(), gameProfile);
    }

    public CompletableFuture<ProfileComponent> getFuture() {
        if (this.isCompleted()) {
            return CompletableFuture.completedFuture(this);
        }
        if (this.id.isPresent()) {
            return SkullBlockEntity.fetchProfileByUuid(this.id.get()).thenApply(optional -> {
                GameProfile gameProfile = optional.orElseGet(() -> new GameProfile(this.id.get(), this.name.orElse("")));
                return new ProfileComponent(gameProfile);
            });
        }
        return SkullBlockEntity.fetchProfileByName(this.name.orElseThrow()).thenApply(profile -> {
            GameProfile gameProfile = profile.orElseGet(() -> new GameProfile(Util.NIL_UUID, this.name.get()));
            return new ProfileComponent(gameProfile);
        });
    }

    private static GameProfile createProfile(Optional<String> name, Optional<UUID> id, PropertyMap properties) {
        GameProfile gameProfile = new GameProfile(id.orElse(Util.NIL_UUID), name.orElse(""));
        gameProfile.getProperties().putAll(properties);
        return gameProfile;
    }

    public boolean isCompleted() {
        if (!this.properties.isEmpty()) {
            return true;
        }
        return this.id.isPresent() == this.name.isPresent();
    }
}

