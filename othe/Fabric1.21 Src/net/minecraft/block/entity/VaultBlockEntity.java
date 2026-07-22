/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.block.entity;

import com.google.common.annotations.VisibleForTesting;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.VaultBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.enums.VaultState;
import net.minecraft.block.vault.VaultClientData;
import net.minecraft.block.vault.VaultConfig;
import net.minecraft.block.vault.VaultServerData;
import net.minecraft.block.vault.VaultSharedData;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.LootTables;
import net.minecraft.loot.context.LootContextParameterSet;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryOps;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class VaultBlockEntity
extends BlockEntity {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final VaultServerData serverData = new VaultServerData();
    private final VaultSharedData sharedData = new VaultSharedData();
    private final VaultClientData clientData = new VaultClientData();
    private VaultConfig config = VaultConfig.DEFAULT;

    public VaultBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntityType.VAULT, pos, state);
    }

    @Override
    @Nullable
    public Packet<ClientPlayPacketListener> toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    @Override
    public NbtCompound toInitialChunkDataNbt(RegistryWrapper.WrapperLookup registryLookup) {
        return Util.make(new NbtCompound(), nbt -> nbt.put("shared_data", VaultBlockEntity.encodeValue(VaultSharedData.codec, this.sharedData, registryLookup)));
    }

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.writeNbt(nbt, registryLookup);
        nbt.put("config", VaultBlockEntity.encodeValue(VaultConfig.codec, this.config, registryLookup));
        nbt.put("shared_data", VaultBlockEntity.encodeValue(VaultSharedData.codec, this.sharedData, registryLookup));
        nbt.put("server_data", VaultBlockEntity.encodeValue(VaultServerData.codec, this.serverData, registryLookup));
    }

    private static <T> NbtElement encodeValue(Codec<T> codec, T value, RegistryWrapper.WrapperLookup registries) {
        return codec.encodeStart(registries.getOps(NbtOps.INSTANCE), (NbtElement)value).getOrThrow();
    }

    @Override
    protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.readNbt(nbt, registryLookup);
        RegistryOps<NbtElement> dynamicOps = registryLookup.getOps(NbtOps.INSTANCE);
        if (nbt.contains("server_data")) {
            VaultServerData.codec.parse(dynamicOps, nbt.get("server_data")).resultOrPartial(LOGGER::error).ifPresent(this.serverData::copyFrom);
        }
        if (nbt.contains("config")) {
            VaultConfig.codec.parse(dynamicOps, nbt.get("config")).resultOrPartial(LOGGER::error).ifPresent(config -> {
                this.config = config;
            });
        }
        if (nbt.contains("shared_data")) {
            VaultSharedData.codec.parse(dynamicOps, nbt.get("shared_data")).resultOrPartial(LOGGER::error).ifPresent(this.sharedData::copyFrom);
        }
    }

    @Nullable
    public VaultServerData getServerData() {
        return this.world == null || this.world.isClient ? null : this.serverData;
    }

    public VaultSharedData getSharedData() {
        return this.sharedData;
    }

    public VaultClientData getClientData() {
        return this.clientData;
    }

    public VaultConfig getConfig() {
        return this.config;
    }

    @VisibleForTesting
    public void setConfig(VaultConfig config) {
        this.config = config;
    }

    public static final class Client {
        private static final int field_48870 = 20;
        private static final float field_48871 = 0.5f;
        private static final float field_48872 = 0.02f;
        private static final int field_48873 = 20;
        private static final int field_48874 = 20;

        public static void tick(World world, BlockPos pos, BlockState state, VaultClientData clientData, VaultSharedData sharedData) {
            clientData.rotateDisplay();
            if (world.getTime() % 20L == 0L) {
                Client.spawnConnectedParticles(world, pos, state, sharedData);
            }
            Client.spawnAmbientParticles(world, pos, sharedData, state.get(VaultBlock.OMINOUS) != false ? ParticleTypes.SOUL_FIRE_FLAME : ParticleTypes.SMALL_FLAME);
            Client.playAmbientSound(world, pos, sharedData);
        }

        public static void spawnActivateParticles(World world, BlockPos pos, BlockState state, VaultSharedData sharedData, ParticleEffect particle) {
            Client.spawnConnectedParticles(world, pos, state, sharedData);
            Random random = world.random;
            for (int i = 0; i < 20; ++i) {
                Vec3d vec3d = Client.getRegularParticlesPos(pos, random);
                world.addParticle(ParticleTypes.SMOKE, vec3d.getX(), vec3d.getY(), vec3d.getZ(), 0.0, 0.0, 0.0);
                world.addParticle(particle, vec3d.getX(), vec3d.getY(), vec3d.getZ(), 0.0, 0.0, 0.0);
            }
        }

        public static void spawnDeactivateParticles(World world, BlockPos pos, ParticleEffect particle) {
            Random random = world.random;
            for (int i = 0; i < 20; ++i) {
                Vec3d vec3d = Client.getDeactivateParticlesPos(pos, random);
                Vec3d vec3d2 = new Vec3d(random.nextGaussian() * 0.02, random.nextGaussian() * 0.02, random.nextGaussian() * 0.02);
                world.addParticle(particle, vec3d.getX(), vec3d.getY(), vec3d.getZ(), vec3d2.getX(), vec3d2.getY(), vec3d2.getZ());
            }
        }

        private static void spawnAmbientParticles(World world, BlockPos pos, VaultSharedData sharedData, ParticleEffect particle) {
            Random random = world.getRandom();
            if (random.nextFloat() <= 0.5f) {
                Vec3d vec3d = Client.getRegularParticlesPos(pos, random);
                world.addParticle(ParticleTypes.SMOKE, vec3d.getX(), vec3d.getY(), vec3d.getZ(), 0.0, 0.0, 0.0);
                if (Client.hasDisplayItem(sharedData)) {
                    world.addParticle(particle, vec3d.getX(), vec3d.getY(), vec3d.getZ(), 0.0, 0.0, 0.0);
                }
            }
        }

        private static void spawnConnectedParticlesFor(World world, Vec3d pos, PlayerEntity player) {
            Random random = world.random;
            Vec3d vec3d = pos.relativize(player.getPos().add(0.0, player.getHeight() / 2.0f, 0.0));
            int i = MathHelper.nextInt(random, 2, 5);
            for (int j = 0; j < i; ++j) {
                Vec3d vec3d2 = vec3d.addRandom(random, 1.0f);
                world.addParticle(ParticleTypes.VAULT_CONNECTION, pos.getX(), pos.getY(), pos.getZ(), vec3d2.getX(), vec3d2.getY(), vec3d2.getZ());
            }
        }

        private static void spawnConnectedParticles(World world, BlockPos pos, BlockState state, VaultSharedData sharedData) {
            Set<UUID> set = sharedData.getConnectedPlayers();
            if (set.isEmpty()) {
                return;
            }
            Vec3d vec3d = Client.getConnectedParticlesOrigin(pos, state.get(VaultBlock.FACING));
            for (UUID uUID : set) {
                PlayerEntity playerEntity = world.getPlayerByUuid(uUID);
                if (playerEntity == null || !Client.isPlayerWithinConnectedParticlesRange(pos, sharedData, playerEntity)) continue;
                Client.spawnConnectedParticlesFor(world, vec3d, playerEntity);
            }
        }

        private static boolean isPlayerWithinConnectedParticlesRange(BlockPos pos, VaultSharedData sharedData, PlayerEntity player) {
            return player.getBlockPos().getSquaredDistance(pos) <= MathHelper.square(sharedData.getConnectedParticlesRange());
        }

        private static void playAmbientSound(World world, BlockPos pos, VaultSharedData sharedData) {
            if (!Client.hasDisplayItem(sharedData)) {
                return;
            }
            Random random = world.getRandom();
            if (random.nextFloat() <= 0.02f) {
                world.playSoundAtBlockCenter(pos, SoundEvents.BLOCK_VAULT_AMBIENT, SoundCategory.BLOCKS, random.nextFloat() * 0.25f + 0.75f, random.nextFloat() + 0.5f, false);
            }
        }

        public static boolean hasDisplayItem(VaultSharedData sharedData) {
            return sharedData.hasDisplayItem();
        }

        private static Vec3d getDeactivateParticlesPos(BlockPos pos, Random random) {
            return Vec3d.of(pos).add(MathHelper.nextDouble(random, 0.4, 0.6), MathHelper.nextDouble(random, 0.4, 0.6), MathHelper.nextDouble(random, 0.4, 0.6));
        }

        private static Vec3d getRegularParticlesPos(BlockPos pos, Random random) {
            return Vec3d.of(pos).add(MathHelper.nextDouble(random, 0.1, 0.9), MathHelper.nextDouble(random, 0.25, 0.75), MathHelper.nextDouble(random, 0.1, 0.9));
        }

        private static Vec3d getConnectedParticlesOrigin(BlockPos pos, Direction direction) {
            return Vec3d.ofBottomCenter(pos).add((double)direction.getOffsetX() * 0.5, 1.75, (double)direction.getOffsetZ() * 0.5);
        }
    }

    public static final class Server {
        private static final int UNLOCK_TIME = 14;
        private static final int DISPLAY_UPDATE_INTERVAL = 20;
        private static final int FAILED_UNLOCK_COOLDOWN = 15;

        public static void tick(ServerWorld world, BlockPos pos, BlockState state, VaultConfig config, VaultServerData serverData, VaultSharedData sharedData) {
            VaultState vaultState = state.get(VaultBlock.VAULT_STATE);
            if (Server.shouldUpdateDisplayItem(world.getTime(), vaultState)) {
                Server.updateDisplayItem(world, vaultState, config, sharedData, pos);
            }
            BlockState blockState = state;
            if (world.getTime() >= serverData.getStateUpdatingResumeTime() && !state.equals(blockState = (BlockState)blockState.with(VaultBlock.VAULT_STATE, vaultState.update(world, pos, config, serverData, sharedData)))) {
                Server.changeVaultState(world, pos, state, blockState, config, sharedData);
            }
            if (serverData.dirty || sharedData.dirty) {
                VaultBlockEntity.markDirty(world, pos, state);
                if (sharedData.dirty) {
                    world.updateListeners(pos, state, blockState, Block.NOTIFY_LISTENERS);
                }
                serverData.dirty = false;
                sharedData.dirty = false;
            }
        }

        public static void tryUnlock(ServerWorld world, BlockPos pos, BlockState state, VaultConfig config, VaultServerData serverData, VaultSharedData sharedData, PlayerEntity player, ItemStack stack) {
            VaultState vaultState = state.get(VaultBlock.VAULT_STATE);
            if (!Server.canBeUnlocked(config, vaultState)) {
                return;
            }
            if (!Server.isValidKey(config, stack)) {
                Server.playFailedUnlockSound(world, serverData, pos, SoundEvents.BLOCK_VAULT_INSERT_ITEM_FAIL);
                return;
            }
            if (serverData.hasRewardedPlayer(player)) {
                Server.playFailedUnlockSound(world, serverData, pos, SoundEvents.BLOCK_VAULT_REJECT_REWARDED_PLAYER);
                return;
            }
            List<ItemStack> list = Server.generateLoot(world, config, pos, player);
            if (list.isEmpty()) {
                return;
            }
            player.incrementStat(Stats.USED.getOrCreateStat(stack.getItem()));
            stack.decrementUnlessCreative(config.keyItem().getCount(), player);
            Server.unlock(world, state, pos, config, serverData, sharedData, list);
            serverData.markPlayerAsRewarded(player);
            sharedData.updateConnectedPlayers(world, pos, serverData, config, config.deactivationRange());
        }

        static void changeVaultState(ServerWorld world, BlockPos pos, BlockState oldState, BlockState newState, VaultConfig config, VaultSharedData sharedData) {
            VaultState vaultState = oldState.get(VaultBlock.VAULT_STATE);
            VaultState vaultState2 = newState.get(VaultBlock.VAULT_STATE);
            world.setBlockState(pos, newState, Block.NOTIFY_ALL);
            vaultState.onStateChange(world, pos, vaultState2, config, sharedData, newState.get(VaultBlock.OMINOUS));
        }

        static void updateDisplayItem(ServerWorld world, VaultState state, VaultConfig config, VaultSharedData sharedData, BlockPos pos) {
            if (!Server.canBeUnlocked(config, state)) {
                sharedData.setDisplayItem(ItemStack.EMPTY);
                return;
            }
            ItemStack itemStack = Server.generateDisplayItem(world, pos, config.overrideLootTableToDisplay().orElse(config.lootTable()));
            sharedData.setDisplayItem(itemStack);
        }

        private static ItemStack generateDisplayItem(ServerWorld world, BlockPos pos, RegistryKey<LootTable> lootTable) {
            LootContextParameterSet lootContextParameterSet;
            LootTable lootTable2 = world.getServer().getReloadableRegistries().getLootTable(lootTable);
            ObjectArrayList<ItemStack> list = lootTable2.generateLoot(lootContextParameterSet = new LootContextParameterSet.Builder(world).add(LootContextParameters.ORIGIN, Vec3d.ofCenter(pos)).build(LootContextTypes.VAULT), world.getRandom());
            if (list.isEmpty()) {
                return ItemStack.EMPTY;
            }
            return Util.getRandom(list, world.getRandom());
        }

        private static void unlock(ServerWorld world, BlockState state, BlockPos pos, VaultConfig config, VaultServerData serverData, VaultSharedData sharedData, List<ItemStack> itemsToEject) {
            serverData.setItemsToEject(itemsToEject);
            sharedData.setDisplayItem(serverData.getItemToDisplay());
            serverData.setStateUpdatingResumeTime(world.getTime() + 14L);
            Server.changeVaultState(world, pos, state, (BlockState)state.with(VaultBlock.VAULT_STATE, VaultState.UNLOCKING), config, sharedData);
        }

        private static List<ItemStack> generateLoot(ServerWorld world, VaultConfig config, BlockPos pos, PlayerEntity player) {
            LootTable lootTable = world.getServer().getReloadableRegistries().getLootTable(config.lootTable());
            LootContextParameterSet lootContextParameterSet = new LootContextParameterSet.Builder(world).add(LootContextParameters.ORIGIN, Vec3d.ofCenter(pos)).luck(player.getLuck()).add(LootContextParameters.THIS_ENTITY, player).build(LootContextTypes.VAULT);
            return lootTable.generateLoot(lootContextParameterSet);
        }

        private static boolean canBeUnlocked(VaultConfig config, VaultState state) {
            return config.lootTable() != LootTables.EMPTY && !config.keyItem().isEmpty() && state != VaultState.INACTIVE;
        }

        private static boolean isValidKey(VaultConfig config, ItemStack stack) {
            return ItemStack.areItemsAndComponentsEqual(stack, config.keyItem()) && stack.getCount() >= config.keyItem().getCount();
        }

        private static boolean shouldUpdateDisplayItem(long time, VaultState state) {
            return time % 20L == 0L && state == VaultState.ACTIVE;
        }

        private static void playFailedUnlockSound(ServerWorld world, VaultServerData serverData, BlockPos pos, SoundEvent sound) {
            if (world.getTime() >= serverData.getLastFailedUnlockTime() + 15L) {
                world.playSound(null, pos, sound, SoundCategory.BLOCKS);
                serverData.setLastFailedUnlockTime(world.getTime());
            }
        }
    }
}

