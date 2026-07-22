/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.block.entity;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.yggdrasil.ProfileResult;
import com.mojang.logging.LogUtils;
import java.time.Duration;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.BooleanSupplier;
import net.minecraft.block.BlockState;
import net.minecraft.block.SkullBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.component.ComponentMap;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ProfileComponent;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.text.Text;
import net.minecraft.util.ApiServices;
import net.minecraft.util.Identifier;
import net.minecraft.util.StringHelper;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class SkullBlockEntity
extends BlockEntity {
    private static final String PROFILE_NBT_KEY = "profile";
    private static final String NOTE_BLOCK_SOUND_NBT_KEY = "note_block_sound";
    private static final String CUSTOM_NAME_NBT_KEY = "custom_name";
    private static final Logger LOGGER = LogUtils.getLogger();
    @Nullable
    private static Executor currentExecutor;
    @Nullable
    private static LoadingCache<String, CompletableFuture<Optional<GameProfile>>> nameToProfileCache;
    @Nullable
    private static LoadingCache<UUID, CompletableFuture<Optional<GameProfile>>> uuidToProfileCache;
    public static final Executor EXECUTOR;
    @Nullable
    private ProfileComponent owner;
    @Nullable
    private Identifier noteBlockSound;
    private int poweredTicks;
    private boolean powered;
    @Nullable
    private Text customName;

    public SkullBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntityType.SKULL, pos, state);
    }

    public static void setServices(final ApiServices apiServices, Executor executor) {
        currentExecutor = executor;
        final BooleanSupplier booleanSupplier = () -> uuidToProfileCache == null;
        nameToProfileCache = CacheBuilder.newBuilder().expireAfterAccess(Duration.ofMinutes(10L)).maximumSize(256L).build(new CacheLoader<String, CompletableFuture<Optional<GameProfile>>>(){

            @Override
            public CompletableFuture<Optional<GameProfile>> load(String string) {
                return SkullBlockEntity.fetchProfileByName(string, apiServices);
            }

            @Override
            public /* synthetic */ Object load(Object name) throws Exception {
                return this.load((String)name);
            }
        });
        uuidToProfileCache = CacheBuilder.newBuilder().expireAfterAccess(Duration.ofMinutes(10L)).maximumSize(256L).build(new CacheLoader<UUID, CompletableFuture<Optional<GameProfile>>>(){

            @Override
            public CompletableFuture<Optional<GameProfile>> load(UUID uUID) {
                return SkullBlockEntity.fetchProfileByUuid(uUID, apiServices, booleanSupplier);
            }

            @Override
            public /* synthetic */ Object load(Object uuid) throws Exception {
                return this.load((UUID)uuid);
            }
        });
    }

    static CompletableFuture<Optional<GameProfile>> fetchProfileByName(String name, ApiServices apiServices) {
        return apiServices.userCache().findByNameAsync(name).thenCompose(optional -> {
            LoadingCache<UUID, CompletableFuture<Optional<GameProfile>>> loadingCache = uuidToProfileCache;
            if (loadingCache == null || optional.isEmpty()) {
                return CompletableFuture.completedFuture(Optional.empty());
            }
            return loadingCache.getUnchecked(((GameProfile)optional.get()).getId()).thenApply(optional2 -> optional2.or(() -> optional));
        });
    }

    static CompletableFuture<Optional<GameProfile>> fetchProfileByUuid(UUID uuid, ApiServices apiServices, BooleanSupplier booleanSupplier) {
        return CompletableFuture.supplyAsync(() -> {
            if (booleanSupplier.getAsBoolean()) {
                return Optional.empty();
            }
            ProfileResult profileResult = apiServices.sessionService().fetchProfile(uuid, true);
            return Optional.ofNullable(profileResult).map(ProfileResult::profile);
        }, Util.getMainWorkerExecutor());
    }

    public static void clearServices() {
        currentExecutor = null;
        nameToProfileCache = null;
        uuidToProfileCache = null;
    }

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.writeNbt(nbt, registryLookup);
        if (this.owner != null) {
            nbt.put(PROFILE_NBT_KEY, ProfileComponent.CODEC.encodeStart(NbtOps.INSTANCE, this.owner).getOrThrow());
        }
        if (this.noteBlockSound != null) {
            nbt.putString(NOTE_BLOCK_SOUND_NBT_KEY, this.noteBlockSound.toString());
        }
        if (this.customName != null) {
            nbt.putString(CUSTOM_NAME_NBT_KEY, Text.Serialization.toJsonString(this.customName, registryLookup));
        }
    }

    @Override
    protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.readNbt(nbt, registryLookup);
        if (nbt.contains(PROFILE_NBT_KEY)) {
            ProfileComponent.CODEC.parse(NbtOps.INSTANCE, nbt.get(PROFILE_NBT_KEY)).resultOrPartial(string -> LOGGER.error("Failed to load profile from player head: {}", string)).ifPresent(this::setOwner);
        }
        if (nbt.contains(NOTE_BLOCK_SOUND_NBT_KEY, NbtElement.STRING_TYPE)) {
            this.noteBlockSound = Identifier.tryParse(nbt.getString(NOTE_BLOCK_SOUND_NBT_KEY));
        }
        this.customName = nbt.contains(CUSTOM_NAME_NBT_KEY, NbtElement.STRING_TYPE) ? SkullBlockEntity.tryParseCustomName(nbt.getString(CUSTOM_NAME_NBT_KEY), registryLookup) : null;
    }

    public static void tick(World world, BlockPos pos, BlockState state, SkullBlockEntity blockEntity) {
        if (state.contains(SkullBlock.POWERED) && state.get(SkullBlock.POWERED).booleanValue()) {
            blockEntity.powered = true;
            ++blockEntity.poweredTicks;
        } else {
            blockEntity.powered = false;
        }
    }

    public float getPoweredTicks(float tickDelta) {
        if (this.powered) {
            return (float)this.poweredTicks + tickDelta;
        }
        return this.poweredTicks;
    }

    @Nullable
    public ProfileComponent getOwner() {
        return this.owner;
    }

    @Nullable
    public Identifier getNoteBlockSound() {
        return this.noteBlockSound;
    }

    public BlockEntityUpdateS2CPacket toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    @Override
    public NbtCompound toInitialChunkDataNbt(RegistryWrapper.WrapperLookup registryLookup) {
        return this.createComponentlessNbt(registryLookup);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void setOwner(@Nullable ProfileComponent profile) {
        SkullBlockEntity skullBlockEntity = this;
        synchronized (skullBlockEntity) {
            this.owner = profile;
        }
        this.loadOwnerProperties();
    }

    private void loadOwnerProperties() {
        if (this.owner == null || this.owner.isCompleted()) {
            this.markDirty();
            return;
        }
        this.owner.getFuture().thenAcceptAsync(owner -> {
            this.owner = owner;
            this.markDirty();
        }, EXECUTOR);
    }

    public static CompletableFuture<Optional<GameProfile>> fetchProfileByName(String name) {
        LoadingCache<String, CompletableFuture<Optional<GameProfile>>> loadingCache = nameToProfileCache;
        if (loadingCache != null && StringHelper.isValidPlayerName(name)) {
            return loadingCache.getUnchecked(name);
        }
        return CompletableFuture.completedFuture(Optional.empty());
    }

    public static CompletableFuture<Optional<GameProfile>> fetchProfileByUuid(UUID uuid) {
        LoadingCache<UUID, CompletableFuture<Optional<GameProfile>>> loadingCache = uuidToProfileCache;
        if (loadingCache != null) {
            return loadingCache.getUnchecked(uuid);
        }
        return CompletableFuture.completedFuture(Optional.empty());
    }

    @Override
    protected void readComponents(BlockEntity.ComponentsAccess components) {
        super.readComponents(components);
        this.setOwner(components.get(DataComponentTypes.PROFILE));
        this.noteBlockSound = components.get(DataComponentTypes.NOTE_BLOCK_SOUND);
        this.customName = components.get(DataComponentTypes.CUSTOM_NAME);
    }

    @Override
    protected void addComponents(ComponentMap.Builder componentMapBuilder) {
        super.addComponents(componentMapBuilder);
        componentMapBuilder.add(DataComponentTypes.PROFILE, this.owner);
        componentMapBuilder.add(DataComponentTypes.NOTE_BLOCK_SOUND, this.noteBlockSound);
        componentMapBuilder.add(DataComponentTypes.CUSTOM_NAME, this.customName);
    }

    @Override
    public void removeFromCopiedStackNbt(NbtCompound nbt) {
        super.removeFromCopiedStackNbt(nbt);
        nbt.remove(PROFILE_NBT_KEY);
        nbt.remove(NOTE_BLOCK_SOUND_NBT_KEY);
        nbt.remove(CUSTOM_NAME_NBT_KEY);
    }

    public /* synthetic */ Packet toUpdatePacket() {
        return this.toUpdatePacket();
    }

    static {
        EXECUTOR = runnable -> {
            Executor executor = currentExecutor;
            if (executor != null) {
                executor.execute(runnable);
            }
        };
    }
}

