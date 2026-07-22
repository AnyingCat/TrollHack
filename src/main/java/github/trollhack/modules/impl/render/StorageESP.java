package github.trollhack.modules.impl.render;

import github.trollhack.modules.Category;
import github.trollhack.modules.Module;
import github.trollhack.settings.impl.BooleanSetting;
import github.trollhack.settings.impl.ColorSetting;
import github.trollhack.settings.impl.EnumSetting;
import github.trollhack.settings.impl.FloatSetting;
import github.trollhack.settings.impl.IntegerSetting;
import github.trollhack.utils.render.Render3DUtil;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.block.entity.DispenserBlockEntity;
import net.minecraft.block.entity.EnderChestBlockEntity;
import net.minecraft.block.entity.FurnaceBlockEntity;
import net.minecraft.block.entity.HopperBlockEntity;
import net.minecraft.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.entity.vehicle.ChestMinecartEntity;
import net.minecraft.entity.vehicle.FurnaceMinecartEntity;
import net.minecraft.entity.vehicle.HopperMinecartEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.WorldChunk;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

public class StorageESP extends Module {
    public static final StorageESP INSTANCE = new StorageESP();

    public enum Page {
        TYPE, COLOR, RENDER
    }

    private final EnumSetting<Page> page = enumSetting("Page", Page.TYPE);

    private final BooleanSetting chest = booleanSetting("Chest", true, () -> page.getValue() == Page.TYPE);
    private final BooleanSetting shulker = booleanSetting("Shulker", true, () -> page.getValue() == Page.TYPE);
    private final BooleanSetting enderChest = booleanSetting("Ender Chest", true, () -> page.getValue() == Page.TYPE);
    private final BooleanSetting frame = booleanSetting("Item Frame", true, () -> page.getValue() == Page.TYPE);
    private final BooleanSetting withShulkerOnly = booleanSetting("With Shulker Only", true, () -> page.getValue() == Page.TYPE && frame.getValue());
    private final BooleanSetting furnace = booleanSetting("Furnace", false, () -> page.getValue() == Page.TYPE);
    private final BooleanSetting dispenser = booleanSetting("Dispenser", false, () -> page.getValue() == Page.TYPE);
    private final BooleanSetting hopper = booleanSetting("Hopper", false, () -> page.getValue() == Page.TYPE);
    private final BooleanSetting cart = booleanSetting("Minecart", false, () -> page.getValue() == Page.TYPE);
    private final FloatSetting range = floatSetting("Range", 64.0f, 8.0f, 128.0f, 4.0f, () -> page.getValue() == Page.TYPE);

    private final ColorSetting colorChest = colorSetting("Chest Color", new Color(255, 132, 32), () -> page.getValue() == Page.COLOR);
    private final ColorSetting colorDispenser = colorSetting("Dispenser Color", new Color(160, 160, 160), () -> page.getValue() == Page.COLOR);
    private final ColorSetting colorShulker = colorSetting("Shulker Color", new Color(220, 64, 220), () -> page.getValue() == Page.COLOR);
    private final ColorSetting colorEnderChest = colorSetting("Ender Chest Color", new Color(137, 50, 184), () -> page.getValue() == Page.COLOR);
    private final ColorSetting colorFurnace = colorSetting("Furnace Color", new Color(160, 160, 160), () -> page.getValue() == Page.COLOR);
    private final ColorSetting colorHopper = colorSetting("Hopper Color", new Color(80, 80, 80), () -> page.getValue() == Page.COLOR);
    private final ColorSetting colorCart = colorSetting("Cart Color", new Color(32, 250, 32), () -> page.getValue() == Page.COLOR);
    private final ColorSetting colorFrame = colorSetting("Frame Color", new Color(255, 132, 32), () -> page.getValue() == Page.COLOR);

    private final BooleanSetting filled = booleanSetting("Filled", true, () -> page.getValue() == Page.RENDER);
    private final BooleanSetting outline = booleanSetting("Outline", true, () -> page.getValue() == Page.RENDER);
    private final IntegerSetting filledAlpha = integerSetting("Filled Alpha", 63, 0, 255, 1, () -> page.getValue() == Page.RENDER && filled.getValue());
    private final IntegerSetting outlineAlpha = integerSetting("Outline Alpha", 200, 0, 255, 1, () -> page.getValue() == Page.RENDER && outline.getValue());
    private final FloatSetting lineWidth = floatSetting("Line Width", 2.0f, 0.25f, 5.0f, 0.25f, () -> page.getValue() == Page.RENDER && outline.getValue());

    private final List<TileEntry> tileEntries = new ArrayList<>();
    private final List<EntityEntry> entityEntries = new ArrayList<>();

    private record TileEntry(Box box, Color color) {}
    private record EntityEntry(Entity entity, Color color) {}

    public StorageESP() {
        super("Storage ESP", Category.RENDER);
    }

