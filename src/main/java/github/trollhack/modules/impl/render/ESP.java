package github.trollhack.modules.impl.render;

import github.trollhack.core.Managers;
import github.trollhack.core.impl.ShaderManager;
import github.trollhack.mixins.accessors.IGameRenderer;
import github.trollhack.modules.Category;
import github.trollhack.modules.Module;
import github.trollhack.settings.impl.BooleanSetting;
import github.trollhack.settings.impl.ColorSetting;
import github.trollhack.settings.impl.EnumSetting;
import github.trollhack.settings.impl.FloatSetting;
import github.trollhack.settings.impl.IntegerSetting;
import github.trollhack.utils.render.Render3DUtil;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.awt.Color;

public class ESP extends Module {
    public static final ESP INSTANCE = new ESP();

    public final EnumSetting<Page> page = enumSetting("Page", Page.ENTITY_TYPE);
    public final EnumSetting<Mode> mode = enumSetting("Mode", Mode.SHADER);

    private final BooleanSetting all = booleanSetting("All", false, () -> page.getValue() == Page.ENTITY_TYPE);
    private final BooleanSetting item = booleanSetting("Item", true, () -> page.getValue() == Page.ENTITY_TYPE && !all.getValue());
    private final BooleanSetting player = booleanSetting("Player", true, () -> page.getValue() == Page.ENTITY_TYPE && !all.getValue());
    private final BooleanSetting friend = booleanSetting("Friend", true, () -> page.getValue() == Page.ENTITY_TYPE && !all.getValue() && player.getValue());
    private final BooleanSetting mob = booleanSetting("Mob", true, () -> page.getValue() == Page.ENTITY_TYPE && !all.getValue());
    private final BooleanSetting passive = booleanSetting("Passive", false, () -> page.getValue() == Page.ENTITY_TYPE && !all.getValue() && mob.getValue());
    private final BooleanSetting neutral = booleanSetting("Neutral", true, () -> page.getValue() == Page.ENTITY_TYPE && !all.getValue() && mob.getValue());
    private final BooleanSetting hostile = booleanSetting("Hostile", true, () -> page.getValue() == Page.ENTITY_TYPE && !all.getValue() && mob.getValue());
    private final BooleanSetting crystals = booleanSetting("Crystals", true, () -> page.getValue() == Page.ENTITY_TYPE && !all.getValue());
    private final FloatSetting range = floatSetting("Range", 32.0f, 8.0f, 64.0f, 0.5f, () -> page.getValue() == Page.ENTITY_TYPE);

    private final ColorSetting playerColor = colorSetting("Player Color", new Color(150, 180, 255), () -> page.getValue() == Page.COLOR && player.getValue());
    private final ColorSetting friendColor = colorSetting("Friend Color", new Color(150, 255, 180), () -> page.getValue() == Page.COLOR && player.getValue() && friend.getValue());
    private final ColorSetting passiveColor = colorSetting("Passive Color", new Color(32, 255, 32), () -> page.getValue() == Page.COLOR && mob.getValue() && passive.getValue());
    private final ColorSetting neutralColor = colorSetting("Neutral Color", new Color(255, 255, 32), () -> page.getValue() == Page.COLOR && mob.getValue() && neutral.getValue());
    private final ColorSetting hostileColor = colorSetting("Hostile Color", new Color(255, 32, 32), () -> page.getValue() == Page.COLOR && mob.getValue() && hostile.getValue());
    private final ColorSetting itemColor = colorSetting("Item Color", new Color(255, 160, 32), () -> page.getValue() == Page.COLOR && item.getValue());
    private final ColorSetting crystalColor = colorSetting("Crystal Color", new Color(255, 32, 255), () -> page.getValue() == Page.COLOR && crystals.getValue());
    private final ColorSetting otherColor = colorSetting("Other Color", new Color(255, 255, 255), () -> page.getValue() == Page.COLOR);

    private final BooleanSetting hands = booleanSetting("Hands", true, () -> page.getValue() == Page.RENDERING && mode.getValue() == Mode.SHADER);
    private final BooleanSetting filled = booleanSetting("Filled", false, () -> page.getValue() == Page.RENDERING && mode.getValue() == Mode.BOX);
    private final BooleanSetting outline = booleanSetting("Outline", true, () -> page.getValue() == Page.RENDERING && mode.getValue() == Mode.BOX);
    private final IntegerSetting aFilled = integerSetting("Filled Alpha", 63, 0, 255, 1, () -> page.getValue() == Page.RENDERING && mode.getValue() == Mode.BOX);
    private final IntegerSetting aOutline = integerSetting("Outline Alpha", 255, 0, 255, 1, () -> page.getValue() == Page.RENDERING && mode.getValue() == Mode.BOX);
    private final FloatSetting width = floatSetting("Width", 2.0f, 1.0f, 8.0f, 0.25f, () -> page.getValue() == Page.RENDERING && mode.getValue() == Mode.BOX);

    public final EnumSetting<RenderMode> renderMode = enumSetting("RenderMode", RenderMode.Both, () -> page.getValue() == Page.SHADER && mode.getValue() == Mode.SHADER);
    public final FloatSetting blurRadius = floatSetting("BlurRadius", 10.0f, 1.0f, 30.0f, 0.5f, () -> page.getValue() == Page.SHADER && mode.getValue() == Mode.SHADER);
    public final FloatSetting thickness = floatSetting("Thickness", 1.5f, 0.5f, 5.0f, 0.1f, () -> page.getValue() == Page.SHADER && mode.getValue() == Mode.SHADER);
    public final ColorSetting outlineColor = colorSetting("OutlineColor", new Color(0x751CFD), () -> page.getValue() == Page.SHADER && mode.getValue() == Mode.SHADER);
    public final IntegerSetting fillAlpha = integerSetting("FillAlpha", 45, 0, 255, 1, () -> page.getValue() == Page.SHADER && mode.getValue() == Mode.SHADER);

