package me.rebirthclient.mod.modules.impl.render;

import me.rebirthclient.mod.modules.Category;
import me.rebirthclient.mod.modules.Module;
import me.rebirthclient.mod.modules.settings.Setting;

public class ItemPhysics extends Module {
   public static ItemPhysics INSTANCE = new ItemPhysics();
   public final Setting<Float> Scaling = this.add(new Setting<>("Scale", 0.5F, 0.1F, 1.0F));
   public final Setting<Float> rotateSpeed = this.add(new Setting<>("RotateSpeed", 0.5F, 0.0F, 1.0F));
   public final Setting<Float> shulkerBox = this.add(new Setting<>("ShulkerBoxScale", 0.5F, 0.0F, 4.0F));

   public ItemPhysics() {
      super("ItemPhysics", "Apply physics to items", Category.RENDER);
      INSTANCE = this;
   }
}
