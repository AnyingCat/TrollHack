/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.item;

import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BrushableBlock;
import net.minecraft.block.entity.BrushableBlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;
import net.minecraft.util.UseAction;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class BrushItem
extends Item {
    public static final int ANIMATION_DURATION = 10;
    private static final int MAX_BRUSH_TIME = 200;

    public BrushItem(Item.Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        PlayerEntity playerEntity = context.getPlayer();
        if (playerEntity != null && this.getHitResult(playerEntity).getType() == HitResult.Type.BLOCK) {
            playerEntity.setCurrentHand(context.getHand());
        }
        return ActionResult.CONSUME;
    }

    @Override
    public UseAction getUseAction(ItemStack stack) {
        return UseAction.BRUSH;
    }

    @Override
    public int getMaxUseTime(ItemStack stack, LivingEntity user) {
        return 200;
    }

    @Override
    public void usageTick(World world, LivingEntity user, ItemStack stack, int remainingUseTicks) {
        boolean bl;
        BlockHitResult blockHitResult;
        PlayerEntity playerEntity;
        block10: {
            block9: {
                if (remainingUseTicks < 0 || !(user instanceof PlayerEntity)) {
                    user.stopUsingItem();
                    return;
                }
                playerEntity = (PlayerEntity)user;
                HitResult hitResult = this.getHitResult(playerEntity);
                if (!(hitResult instanceof BlockHitResult)) break block9;
                blockHitResult = (BlockHitResult)hitResult;
                if (hitResult.getType() == HitResult.Type.BLOCK) break block10;
            }
            user.stopUsingItem();
            return;
        }
        int i = this.getMaxUseTime(stack, user) - remainingUseTicks + 1;
        boolean bl2 = bl = i % 10 == 5;
        if (bl) {
            BrushableBlockEntity brushableBlockEntity;
            boolean bl22;
            SoundEvent soundEvent;
            Object object;
            Arm arm;
            BlockPos blockPos = blockHitResult.getBlockPos();
            BlockState blockState = world.getBlockState(blockPos);
            Arm arm2 = arm = user.getActiveHand() == Hand.MAIN_HAND ? playerEntity.getMainArm() : playerEntity.getMainArm().getOpposite();
            if (blockState.hasBlockBreakParticles() && blockState.getRenderType() != BlockRenderType.INVISIBLE) {
                this.addDustParticles(world, blockHitResult, blockState, user.getRotationVec(0.0f), arm);
            }
            if ((object = blockState.getBlock()) instanceof BrushableBlock) {
                BrushableBlock brushableBlock = (BrushableBlock)object;
                soundEvent = brushableBlock.getBrushingSound();
            } else {
                soundEvent = SoundEvents.ITEM_BRUSH_BRUSHING_GENERIC;
            }
            world.playSound(playerEntity, blockPos, soundEvent, SoundCategory.BLOCKS);
            if (!world.isClient() && (object = world.getBlockEntity(blockPos)) instanceof BrushableBlockEntity && (bl22 = (brushableBlockEntity = (BrushableBlockEntity)object).brush(world.getTime(), playerEntity, blockHitResult.getSide()))) {
                EquipmentSlot equipmentSlot = stack.equals(playerEntity.getEquippedStack(EquipmentSlot.OFFHAND)) ? EquipmentSlot.OFFHAND : EquipmentSlot.MAINHAND;
                stack.damage(1, user, equipmentSlot);
            }
        }
    }

    private HitResult getHitResult(PlayerEntity user) {
        return ProjectileUtil.getCollision((Entity)user, entity -> !entity.isSpectator() && entity.canHit(), user.getBlockInteractionRange());
    }

    private void addDustParticles(World world, BlockHitResult hitResult, BlockState state, Vec3d userRotation, Arm arm) {
        double d = 3.0;
        int i = arm == Arm.RIGHT ? 1 : -1;
        int j = world.getRandom().nextBetweenExclusive(7, 12);
        BlockStateParticleEffect blockStateParticleEffect = new BlockStateParticleEffect(ParticleTypes.BLOCK, state);
        Direction direction = hitResult.getSide();
        DustParticlesOffset dustParticlesOffset = DustParticlesOffset.fromSide(userRotation, direction);
        Vec3d vec3d = hitResult.getPos();
        for (int k = 0; k < j; ++k) {
            world.addParticle(blockStateParticleEffect, vec3d.x - (double)(direction == Direction.WEST ? 1.0E-6f : 0.0f), vec3d.y, vec3d.z - (double)(direction == Direction.NORTH ? 1.0E-6f : 0.0f), dustParticlesOffset.xd() * (double)i * 3.0 * world.getRandom().nextDouble(), 0.0, dustParticlesOffset.zd() * (double)i * 3.0 * world.getRandom().nextDouble());
        }
    }

    record DustParticlesOffset(double xd, double yd, double zd) {
        private static final double field_42685 = 1.0;
        private static final double field_42686 = 0.1;

        public static DustParticlesOffset fromSide(Vec3d userRotation, Direction side) {
            double d = 0.0;
            return switch (side) {
                default -> throw new MatchException(null, null);
                case Direction.DOWN, Direction.UP -> new DustParticlesOffset(userRotation.getZ(), 0.0, -userRotation.getX());
                case Direction.NORTH -> new DustParticlesOffset(1.0, 0.0, -0.1);
                case Direction.SOUTH -> new DustParticlesOffset(-1.0, 0.0, 0.1);
                case Direction.WEST -> new DustParticlesOffset(-0.1, 0.0, -1.0);
                case Direction.EAST -> new DustParticlesOffset(0.1, 0.0, 1.0);
            };
        }
    }
}

