/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.block.enums;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.spawner.MobSpawnerEntry;
import net.minecraft.block.spawner.TrialSpawnerConfig;
import net.minecraft.block.spawner.TrialSpawnerData;
import net.minecraft.block.spawner.TrialSpawnerLogic;
import net.minecraft.entity.Entity;
import net.minecraft.entity.OminousItemSpawnerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootTable;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.particle.SimpleParticleType;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.Util;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public enum TrialSpawnerState implements StringIdentifiable
{
    INACTIVE("inactive", 0, ParticleEmitter.NONE, -1.0, false),
    WAITING_FOR_PLAYERS("waiting_for_players", 4, ParticleEmitter.WAITING, 200.0, true),
    ACTIVE("active", 8, ParticleEmitter.ACTIVE, 1000.0, true),
    WAITING_FOR_REWARD_EJECTION("waiting_for_reward_ejection", 8, ParticleEmitter.WAITING, -1.0, false),
    EJECTING_REWARD("ejecting_reward", 8, ParticleEmitter.WAITING, -1.0, false),
    COOLDOWN("cooldown", 0, ParticleEmitter.COOLDOWN, -1.0, false);

    private static final float START_EJECTING_REWARDS_COOLDOWN = 40.0f;
    private static final int BETWEEN_EJECTING_REWARDS_COOLDOWN;
    private final String id;
    private final int luminance;
    private final double displayRotationSpeed;
    private final ParticleEmitter particleEmitter;
    private final boolean playsSound;

    private TrialSpawnerState(String id, int luminance, ParticleEmitter particleEmitter, double displayRotationSpeed, boolean playsSound) {
        this.id = id;
        this.luminance = luminance;
        this.particleEmitter = particleEmitter;
        this.displayRotationSpeed = displayRotationSpeed;
        this.playsSound = playsSound;
    }

    TrialSpawnerState tick(BlockPos pos, TrialSpawnerLogic logic, ServerWorld world) {
        TrialSpawnerData trialSpawnerData = logic.getData();
        TrialSpawnerConfig trialSpawnerConfig = logic.getConfig();
        return switch (this.ordinal()) {
            default -> throw new MatchException(null, null);
            case 0 -> {
                if (trialSpawnerData.setDisplayEntity(logic, world, WAITING_FOR_PLAYERS) == null) {
                    yield this;
                }
                yield WAITING_FOR_PLAYERS;
            }
            case 1 -> {
                if (!logic.canActivate(world)) {
                    trialSpawnerData.reset();
                    yield this;
                }
                if (!trialSpawnerData.hasSpawnData(logic, world.random)) {
                    yield INACTIVE;
                }
                trialSpawnerData.updatePlayers(world, pos, logic);
                if (trialSpawnerData.players.isEmpty()) {
                    yield this;
                }
                yield ACTIVE;
            }
            case 2 -> {
                if (!logic.canActivate(world)) {
                    trialSpawnerData.reset();
                    yield WAITING_FOR_PLAYERS;
                }
                if (!trialSpawnerData.hasSpawnData(logic, world.random)) {
                    yield INACTIVE;
                }
                int i = trialSpawnerData.getAdditionalPlayers(pos);
                trialSpawnerData.updatePlayers(world, pos, logic);
                if (logic.isOminous()) {
                    this.spawnOminousItemSpawner(world, pos, logic);
                }
                if (trialSpawnerData.hasSpawnedAllMobs(trialSpawnerConfig, i)) {
                    if (trialSpawnerData.areMobsDead()) {
                        trialSpawnerData.cooldownEnd = world.getTime() + (long)logic.getCooldownLength();
                        trialSpawnerData.totalSpawnedMobs = 0;
                        trialSpawnerData.nextMobSpawnsAt = 0L;
                        yield WAITING_FOR_REWARD_EJECTION;
                    }
                } else if (trialSpawnerData.canSpawnMore(world, trialSpawnerConfig, i)) {
                    logic.trySpawnMob(world, pos).ifPresent(uuid -> {
                        trialSpawnerData.spawnedMobsAlive.add((UUID)uuid);
                        ++trialSpawnerData.totalSpawnedMobs;
                        trialSpawnerData.nextMobSpawnsAt = world.getTime() + (long)trialSpawnerConfig.ticksBetweenSpawn();
                        trialSpawnerConfig.spawnPotentialsDefinition().getOrEmpty(world.getRandom()).ifPresent(spawnData -> {
                            trialSpawnerData.spawnData = Optional.of((MobSpawnerEntry)spawnData.data());
                            logic.updateListeners();
                        });
                    });
                }
                yield this;
            }
            case 3 -> {
                if (trialSpawnerData.isCooldownPast(world, 40.0f, logic.getCooldownLength())) {
                    world.playSound(null, pos, SoundEvents.BLOCK_TRIAL_SPAWNER_OPEN_SHUTTER, SoundCategory.BLOCKS);
                    yield EJECTING_REWARD;
                }
                yield this;
            }
            case 4 -> {
                if (!trialSpawnerData.isCooldownAtRepeating(world, BETWEEN_EJECTING_REWARDS_COOLDOWN, logic.getCooldownLength())) {
                    yield this;
                }
                if (trialSpawnerData.players.isEmpty()) {
                    world.playSound(null, pos, SoundEvents.BLOCK_TRIAL_SPAWNER_CLOSE_SHUTTER, SoundCategory.BLOCKS);
                    trialSpawnerData.rewardLootTable = Optional.empty();
                    yield COOLDOWN;
                }
                if (trialSpawnerData.rewardLootTable.isEmpty()) {
                    trialSpawnerData.rewardLootTable = trialSpawnerConfig.lootTablesToEject().getDataOrEmpty(world.getRandom());
                }
                trialSpawnerData.rewardLootTable.ifPresent(lootTable -> logic.ejectLootTable(world, pos, (RegistryKey<LootTable>)lootTable));
                trialSpawnerData.players.remove(trialSpawnerData.players.iterator().next());
                yield this;
            }
            case 5 -> {
                trialSpawnerData.updatePlayers(world, pos, logic);
                if (!trialSpawnerData.players.isEmpty()) {
                    trialSpawnerData.totalSpawnedMobs = 0;
                    trialSpawnerData.nextMobSpawnsAt = 0L;
                    yield ACTIVE;
                }
                if (trialSpawnerData.isCooldownOver(world)) {
                    logic.setNotOminous(world, pos);
                    trialSpawnerData.reset();
                    yield WAITING_FOR_PLAYERS;
                }
                yield this;
            }
        };
    }

    private void spawnOminousItemSpawner(ServerWorld world, BlockPos pos2, TrialSpawnerLogic logic) {
        TrialSpawnerConfig trialSpawnerConfig;
        TrialSpawnerData trialSpawnerData = logic.getData();
        ItemStack itemStack = trialSpawnerData.getItemsToDropWhenOminous(world, trialSpawnerConfig = logic.getConfig(), pos2).getDataOrEmpty(world.random).orElse(ItemStack.EMPTY);
        if (itemStack.isEmpty()) {
            return;
        }
        if (this.shouldCooldownEnd(world, trialSpawnerData)) {
            TrialSpawnerState.getPosToSpawnItemSpawner(world, pos2, logic, trialSpawnerData).ifPresent(pos -> {
                OminousItemSpawnerEntity ominousItemSpawnerEntity = OminousItemSpawnerEntity.create(world, itemStack);
                ominousItemSpawnerEntity.refreshPositionAfterTeleport((Vec3d)pos);
                world.spawnEntity(ominousItemSpawnerEntity);
                float f = (world.getRandom().nextFloat() - world.getRandom().nextFloat()) * 0.2f + 1.0f;
                world.playSound(null, BlockPos.ofFloored(pos), SoundEvents.BLOCK_TRIAL_SPAWNER_SPAWN_ITEM_BEGIN, SoundCategory.BLOCKS, 1.0f, f);
                trialSpawnerData.cooldownEnd = world.getTime() + logic.getOminousConfig().getCooldownLength();
            });
        }
    }

    private static Optional<Vec3d> getPosToSpawnItemSpawner(ServerWorld world, BlockPos pos, TrialSpawnerLogic logic, TrialSpawnerData data) {
        List<PlayerEntity> list = data.players.stream().map(world::getPlayerByUuid).filter(Objects::nonNull).filter(player -> !player.isCreative() && !player.isSpectator() && player.isAlive() && player.squaredDistanceTo(pos.toCenterPos()) <= (double)MathHelper.square(logic.getDetectionRadius())).toList();
        if (list.isEmpty()) {
            return Optional.empty();
        }
        Entity entity = TrialSpawnerState.getRandomEntity(list, data.spawnedMobsAlive, logic, pos, world);
        if (entity == null) {
            return Optional.empty();
        }
        return TrialSpawnerState.getPosAbove(entity, world);
    }

    private static Optional<Vec3d> getPosAbove(Entity entity, ServerWorld world) {
        Vec3d vec3d2;
        Vec3d vec3d = entity.getPos();
        BlockHitResult blockHitResult = world.raycast(new RaycastContext(vec3d, vec3d2 = vec3d.offset(Direction.UP, entity.getHeight() + 2.0f + (float)world.random.nextInt(4)), RaycastContext.ShapeType.VISUAL, RaycastContext.FluidHandling.NONE, ShapeContext.absent()));
        Vec3d vec3d3 = blockHitResult.getBlockPos().toCenterPos().offset(Direction.DOWN, 1.0);
        BlockPos blockPos = BlockPos.ofFloored(vec3d3);
        if (!world.getBlockState(blockPos).getCollisionShape(world, blockPos).isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(vec3d3);
    }

    @Nullable
    private static Entity getRandomEntity(List<PlayerEntity> players, Set<UUID> entityUuids, TrialSpawnerLogic logic, BlockPos pos, ServerWorld world) {
        List<Entity> list;
        Stream<Entity> stream = entityUuids.stream().map(world::getEntity).filter(Objects::nonNull).filter(entity -> entity.isAlive() && entity.squaredDistanceTo(pos.toCenterPos()) <= (double)MathHelper.square(logic.getDetectionRadius()));
        List<Entity> list2 = list = world.random.nextBoolean() ? stream.toList() : players;
        if (list.isEmpty()) {
            return null;
        }
        if (list.size() == 1) {
            return list.getFirst();
        }
        return Util.getRandom(list, world.random);
    }

    private boolean shouldCooldownEnd(ServerWorld world, TrialSpawnerData data) {
        return world.getTime() >= data.cooldownEnd;
    }

    public int getLuminance() {
        return this.luminance;
    }

    public double getDisplayRotationSpeed() {
        return this.displayRotationSpeed;
    }

    public boolean doesDisplayRotate() {
        return this.displayRotationSpeed >= 0.0;
    }

    public boolean playsSound() {
        return this.playsSound;
    }

    public void emitParticles(World world, BlockPos pos, boolean ominous) {
        this.particleEmitter.emit(world, world.getRandom(), pos, ominous);
    }

    @Override
    public String asString() {
        return this.id;
    }

    static {
        BETWEEN_EJECTING_REWARDS_COOLDOWN = MathHelper.floor(30.0f);
    }

    static interface ParticleEmitter {
        public static final ParticleEmitter NONE = (world, random, pos, ominous) -> {};
        public static final ParticleEmitter WAITING = (world, random, pos, ominous) -> {
            if (random.nextInt(2) == 0) {
                Vec3d vec3d = pos.toCenterPos().addRandom(random, 0.9f);
                ParticleEmitter.emitParticle(ominous ? ParticleTypes.SOUL_FIRE_FLAME : ParticleTypes.SMALL_FLAME, vec3d, world);
            }
        };
        public static final ParticleEmitter ACTIVE = (world, random, pos, ominous) -> {
            Vec3d vec3d = pos.toCenterPos().addRandom(random, 1.0f);
            ParticleEmitter.emitParticle(ParticleTypes.SMOKE, vec3d, world);
            ParticleEmitter.emitParticle(ominous ? ParticleTypes.SOUL_FIRE_FLAME : ParticleTypes.FLAME, vec3d, world);
        };
        public static final ParticleEmitter COOLDOWN = (world, random, pos, ominous) -> {
            Vec3d vec3d = pos.toCenterPos().addRandom(random, 0.9f);
            if (random.nextInt(3) == 0) {
                ParticleEmitter.emitParticle(ParticleTypes.SMOKE, vec3d, world);
            }
            if (world.getTime() % 20L == 0L) {
                Vec3d vec3d2 = pos.toCenterPos().add(0.0, 0.5, 0.0);
                int i = world.getRandom().nextInt(4) + 20;
                for (int j = 0; j < i; ++j) {
                    ParticleEmitter.emitParticle(ParticleTypes.SMOKE, vec3d2, world);
                }
            }
        };

        private static void emitParticle(SimpleParticleType type, Vec3d pos, World world) {
            world.addParticle(type, pos.getX(), pos.getY(), pos.getZ(), 0.0, 0.0, 0.0);
        }

        public void emit(World var1, Random var2, BlockPos var3, boolean var4);
    }

    static class Luminance {
        private static final int NONE = 0;
        private static final int LOW = 4;
        private static final int HIGH = 8;

        private Luminance() {
        }
    }

    static class DisplayRotationSpeed {
        private static final double NONE = -1.0;
        private static final double SLOW = 200.0;
        private static final double FAST = 1000.0;

        private DisplayRotationSpeed() {
        }
    }
}

