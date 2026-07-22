/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.predicate.entity;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import java.util.function.Function;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.painting.PaintingEntity;
import net.minecraft.entity.decoration.painting.PaintingVariant;
import net.minecraft.entity.passive.AxolotlEntity;
import net.minecraft.entity.passive.CatEntity;
import net.minecraft.entity.passive.CatVariant;
import net.minecraft.entity.passive.FoxEntity;
import net.minecraft.entity.passive.FrogEntity;
import net.minecraft.entity.passive.FrogVariant;
import net.minecraft.entity.passive.HorseColor;
import net.minecraft.entity.passive.HorseEntity;
import net.minecraft.entity.passive.LlamaEntity;
import net.minecraft.entity.passive.MooshroomEntity;
import net.minecraft.entity.passive.ParrotEntity;
import net.minecraft.entity.passive.RabbitEntity;
import net.minecraft.entity.passive.TropicalFishEntity;
import net.minecraft.entity.passive.WolfEntity;
import net.minecraft.entity.passive.WolfVariant;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.predicate.entity.EntitySubPredicate;
import net.minecraft.predicate.entity.FishingHookPredicate;
import net.minecraft.predicate.entity.LightningBoltPredicate;
import net.minecraft.predicate.entity.PlayerPredicate;
import net.minecraft.predicate.entity.RaiderPredicate;
import net.minecraft.predicate.entity.SlimePredicate;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryCodecs;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import net.minecraft.village.VillagerDataContainer;
import net.minecraft.village.VillagerType;
import org.jetbrains.annotations.Nullable;