    public enum Page {
        ENTITY_TYPE, COLOR, RENDERING, SHADER
    }

    public enum Mode {
        BOX, GLOW, SHADER
    }

    public enum RenderMode {
        Fill(0),
        Outline(1),
        Both(2);

        private final int value;

        RenderMode(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    public ESP() {
        super("ESP", Category.RENDER);
    }

    @Override
    public String getHudInfo() {
        return mode.getValue().name();
    }

    @Override
    public void onDisable() {
        Managers.SHADER.reloadShaders();
    }

    public boolean isGlowing(Entity entity) {
        if (!isEnabled() || nullCheck()) return false;
        Mode m = mode.getValue();
        if (m == Mode.BOX) return false;
        if (entity == mc.player) return false;
        float rangeSq = range.getValue() * range.getValue();
        return mc.player.squaredDistanceTo(entity.getPos()) <= rangeSq && checkEntityType(entity);
    }

    @Override
    public void onRender3D(MatrixStack matrices) {
        if (nullCheck()) return;

        Mode m = mode.getValue();
        if (m == Mode.BOX) {
            float rangeSq = range.getValue() * range.getValue();
            Vec3d cameraPos = mc.gameRenderer.getCamera().getPos();
            float tickDelta = Render3DUtil.getTickDelta();

            for (Entity entity : mc.world.getEntities()) {
                if (mc.player.squaredDistanceTo(entity.getPos()) > rangeSq) continue;
                if (!checkEntityType(entity)) continue;

                Color color = getEntityColor(entity);
                Box box = entity.getBoundingBox();
                double x = entity.getLerpedPos(tickDelta).x - cameraPos.x;
                double y = entity.getLerpedPos(tickDelta).y - cameraPos.y;
                double z = entity.getLerpedPos(tickDelta).z - cameraPos.z;

                Box offsetBox = new Box(
                    box.minX - entity.getX() + x,
                    box.minY - entity.getY() + y,
                    box.minZ - entity.getZ() + z,
                    box.maxX - entity.getX() + x,
                    box.maxY - entity.getY() + y,
                    box.maxZ - entity.getZ() + z
                );

                if (filled.getValue()) {
                    float fillAlpha = aFilled.getValue() / 255f;
                    Render3DUtil.drawFilledBox(matrices, offsetBox, color, fillAlpha);
                }
                if (outline.getValue()) {
                    float outlineAlpha = aOutline.getValue() / 255f;
                    Render3DUtil.drawBoxOutline(matrices, offsetBox, color, outlineAlpha, width.getValue());
                }
            }
        } else if (m == Mode.SHADER) {
            if (hands.getValue()) {
                Managers.SHADER.renderShader(() -> ((IGameRenderer) mc.gameRenderer).irenderHand(mc.gameRenderer.getCamera(), Render3DUtil.getTickDelta(), matrices.peek().getPositionMatrix()), ShaderManager.Shader.Default);
            }
        }
    }

    public boolean checkEntityType(Entity entity) {
        if (entity == mc.player) return false;
        if (all.getValue()) return true;

        if (item.getValue() && entity instanceof ItemEntity) return true;

        if (crystals.getValue() && entity instanceof EndCrystalEntity) return true;

        if (player.getValue() && entity instanceof PlayerEntity) return true;

        if (mob.getValue()) {
            SpawnGroup group = entity.getType().getSpawnGroup();
            if (passive.getValue() && (group == SpawnGroup.CREATURE || group == SpawnGroup.WATER_CREATURE)) return true;
            if (neutral.getValue() && (group == SpawnGroup.AMBIENT || group == SpawnGroup.WATER_AMBIENT)) return true;
            if (hostile.getValue() && group == SpawnGroup.MONSTER) return true;
        }

        return false;
    }

    public Color getEntityColor(Entity entity) {
        if (entity instanceof ItemEntity) {
            return itemColor.getValue();
        }
        if (entity instanceof EndCrystalEntity) {
            return crystalColor.getValue();
        }
        if (entity instanceof PlayerEntity) {
            if (friend.getValue() && Managers.FRIEND.isFriend((PlayerEntity) entity)) {
                return friendColor.getValue();
            }
            return playerColor.getValue();
        }

        SpawnGroup group = entity.getType().getSpawnGroup();
        if (group == SpawnGroup.CREATURE || group == SpawnGroup.WATER_CREATURE) {
            return passiveColor.getValue();
        }
        if (group == SpawnGroup.AMBIENT || group == SpawnGroup.WATER_AMBIENT) {
            return neutralColor.getValue();
        }
        if (group == SpawnGroup.MONSTER) {
            return hostileColor.getValue();
        }
        return otherColor.getValue();
    }

    public Integer getEspColor(Entity entity) {
        if (!isEnabled() || mode.getValue() != Mode.GLOW || !isGlowing(entity)) return null;
        Color color = getEntityColor(entity);
        return (color.getRed() << 16) | (color.getGreen() << 8) | color.getBlue();
    }
}
