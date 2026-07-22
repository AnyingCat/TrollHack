package github.trollhack.modules.impl.render;

import github.trollhack.modules.Category;
import github.trollhack.modules.Module;
import github.trollhack.settings.impl.BooleanSetting;
import github.trollhack.settings.impl.EnumSetting;
import github.trollhack.settings.impl.FloatSetting;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;
import net.minecraft.util.math.RotationAxis;

public class ViewModel extends Module {
    public static final ViewModel INSTANCE = new ViewModel();

    public final BooleanSetting separatedHand0 = booleanSetting("Separated Hand", false);
    public final BooleanSetting totem0 = booleanSetting("Totem", false);
    public final EnumSetting<Page> page = enumSetting("Page", Page.POSITION);

    public final FloatSetting posX = floatSetting("Pos X", 0.0f, -5.0f, 5.0f, 0.025f, () -> page.getValue() == Page.POSITION);
    public final FloatSetting posY = floatSetting("Pos Y", 0.0f, -5.0f, 5.0f, 0.025f, () -> page.getValue() == Page.POSITION);
    public final FloatSetting posZ = floatSetting("Pos Z", 0.0f, -5.0f, 5.0f, 0.025f, () -> page.getValue() == Page.POSITION);
    public final FloatSetting posXR = floatSetting("Pos X Right", 0.0f, -5.0f, 5.0f, 0.025f, () -> page.getValue() == Page.POSITION && separatedHand0.getValue());
    public final FloatSetting posYR = floatSetting("Pos Y Right", 0.0f, -5.0f, 5.0f, 0.025f, () -> page.getValue() == Page.POSITION && separatedHand0.getValue());
    public final FloatSetting posZR = floatSetting("Pos Z Right", 0.0f, -5.0f, 5.0f, 0.025f, () -> page.getValue() == Page.POSITION && separatedHand0.getValue());
    public final FloatSetting posXT = floatSetting("Pos X Totem", 0.0f, -5.0f, 5.0f, 0.025f, () -> page.getValue() == Page.POSITION && totem0.getValue());
    public final FloatSetting posYT = floatSetting("Pos Y Totem", 0.0f, -5.0f, 5.0f, 0.025f, () -> page.getValue() == Page.POSITION && totem0.getValue());
    public final FloatSetting posZT = floatSetting("Pos Z Totem", 0.0f, -5.0f, 5.0f, 0.025f, () -> page.getValue() == Page.POSITION && totem0.getValue());

    public final FloatSetting rotateX = floatSetting("Rotate X", 0.0f, -180.0f, 180.0f, 1.0f, () -> page.getValue() == Page.ROTATION);
    public final FloatSetting rotateY = floatSetting("Rotate Y", 0.0f, -180.0f, 180.0f, 1.0f, () -> page.getValue() == Page.ROTATION);
    public final FloatSetting rotateZ = floatSetting("Rotate Z", 0.0f, -180.0f, 180.0f, 1.0f, () -> page.getValue() == Page.ROTATION);
    public final FloatSetting rotateXR = floatSetting("Rotate X Right", 0.0f, -180.0f, 180.0f, 1.0f, () -> page.getValue() == Page.ROTATION && separatedHand0.getValue());
    public final FloatSetting rotateYR = floatSetting("Rotate Y Right", 0.0f, -180.0f, 180.0f, 1.0f, () -> page.getValue() == Page.ROTATION && separatedHand0.getValue());
    public final FloatSetting rotateZR = floatSetting("Rotate Z Right", 0.0f, -180.0f, 180.0f, 1.0f, () -> page.getValue() == Page.ROTATION && separatedHand0.getValue());
    public final FloatSetting rotateXT = floatSetting("Rotate X Totem", 0.0f, -180.0f, 180.0f, 1.0f, () -> page.getValue() == Page.ROTATION && totem0.getValue());
    public final FloatSetting rotateYT = floatSetting("Rotate Y Totem", 0.0f, -180.0f, 180.0f, 1.0f, () -> page.getValue() == Page.ROTATION && totem0.getValue());
    public final FloatSetting rotateZT = floatSetting("Rotate Z Totem", 0.0f, -180.0f, 180.0f, 1.0f, () -> page.getValue() == Page.ROTATION && totem0.getValue());

