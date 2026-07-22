/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.block;

import com.mojang.serialization.MapCodec;
import java.util.Optional;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.dispenser.ItemDispenserBehavior;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.CrafterBlockEntity;
import net.minecraft.block.entity.HopperBlockEntity;
import net.minecraft.block.enums.Orientation;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.CraftingRecipe;
import net.minecraft.recipe.RecipeCache;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.recipe.input.CraftingRecipeInput;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.minecraft.world.WorldEvents;
import org.jetbrains.annotations.Nullable;

public class CrafterBlock
extends BlockWithEntity {
    public static final MapCodec<CrafterBlock> CODEC = CrafterBlock.createCodec(CrafterBlock::new);
    public static final BooleanProperty CRAFTING = Properties.CRAFTING;
    public static final BooleanProperty TRIGGERED = Properties.TRIGGERED;
    private static final EnumProperty<Orientation> ORIENTATION = Properties.ORIENTATION;
    private static final int field_46802 = 6;
    private static final int TRIGGER_DELAY = 4;
    private static final RecipeCache recipeCache = new RecipeCache(10);
    private static final int field_50015 = 17;

    public CrafterBlock(AbstractBlock.Settings settings) {
        super(settings);
        this.setDefaultState((BlockState)((BlockState)((BlockState)((BlockState)this.stateManager.getDefaultState()).with(ORIENTATION, Orientation.NORTH_UP)).with(TRIGGERED, false)).with(CRAFTING, false));
    }

    protected MapCodec<CrafterBlock> getCodec() {
        return CODEC;
    }

    @Override
    protected boolean hasComparatorOutput(BlockState state) {
        return true;
    }

    @Override
    protected int getComparatorOutput(BlockState state, World world, BlockPos pos) {
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity instanceof CrafterBlockEntity) {
            CrafterBlockEntity crafterBlockEntity = (CrafterBlockEntity)blockEntity;
            return crafterBlockEntity.getComparatorOutput();
        }
        return 0;
    }

    @Override
    protected void neighborUpdate(BlockState state, World world, BlockPos pos, Block sourceBlock, BlockPos sourcePos, boolean notify) {
        boolean bl = world.isReceivingRedstonePower(pos);
        boolean bl2 = state.get(TRIGGERED);
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (bl && !bl2) {
            world.scheduleBlockTick(pos, this, 4);
            world.setBlockState(pos, (BlockState)state.with(TRIGGERED, true), Block.NOTIFY_LISTENERS);
            this.setTriggered(blockEntity, true);
        } else if (!bl && bl2) {
            world.setBlockState(pos, (BlockState)((BlockState)state.with(TRIGGERED, false)).with(CRAFTING, false), Block.NOTIFY_LISTENERS);
            this.setTriggered(blockEntity, false);
        }
    }

    @Override
    protected void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        this.craft(state, world, pos);
    }

    @Override
    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return world.isClient ? null : CrafterBlock.validateTicker(type, BlockEntityType.CRAFTER, CrafterBlockEntity::tickCrafting);
    }

    private void setTriggered(@Nullable BlockEntity blockEntity, boolean triggered) {
        if (blockEntity instanceof CrafterBlockEntity) {
            CrafterBlockEntity crafterBlockEntity = (CrafterBlockEntity)blockEntity;
            crafterBlockEntity.setTriggered(triggered);
        }
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        CrafterBlockEntity crafterBlockEntity = new CrafterBlockEntity(pos, state);
        crafterBlockEntity.setTriggered(state.contains(TRIGGERED) && state.get(TRIGGERED) != false);
        return crafterBlockEntity;
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        Direction direction = ctx.getPlayerLookDirection().getOpposite();
        Direction direction2 = switch (direction) {
            default -> throw new MatchException(null, null);
            case Direction.DOWN -> ctx.getHorizontalPlayerFacing().getOpposite();
            case Direction.UP -> ctx.getHorizontalPlayerFacing();
            case Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST -> Direction.UP;
        };
        return (BlockState)((BlockState)this.getDefaultState().with(ORIENTATION, Orientation.byDirections(direction, direction2))).with(TRIGGERED, ctx.getWorld().isReceivingRedstonePower(ctx.getBlockPos()));
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack itemStack) {
        if (state.get(TRIGGERED).booleanValue()) {
            world.scheduleBlockTick(pos, this, 4);
        }
    }

    @Override
    protected void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        ItemScatterer.onStateReplaced(state, newState, world, pos);
        super.onStateReplaced(state, world, pos, newState, moved);
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (world.isClient) {
            return ActionResult.SUCCESS;
        }
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity instanceof CrafterBlockEntity) {
            player.openHandledScreen((CrafterBlockEntity)blockEntity);
        }
        return ActionResult.CONSUME;
    }

    protected void craft(BlockState state, ServerWorld world, BlockPos pos) {
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (!(blockEntity instanceof CrafterBlockEntity)) {
            return;
        }
        CrafterBlockEntity crafterBlockEntity = (CrafterBlockEntity)blockEntity;
        CraftingRecipeInput craftingRecipeInput = crafterBlockEntity.createRecipeInput();
        Optional<RecipeEntry<CraftingRecipe>> optional = CrafterBlock.getCraftingRecipe(world, craftingRecipeInput);
        if (optional.isEmpty()) {
            world.syncWorldEvent(WorldEvents.CRAFTER_FAILS, pos, 0);
            return;
        }
        RecipeEntry<CraftingRecipe> recipeEntry = optional.get();
        ItemStack itemStack = recipeEntry.value().craft(craftingRecipeInput, world.getRegistryManager());
        if (itemStack.isEmpty()) {
            world.syncWorldEvent(WorldEvents.CRAFTER_FAILS, pos, 0);
            return;
        }
        crafterBlockEntity.setCraftingTicksRemaining(6);
        world.setBlockState(pos, (BlockState)state.with(CRAFTING, true), Block.NOTIFY_LISTENERS);
        itemStack.onCraftByCrafter(world);
        this.transferOrSpawnStack(world, pos, crafterBlockEntity, itemStack, state, recipeEntry);
        for (ItemStack itemStack2 : recipeEntry.value().getRemainder(craftingRecipeInput)) {
            if (itemStack2.isEmpty()) continue;
            this.transferOrSpawnStack(world, pos, crafterBlockEntity, itemStack2, state, recipeEntry);
        }
        crafterBlockEntity.getHeldStacks().forEach(stack -> {
            if (stack.isEmpty()) {
                return;
            }
            stack.decrement(1);
        });
        crafterBlockEntity.markDirty();
    }

    public static Optional<RecipeEntry<CraftingRecipe>> getCraftingRecipe(World world, CraftingRecipeInput input) {
        return recipeCache.getRecipe(world, input);
    }

    private void transferOrSpawnStack(ServerWorld world, BlockPos pos, CrafterBlockEntity blockEntity, ItemStack stack, BlockState state, RecipeEntry<CraftingRecipe> recipe) {
        Direction direction = state.get(ORIENTATION).getFacing();
        Inventory inventory = HopperBlockEntity.getInventoryAt(world, pos.offset(direction));
        ItemStack itemStack = stack.copy();
        if (inventory != null && (inventory instanceof CrafterBlockEntity || stack.getCount() > inventory.getMaxCount(stack))) {
            ItemStack itemStack2;
            ItemStack itemStack3;
            while (!itemStack.isEmpty() && (itemStack3 = HopperBlockEntity.transfer(blockEntity, inventory, itemStack2 = itemStack.copyWithCount(1), direction.getOpposite())).isEmpty()) {
                itemStack.decrement(1);
            }
        } else if (inventory != null) {
            int i;
            while (!itemStack.isEmpty() && (i = itemStack.getCount()) != (itemStack = HopperBlockEntity.transfer(blockEntity, inventory, itemStack, direction.getOpposite())).getCount()) {
            }
        }
        if (!itemStack.isEmpty()) {
            Vec3d vec3d = Vec3d.ofCenter(pos);
            Vec3d vec3d2 = vec3d.offset(direction, 0.7);
            ItemDispenserBehavior.spawnItem(world, itemStack, 6, direction, vec3d2);
            for (ServerPlayerEntity serverPlayerEntity : world.getNonSpectatingEntities(ServerPlayerEntity.class, Box.of(vec3d, 17.0, 17.0, 17.0))) {
                Criteria.CRAFTER_RECIPE_CRAFTED.trigger(serverPlayerEntity, recipe.id(), blockEntity.getHeldStacks());
            }
            world.syncWorldEvent(WorldEvents.CRAFTER_CRAFTS, pos, 0);
            world.syncWorldEvent(WorldEvents.CRAFTER_SHOOTS, pos, direction.getId());
        }
    }

    @Override
    protected BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    protected BlockState rotate(BlockState state, BlockRotation rotation) {
        return (BlockState)state.with(ORIENTATION, rotation.getDirectionTransformation().mapJigsawOrientation(state.get(ORIENTATION)));
    }

    @Override
    protected BlockState mirror(BlockState state, BlockMirror mirror) {
        return (BlockState)state.with(ORIENTATION, mirror.getDirectionTransformation().mapJigsawOrientation(state.get(ORIENTATION)));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(ORIENTATION, TRIGGERED, CRAFTING);
    }
}

