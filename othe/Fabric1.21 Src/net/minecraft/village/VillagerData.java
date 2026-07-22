/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.village;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.village.VillagerProfession;
import net.minecraft.village.VillagerType;

public class VillagerData {
    public static final int MIN_LEVEL = 1;
    public static final int MAX_LEVEL = 5;
    private static final int[] LEVEL_BASE_EXPERIENCE = new int[]{0, 10, 70, 150, 250};
    public static final Codec<VillagerData> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)Registries.VILLAGER_TYPE.getCodec().fieldOf("type")).orElseGet(() -> VillagerType.PLAINS).forGetter(data -> data.type), ((MapCodec)Registries.VILLAGER_PROFESSION.getCodec().fieldOf("profession")).orElseGet(() -> VillagerProfession.NONE).forGetter(data -> data.profession), ((MapCodec)Codec.INT.fieldOf("level")).orElse(1).forGetter(data -> data.level)).apply((Applicative<VillagerData, ?>)instance, VillagerData::new));
    public static final PacketCodec<RegistryByteBuf, VillagerData> PACKET_CODEC = PacketCodec.tuple(PacketCodecs.registryValue(RegistryKeys.VILLAGER_TYPE), data -> data.type, PacketCodecs.registryValue(RegistryKeys.VILLAGER_PROFESSION), data -> data.profession, PacketCodecs.VAR_INT, data -> data.level, VillagerData::new);
    private final VillagerType type;
    private final VillagerProfession profession;
    private final int level;

    public VillagerData(VillagerType type, VillagerProfession profession, int level) {
        this.type = type;
        this.profession = profession;
        this.level = Math.max(1, level);
    }

    public VillagerType getType() {
        return this.type;
    }

    public VillagerProfession getProfession() {
        return this.profession;
    }

    public int getLevel() {
        return this.level;
    }

    public VillagerData withType(VillagerType type) {
        return new VillagerData(type, this.profession, this.level);
    }

    public VillagerData withProfession(VillagerProfession profession) {
        return new VillagerData(this.type, profession, this.level);
    }

    public VillagerData withLevel(int level) {
        return new VillagerData(this.type, this.profession, level);
    }

    public static int getLowerLevelExperience(int level) {
        return VillagerData.canLevelUp(level) ? LEVEL_BASE_EXPERIENCE[level - 1] : 0;
    }

    public static int getUpperLevelExperience(int level) {
        return VillagerData.canLevelUp(level) ? LEVEL_BASE_EXPERIENCE[level] : 0;
    }

    public static boolean canLevelUp(int level) {
        return level >= 1 && level < 5;
    }
}