    public final FloatSetting scale = floatSetting("Scale", 1.0f, 0.1f, 3.0f, 0.025f, () -> page.getValue() == Page.SCALE);
    public final FloatSetting scaleR = floatSetting("Scale Right", 1.0f, 0.1f, 3.0f, 0.025f, () -> page.getValue() == Page.SCALE && separatedHand0.getValue());
    public final FloatSetting scaleT = floatSetting("Scale Totem", 1.0f, 0.1f, 3.0f, 0.025f, () -> page.getValue() == Page.SCALE && totem0.getValue());

    public final BooleanSetting modifyHand = booleanSetting("Modify Hand", false);

    public enum Page {
        POSITION, ROTATION, SCALE
    }

    public ViewModel() {
        super("View Model", Category.RENDER);
    }

    public static void translate(MatrixStack matrices, ItemStack stack, Hand hand, AbstractClientPlayerEntity player) {
        if (!INSTANCE.isEnabled() || (!INSTANCE.modifyHand.getValue() && stack.isEmpty())) return;

        Arm arm = getArm(player, hand);

        if (INSTANCE.totem0.getValue() && player.getStackInHand(hand).isOf(Items.TOTEM_OF_UNDYING)) {
            translate(matrices, INSTANCE.posXT.getValue(), INSTANCE.posYT.getValue(), INSTANCE.posZT.getValue(), getSideMultiplier(arm));
        } else if (INSTANCE.separatedHand0.getValue()) {
            if (arm == Arm.LEFT) {
                translate(matrices, INSTANCE.posX.getValue(), INSTANCE.posY.getValue(), INSTANCE.posZ.getValue(), -1.0f);
            } else {
                translate(matrices, INSTANCE.posXR.getValue(), INSTANCE.posYR.getValue(), INSTANCE.posZR.getValue(), 1.0f);
            }
        } else {
            translate(matrices, INSTANCE.posX.getValue(), INSTANCE.posY.getValue(), INSTANCE.posZ.getValue(), getSideMultiplier(arm));
        }
    }

    private static void translate(MatrixStack matrices, float x, float y, float z, float sideMultiplier) {
        matrices.translate(x * sideMultiplier, y, -z);
    }

    public static void rotateAndScale(MatrixStack matrices, ItemStack stack, Hand hand, AbstractClientPlayerEntity player) {
        if (!INSTANCE.isEnabled() || (!INSTANCE.modifyHand.getValue() && stack.isEmpty())) return;

        Arm arm = getArm(player, hand);

        if (INSTANCE.totem0.getValue() && player.getStackInHand(hand).isOf(Items.TOTEM_OF_UNDYING)) {
            rotate(matrices, INSTANCE.rotateXT.getValue(), INSTANCE.rotateYT.getValue(), INSTANCE.rotateZT.getValue(), getSideMultiplier(arm));
            matrices.scale(INSTANCE.scaleT.getValue(), INSTANCE.scaleT.getValue(), INSTANCE.scaleT.getValue());
        } else if (INSTANCE.separatedHand0.getValue()) {
            if (arm == Arm.LEFT) {
                rotate(matrices, INSTANCE.rotateX.getValue(), INSTANCE.rotateY.getValue(), INSTANCE.rotateZ.getValue(), -1.0f);
                matrices.scale(INSTANCE.scale.getValue(), INSTANCE.scale.getValue(), INSTANCE.scale.getValue());
            } else {
                rotate(matrices, INSTANCE.rotateXR.getValue(), INSTANCE.rotateYR.getValue(), INSTANCE.rotateZR.getValue(), 1.0f);
                matrices.scale(INSTANCE.scaleR.getValue(), INSTANCE.scaleR.getValue(), INSTANCE.scaleR.getValue());
            }
        } else {
            rotate(matrices, INSTANCE.rotateX.getValue(), INSTANCE.rotateY.getValue(), INSTANCE.rotateZ.getValue(), getSideMultiplier(arm));
            matrices.scale(INSTANCE.scale.getValue(), INSTANCE.scale.getValue(), INSTANCE.scale.getValue());
        }
    }

    private static void rotate(MatrixStack matrices, float x, float y, float z, float sideMultiplier) {
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(x));
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(y * sideMultiplier));
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(z * sideMultiplier));
    }

    private static Arm getArm(AbstractClientPlayerEntity player, Hand hand) {
        return hand == Hand.MAIN_HAND ? player.getMainArm() : player.getMainArm().getOpposite();
    }

    private static float getSideMultiplier(Arm arm) {
        return arm == Arm.LEFT ? -1.0f : 1.0f;
    }
}
