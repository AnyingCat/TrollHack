package me.catrix.mod.modules.impl.render;

import me.catrix.Catrix;
import me.catrix.api.events.eventbus.EventHandler;
import me.catrix.api.events.impl.DeathEvent;
import me.catrix.api.events.impl.TotemEvent;
import me.catrix.mod.modules.Module;
import me.catrix.mod.modules.settings.impl.BooleanSetting;
import me.catrix.mod.modules.settings.impl.EnumSetting;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LightningEntity;

public class DeathEffects extends Module {
    public static DeathEffects INSTANCE;
    private final EnumSetting<Mode> modes = add(new EnumSetting<>("Modes", Mode.Sound));
    public final EnumSetting<SoundMode> sounds = add(new EnumSetting<>("KillSounds", SoundMode.UWU, () -> modes.getValue() == Mode.Sound));
    private final BooleanSetting death = add(new BooleanSetting("Death",true));
    private final BooleanSetting pop = add(new BooleanSetting("Pop",true));
    public DeathEffects() {
        super("DeathEffects", Category.Render);
        INSTANCE = this;
        setChinese("死亡特效");
    }

    public enum Mode {
        Lightning,Sound,
    }

    public enum SoundMode {
        UWU, MOAN, ORTHODOX, OFF
    }

    @EventHandler
    public void onPlayerDeath(DeathEvent event) {
        if (mc.world == null || event.getPlayer() == null) return;
        if (death.getValue() && modes.is(DeathEffects.Mode.Lightning)) {
            LightningEntity entity = new LightningEntity(EntityType.LIGHTNING_BOLT, mc.world);
            entity.setPosition(event.getPlayer().getPos());
            entity.setId(-701);
            mc.world.addEntity(entity);
        }
        if (death.getValue() && modes.is(Mode.Sound)) {
            Catrix.SOUND.playKillSound(DeathEffects.INSTANCE.sounds.getValue());
        }
    }

    @EventHandler
    public void onTotemPop(TotemEvent event) {
        if (mc.world == null || event.getPlayer() == null) return;
        if (pop.getValue() && modes.is(DeathEffects.Mode.Lightning)) {
            LightningEntity entity = new LightningEntity(EntityType.LIGHTNING_BOLT, mc.world);
            entity.setPosition(event.getPlayer().getPos());
            entity.setId(-701);
            mc.world.addEntity(entity);
        }
        if (pop.getValue() && modes.is(Mode.Sound)) {
            Catrix.SOUND.playKillSound(DeathEffects.INSTANCE.sounds.getValue());
        }
    }
}