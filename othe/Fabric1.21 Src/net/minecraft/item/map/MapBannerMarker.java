/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.item.map;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import net.minecraft.block.entity.BannerBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.map.MapDecorationType;
import net.minecraft.item.map.MapDecorationTypes;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.Text;
import net.minecraft.text.TextCodecs;
import net.minecraft.util.DyeColor;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a banner marker in world.
 * <p>
 * Used to track banners in a map state.
 */
public record MapBannerMarker(BlockPos pos, DyeColor color, Optional<Text> name) {
    public static final Codec<MapBannerMarker> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)BlockPos.CODEC.fieldOf("pos")).forGetter(MapBannerMarker::pos), DyeColor.CODEC.lenientOptionalFieldOf("color", DyeColor.WHITE).forGetter(MapBannerMarker::color), TextCodecs.STRINGIFIED_CODEC.lenientOptionalFieldOf("name").forGetter(MapBannerMarker::name)).apply((Applicative<MapBannerMarker, ?>)instance, MapBannerMarker::new));
    public static final Codec<List<MapBannerMarker>> LIST_CODEC = CODEC.listOf();

    @Nullable
    public static MapBannerMarker fromWorldBlock(BlockView blockView, BlockPos blockPos) {
        BlockEntity blockEntity = blockView.getBlockEntity(blockPos);
        if (blockEntity instanceof BannerBlockEntity) {
            BannerBlockEntity bannerBlockEntity = (BannerBlockEntity)blockEntity;
            DyeColor dyeColor = bannerBlockEntity.getColorForState();
            Optional<Text> optional = Optional.ofNullable(bannerBlockEntity.getCustomName());
            return new MapBannerMarker(blockPos, dyeColor, optional);
        }
        return null;
    }

    public RegistryEntry<MapDecorationType> getDecorationType() {
        return switch (this.color) {
            default -> throw new MatchException(null, null);
            case DyeColor.WHITE -> MapDecorationTypes.BANNER_WHITE;
            case DyeColor.ORANGE -> MapDecorationTypes.BANNER_ORANGE;
            case DyeColor.MAGENTA -> MapDecorationTypes.BANNER_MAGENTA;
            case DyeColor.LIGHT_BLUE -> MapDecorationTypes.BANNER_LIGHT_BLUE;
            case DyeColor.YELLOW -> MapDecorationTypes.BANNER_YELLOW;
            case DyeColor.LIME -> MapDecorationTypes.BANNER_LIME;
            case DyeColor.PINK -> MapDecorationTypes.BANNER_PINK;
            case DyeColor.GRAY -> MapDecorationTypes.BANNER_GRAY;
            case DyeColor.LIGHT_GRAY -> MapDecorationTypes.BANNER_LIGHT_GRAY;
            case DyeColor.CYAN -> MapDecorationTypes.BANNER_CYAN;
            case DyeColor.PURPLE -> MapDecorationTypes.BANNER_PURPLE;
            case DyeColor.BLUE -> MapDecorationTypes.BANNER_BLUE;
            case DyeColor.BROWN -> MapDecorationTypes.BANNER_BROWN;
            case DyeColor.GREEN -> MapDecorationTypes.BANNER_GREEN;
            case DyeColor.RED -> MapDecorationTypes.BANNER_RED;
            case DyeColor.BLACK -> MapDecorationTypes.BANNER_BLACK;
        };
    }

    public String getKey() {
        return "banner-" + this.pos.getX() + "," + this.pos.getY() + "," + this.pos.getZ();
    }
}

