/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.entity.passive;

import com.mojang.serialization.Dynamic;
import io.netty.buffer.ByteBuf;
import java.util.function.IntFunction;
import net.minecraft.block.BlockState;
import net.minecraft.entity.AnimationState;
import net.minecraft.entity.EntityStatuses;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.control.BodyControl;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.ArmadilloBrain;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.registry.tag.EntityTypeTags;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.server.network.DebugInfoSender;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.TimeHelper;
import net.minecraft.util.function.ValueLists;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;

public class ArmadilloEntity
extends AnimalEntity {
    public static final float field_47778 = 0.6f;
    public static final float field_48332 = 32.5f;
    public static final int field_47779 = 80;
    private static final double field_48333 = 7.0;
    private static final double field_48334 = 2.0;
    private static final TrackedData<State> STATE = DataTracker.registerData(ArmadilloEntity.class, TrackedDataHandlerRegistry.ARMADILLO_STATE);
    private long currentStateTicks = 0L;
    public final AnimationState unrollingAnimationState = new AnimationState();
    public final AnimationState rollingAnimationState = new AnimationState();
    public final AnimationState scaredAnimationState = new AnimationState();
    private int nextScuteShedCooldown;
    private boolean peeking = false;

    public ArmadilloEntity(EntityType<? extends AnimalEntity> entityType, World world) {
        super(entityType, world);
        this.getNavigation().setCanSwim(true);
        this.nextScuteShedCooldown = this.getNextScuteShedCooldown();
    }

    @Override
    @Nullable
    public PassiveEntity createChild(ServerWorld world, PassiveEntity entity) {
        return EntityType.ARMADILLO.create(world);
    }

    public static DefaultAttributeContainer.Builder createArmadilloAttributes() {
        return MobEntity.createMobAttributes().add(EntityAttributes.GENERIC_MAX_HEALTH, 12.0).add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.14);
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        super.initDataTracker(builder);
        builder.add(STATE, State.IDLE);
    }

    public boolean isNotIdle() {
        return this.dataTracker.get(STATE) != State.IDLE;
    }

    public boolean isRolledUp() {
        return this.getState().isRolledUp(this.currentStateTicks);
    }

    public boolean shouldSwitchToScaredState() {
        return this.getState() == State.ROLLING && this.currentStateTicks > (long)State.ROLLING.getLengthInTicks();
    }

    public State getState() {
        return this.dataTracker.get(STATE);
    }

    @Override
    protected void sendAiDebugData() {
        super.sendAiDebugData();
        DebugInfoSender.sendBrainDebugData(this);
    }

    public void setState(State state) {
        this.dataTracker.set(STATE, state);
    }

    @Override
    public void onTrackedDataSet(TrackedData<?> data) {
        if (STATE.equals(data)) {
            this.currentStateTicks = 0L;
        }
        super.onTrackedDataSet(data);
    }

    protected Brain.Profile<ArmadilloEntity> createBrainProfile() {
        return ArmadilloBrain.createBrainProfile();
    }

    @Override
    protected Brain<?> deserializeBrain(Dynamic<?> dynamic) {
        return ArmadilloBrain.create(this.createBrainProfile().deserialize(dynamic));
    }

    @Override
    protected void mobTick() {
        this.getWorld().getProfiler().push("armadilloBrain");
        this.brain.tick((ServerWorld)this.getWorld(), this);
        this.getWorld().getProfiler().pop();
        this.getWorld().getProfiler().push("armadilloActivityUpdate");
        ArmadilloBrain.updateActivities(this);
        this.getWorld().getProfiler().pop();
        if (this.isAlive() && !this.isBaby() && --this.nextScuteShedCooldown <= 0) {
            this.playSound(SoundEvents.ENTITY_ARMADILLO_SCUTE_DROP, 1.0f, (this.random.nextFloat() - this.random.nextFloat()) * 0.2f + 1.0f);
            this.dropItem(Items.ARMADILLO_SCUTE);
            this.emitGameEvent(GameEvent.ENTITY_PLACE);
            this.nextScuteShedCooldown = this.getNextScuteShedCooldown();
        }
        super.mobTick();
    }

    private int getNextScuteShedCooldown() {
        return this.random.nextInt(20 * TimeHelper.MINUTE_IN_SECONDS * 5) + 20 * TimeHelper.MINUTE_IN_SECONDS * 5;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.getWorld().isClient()) {
            this.updateAnimationStates();
        }
        if (this.isNotIdle()) {
            this.clampHeadYaw();
        }
        ++this.currentStateTicks;
    }

    @Override
    public float getScaleFactor() {
        return this.isBaby() ? 0.6f : 1.0f;
    }

    private void updateAnimationStates() {
        switch (this.getState().ordinal()) {
            case 0: {
                this.unrollingAnimationState.stop();
                this.rollingAnimationState.stop();
                this.scaredAnimationState.stop();
                break;
            }
            case 3: {
                this.unrollingAnimationState.startIfNotRunning(this.age);
                this.rollingAnimationState.stop();
                this.scaredAnimationState.stop();
                break;
            }
            case 1: {
                this.unrollingAnimationState.stop();
                this.rollingAnimationState.startIfNotRunning(this.age);
                this.scaredAnimationState.stop();
                break;
            }
            case 2: {
                this.unrollingAnimationState.stop();
                this.rollingAnimationState.stop();
                if (this.peeking) {
                    this.scaredAnimationState.stop();
                    this.peeking = false;
                }
                if (this.currentStateTicks == 0L) {
                    this.scaredAnimationState.start(this.age);
                    this.scaredAnimationState.skip(State.SCARED.getLengthInTicks(), 1.0f);
                    break;
                }
                this.scaredAnimationState.startIfNotRunning(this.age);
            }
        }
    }

    @Override
    public void handleStatus(byte status) {
        if (status == EntityStatuses.PEEKING && this.getWorld().isClient) {
            this.peeking = true;
            this.getWorld().playSound(this.getX(), this.getY(), this.getZ(), SoundEvents.ENTITY_ARMADILLO_PEEK, this.getSoundCategory(), 1.0f, 1.0f, false);
        } else {
            super.handleStatus(status);
        }
    }

    @Override
    public boolean isBreedingItem(ItemStack stack) {
        return stack.isIn(ItemTags.ARMADILLO_FOOD);
    }

    public static boolean canSpawn(EntityType<ArmadilloEntity> entityType, WorldAccess world, SpawnReason spawnReason, BlockPos pos, Random random) {
        return world.getBlockState(pos.down()).isIn(BlockTags.ARMADILLO_SPAWNABLE_ON) && ArmadilloEntity.isLightLevelValidForNaturalSpawn(world, pos);
    }

    public boolean isEntityThreatening(LivingEntity entity) {
        if (!this.getBoundingBox().expand(7.0, 2.0, 7.0).intersects(entity.getBoundingBox())) {
            return false;
        }
        if (entity.getType().isIn(EntityTypeTags.UNDEAD)) {
            return true;
        }
        if (this.getAttacker() == entity) {
            return true;
        }
        if (entity instanceof PlayerEntity) {
            PlayerEntity playerEntity = (PlayerEntity)entity;
            if (playerEntity.isSpectator()) {
                return false;
            }
            return playerEntity.isSprinting() || playerEntity.hasVehicle();
        }
        return false;
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        nbt.putString("state", this.getState().asString());
        nbt.putInt("scute_time", this.nextScuteShedCooldown);
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        this.setState(State.fromName(nbt.getString("state")));
        if (nbt.contains("scute_time")) {
            this.nextScuteShedCooldown = nbt.getInt("scute_time");
        }
    }

    public void startRolling() {
        if (this.isNotIdle()) {
            return;
        }
        this.stopMovement();
        this.resetLoveTicks();
        this.emitGameEvent(GameEvent.ENTITY_ACTION);
        this.playSound(SoundEvents.ENTITY_ARMADILLO_ROLL);
        this.setState(State.ROLLING);
    }

    public void unroll() {
        if (!this.isNotIdle()) {
            return;
        }
        this.emitGameEvent(GameEvent.ENTITY_ACTION);
        this.playSound(SoundEvents.ENTITY_ARMADILLO_UNROLL_FINISH);
        this.setState(State.IDLE);
    }

    @Override
    public boolean damage(DamageSource source, float amount) {
        if (this.isNotIdle()) {
            amount = (amount - 1.0f) / 2.0f;
        }
        return super.damage(source, amount);
    }

    @Override
    protected void applyDamage(DamageSource source, float amount) {
        super.applyDamage(source, amount);
        if (this.isAiDisabled() || this.isDead()) {
            return;
        }
        if (source.getAttacker() instanceof LivingEntity) {
            this.getBrain().remember(MemoryModuleType.DANGER_DETECTED_RECENTLY, true, 80L);
            if (this.canRollUp()) {
                this.startRolling();
            }
        } else if (source.isIn(DamageTypeTags.PANIC_ENVIRONMENTAL_CAUSES)) {
            this.unroll();
        }
    }

    @Override
    public ActionResult interactMob(PlayerEntity player, Hand hand) {
        ItemStack itemStack = player.getStackInHand(hand);
        if (itemStack.isOf(Items.BRUSH) && this.brushScute()) {
            itemStack.damage(16, player, ArmadilloEntity.getSlotForHand(hand));
            return ActionResult.success(this.getWorld().isClient);
        }
        if (this.isNotIdle()) {
            return ActionResult.FAIL;
        }
        return super.interactMob(player, hand);
    }

    @Override
    public void growUp(int age, boolean overGrow) {
        if (this.isBaby() && overGrow) {
            this.playSound(SoundEvents.ENTITY_ARMADILLO_EAT);
        }
        super.growUp(age, overGrow);
    }

    public boolean brushScute() {
        if (this.isBaby()) {
            return false;
        }
        this.dropStack(new ItemStack(Items.ARMADILLO_SCUTE));
        this.emitGameEvent(GameEvent.ENTITY_INTERACT);
        this.playSoundIfNotSilent(SoundEvents.ENTITY_ARMADILLO_BRUSH);
        return true;
    }

    public boolean canRollUp() {
        return !this.isPanicking() && !this.isInFluid() && !this.isLeashed() && !this.hasVehicle() && !this.hasPassengers();
    }

    @Override
    public void lovePlayer(@Nullable PlayerEntity player) {
        super.lovePlayer(player);
        this.playSound(SoundEvents.ENTITY_ARMADILLO_EAT);
    }

    @Override
    public boolean canEat() {
        return super.canEat() && !this.isNotIdle();
    }

    @Override
    public SoundEvent getEatSound(ItemStack stack) {
        return SoundEvents.ENTITY_ARMADILLO_EAT;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        if (this.isNotIdle()) {
            return null;
        }
        return SoundEvents.ENTITY_ARMADILLO_AMBIENT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ENTITY_ARMADILLO_DEATH;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        if (this.isNotIdle()) {
            return SoundEvents.ENTITY_ARMADILLO_HURT_REDUCED;
        }
        return SoundEvents.ENTITY_ARMADILLO_HURT;
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        this.playSound(SoundEvents.ENTITY_ARMADILLO_STEP, 0.15f, 1.0f);
    }

    @Override
    public int getMaxHeadRotation() {
        if (this.isNotIdle()) {
            return 0;
        }
        return 32;
    }

    @Override
    protected BodyControl createBodyControl() {
        return new BodyControl(this){

            @Override
            public void tick() {
                if (!ArmadilloEntity.this.isNotIdle()) {
                    super.tick();
                }
            }
        };
    }

    public static enum State implements StringIdentifiable
    {
        IDLE("idle", false, 0, 0){

            @Override
            public boolean isRolledUp(long currentStateTicks) {
                return false;
            }
        }
        ,
        ROLLING("rolling", true, 10, 1){

            @Override
            public boolean isRolledUp(long currentStateTicks) {
                return currentStateTicks > 5L;
            }
        }
        ,
        SCARED("scared", true, 50, 2){

            @Override
            public boolean isRolledUp(long currentStateTicks) {
                return true;
            }
        }
        ,
        UNROLLING("unrolling", true, 30, 3){

            @Override
            public boolean isRolledUp(long currentStateTicks) {
                return currentStateTicks < 26L;
            }
        };

        private static final StringIdentifiable.EnumCodec<State> CODEC;
        private static final IntFunction<State> INDEX_TO_VALUE;
        public static final PacketCodec<ByteBuf, State> PACKET_CODEC;
        private final String name;
        private final boolean runRollUpTask;
        private final int lengthInTicks;
        private final int index;

        State(String name, boolean runRollUpTask, int lengthInTicks, int index) {
            this.name = name;
            this.runRollUpTask = runRollUpTask;
            this.lengthInTicks = lengthInTicks;
            this.index = index;
        }

        public static State fromName(String name) {
            return CODEC.byId(name, IDLE);
        }

        @Override
        public String asString() {
            return this.name;
        }

        private int getIndex() {
            return this.index;
        }

        public abstract boolean isRolledUp(long var1);

        public boolean shouldRunRollUpTask() {
            return this.runRollUpTask;
        }

        public int getLengthInTicks() {
            return this.lengthInTicks;
        }

        static {
            CODEC = StringIdentifiable.createCodec(State::values);
            INDEX_TO_VALUE = ValueLists.createIdToValueFunction(State::getIndex, State.values(), ValueLists.OutOfBoundsHandling.ZERO);
            PACKET_CODEC = PacketCodecs.indexed(INDEX_TO_VALUE, State::getIndex);
        }
    }
}

