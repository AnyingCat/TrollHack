package github.trollhack.modules.impl.combat;

import github.trollhack.core.Managers;
import github.trollhack.events.impl.PacketEvent;
import github.trollhack.modules.Category;
import github.trollhack.modules.Module;
import github.trollhack.modules.impl.hud.Notification;
import github.trollhack.settings.impl.FloatSetting;
import github.trollhack.settings.impl.IntegerSetting;
import github.trollhack.utils.world.ExplosionUtil;
import github.trollhack.utils.world.InventoryUtil;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.network.packet.s2c.play.ExperienceOrbSpawnS2CPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Box;

public class AutoEXP extends Module {
    public static final AutoEXP INSTANCE = new AutoEXP();

    public final FloatSetting minHealth = floatSetting("Min Health", 8.0f, 0.0f, 20.0f, 0.5f);
    public final IntegerSetting targetDurability = integerSetting("Target Durability", 85, 50, 100, 1);
    public final IntegerSetting delay = integerSetting("Delay", 0, 0, 50, 1);

    private int throwAmount = 0;
    private int xpSlot = -1;
    private long nextThrowTime = 0L;

    public AutoEXP() {
        super("AutoEXP", Category.COMBAT);
    }

    @Override
    public void onEnable() {
        if (nullCheck()) {
            setEnabled(false);
            return;
        }
        boolean hasMending = false;
        for (int i = 0; i < 4; i++) {
            if (isRepairable(mc.player.getInventory().getStack(36 + i))) {
                hasMending = true;
                break;
            }
        }
        if (!hasMending) {
            Notification.sendReplace(AutoEXP.class.hashCode(), "AutoEXP No armor to repair", 2000);
            setEnabled(false);
            return;
        }
        throwAmount = 0;
        xpSlot = -1;
        nextThrowTime = 0L;
    }

    @Override
    public void onDisable() {
        throwAmount = 0;
        xpSlot = -1;
        if (Managers.ROTATION != null) Managers.ROTATION.stopRotating();
    }

    @Override
    public void onUpdate() {
        if (nullCheck()) return;
        if (mc.player.getHealth() + mc.player.getAbsorptionAmount() <= minHealth.getValue()) {
            Notification.sendReplace(AutoEXP.class.hashCode(), "AutoEXP Low health", 2000);
            setEnabled(false);
            return;
        }
        Box box = new Box(
                mc.player.getX() - 0.5, mc.player.getY() - 0.5, mc.player.getZ() - 0.5,
                mc.player.getX() + 0.5, mc.player.getY() + 2.5, mc.player.getZ() + 0.5
        );
        for (PlayerEntity player : mc.world.getPlayers()) {
            if (player == mc.player) continue;
            if (Managers.FRIEND.isFriend(player)) continue;
            if (player.getBoundingBox().intersects(box)) {
                Notification.sendReplace(AutoEXP.class.hashCode(), "AutoEXP Players nearby", 2000);
                setEnabled(false);
                return;
            }
        }
        xpSlot = InventoryUtil.findItem(Items.EXPERIENCE_BOTTLE);
        if (xpSlot == -1) {
            Notification.sendReplace(AutoEXP.class.hashCode(), "AutoEXP No xp bottle in hotbar", 2000);
            setEnabled(false);
            return;
        }
        boolean finished = true;
        for (int i = 0; i < 4; i++) {
            ItemStack stack = mc.player.getInventory().getStack(36 + i);
            if (!isRepairable(stack)) continue;
            float duraPct;
            if (stack.isEmpty() || !stack.isDamageable()) {
                duraPct = 100.0f;
            } else {
                int max = stack.getMaxDamage();
                duraPct = max <= 0 ? 100.0f : 100.0f * (1.0f - (float) stack.getDamage() / max);
            }
            if (duraPct < targetDurability.getValue()) {
                finished = false;
                break;
            }
        }
        if (finished) {
            Notification.sendReplace(AutoEXP.class.hashCode(), "AutoEXP Finished", 2000);
            setEnabled(false);
            return;
        }
        Managers.ROTATION.setRotation(mc.player.getYaw(), 90.0f);
        if (throwAmount <= 0) {
            ItemStack mostDamaged = null;
            int maxDamage = 0;
            for (int i = 0; i < 4; i++) {
                ItemStack stack = mc.player.getInventory().getStack(36 + i);
                if (!isRepairable(stack)) continue;
                int dmg = stack.getDamage();
                if (dmg > maxDamage) {
                    mostDamaged = stack;
                    maxDamage = dmg;
                }
            }
            if (mostDamaged != null) {
                int targetMax = (int) (mostDamaged.getMaxDamage() * (1.0f - targetDurability.getValue() / 100.0f));
                int needed = mostDamaged.getDamage() - targetMax;
                if (needed > 0) {
                    throwAmount = (int) Math.ceil(needed / 22.0f);
                }
            }
        }
        if (throwAmount > 0 && mc.getNetworkHandler() != null
                && Managers.ROTATION.getPitch() > 85.0f
                && System.currentTimeMillis() >= nextThrowTime) {
            int original = mc.player.getInventory().selectedSlot;
            float yaw = Managers.ROTATION.getYaw();
            float pitch = Managers.ROTATION.getPitch();
            if (original != xpSlot) {
                mc.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(xpSlot));
            }
            mc.getNetworkHandler().sendPacket(new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, 0, yaw, pitch));
            throwAmount--;
            if (original != xpSlot) {
                mc.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(original));
            }
            nextThrowTime = System.currentTimeMillis() + delay.getValue() * 50L;
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    private void onPacketReceive(PacketEvent.Receive event) {
        if (nullCheck()) return;
        if (event.getPacket() instanceof ExperienceOrbSpawnS2CPacket packet) {
            double dx = packet.getX() - mc.player.getX();
            double dy = packet.getY() - mc.player.getY();
            double dz = packet.getZ() - mc.player.getZ();
            if (dx * dx + dy * dy + dz * dz < 5.0 && throwAmount > 0) {
                throwAmount--;
            }
        }
    }

    private boolean isRepairable(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        if (!stack.isDamageable()) return false;
        return ExplosionUtil.getEnchantmentLevel(stack, Enchantments.MENDING) > 0;
    }

    @Override
    public String getHudInfo() {
        return String.valueOf(throwAmount);
    }
}
