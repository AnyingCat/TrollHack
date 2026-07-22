/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.block.jukebox;

import net.minecraft.block.jukebox.JukeboxSong;
import net.minecraft.registry.Registerable;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;

public interface JukeboxSongs {
    public static final RegistryKey<JukeboxSong> THIRTEEN = JukeboxSongs.of("13");
    public static final RegistryKey<JukeboxSong> CAT = JukeboxSongs.of("cat");
    public static final RegistryKey<JukeboxSong> BLOCKS = JukeboxSongs.of("blocks");
    public static final RegistryKey<JukeboxSong> CHIRP = JukeboxSongs.of("chirp");
    public static final RegistryKey<JukeboxSong> FAR = JukeboxSongs.of("far");
    public static final RegistryKey<JukeboxSong> MALL = JukeboxSongs.of("mall");
    public static final RegistryKey<JukeboxSong> MELLOHI = JukeboxSongs.of("mellohi");
    public static final RegistryKey<JukeboxSong> STAL = JukeboxSongs.of("stal");
    public static final RegistryKey<JukeboxSong> STRAD = JukeboxSongs.of("strad");
    public static final RegistryKey<JukeboxSong> WARD = JukeboxSongs.of("ward");
    public static final RegistryKey<JukeboxSong> ELEVEN = JukeboxSongs.of("11");
    public static final RegistryKey<JukeboxSong> WAIT = JukeboxSongs.of("wait");
    public static final RegistryKey<JukeboxSong> PIGSTEP = JukeboxSongs.of("pigstep");
    public static final RegistryKey<JukeboxSong> OTHERSIDE = JukeboxSongs.of("otherside");
    public static final RegistryKey<JukeboxSong> FIVE = JukeboxSongs.of("5");
    public static final RegistryKey<JukeboxSong> RELIC = JukeboxSongs.of("relic");
    public static final RegistryKey<JukeboxSong> PRECIPICE = JukeboxSongs.of("precipice");
    public static final RegistryKey<JukeboxSong> CREATOR = JukeboxSongs.of("creator");
    public static final RegistryKey<JukeboxSong> CREATOR_MUSIC_BOX = JukeboxSongs.of("creator_music_box");

    private static RegistryKey<JukeboxSong> of(String id) {
        return RegistryKey.of(RegistryKeys.JUKEBOX_SONG, Identifier.ofVanilla(id));
    }

    private static void register(Registerable<JukeboxSong> registry, RegistryKey<JukeboxSong> key, RegistryEntry.Reference<SoundEvent> soundEvent, int lengthInSeconds, int comparatorOutput) {
        registry.register(key, new JukeboxSong(soundEvent, Text.translatable(Util.createTranslationKey("jukebox_song", key.getValue())), lengthInSeconds, comparatorOutput));
    }

    public static void bootstrap(Registerable<JukeboxSong> registry) {
        JukeboxSongs.register(registry, THIRTEEN, SoundEvents.MUSIC_DISC_13, 178, 1);
        JukeboxSongs.register(registry, CAT, SoundEvents.MUSIC_DISC_CAT, 185, 2);
        JukeboxSongs.register(registry, BLOCKS, SoundEvents.MUSIC_DISC_BLOCKS, 345, 3);
        JukeboxSongs.register(registry, CHIRP, SoundEvents.MUSIC_DISC_CHIRP, 185, 4);
        JukeboxSongs.register(registry, FAR, SoundEvents.MUSIC_DISC_FAR, 174, 5);
        JukeboxSongs.register(registry, MALL, SoundEvents.MUSIC_DISC_MALL, 197, 6);
        JukeboxSongs.register(registry, MELLOHI, SoundEvents.MUSIC_DISC_MELLOHI, 96, 7);
        JukeboxSongs.register(registry, STAL, SoundEvents.MUSIC_DISC_STAL, 150, 8);
        JukeboxSongs.register(registry, STRAD, SoundEvents.MUSIC_DISC_STRAD, 188, 9);
        JukeboxSongs.register(registry, WARD, SoundEvents.MUSIC_DISC_WARD, 251, 10);
        JukeboxSongs.register(registry, ELEVEN, SoundEvents.MUSIC_DISC_11, 71, 11);
        JukeboxSongs.register(registry, WAIT, SoundEvents.MUSIC_DISC_WAIT, 238, 12);
        JukeboxSongs.register(registry, PIGSTEP, SoundEvents.MUSIC_DISC_PIGSTEP, 149, 13);
        JukeboxSongs.register(registry, OTHERSIDE, SoundEvents.MUSIC_DISC_OTHERSIDE, 195, 14);
        JukeboxSongs.register(registry, FIVE, SoundEvents.MUSIC_DISC_5, 178, 15);
        JukeboxSongs.register(registry, RELIC, SoundEvents.MUSIC_DISC_RELIC, 218, 14);
        JukeboxSongs.register(registry, PRECIPICE, SoundEvents.MUSIC_DISC_PRECIPICE, 299, 13);
        JukeboxSongs.register(registry, CREATOR, SoundEvents.MUSIC_DISC_CREATOR, 176, 12);
        JukeboxSongs.register(registry, CREATOR_MUSIC_BOX, SoundEvents.MUSIC_DISC_CREATOR_MUSIC_BOX, 73, 11);
    }
}

