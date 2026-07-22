package github.trollhack.utils.world;

import github.trollhack.utils.interfaces.Mc;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.explosion.Explosion;

public class ExplosionUtil implements Mc {

    public static final float CRYSTAL_POWER = 6.0f;
    public static final float CRYSTAL_RADIUS = CRYSTAL_POWER * 2.0f;

    public static float getExposure(Vec3d source, Entity entity) {
        return Explosion.getExposure(source, entity);
    }

    public static float getExposureWithBox(Vec3d source, Box box, Entity entity) {
        if (mc.world == null) return 0.0f;
        double d = 1.0 / ((box.maxX - box.minX) * 2.0 + 1.0);
        double e = 1.0 / ((box.maxY - box.minY) * 2.0 + 1.0);
        double f = 1.0 / ((box.maxZ - box.minZ) * 2.0 + 1.0);
        double g = (1.0 - Math.floor(1.0 / d) * d) / 2.0;
        double h = (1.0 - Math.floor(1.0 / f) * f) / 2.0;
        if (d < 0.0 || e < 0.0 || f < 0.0) return 0.0f;

        int hit = 0;
        int total = 0;
        for (double k = 0.0; k <= 1.0; k += d) {
            for (double l = 0.0; l <= 1.0; l += e) {
                for (double m = 0.0; m <= 1.0; m += f) {
                    Vec3d sample = new Vec3d(MathHelper.lerp(k, box.minX, box.maxX) + g,
                            MathHelper.lerp(l, box.minY, box.maxY),
                            MathHelper.lerp(m, box.minZ, box.maxZ) + h);
                    if (mc.world.raycast(new RaycastContext(sample, source,
                            RaycastContext.ShapeType.COLLIDER,
                            RaycastContext.FluidHandling.NONE, entity)).getType() == HitResult.Type.MISS) {
                        hit++;
                    }
                    total++;
                }
            }
        }
        return total == 0 ? 0.0f : (float) hit / (float) total;
    }

    public static float getRawDamage(Vec3d source, Entity entity) {
        double distance = Math.sqrt(entity.squaredDistanceTo(source));
        if (distance > CRYSTAL_RADIUS) return 0.0f;
        double multiplier = (1.0 - distance / CRYSTAL_RADIUS) * Explosion.getExposure(source, entity);
        return (float) ((multiplier * multiplier + multiplier) / 2.0 * 7.0 * CRYSTAL_RADIUS + 1.0);
    }

    public static float getDamage(BlockPos pos, Entity entity, boolean terrain) {
        return getDamage(posToVec(pos), entity, terrain);
    }

    public static float getDamage(Vec3d source, Entity entity, boolean terrain) {
        float damage = getRawDamage(source, entity);
        if (damage <= 0.0f) return 0.0f;
        if (!(entity instanceof LivingEntity living)) {
            return damage;
        }
        damage = applyArmor(living, damage);
        damage = applyResistance(living, damage);
        damage = applyEnchantments(living, damage);
        damage = applyAbsorption(living, damage);
        return Math.max(damage, 0.0f);
    }

    public static float applyArmor(LivingEntity entity, float damage) {
        return applyArmorRaw(damage,
                (float) entity.getAttributeValue(EntityAttributes.GENERIC_ARMOR),
                (float) entity.getAttributeValue(EntityAttributes.GENERIC_ARMOR_TOUGHNESS));
    }

    public static float applyArmorRaw(float damage, float armor, float toughness) {
        float f = 2.0f + toughness / 4.0f;
        float g = MathHelper.clamp(armor - damage / f, armor * 0.2f, 20.0f);
        return damage * (1.0f - g / 25.0f);
    }

    public static float applyResistance(LivingEntity entity, float damage) {
        StatusEffectInstance effect = entity.getStatusEffect(StatusEffects.RESISTANCE);
        if (effect == null) return damage;
        return damage * (MathHelper.clamp(25 - (effect.getAmplifier() + 1) * 5, 0, 25) / 25.0f);
    }

    public static float applyEnchantments(LivingEntity entity, float damage) {
        int epf = getBlastProtectionEPF(entity);
        if (epf <= 0) return damage;
        return damage * (1.0f - MathHelper.clamp(epf, 0.0f, 20.0f) / 25.0f);
    }

    public static int getBlastProtectionEPF(LivingEntity entity) {
        int total = 0;
        for (ItemStack stack : entity.getArmorItems()) {
            if (stack == null || stack.isEmpty()) continue;
            int level = getEnchantmentLevel(stack, Enchantments.BLAST_PROTECTION);
            if (level > 0) {
                total += level * 2;
            }
        }
        return Math.min(total, 20);
    }

    public static int getEnchantmentLevel(ItemStack stack, RegistryKey<Enchantment> key) {
        if (mc.world == null || stack == null || stack.isEmpty()) return 0;
        return stack.getOrDefault(DataComponentTypes.ENCHANTMENTS, ItemEnchantmentsComponent.DEFAULT)
                .getLevel(mc.world.getRegistryManager().get(RegistryKeys.ENCHANTMENT).entryOf(key));
    }

    public static float applyAbsorption(LivingEntity entity, float damage) {
        return Math.max(damage - entity.getAbsorptionAmount(), 0.0f);
    }

    public static float getSelfDamage(Vec3d source) {
        if (mc.player == null) return 0.0f;
        return getDamage(source, mc.player, true);
    }

    public static float getSelfDamage(BlockPos pos) {
        return getSelfDamage(posToVec(pos));
    }

    public static Vec3d posToVec(BlockPos pos) {
        return new Vec3d(pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5);
    }

    public static boolean willKill(PlayerEntity player, float damage) {
        return damage >= player.getHealth() + player.getAbsorptionAmount();
    }

    public static float getDamagePredicted(Vec3d source, Entity entity, Vec3d predictedPos, boolean terrain) {
        double distance = Math.sqrt(predictedPos.squaredDistanceTo(source));
        if (distance > CRYSTAL_RADIUS) return 0.0f;

        if (!(entity instanceof LivingEntity living)) {
            double multiplier = 1.0 - distance / CRYSTAL_RADIUS;
            return (float) Math.max((multiplier * multiplier + multiplier) / 2.0 * 7.0 * CRYSTAL_RADIUS + 1.0, 0.0f);
        }

        double halfWidth = living.getWidth() / 2.0;
        double multiplier = (1.0 - distance / CRYSTAL_RADIUS) * getExposureWithBox(source,
                new Box(
                        predictedPos.x - halfWidth, predictedPos.y, predictedPos.z - halfWidth,
                        predictedPos.x + halfWidth, predictedPos.y + living.getHeight(), predictedPos.z + halfWidth),
                living);
        float damage = (float) ((multiplier * multiplier + multiplier) / 2.0 * 7.0 * CRYSTAL_RADIUS + 1.0);
        if (damage <= 0.0f) return 0.0f;

        damage = applyArmor(living, damage);
        damage = applyResistance(living, damage);
        damage = applyEnchantments(living, damage);
        damage = applyAbsorption(living, damage);
        return Math.max(damage, 0.0f);
    }
}
