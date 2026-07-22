package me.catrix.core.impl;

import me.catrix.api.utils.Wrapper;
import me.catrix.api.utils.math.MathUtil;
import me.catrix.mod.modules.impl.client.Sound;
import me.catrix.mod.modules.impl.render.DeathEffects;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

public class SoundManager implements Wrapper {
    public final Identifier KEYPRESS_SOUND = new Identifier("minecraft:keypress");
    public SoundEvent KEYPRESS_SOUNDEVENT = SoundEvent.of(KEYPRESS_SOUND);

    public final Identifier KEYRELEASE_SOUND = new Identifier("minecraft:keyrelease");
    public SoundEvent KEYRELEASE_SOUNDEVENT = SoundEvent.of(KEYRELEASE_SOUND);

    public final Identifier UWU_SOUND = new Identifier("minecraft:uwu");
    public SoundEvent UWU_SOUNDEVENT = SoundEvent.of(UWU_SOUND);

    public final Identifier ENABLE_SOUND = new Identifier("minecraft:enable");
    public SoundEvent ENABLE_SOUNDEVENT = SoundEvent.of(ENABLE_SOUND);

    public final Identifier DISABLE_SOUND = new Identifier("minecraft:disable");
    public SoundEvent DISABLE_SOUNDEVENT = SoundEvent.of(DISABLE_SOUND);

    public final Identifier MOAN1_SOUND = new Identifier("minecraft:moan1");
    public SoundEvent MOAN1_SOUNDEVENT = SoundEvent.of(MOAN1_SOUND);

    public final Identifier MOAN2_SOUND = new Identifier("minecraft:moan2");
    public SoundEvent MOAN2_SOUNDEVENT = SoundEvent.of(MOAN2_SOUND);

    public final Identifier MOAN3_SOUND = new Identifier("minecraft:moan3");
    public SoundEvent MOAN3_SOUNDEVENT = SoundEvent.of(MOAN3_SOUND);

    public final Identifier MOAN4_SOUND = new Identifier("minecraft:moan4");
    public SoundEvent MOAN4_SOUNDEVENT = SoundEvent.of(MOAN4_SOUND);

    public final Identifier SKEET_SOUND = new Identifier("minecraft:skeet");
    public SoundEvent SKEET_SOUNDEVENT = SoundEvent.of(SKEET_SOUND);

    public final Identifier ORTHODOX_SOUND = new Identifier("minecraft:orthodox");
    public SoundEvent ORTHODOX_SOUNDEVENT = SoundEvent.of(ORTHODOX_SOUND);

    public final Identifier BOOLEAN_SOUND = new Identifier("minecraft:boolean");
    public SoundEvent BOOLEAN_SOUNDEVENT = SoundEvent.of(BOOLEAN_SOUND);

    public final Identifier SCROLL_SOUND = new Identifier("minecraft:scroll");
    public SoundEvent SCROLL_SOUNDEVENT = SoundEvent.of(SCROLL_SOUND);

    public void registerSounds() {
        Registry.register(Registries.SOUND_EVENT, KEYPRESS_SOUND, KEYPRESS_SOUNDEVENT);
        Registry.register(Registries.SOUND_EVENT, KEYRELEASE_SOUND, KEYRELEASE_SOUNDEVENT);
        Registry.register(Registries.SOUND_EVENT, ENABLE_SOUND, ENABLE_SOUNDEVENT);
        Registry.register(Registries.SOUND_EVENT, DISABLE_SOUND, DISABLE_SOUNDEVENT);
        Registry.register(Registries.SOUND_EVENT, MOAN1_SOUND, MOAN1_SOUNDEVENT);
        Registry.register(Registries.SOUND_EVENT, MOAN2_SOUND, MOAN2_SOUNDEVENT);
        Registry.register(Registries.SOUND_EVENT, MOAN3_SOUND, MOAN3_SOUNDEVENT);
        Registry.register(Registries.SOUND_EVENT, MOAN4_SOUND, MOAN4_SOUNDEVENT);
        Registry.register(Registries.SOUND_EVENT, UWU_SOUND, UWU_SOUNDEVENT);
        Registry.register(Registries.SOUND_EVENT, SKEET_SOUND, SKEET_SOUNDEVENT);
        Registry.register(Registries.SOUND_EVENT, ORTHODOX_SOUND, ORTHODOX_SOUNDEVENT);
        Registry.register(Registries.SOUND_EVENT, SCROLL_SOUND, SCROLL_SOUNDEVENT);
        Registry.register(Registries.SOUND_EVENT, BOOLEAN_SOUND, BOOLEAN_SOUNDEVENT);
    }

    public void playHitSound(Sound.HitSound value) {
        switch (value) {
            case UWU -> playSound(UWU_SOUNDEVENT);
            case SKEET -> playSound(SKEET_SOUNDEVENT);
            case KEYBOARD -> playSound(KEYPRESS_SOUNDEVENT);
            case MOAN -> {
                SoundEvent sound = switch ((int) (MathUtil.random(0, 3))) {
                    case 0 -> MOAN1_SOUNDEVENT;
                    case 1 -> MOAN2_SOUNDEVENT;
                    case 2 -> MOAN3_SOUNDEVENT;
                    default -> MOAN4_SOUNDEVENT;
                };
                playSound(sound);
            }
        }
    }

    public void playKillSound(DeathEffects.SoundMode value) {
        switch (value) {
            case UWU -> playSound(UWU_SOUNDEVENT);
            case ORTHODOX -> playSound(ORTHODOX_SOUNDEVENT);
            case MOAN -> {
                SoundEvent sound = switch ((int) (MathUtil.random(0, 3))) {
                    case 0 -> MOAN1_SOUNDEVENT;
                    case 1 -> MOAN2_SOUNDEVENT;
                    case 2 -> MOAN3_SOUNDEVENT;
                    default -> MOAN4_SOUNDEVENT;
                };
                playSound(sound);
            }
        }
    }

    public void playEnable() {
        playSound(ENABLE_SOUNDEVENT);
    }

    public void playDisable() {
        playSound(DISABLE_SOUNDEVENT);
    }

//    public void playScroll() {
//        if (scrollTimer.every(50)) {
//            if (SoundFX.INSTANCE.scrollSound.getValue() == SoundFX.ScrollSound.KeyBoard) {
//                playSound(KEYPRESS_SOUNDEVENT);
//            }
//        }
//    }

    public void playSound(SoundEvent sound) {
        if (mc.player != null && mc.world != null)
            mc.world.playSound(mc.player, mc.player.getBlockPos(), sound, SoundCategory.BLOCKS, Sound.INSTANCE.volume.getValueInt() / 100f, 1f);
    }

    public void playSlider() {
        playSound(SCROLL_SOUNDEVENT);
    }

    public void playBoolean() {
        playSound(BOOLEAN_SOUNDEVENT);
    }
}