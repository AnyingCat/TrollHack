package me.rebirthclient.mod.modules.impl.render;

import me.rebirthclient.mod.modules.Category;
import me.rebirthclient.mod.modules.Module;
import me.rebirthclient.mod.modules.settings.Setting;

public class RenderSetting extends Module {
   public static RenderSetting INSTANCE;
   public final Setting<Float> outlineWidth = this.add(new Setting<>("OutlineWidth", 1.0F, 0.1F, 4.0F));

   public RenderSetting() {
      super("RenderSetting", "idk", Category.RENDER);
      INSTANCE = this;
   }
}
