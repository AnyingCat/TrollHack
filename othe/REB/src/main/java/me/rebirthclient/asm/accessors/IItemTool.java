package me.rebirthclient.asm.accessors;

import net.minecraft.item.ItemTool;
import net.minecraft.item.Item.ToolMaterial;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin({ItemTool.class})
public interface IItemTool {
   @Accessor("attackDamage")
   float getAttackDamage();

   @Accessor("toolMaterial")
   ToolMaterial getToolMaterial();
}