public class EntitySubPredicateTypes {
    public static final MapCodec<LightningBoltPredicate> LIGHTNING = EntitySubPredicateTypes.register("lightning", LightningBoltPredicate.CODEC);
    public static final MapCodec<FishingHookPredicate> FISHING_HOOK = EntitySubPredicateTypes.register("fishing_hook", FishingHookPredicate.CODEC);
    public static final MapCodec<PlayerPredicate> PLAYER = EntitySubPredicateTypes.register("player", PlayerPredicate.CODEC);
    public static final MapCodec<SlimePredicate> SLIME = EntitySubPredicateTypes.register("slime", SlimePredicate.CODEC);
    public static final MapCodec<RaiderPredicate> RAIDER = EntitySubPredicateTypes.register("raider", RaiderPredicate.CODEC);
    public static final VariantType<AxolotlEntity.Variant> AXOLOTL = EntitySubPredicateTypes.register("axolotl", VariantType.create(AxolotlEntity.Variant.CODEC, entity -> {
        Optional<Object> optional;
        if (entity instanceof AxolotlEntity) {
            AxolotlEntity axolotlEntity = (AxolotlEntity)entity;
            optional = Optional.of(axolotlEntity.getVariant());
        } else {
            optional = Optional.empty();
        }
        return optional;
    }));
    public static final VariantType<BoatEntity.Type> BOAT = EntitySubPredicateTypes.register("boat", VariantType.create(BoatEntity.Type.CODEC, entity -> {
        Optional<Object> optional;
        if (entity instanceof BoatEntity) {
            BoatEntity boatEntity = (BoatEntity)entity;
            optional = Optional.of(boatEntity.getVariant());
        } else {
            optional = Optional.empty();
        }
        return optional;
    }));
    public static final VariantType<FoxEntity.Type> FOX = EntitySubPredicateTypes.register("fox", VariantType.create(FoxEntity.Type.CODEC, entity -> {
        Optional<Object> optional;
        if (entity instanceof FoxEntity) {
            FoxEntity foxEntity = (FoxEntity)entity;
            optional = Optional.of(foxEntity.getVariant());
        } else {
            optional = Optional.empty();
        }
        return optional;
    }));
    public static final VariantType<MooshroomEntity.Type> MOOSHROOM = EntitySubPredicateTypes.register("mooshroom", VariantType.create(MooshroomEntity.Type.CODEC, entity -> {
        Optional<Object> optional;
        if (entity instanceof MooshroomEntity) {
            MooshroomEntity mooshroomEntity = (MooshroomEntity)entity;
            optional = Optional.of(mooshroomEntity.getVariant());
        } else {
            optional = Optional.empty();
        }
        return optional;
    }));
    public static final VariantType<RabbitEntity.RabbitType> RABBIT = EntitySubPredicateTypes.register("rabbit", VariantType.create(RabbitEntity.RabbitType.CODEC, entity -> {
        Optional<Object> optional;
        if (entity instanceof RabbitEntity) {
            RabbitEntity rabbitEntity = (RabbitEntity)entity;
            optional = Optional.of(rabbitEntity.getVariant());
        } else {
            optional = Optional.empty();
        }
        return optional;
    }));
    public static final VariantType<HorseColor> HORSE = EntitySubPredicateTypes.register("horse", VariantType.create(HorseColor.CODEC, entity -> {
        Optional<Object> optional;
        if (entity instanceof HorseEntity) {
            HorseEntity horseEntity = (HorseEntity)entity;
            optional = Optional.of(horseEntity.getVariant());
        } else {
            optional = Optional.empty();
        }
        return optional;
    }));
    public static final VariantType<LlamaEntity.Variant> LLAMA = EntitySubPredicateTypes.register("llama", VariantType.create(LlamaEntity.Variant.CODEC, entity -> {
        Optional<Object> optional;
        if (entity instanceof LlamaEntity) {
            LlamaEntity llamaEntity = (LlamaEntity)entity;
            optional = Optional.of(llamaEntity.getVariant());
        } else {
            optional = Optional.empty();
        }
        return optional;
    }));
    public static final VariantType<VillagerType> VILLAGER = EntitySubPredicateTypes.register("villager", VariantType.create(Registries.VILLAGER_TYPE.getCodec(), entity -> {
        Optional<Object> optional;
        if (entity instanceof VillagerDataContainer) {
            VillagerDataContainer villagerDataContainer = (VillagerDataContainer)((Object)entity);
            optional = Optional.of(villagerDataContainer.getVariant());
        } else {
            optional = Optional.empty();
        }
        return optional;
    }));
    public static final VariantType<ParrotEntity.Variant> PARROT = EntitySubPredicateTypes.register("parrot", VariantType.create(ParrotEntity.Variant.CODEC, entity -> {
        Optional<Object> optional;
        if (entity instanceof ParrotEntity) {
            ParrotEntity parrotEntity = (ParrotEntity)entity;
            optional = Optional.of(parrotEntity.getVariant());
        } else {
            optional = Optional.empty();
        }
        return optional;
    }));
    public static final VariantType<TropicalFishEntity.Variety> TROPICAL_FISH = EntitySubPredicateTypes.register("tropical_fish", VariantType.create(TropicalFishEntity.Variety.CODEC, entity -> {
        Optional<Object> optional;
        if (entity instanceof TropicalFishEntity) {
            TropicalFishEntity tropicalFishEntity = (TropicalFishEntity)entity;
            optional = Optional.of(tropicalFishEntity.getVariant());
        } else {
            optional = Optional.empty();
        }
        return optional;
    }));
    public static final DynamicVariantType<PaintingVariant> PAINTING = EntitySubPredicateTypes.register("painting", DynamicVariantType.create(RegistryKeys.PAINTING_VARIANT, entity -> {
        Optional<Object> optional;
        if (entity instanceof PaintingEntity) {
            PaintingEntity paintingEntity = (PaintingEntity)entity;
            optional = Optional.of(paintingEntity.getVariant());
        } else {
            optional = Optional.empty();
        }
        return optional;
    }));
    public static final DynamicVariantType<CatVariant> CAT = EntitySubPredicateTypes.register("cat", DynamicVariantType.create(RegistryKeys.CAT_VARIANT, entity -> {
        Optional<Object> optional;
        if (entity instanceof CatEntity) {
            CatEntity catEntity = (CatEntity)entity;
            optional = Optional.of(catEntity.getVariant());
        } else {
            optional = Optional.empty();
        }
        return optional;
    }));
    public static final DynamicVariantType<FrogVariant> FROG = EntitySubPredicateTypes.register("frog", DynamicVariantType.create(RegistryKeys.FROG_VARIANT, entity -> {
        Optional<Object> optional;
        if (entity instanceof FrogEntity) {
            FrogEntity frogEntity = (FrogEntity)entity;
            optional = Optional.of(frogEntity.getVariant());
        } else {
            optional = Optional.empty();
        }
        return optional;
    }));
    public static final DynamicVariantType<WolfVariant> WOLF = EntitySubPredicateTypes.register("wolf", DynamicVariantType.create(RegistryKeys.WOLF_VARIANT, entity -> {
        Optional<Object> optional;
        if (entity instanceof WolfEntity) {
            WolfEntity wolfEntity = (WolfEntity)entity;
            optional = Optional.of(wolfEntity.getVariant());
        } else {
            optional = Optional.empty();
        }
        return optional;
    }));

