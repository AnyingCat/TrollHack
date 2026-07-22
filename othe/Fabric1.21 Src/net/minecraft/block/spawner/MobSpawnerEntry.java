/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.block.spawner;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.entity.EquipmentTable;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DataPool;
import net.minecraft.util.dynamic.Range;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.LightType;

public record MobSpawnerEntry(NbtCompound entity, Optional<CustomSpawnRules> customSpawnRules, Optional<EquipmentTable> equipment) {
    public static final String ENTITY_KEY = "entity";
    public static final Codec<MobSpawnerEntry> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)NbtCompound.CODEC.fieldOf(ENTITY_KEY)).forGetter(entry -> entry.entity), CustomSpawnRules.CODEC.optionalFieldOf("custom_spawn_rules").forGetter(entry -> entry.customSpawnRules), EquipmentTable.CODEC.optionalFieldOf("equipment").forGetter(entry -> entry.equipment)).apply((Applicative<MobSpawnerEntry, ?>)instance, MobSpawnerEntry::new));
    public static final Codec<DataPool<MobSpawnerEntry>> DATA_POOL_CODEC = DataPool.createEmptyAllowedCodec(CODEC);

    public MobSpawnerEntry() {
        this(new NbtCompound(), Optional.empty(), Optional.empty());
    }

    public MobSpawnerEntry {
        if (nbtCompound.contains("id")) {
            Identifier identifier = Identifier.tryParse(nbtCompound.getString("id"));
            if (identifier != null) {
                nbtCompound.putString("id", identifier.toString());
            } else {
                nbtCompound.remove("id");
            }
        }
    }

    public NbtCompound getNbt() {
        return this.entity;
    }

    public Optional<CustomSpawnRules> getCustomSpawnRules() {
        return this.customSpawnRules;
    }

    public Optional<EquipmentTable> getEquipment() {
        return this.equipment;
    }

    public record CustomSpawnRules(Range<Integer> blockLightLimit, Range<Integer> skyLightLimit) {
        private static final Range<Integer> DEFAULT = new Range<Integer>(0, 15);
        public static final Codec<CustomSpawnRules> CODEC = RecordCodecBuilder.create(instance -> instance.group(CustomSpawnRules.createLightLimitCodec("block_light_limit").forGetter(rules -> rules.blockLightLimit), CustomSpawnRules.createLightLimitCodec("sky_light_limit").forGetter(rules -> rules.skyLightLimit)).apply((Applicative<CustomSpawnRules, ?>)instance, CustomSpawnRules::new));

        private static DataResult<Range<Integer>> validate(Range<Integer> provider) {
            if (!DEFAULT.contains(provider)) {
                return DataResult.error(() -> "Light values must be withing range " + String.valueOf(DEFAULT));
            }
            return DataResult.success(provider);
        }

        private static MapCodec<Range<Integer>> createLightLimitCodec(String name) {
            return Range.CODEC.lenientOptionalFieldOf(name, DEFAULT).validate(CustomSpawnRules::validate);
        }

        public boolean canSpawn(BlockPos pos, ServerWorld world) {
            return this.blockLightLimit.contains(world.getLightLevel(LightType.BLOCK, pos)) && this.skyLightLimit.contains(world.getLightLevel(LightType.SKY, pos));
        }
    }
}

