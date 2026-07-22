package me.catrix.mod.modules.impl.client;

import me.catrix.Catrix;
import me.catrix.api.events.eventbus.EventHandler;
import me.catrix.api.events.impl.EventAttack;
import me.catrix.mod.modules.Module;
import me.catrix.mod.modules.settings.impl.EnumSetting;
import me.catrix.mod.modules.settings.impl.SliderSetting;
import net.minecraft.entity.decoration.EndCrystalEntity;
import org.jetbrains.annotations.NotNull;

public class Sound extends Module {
    public static Sound INSTANCE;

    public Sound() {
        super("Sound", Category.Client);
        setChinese("音效");
        INSTANCE = this;
    }

    public final SliderSetting volume = add(new SliderSetting("Volume", 100, 0, 100));
    public final EnumSetting<HitSound> hitSound = add(new EnumSetting<>("HitSound", HitSound.UWU));
//    public final EnumSetting<KillSound> killSound = add(new EnumSetting<>("KillSound", KillSound.OFF));
//    public final EnumSetting<ScrollSound> scrollSound = add(new EnumSetting<>("ScrollSound", ScrollSound.KeyBoard));

    @EventHandler
    @SuppressWarnings("unused")
    public void onAttack(@NotNull EventAttack event) {
        if (!(event.getEntity() instanceof EndCrystalEntity) && !event.isPre())
            Catrix.SOUND.playHitSound(hitSound.getValue());
    }

    //    @EventHandler
//    @SuppressWarnings("unused")
//    public void onDeath(DeathEvent e) {
//        if (Aura.target != null && Aura.target == e.getPlayer() && killSound.is(KillSound.Custom)) {
//            ThunderHack.soundManager.playSound("kill");
//            return;
//        }
//        if (AutoCrystal.target != null && AutoCrystal.target == e.getPlayer() && killSound.is(KillSound.Custom)) {
//            ThunderHack.soundManager.playSound("kill");
//        }
//    }

    public enum HitSound {
        UWU, MOAN, SKEET, KEYBOARD, OFF
    }

    @Override
    public void enable() {
        this.state = true;
    }

    @Override
    public void disable() {
        this.state = true;
    }

    @Override
    public boolean isOn() {
        return true;
    }
}


//    public enum KillSound {
//        OFF
//    }

//    public enum ScrollSound {
//        OFF, KeyBoard
//    }
//}