    @Override
    public String getHudInfo() {
        return String.valueOf(tileEntries.size() + entityEntries.size());
    }

    @Override
    public void onUpdate() {
        if (nullCheck()) return;

        tileEntries.clear();
        entityEntries.clear();

        Vec3d eyePos = mc.player.getEyePos();
        double rangeSq = (double) range.getValue() * (double) range.getValue();

        int renderDistance = mc.options.getViewDistance().getValue();
        ChunkPos centerChunk = mc.player.getChunkPos();

        for (int cx = centerChunk.x - renderDistance; cx <= centerChunk.x + renderDistance; cx++) {
            for (int cz = centerChunk.z - renderDistance; cz <= centerChunk.z + renderDistance; cz++) {
                WorldChunk chunk = mc.world.getChunkManager().getChunk(cx, cz, ChunkStatus.FULL, false);
                if (chunk == null) continue;

                for (BlockEntity be : chunk.getBlockEntities().values()) {
                    BlockPos pos = be.getPos();
                    Vec3d centerPos = Vec3d.ofCenter(pos);
                    if (eyePos.squaredDistanceTo(centerPos) > rangeSq) continue;

                    Color color = getTileEntityColor(be);
                    if (color == null) continue;

                    BlockState state = mc.world.getBlockState(pos);
                    VoxelShape shape = state.getOutlineShape(mc.world, pos);
                    if (shape.isEmpty()) continue;

                    Box box = shape.getBoundingBox().offset(pos);
                    tileEntries.add(new TileEntry(box, color));
                }
            }
        }

        for (Entity entity : mc.world.getEntities()) {
            if (eyePos.squaredDistanceTo(entity.getPos()) > rangeSq) continue;

            Color color = getEntityColor(entity);
            if (color == null) continue;

            entityEntries.add(new EntityEntry(entity, color));
        }
    }

    private Color getTileEntityColor(BlockEntity be) {
        if (chest.getValue() && be instanceof ChestBlockEntity) return colorChest.getValue();
        if (dispenser.getValue() && be instanceof DispenserBlockEntity) return colorDispenser.getValue();
        if (shulker.getValue() && be instanceof ShulkerBoxBlockEntity) return colorShulker.getValue();
        if (enderChest.getValue() && be instanceof EnderChestBlockEntity) return colorEnderChest.getValue();
        if (furnace.getValue() && be instanceof FurnaceBlockEntity) return colorFurnace.getValue();
        if (hopper.getValue() && be instanceof HopperBlockEntity) return colorHopper.getValue();
        return null;
    }

    private Color getEntityColor(Entity entity) {
        if (cart.getValue() && (entity instanceof ChestMinecartEntity
                || entity instanceof HopperMinecartEntity
                || entity instanceof FurnaceMinecartEntity)) {
            return colorCart.getValue();
        }
        if (frame.getValue() && entity instanceof ItemFrameEntity itemFrame) {
            ItemStack stack = itemFrame.getHeldItemStack();
            if (stack.isEmpty()) return null;
            if (withShulkerOnly.getValue()
                    && !(stack.getItem() instanceof BlockItem blockItem && blockItem.getBlock() instanceof ShulkerBoxBlock)) {
                return null;
            }
            return colorFrame.getValue();
        }
        return null;
    }

    @Override
    public void onRender3D(MatrixStack matrices) {
        if (nullCheck()) return;

        Vec3d cameraPos = mc.gameRenderer.getCamera().getPos();
        float tickDelta = mc.getRenderTickCounter().getTickDelta(false);
        float lw = lineWidth.getValue();

        float filledA = filled.getValue() ? filledAlpha.getValue() / 255f : 0f;
        float outlineA = outline.getValue() ? outlineAlpha.getValue() / 255f : 0f;

        for (TileEntry entry : tileEntries) {
            Box relativeBox = entry.box.offset(-cameraPos.x, -cameraPos.y, -cameraPos.z);
            if (filledA > 0f) Render3DUtil.drawFilledBox(matrices, relativeBox, entry.color, filledA);
            if (outlineA > 0f) Render3DUtil.drawBoxOutline(matrices, relativeBox, entry.color, outlineA, lw);
        }

        for (EntityEntry entry : entityEntries) {
            Entity entity = entry.entity;
            if (entity.isRemoved()) continue;

            Vec3d lerpPos = entity.getLerpedPos(tickDelta);
            Vec3d currentPos = entity.getPos();
            Vec3d offset = lerpPos.subtract(currentPos);
            Box interpolatedBox = entity.getBoundingBox().offset(offset);
            Box relativeBox = interpolatedBox.offset(-cameraPos.x, -cameraPos.y, -cameraPos.z);

            if (filledA > 0f) Render3DUtil.drawFilledBox(matrices, relativeBox, entry.color, filledA);
            if (outlineA > 0f) Render3DUtil.drawBoxOutline(matrices, relativeBox, entry.color, outlineA, lw);
        }
    }
}
