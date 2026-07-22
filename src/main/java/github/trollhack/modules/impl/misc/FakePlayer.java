package github.trollhack.modules.impl.misc;

import com.mojang.authlib.GameProfile;
import github.trollhack.events.impl.PacketEvent;
import github.trollhack.events.impl.TickEvent;
import github.trollhack.modules.Category;
import github.trollhack.modules.Module;
import github.trollhack.settings.impl.BooleanSetting;
import github.trollhack.settings.impl.StringSetting;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityStatuses;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;
import net.minecraft.network.packet.s2c.play.ExplosionS2CPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.util.UUID;

public class FakePlayer extends Module {
    public static OtherClientPlayerEntity fakePlayer;
    private final BooleanSetting damage = booleanSetting("Damage", true);
    private final BooleanSetting autoTotem = booleanSetting("AutoTotem", true);
    private final StringSetting name = stringSetting("Name", "NekoCat");
    public static final FakePlayer INSTANCE = new FakePlayer();
    public FakePlayer() {
        super("FakePlayer", Category.MISC);
    }

    @Override
    public void onEnable() {
        if (nullCheck()) {
            setEnabled(false);
            return;
        }

        fakePlayer = new OtherClientPlayerEntity(mc.world, new GameProfile(
                UUID.fromString("1978-6666-6666-6666-121121"),
                name.getValue()
        )) {
            @Override
            public boolean isOnGround() {
                return true;
            }
        };

        mc.world.addEntity(fakePlayer);
        fakePlayer.copyPositionAndRotation(mc.player);
        fakePlayer.bodyYaw = mc.player.bodyYaw;
        fakePlayer.headYaw = mc.player.headYaw;

        fakePlayer.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, 9999, 2));
        fakePlayer.addStatusEffect(new StatusEffectInstance(StatusEffects.ABSORPTION, 9999, 3));
        fakePlayer.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 9999, 1));
    }

    @Override
    public void onDisable() {
        if (fakePlayer == null) return;
        fakePlayer.kill();
        fakePlayer.setRemoved(Entity.RemovalReason.KILLED);
        fakePlayer.onRemoved();
        fakePlayer = null;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onTick(TickEvent event) {
        if (fakePlayer == null || fakePlayer.isDead() || fakePlayer.clientWorld != mc.world) {
            setEnabled(false);
            return;
        }

        fakePlayer.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, 9999, 2));

        if (autoTotem.getValue() && fakePlayer.getOffHandStack().getItem() != Items.TOTEM_OF_UNDYING) {
            fakePlayer.setStackInHand(Hand.OFF_HAND, new ItemStack(Items.TOTEM_OF_UNDYING));
        }

        if (fakePlayer.isDead()) {
            if (fakePlayer.tryUseTotem(mc.world.getDamageSources().generic())) {
                fakePlayer.setHealth(10f);
                new EntityStatusS2CPacket(fakePlayer, EntityStatuses.USE_TOTEM_OF_UNDYING).apply(mc.getNetworkHandler());
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onPacketReceive(PacketEvent.Receive event) {
        if (!damage.getValue() || fakePlayer == null || fakePlayer.hurtTime != 0) return;

        if (event.getPacket() instanceof ExplosionS2CPacket explosion) {
            double distance = Math.sqrt(new Vec3d(explosion.getX(), explosion.getY(), explosion.getZ())
                    .squaredDistanceTo(fakePlayer.getPos()));
            if (distance > 10) return;

            float dmg = calculateExplosionDamage(
                    new Vec3d(explosion.getX(), explosion.getY(), explosion.getZ()),
                    fakePlayer
            );

            applyDamage(dmg);

            if (fakePlayer.isDead()) {
                useTotem();
            }
        }
    }

    private void applyDamage(float damage) {
        fakePlayer.onDamaged(mc.world.getDamageSources().generic());

        float absorption = fakePlayer.getAbsorptionAmount();
        if (absorption >= damage) {
            fakePlayer.setAbsorptionAmount(absorption - damage);
        } else {
            float remainingDamage = damage - absorption;
            fakePlayer.setAbsorptionAmount(0);
            fakePlayer.setHealth(fakePlayer.getHealth() - remainingDamage);
        }
    }

    private void useTotem() {
        if (fakePlayer.tryUseTotem(mc.world.getDamageSources().generic())) {
            fakePlayer.setHealth(10f);
            new EntityStatusS2CPacket(fakePlayer, EntityStatuses.USE_TOTEM_OF_UNDYING).apply(mc.getNetworkHandler());
        }
    }

    private float calculateExplosionDamage(Vec3d explosionPos, OtherClientPlayerEntity target) {
        if (mc.world == null) return 0f;

        double distance = Math.sqrt(target.squaredDistanceTo(explosionPos)) / 12.0;
        if (distance > 1.0) return 0f;

        double exposure = getExposure(explosionPos, target);
        double finalExposure = (1.0 - distance) * exposure;

        float rawDamage = (float) Math.floor((finalExposure * finalExposure + finalExposure) / 2.0 * 7.0 * 12.0 + 1.0);

        float armor = target.getArmor();
        float toughness = (float) target.getAttributeValue(net.minecraft.entity.attribute.EntityAttributes.GENERIC_ARMOR_TOUGHNESS);

        rawDamage = rawDamage * (1.0f - Math.min(20.0f, armor) / 25.0f);
        rawDamage = rawDamage * (1.0f - toughness / 8.0f);

        if (target.hasStatusEffect(StatusEffects.RESISTANCE)) {
            int resistance = 25 - (target.getStatusEffect(StatusEffects.RESISTANCE).getAmplifier() + 1) * 5;
            rawDamage = Math.max(rawDamage * resistance / 25.0f, 0.0f);
        }

        return Math.max(rawDamage, 0.0f);
    }

    private double getExposure(Vec3d source, Entity entity) {
        net.minecraft.util.math.Box box = entity.getBoundingBox();
        double d = 1.0 / ((box.maxX - box.minX) * 2.0 + 1.0);
        double e = 1.0 / ((box.maxY - box.minY) * 2.0 + 1.0);
        double f = 1.0 / ((box.maxZ - box.minZ) * 2.0 + 1.0);

        int i = 0;
        int j = 0;

        for (double k = 0.0; k <= 1.0; k += d) {
            for (double l = 0.0; l <= 1.0; l += e) {
                for (double m = 0.0; m <= 1.0; m += f) {
                    double n = MathHelper.lerp(k, box.minX, box.maxX);
                    double o = MathHelper.lerp(l, box.minY, box.maxY);
                    double p = MathHelper.lerp(m, box.minZ, box.maxZ);

                    if (mc.world.raycast(new net.minecraft.world.RaycastContext(
                            new Vec3d(n, o, p),
                            source,
                            net.minecraft.world.RaycastContext.ShapeType.COLLIDER,
                            net.minecraft.world.RaycastContext.FluidHandling.NONE,
                            entity
                    )).getType() == net.minecraft.util.hit.HitResult.Type.MISS) {
                        i++;
                    }
                    j++;
                }
            }
        }

        return (double) i / (double) j;
    }
}
