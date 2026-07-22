package github.trollhack.modules.impl.combat;

import github.trollhack.core.Managers;
import github.trollhack.modules.Category;
import github.trollhack.modules.Module;
import github.trollhack.modules.impl.hud.Notification;
import github.trollhack.settings.impl.BooleanSetting;
import github.trollhack.settings.impl.EnumSetting;
import github.trollhack.settings.impl.FloatSetting;
import github.trollhack.settings.impl.IntegerSetting;
import github.trollhack.utils.world.ExplosionUtil;
import github.trollhack.utils.world.InventoryUtil;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.util.math.Box;

import java.util.function.Predicate;

public class AutoOffhand extends Module {
    public static final AutoOffhand INSTANCE = new AutoOffhand();

    public enum Type { TOTEM, CRYSTAL }
    public enum Priority { INVENTORY, HOTBAR }

    public final EnumSetting<Type> type = enumSetting("Type", Type.TOTEM);

    public final EnumSetting<Priority> priority = enumSetting("Priority", Priority.INVENTORY);
    public final BooleanSetting switchMessage = booleanSetting("Switch Message", false);
    public final IntegerSetting delay = integerSetting("Delay", 1, 1, 20, 1);
    public final IntegerSetting damageTimeout = integerSetting("Damage Timeout", 100, 10, 1000, 10);

    public final FloatSetting staticHp = floatSetting("Static Hp", 12.0f, 1f, 20f, 0.5f, () -> type.getValue() == Type.TOTEM);
    public final FloatSetting damageHp = floatSetting("Damage Hp", 4.0f, 1f, 20f, 0.5f, () -> type.getValue() == Type.TOTEM);
    public final BooleanSetting mainHandTotem = booleanSetting("Main Hand Totem", false, () -> type.getValue() == Type.TOTEM);
    public final BooleanSetting checkDamage = booleanSetting("Check Damage", true, () -> type.getValue() == Type.TOTEM);
    public final BooleanSetting falling = booleanSetting("Falling", true, () -> type.getValue() == Type.TOTEM && checkDamage.getValue());
    public final BooleanSetting checkMob = booleanSetting("Mob", true, () -> type.getValue() == Type.TOTEM && checkDamage.getValue());
    public final BooleanSetting checkPlayer = booleanSetting("Player", true, () -> type.getValue() == Type.TOTEM && checkDamage.getValue());
    public final BooleanSetting checkArrow = booleanSetting("Arrow", true, () -> type.getValue() == Type.TOTEM && checkDamage.getValue());
    public final BooleanSetting checkCrystal = booleanSetting("Crystal", true, () -> type.getValue() == Type.TOTEM && checkDamage.getValue());
    public final FloatSetting crystalBias = floatSetting("Crystal Bias", 1.1f, 0.0f, 2.0f, 0.05f, () -> type.getValue() == Type.TOTEM && checkDamage.getValue() && checkCrystal.getValue());

    public final BooleanSetting offhandCrystal = booleanSetting("Offhand Crystal", true, () -> type.getValue() == Type.CRYSTAL);
    public final BooleanSetting checkCACrystal = booleanSetting("Check CrystalAura C", true, () -> type.getValue() == Type.CRYSTAL && offhandCrystal.getValue());

    private long lastMoveTime;
    private float lastDamage;
    private long lastDamageTime;
    private Type lastType;

    public AutoOffhand() {
        super("AutoOffhand", Category.COMBAT);
    }

    @Override
    public void onEnable() {
        lastMoveTime = 0;
        lastDamage = 0.0f;
        lastDamageTime = 0;
        lastType = null;
    }

    @Override
    public void onDisable() {
        lastDamage = 0.0f;
        lastType = null;
    }

