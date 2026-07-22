/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.particle;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import java.util.function.Function;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.DustColorTransitionParticleEffect;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.particle.EntityEffectParticleEffect;
import net.minecraft.particle.ItemStackParticleEffect;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleType;
import net.minecraft.particle.SculkChargeParticleEffect;
import net.minecraft.particle.ShriekParticleEffect;
import net.minecraft.particle.SimpleParticleType;
import net.minecraft.particle.VibrationParticleEffect;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;

public class ParticleTypes {
    public static final SimpleParticleType ANGRY_VILLAGER = ParticleTypes.register("angry_villager", false);
    public static final ParticleType<BlockStateParticleEffect> BLOCK = ParticleTypes.register("block", false, BlockStateParticleEffect::createCodec, BlockStateParticleEffect::createPacketCodec);
    public static final ParticleType<BlockStateParticleEffect> BLOCK_MARKER = ParticleTypes.register("block_marker", true, BlockStateParticleEffect::createCodec, BlockStateParticleEffect::createPacketCodec);
    public static final SimpleParticleType BUBBLE = ParticleTypes.register("bubble", false);
    public static final SimpleParticleType CLOUD = ParticleTypes.register("cloud", false);
    public static final SimpleParticleType CRIT = ParticleTypes.register("crit", false);
    public static final SimpleParticleType DAMAGE_INDICATOR = ParticleTypes.register("damage_indicator", true);
    public static final SimpleParticleType DRAGON_BREATH = ParticleTypes.register("dragon_breath", false);
    public static final SimpleParticleType DRIPPING_LAVA = ParticleTypes.register("dripping_lava", false);
    public static final SimpleParticleType FALLING_LAVA = ParticleTypes.register("falling_lava", false);
    public static final SimpleParticleType LANDING_LAVA = ParticleTypes.register("landing_lava", false);
    public static final SimpleParticleType DRIPPING_WATER = ParticleTypes.register("dripping_water", false);
    public static final SimpleParticleType FALLING_WATER = ParticleTypes.register("falling_water", false);
    public static final ParticleType<DustParticleEffect> DUST = ParticleTypes.register("dust", false, type -> DustParticleEffect.CODEC, type -> DustParticleEffect.PACKET_CODEC);
    public static final ParticleType<DustColorTransitionParticleEffect> DUST_COLOR_TRANSITION = ParticleTypes.register("dust_color_transition", false, type -> DustColorTransitionParticleEffect.CODEC, type -> DustColorTransitionParticleEffect.PACKET_CODEC);
    public static final SimpleParticleType EFFECT = ParticleTypes.register("effect", false);
    public static final SimpleParticleType ELDER_GUARDIAN = ParticleTypes.register("elder_guardian", true);
    public static final SimpleParticleType ENCHANTED_HIT = ParticleTypes.register("enchanted_hit", false);
    public static final SimpleParticleType ENCHANT = ParticleTypes.register("enchant", false);
    public static final SimpleParticleType END_ROD = ParticleTypes.register("end_rod", false);
    public static final ParticleType<EntityEffectParticleEffect> ENTITY_EFFECT = ParticleTypes.register("entity_effect", false, EntityEffectParticleEffect::createCodec, EntityEffectParticleEffect::createPacketCodec);
    public static final SimpleParticleType EXPLOSION_EMITTER = ParticleTypes.register("explosion_emitter", true);
    public static final SimpleParticleType EXPLOSION = ParticleTypes.register("explosion", true);
    public static final SimpleParticleType GUST = ParticleTypes.register("gust", true);
    public static final SimpleParticleType SMALL_GUST = ParticleTypes.register("small_gust", false);
    public static final SimpleParticleType GUST_EMITTER_LARGE = ParticleTypes.register("gust_emitter_large", true);
    public static final SimpleParticleType GUST_EMITTER_SMALL = ParticleTypes.register("gust_emitter_small", true);
    public static final SimpleParticleType SONIC_BOOM = ParticleTypes.register("sonic_boom", true);
    public static final ParticleType<BlockStateParticleEffect> FALLING_DUST = ParticleTypes.register("falling_dust", false, BlockStateParticleEffect::createCodec, BlockStateParticleEffect::createPacketCodec);
    public static final SimpleParticleType FIREWORK = ParticleTypes.register("firework", false);
    public static final SimpleParticleType FISHING = ParticleTypes.register("fishing", false);
    public static final SimpleParticleType FLAME = ParticleTypes.register("flame", false);
    public static final SimpleParticleType INFESTED = ParticleTypes.register("infested", false);
    public static final SimpleParticleType CHERRY_LEAVES = ParticleTypes.register("cherry_leaves", false);
    public static final SimpleParticleType SCULK_SOUL = ParticleTypes.register("sculk_soul", false);
    public static final ParticleType<SculkChargeParticleEffect> SCULK_CHARGE = ParticleTypes.register("sculk_charge", true, type -> SculkChargeParticleEffect.CODEC, type -> SculkChargeParticleEffect.PACKET_CODEC);
    public static final SimpleParticleType SCULK_CHARGE_POP = ParticleTypes.register("sculk_charge_pop", true);
    public static final SimpleParticleType SOUL_FIRE_FLAME = ParticleTypes.register("soul_fire_flame", false);
    public static final SimpleParticleType SOUL = ParticleTypes.register("soul", false);
    public static final SimpleParticleType FLASH = ParticleTypes.register("flash", false);
    public static final SimpleParticleType HAPPY_VILLAGER = ParticleTypes.register("happy_villager", false);
    public static final SimpleParticleType COMPOSTER = ParticleTypes.register("composter", false);
    public static final SimpleParticleType HEART = ParticleTypes.register("heart", false);
    public static final SimpleParticleType INSTANT_EFFECT = ParticleTypes.register("instant_effect", false);
    public static final ParticleType<ItemStackParticleEffect> ITEM = ParticleTypes.register("item", false, ItemStackParticleEffect::createCodec, ItemStackParticleEffect::createPacketCodec);
    public static final ParticleType<VibrationParticleEffect> VIBRATION = ParticleTypes.register("vibration", true, type -> VibrationParticleEffect.CODEC, type -> VibrationParticleEffect.PACKET_CODEC);
    public static final SimpleParticleType ITEM_SLIME = ParticleTypes.register("item_slime", false);
    public static final SimpleParticleType ITEM_COBWEB = ParticleTypes.register("item_cobweb", false);
    public static final SimpleParticleType ITEM_SNOWBALL = ParticleTypes.register("item_snowball", false);
    public static final SimpleParticleType LARGE_SMOKE = ParticleTypes.register("large_smoke", false);
    public static final SimpleParticleType LAVA = ParticleTypes.register("lava", false);
    public static final SimpleParticleType MYCELIUM = ParticleTypes.register("mycelium", false);
    public static final SimpleParticleType NOTE = ParticleTypes.register("note", false);
    public static final SimpleParticleType POOF = ParticleTypes.register("poof", true);
    public static final SimpleParticleType PORTAL = ParticleTypes.register("portal", false);
    public static final SimpleParticleType RAIN = ParticleTypes.register("rain", false);
    public static final SimpleParticleType SMOKE = ParticleTypes.register("smoke", false);
    public static final SimpleParticleType WHITE_SMOKE = ParticleTypes.register("white_smoke", false);
    public static final SimpleParticleType SNEEZE = ParticleTypes.register("sneeze", false);
    public static final SimpleParticleType SPIT = ParticleTypes.register("spit", true);
    public static final SimpleParticleType SQUID_INK = ParticleTypes.register("squid_ink", true);
    public static final SimpleParticleType SWEEP_ATTACK = ParticleTypes.register("sweep_attack", true);
    public static final SimpleParticleType TOTEM_OF_UNDYING = ParticleTypes.register("totem_of_undying", false);
    public static final SimpleParticleType UNDERWATER = ParticleTypes.register("underwater", false);
    public static final SimpleParticleType SPLASH = ParticleTypes.register("splash", false);
    public static final SimpleParticleType WITCH = ParticleTypes.register("witch", false);
    public static final SimpleParticleType BUBBLE_POP = ParticleTypes.register("bubble_pop", false);
    public static final SimpleParticleType CURRENT_DOWN = ParticleTypes.register("current_down", false);
    public static final SimpleParticleType BUBBLE_COLUMN_UP = ParticleTypes.register("bubble_column_up", false);
    public static final SimpleParticleType NAUTILUS = ParticleTypes.register("nautilus", false);
    public static final SimpleParticleType DOLPHIN = ParticleTypes.register("dolphin", false);
    public static final SimpleParticleType CAMPFIRE_COSY_SMOKE = ParticleTypes.register("campfire_cosy_smoke", true);
    public static final SimpleParticleType CAMPFIRE_SIGNAL_SMOKE = ParticleTypes.register("campfire_signal_smoke", true);
    public static final SimpleParticleType DRIPPING_HONEY = ParticleTypes.register("dripping_honey", false);
    public static final SimpleParticleType FALLING_HONEY = ParticleTypes.register("falling_honey", false);
    public static final SimpleParticleType LANDING_HONEY = ParticleTypes.register("landing_honey", false);
    public static final SimpleParticleType FALLING_NECTAR = ParticleTypes.register("falling_nectar", false);
    public static final SimpleParticleType FALLING_SPORE_BLOSSOM = ParticleTypes.register("falling_spore_blossom", false);
    public static final SimpleParticleType ASH = ParticleTypes.register("ash", false);
    public static final SimpleParticleType CRIMSON_SPORE = ParticleTypes.register("crimson_spore", false);
    public static final SimpleParticleType WARPED_SPORE = ParticleTypes.register("warped_spore", false);
    public static final SimpleParticleType SPORE_BLOSSOM_AIR = ParticleTypes.register("spore_blossom_air", false);
    public static final SimpleParticleType DRIPPING_OBSIDIAN_TEAR = ParticleTypes.register("dripping_obsidian_tear", false);
    public static final SimpleParticleType FALLING_OBSIDIAN_TEAR = ParticleTypes.register("falling_obsidian_tear", false);
    public static final SimpleParticleType LANDING_OBSIDIAN_TEAR = ParticleTypes.register("landing_obsidian_tear", false);
    public static final SimpleParticleType REVERSE_PORTAL = ParticleTypes.register("reverse_portal", false);
    public static final SimpleParticleType WHITE_ASH = ParticleTypes.register("white_ash", false);
    public static final SimpleParticleType SMALL_FLAME = ParticleTypes.register("small_flame", false);
    public static final SimpleParticleType SNOWFLAKE = ParticleTypes.register("snowflake", false);
    public static final SimpleParticleType DRIPPING_DRIPSTONE_LAVA = ParticleTypes.register("dripping_dripstone_lava", false);
    public static final SimpleParticleType FALLING_DRIPSTONE_LAVA = ParticleTypes.register("falling_dripstone_lava", false);
    public static final SimpleParticleType DRIPPING_DRIPSTONE_WATER = ParticleTypes.register("dripping_dripstone_water", false);
    public static final SimpleParticleType FALLING_DRIPSTONE_WATER = ParticleTypes.register("falling_dripstone_water", false);
    public static final SimpleParticleType GLOW_SQUID_INK = ParticleTypes.register("glow_squid_ink", true);
    public static final SimpleParticleType GLOW = ParticleTypes.register("glow", true);
    public static final SimpleParticleType WAX_ON = ParticleTypes.register("wax_on", true);
    public static final SimpleParticleType WAX_OFF = ParticleTypes.register("wax_off", true);
    public static final SimpleParticleType ELECTRIC_SPARK = ParticleTypes.register("electric_spark", true);
    public static final SimpleParticleType SCRAPE = ParticleTypes.register("scrape", true);
    public static final ParticleType<ShriekParticleEffect> SHRIEK = ParticleTypes.register("shriek", false, type -> ShriekParticleEffect.CODEC, type -> ShriekParticleEffect.PACKET_CODEC);
    public static final SimpleParticleType EGG_CRACK = ParticleTypes.register("egg_crack", false);
    public static final SimpleParticleType DUST_PLUME = ParticleTypes.register("dust_plume", false);
    public static final SimpleParticleType TRIAL_SPAWNER_DETECTION = ParticleTypes.register("trial_spawner_detection", true);
    public static final SimpleParticleType TRIAL_SPAWNER_DETECTION_OMINOUS = ParticleTypes.register("trial_spawner_detection_ominous", true);
    public static final SimpleParticleType VAULT_CONNECTION = ParticleTypes.register("vault_connection", true);
    public static final ParticleType<BlockStateParticleEffect> DUST_PILLAR = ParticleTypes.register("dust_pillar", false, BlockStateParticleEffect::createCodec, BlockStateParticleEffect::createPacketCodec);
    public static final SimpleParticleType OMINOUS_SPAWNING = ParticleTypes.register("ominous_spawning", true);
    public static final SimpleParticleType RAID_OMEN = ParticleTypes.register("raid_omen", false);
    public static final SimpleParticleType TRIAL_OMEN = ParticleTypes.register("trial_omen", false);
    public static final Codec<ParticleEffect> TYPE_CODEC = Registries.PARTICLE_TYPE.getCodec().dispatch("type", ParticleEffect::getType, ParticleType::getCodec);
    public static final PacketCodec<RegistryByteBuf, ParticleEffect> PACKET_CODEC = PacketCodecs.registryValue(RegistryKeys.PARTICLE_TYPE).dispatch(ParticleEffect::getType, ParticleType::getPacketCodec);

    private static SimpleParticleType register(String name, boolean alwaysShow) {
        return Registry.register(Registries.PARTICLE_TYPE, name, new SimpleParticleType(alwaysShow));
    }

    private static <T extends ParticleEffect> ParticleType<T> register(String name, boolean alwaysShow, final Function<ParticleType<T>, MapCodec<T>> codecGetter, final Function<ParticleType<T>, PacketCodec<? super RegistryByteBuf, T>> packetCodecGetter) {
        return Registry.register(Registries.PARTICLE_TYPE, name, new ParticleType<T>(alwaysShow){

            @Override
            public MapCodec<T> getCodec() {
                return (MapCodec)codecGetter.apply(this);
            }

            @Override
            public PacketCodec<? super RegistryByteBuf, T> getPacketCodec() {
                return (PacketCodec)packetCodecGetter.apply(this);
            }
        });
    }
}

