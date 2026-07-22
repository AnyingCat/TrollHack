package github.trollhack.modules.impl.render;

import github.trollhack.events.impl.PacketEvent;
import github.trollhack.events.impl.TickEvent;
import github.trollhack.modules.Category;
import github.trollhack.modules.Module;
import github.trollhack.settings.impl.BooleanSetting;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.entity.projectile.thrown.EggEntity;
import net.minecraft.entity.projectile.thrown.ExperienceBottleEntity;
import net.minecraft.network.packet.s2c.play.TitleS2CPacket;

public class NoRender extends Module {
    public static final NoRender INSTANCE = new NoRender();

    public final BooleanSetting hurtCam = booleanSetting("HurtCam", true);
    public final BooleanSetting fireOverlay = booleanSetting("FireOverlay", true);
    public final BooleanSetting waterOverlay = booleanSetting("WaterOverlay", true);
    public final BooleanSetting blockOverlay = booleanSetting("BlockOverlay", true);
    public final BooleanSetting portalOverlay = booleanSetting("PortalOverlay", true);
    public final BooleanSetting totemAnimation = booleanSetting("TotemAnimation", true);
    public final BooleanSetting nauseaEffect = booleanSetting("NauseaEffect", true);
    public final BooleanSetting blindnessEffect = booleanSetting("BlindnessEffect", true);
    public final BooleanSetting darknessEffect = booleanSetting("DarknessEffect", true);
    public final BooleanSetting fogEffect = booleanSetting("FogEffect", false);
    public final BooleanSetting weather = booleanSetting("Weather", true);
    public final BooleanSetting antiTitle = booleanSetting("AntiTitle", false);
    public final BooleanSetting xpBottles = booleanSetting("XPBottles", false);
    public final BooleanSetting arrows = booleanSetting("Arrows", false);
    public final BooleanSetting eggs = booleanSetting("Eggs", false);
    public final BooleanSetting armorStands = booleanSetting("ArmorStands", false);
    public final BooleanSetting armor = booleanSetting("Armor", false);
    public final BooleanSetting invisiblePlayers = booleanSetting("InvisiblePlayers", false);
    public final BooleanSetting vignette = booleanSetting("Vignette", false);
    public final BooleanSetting potionIcons = booleanSetting("PotionIcons", false);
    public final BooleanSetting explosionParticles = booleanSetting("ExplosionParticles", true);
    public final BooleanSetting playerNametags = booleanSetting("PlayerNametags", false);

    public NoRender() {
        super("NoRender", Category.RENDER);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onPacketReceive(PacketEvent.Receive event) {
        if (antiTitle.getValue() && event.getPacket() instanceof TitleS2CPacket) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onTick(TickEvent event) {
        if (nullCheck()) return;

        for (Entity entity : mc.world.getEntities()) {
            if ((xpBottles.getValue() && entity instanceof ExperienceBottleEntity)
                    || (arrows.getValue() && entity instanceof ArrowEntity)
                    || (eggs.getValue() && entity instanceof EggEntity)
                    || (armorStands.getValue() && entity instanceof ArmorStandEntity)) {
                mc.world.removeEntity(entity.getId(), Entity.RemovalReason.KILLED);
            }
        }
    }
}