    private static <T extends EntitySubPredicate> MapCodec<T> register(String id, MapCodec<T> codec) {
        return Registry.register(Registries.ENTITY_SUB_PREDICATE_TYPE, id, codec);
    }

    private static <V> VariantType<V> register(String id, VariantType<V> type) {
        Registry.register(Registries.ENTITY_SUB_PREDICATE_TYPE, id, type.codec);
        return type;
    }

    private static <V> DynamicVariantType<V> register(String id, DynamicVariantType<V> type) {
        Registry.register(Registries.ENTITY_SUB_PREDICATE_TYPE, id, type.codec);
        return type;
    }

    public static MapCodec<? extends EntitySubPredicate> getDefault(Registry<MapCodec<? extends EntitySubPredicate>> registry) {
        return LIGHTNING;
    }

    public static EntitySubPredicate catVariant(RegistryEntry<CatVariant> catVariant) {
        return CAT.createPredicate(RegistryEntryList.of(catVariant));
    }

    public static EntitySubPredicate frogVariant(RegistryEntry<FrogVariant> frogVariant) {
        return FROG.createPredicate(RegistryEntryList.of(frogVariant));
    }

    public static EntitySubPredicate wolfVariant(RegistryEntryList<WolfVariant> wolfVariant) {
        return WOLF.createPredicate(wolfVariant);
    }

    public static class VariantType<V> {
        final MapCodec<VariantPredicate> codec;
        final Function<Entity, Optional<V>> variantGetter;

        public static <V> VariantType<V> create(Registry<V> registry, Function<Entity, Optional<V>> variantGetter) {
            return new VariantType<V>(registry.getCodec(), variantGetter);
        }

        public static <V> VariantType<V> create(Codec<V> codec, Function<Entity, Optional<V>> variantGetter) {
            return new VariantType<V>(codec, variantGetter);
        }

        public VariantType(Codec<V> variantCodec, Function<Entity, Optional<V>> variantGetter) {
            this.variantGetter = variantGetter;
            this.codec = RecordCodecBuilder.mapCodec(instance -> instance.group(((MapCodec)variantCodec.fieldOf("variant")).forGetter(predicate -> predicate.variant)).apply((Applicative<VariantPredicate, ?>)instance, variant -> new VariantPredicate(variant)));
        }

        public EntitySubPredicate createPredicate(V variant) {
            return new VariantPredicate(variant);
        }

        class VariantPredicate
        implements EntitySubPredicate {
            final V variant;

            VariantPredicate(V variant) {
                this.variant = variant;
            }

            public MapCodec<VariantPredicate> getCodec() {
                return VariantType.this.codec;
            }

            @Override
            public boolean test(Entity entity, ServerWorld world, @Nullable Vec3d pos) {
                return VariantType.this.variantGetter.apply(entity).filter(this.variant::equals).isPresent();
            }
        }
    }

    public static class DynamicVariantType<V> {
        final MapCodec<DynamicPredicate> codec;
        final Function<Entity, Optional<RegistryEntry<V>>> variantGetter;

        public static <V> DynamicVariantType<V> create(RegistryKey<? extends Registry<V>> registryRef, Function<Entity, Optional<RegistryEntry<V>>> variantGetter) {
            return new DynamicVariantType<V>(registryRef, variantGetter);
        }

        public DynamicVariantType(RegistryKey<? extends Registry<V>> registryRef, Function<Entity, Optional<RegistryEntry<V>>> variantGetter) {
            this.variantGetter = variantGetter;
            this.codec = RecordCodecBuilder.mapCodec(instance -> instance.group(((MapCodec)RegistryCodecs.entryList(registryRef).fieldOf("variant")).forGetter(type -> type.variants)).apply((Applicative<DynamicPredicate, ?>)instance, entries -> new DynamicPredicate(entries)));
        }

        public EntitySubPredicate createPredicate(RegistryEntryList<V> variants) {
            return new DynamicPredicate(variants);
        }

        class DynamicPredicate
        implements EntitySubPredicate {
            final RegistryEntryList<V> variants;

            DynamicPredicate(RegistryEntryList<V> variants) {
                this.variants = variants;
            }

            public MapCodec<DynamicPredicate> getCodec() {
                return DynamicVariantType.this.codec;
            }

            @Override
            public boolean test(Entity entity, ServerWorld world, @Nullable Vec3d pos) {
                return DynamicVariantType.this.variantGetter.apply(entity).filter(this.variants::contains).isPresent();
            }
        }
    }
}

