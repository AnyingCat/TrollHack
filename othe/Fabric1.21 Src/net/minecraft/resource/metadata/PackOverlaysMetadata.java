/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.resource.metadata;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.regex.Pattern;
import net.minecraft.resource.metadata.ResourceMetadataSerializer;
import net.minecraft.util.dynamic.Range;

public record PackOverlaysMetadata(List<Entry> overlays) {
    private static final Pattern DIRECTORY_NAME_PATTERN = Pattern.compile("[-_a-zA-Z0-9.]+");
    private static final Codec<PackOverlaysMetadata> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)Entry.CODEC.listOf().fieldOf("entries")).forGetter(PackOverlaysMetadata::overlays)).apply((Applicative<PackOverlaysMetadata, ?>)instance, PackOverlaysMetadata::new));
    public static final ResourceMetadataSerializer<PackOverlaysMetadata> SERIALIZER = ResourceMetadataSerializer.fromCodec("overlays", CODEC);

    private static DataResult<String> validate(String directoryName) {
        if (!DIRECTORY_NAME_PATTERN.matcher(directoryName).matches()) {
            return DataResult.error(() -> directoryName + " is not accepted directory name");
        }
        return DataResult.success(directoryName);
    }

    public List<String> getAppliedOverlays(int packFormat) {
        return this.overlays.stream().filter(overlay -> overlay.isValid(packFormat)).map(Entry::overlay).toList();
    }

    public record Entry(Range<Integer> format, String overlay) {
        static final Codec<Entry> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)Range.createCodec(Codec.INT).fieldOf("formats")).forGetter(Entry::format), ((MapCodec)Codec.STRING.validate(PackOverlaysMetadata::validate).fieldOf("directory")).forGetter(Entry::overlay)).apply((Applicative<Entry, ?>)instance, Entry::new));

        public boolean isValid(int packFormat) {
            return this.format.contains(packFormat);
        }
    }
}

