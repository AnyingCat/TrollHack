/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.block.enums;

import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.StringIdentifiable;

public enum NoteBlockInstrument implements StringIdentifiable
{
    HARP("harp", SoundEvents.BLOCK_NOTE_BLOCK_HARP, Type.BASE_BLOCK),
    BASEDRUM("basedrum", SoundEvents.BLOCK_NOTE_BLOCK_BASEDRUM, Type.BASE_BLOCK),
    SNARE("snare", SoundEvents.BLOCK_NOTE_BLOCK_SNARE, Type.BASE_BLOCK),
    HAT("hat", SoundEvents.BLOCK_NOTE_BLOCK_HAT, Type.BASE_BLOCK),
    BASS("bass", SoundEvents.BLOCK_NOTE_BLOCK_BASS, Type.BASE_BLOCK),
    FLUTE("flute", SoundEvents.BLOCK_NOTE_BLOCK_FLUTE, Type.BASE_BLOCK),
    BELL("bell", SoundEvents.BLOCK_NOTE_BLOCK_BELL, Type.BASE_BLOCK),
    GUITAR("guitar", SoundEvents.BLOCK_NOTE_BLOCK_GUITAR, Type.BASE_BLOCK),
    CHIME("chime", SoundEvents.BLOCK_NOTE_BLOCK_CHIME, Type.BASE_BLOCK),
    XYLOPHONE("xylophone", SoundEvents.BLOCK_NOTE_BLOCK_XYLOPHONE, Type.BASE_BLOCK),
    IRON_XYLOPHONE("iron_xylophone", SoundEvents.BLOCK_NOTE_BLOCK_IRON_XYLOPHONE, Type.BASE_BLOCK),
    COW_BELL("cow_bell", SoundEvents.BLOCK_NOTE_BLOCK_COW_BELL, Type.BASE_BLOCK),
    DIDGERIDOO("didgeridoo", SoundEvents.BLOCK_NOTE_BLOCK_DIDGERIDOO, Type.BASE_BLOCK),
    BIT("bit", SoundEvents.BLOCK_NOTE_BLOCK_BIT, Type.BASE_BLOCK),
    BANJO("banjo", SoundEvents.BLOCK_NOTE_BLOCK_BANJO, Type.BASE_BLOCK),
    PLING("pling", SoundEvents.BLOCK_NOTE_BLOCK_PLING, Type.BASE_BLOCK),
    ZOMBIE("zombie", SoundEvents.BLOCK_NOTE_BLOCK_IMITATE_ZOMBIE, Type.MOB_HEAD),
    SKELETON("skeleton", SoundEvents.BLOCK_NOTE_BLOCK_IMITATE_SKELETON, Type.MOB_HEAD),
    CREEPER("creeper", SoundEvents.BLOCK_NOTE_BLOCK_IMITATE_CREEPER, Type.MOB_HEAD),
    DRAGON("dragon", SoundEvents.BLOCK_NOTE_BLOCK_IMITATE_ENDER_DRAGON, Type.MOB_HEAD),
    WITHER_SKELETON("wither_skeleton", SoundEvents.BLOCK_NOTE_BLOCK_IMITATE_WITHER_SKELETON, Type.MOB_HEAD),
    PIGLIN("piglin", SoundEvents.BLOCK_NOTE_BLOCK_IMITATE_PIGLIN, Type.MOB_HEAD),
    CUSTOM_HEAD("custom_head", SoundEvents.UI_BUTTON_CLICK, Type.CUSTOM);

    private final String name;
    private final RegistryEntry<SoundEvent> sound;
    private final Type type;

    private NoteBlockInstrument(String name, RegistryEntry<SoundEvent> sound, Type type) {
        this.name = name;
        this.sound = sound;
        this.type = type;
    }

    @Override
    public String asString() {
        return this.name;
    }

    public RegistryEntry<SoundEvent> getSound() {
        return this.sound;
    }

    /**
     * {@return whether note blocks playing this instrument can change its pitch}
     */
    public boolean canBePitched() {
        return this.type == Type.BASE_BLOCK;
    }

    /**
     * {@return whether note blocks playing this instrument should determine the sound from skulls above them}
     */
    public boolean hasCustomSound() {
        return this.type == Type.CUSTOM;
    }

    public boolean isNotBaseBlock() {
        return this.type != Type.BASE_BLOCK;
    }

    static enum Type {
        BASE_BLOCK,
        MOB_HEAD,
        CUSTOM;

    }
}