    @Override
    public void onUpdate() {
        if (nullCheck()) return;
        if (mc.player.isDead() || mc.player.getHealth() <= 0.0f) return;

        if (!checkDamage.getValue()) {
            lastDamage = 0.0f;
        } else {
            float maxDamage = 0.0f;
            if (checkMob.getValue() && mc.world != null && mc.player != null) {
                Box searchBox = mc.player.getBoundingBox().expand(8.0);
                for (MobEntity e : mc.world.getEntitiesByClass(MobEntity.class, searchBox, m -> true)) {
                    if (mc.player.squaredDistanceTo(e) > 64.0) continue;
                    maxDamage = Math.max(maxDamage, (float) e.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE));
                }
            }
            if (checkPlayer.getValue() && mc.world != null && mc.player != null) {
                for (PlayerEntity p : mc.world.getPlayers()) {
                    if (p == mc.player) continue;
                    if (p.isDead() || p.getHealth() <= 0.0f) continue;
                    if (p.isSpectator()) continue;
                    if (Managers.FRIEND != null && Managers.FRIEND.isFriend(p)) continue;
                    if (mc.player.squaredDistanceTo(p) > 64.0) continue;
                    maxDamage = Math.max(maxDamage, (float) p.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE));
                }
            }
            if (checkArrow.getValue() && mc.world != null && mc.player != null) {
                Box searchBox = mc.player.getBoundingBox().expand(16.0);
                for (PersistentProjectileEntity arrow : mc.world.getEntitiesByClass(PersistentProjectileEntity.class, searchBox, a -> true)) {
                    if (mc.player.squaredDistanceTo(arrow) > 250.0) continue;
                    float speed = (float) arrow.getVelocity().length();
                    float dmg = (float) Math.ceil(speed * arrow.getDamage());
                    if (arrow.isCritical()) dmg = dmg * 0.5f + 1.0f;
                    maxDamage = Math.max(maxDamage, dmg);
                }
            }
            if (checkCrystal.getValue() && mc.world != null && mc.player != null) {
                Box searchBox = mc.player.getBoundingBox().expand(12.0);
                for (EndCrystalEntity crystal : mc.world.getEntitiesByClass(EndCrystalEntity.class, searchBox, c -> !c.isRemoved())) {
                    float dmg = (float) Math.pow(ExplosionUtil.getSelfDamage(crystal.getPos()), crystalBias.getValue());
                    maxDamage = Math.max(maxDamage, dmg);
                }
            }
            if (falling.getValue() && getNextFallDist() > 3.0f) {
                maxDamage = Math.max((float) Math.ceil(getNextFallDist() - 3.0f), maxDamage);
            }
            long nowDamage = System.currentTimeMillis();
            if (maxDamage >= lastDamage) {
                lastDamage = maxDamage;
                lastDamageTime = nowDamage;
            } else if (nowDamage - lastDamageTime >= damageTimeout.getValue()) {
                lastDamage = maxDamage;
            }
        }

        float health = mc.player.getHealth() + mc.player.getAbsorptionAmount();
        boolean totemNeeded = health < staticHp.getValue()
                || (checkDamage.getValue() && health - lastDamage <= damageHp.getValue());
        Type targetType;
        if (totemNeeded) {
            targetType = Type.TOTEM;
        } else if (offhandCrystal.getValue() && checkCACrystal.getValue() && AutoCrystal.INSTANCE.isEnabled()) {
            targetType = Type.CRYSTAL;
        } else if (!mainHandTotem.getValue() && mc.player.getOffHandStack().isEmpty()) {
            targetType = Type.TOTEM;
        } else {
            targetType = null;
        }

        if (targetType == null) {
            lastType = null;
            return;
        }
        if (typeFilter(targetType, mc.player.getOffHandStack())) {
            lastType = targetType;
            return;
        }
        Type currentType = targetType;
        int slot = -1;
        Type foundType = null;
        for (int i = 0; i < 2; i++) {
            Type finalCurrentType = currentType;
            Predicate<ItemStack> filter = stack -> typeFilter(finalCurrentType, stack);
            if (priority.getValue() == Priority.HOTBAR) {
                int hotbarInv = InventoryUtil.findItemInHotbar(filter);
                if (hotbarInv != -1) {
                    slot = InventoryUtil.inventoryToScreenSlot(hotbarInv);
                } else {
                    int mainInv = InventoryUtil.findItemInMainInventory(filter);
                    if (mainInv != -1) slot = InventoryUtil.inventoryToScreenSlot(mainInv);
                }
            } else {
                int mainInv = InventoryUtil.findItemInMainInventory(filter);
                if (mainInv != -1) {
                    slot = InventoryUtil.inventoryToScreenSlot(mainInv);
                } else {
                    int hotbarInv = InventoryUtil.findItemInHotbar(filter);
                    if (hotbarInv != -1) slot = InventoryUtil.inventoryToScreenSlot(hotbarInv);
                }
            }
            if (slot != -1) {
                foundType = currentType;
                break;
            }
            Type[] values = Type.values();
            currentType = values[(currentType.ordinal() + 1) % values.length];
        }

        if (slot == -1 || foundType == null) return;

        long nowSwitch = System.currentTimeMillis();
        if (nowSwitch - lastMoveTime < delay.getValue() * 50L) return;

        if (mainHandTotem.getValue() && foundType == Type.TOTEM) {
            if (mc.player.getMainHandStack().getItem() != Items.TOTEM_OF_UNDYING) {
                int totemSlot = InventoryUtil.findItemInHotbar(Items.TOTEM_OF_UNDYING);
                if (totemSlot != -1) {
                    InventoryUtil.switchToSlot(totemSlot);
                }
            } else if (mc.getNetworkHandler() != null) {
                mc.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(mc.player.getInventory().selectedSlot));
            }
        } else {
            InventoryUtil.moveToOffhand(slot);
        }

        lastMoveTime = nowSwitch;
        lastType = foundType;

        if (switchMessage.getValue()) {
            Notification.sendReplace(AutoOffhand.class.hashCode(),
                    "Offhand now has a " + foundType.name().toLowerCase(), 2000);
        }
    }

    @Override
    public String getHudInfo() {
        return lastType != null ? lastType.name() : "";
    }

    private boolean typeFilter(Type type, ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        switch (type) {
            case TOTEM: return stack.getItem() == Items.TOTEM_OF_UNDYING;
            case CRYSTAL: return stack.getItem() == Items.END_CRYSTAL;
            default: return false;
        }
    }

    private float getNextFallDist() {
        if (mc.player == null) return 0.0f;
        return mc.player.fallDistance - (float) (mc.player.getY() - mc.player.prevY);
    }
}
