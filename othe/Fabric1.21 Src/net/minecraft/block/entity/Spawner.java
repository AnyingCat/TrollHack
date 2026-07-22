/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.block.entity;

import java.util.List;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.EntityType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.registry.Registries;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.random.Random;
import org.jetbrains.annotations.Nullable;

public interface Spawner {
    public void setEntityType(EntityType<?> var1, Random var2);

    public static void appendSpawnDataToTooltip(ItemStack stack, List<Text> tooltip, String spawnDataKey) {
        Text text = Spawner.getSpawnedEntityText(stack, spawnDataKey);
        if (text != null) {
            tooltip.add(text);
        } else {
            tooltip.add(ScreenTexts.EMPTY);
            tooltip.add(Text.translatable("block.minecraft.spawner.desc1").formatted(Formatting.GRAY));
            tooltip.add(ScreenTexts.space().append(Text.translatable("block.minecraft.spawner.desc2").formatted(Formatting.BLUE)));
        }
    }

    @Nullable
    public static Text getSpawnedEntityText(ItemStack stack, String spawnDataKey) {
        NbtCompound nbtCompound = stack.getOrDefault(DataComponentTypes.BLOCK_ENTITY_DATA, NbtComponent.DEFAULT).getNbt();
        Identifier identifier = Spawner.getSpawnedEntityId(nbtCompound, spawnDataKey);
        if (identifier != null) {
            return Registries.ENTITY_TYPE.getOrEmpty(identifier).map(entityType -> Text.translatable(entityType.getTranslationKey()).formatted(Formatting.GRAY)).orElse(null);
        }
        return null;
    }

    @Nullable
    private static Identifier getSpawnedEntityId(NbtCompound nbt, String spawnDataKey) {
        if (nbt.contains(spawnDataKey, NbtElement.COMPOUND_TYPE)) {
            String string = nbt.getCompound(spawnDataKey).getCompound("entity").getString("id");
            return Identifier.tryParse(string);
        }
        return null;
    }
}

