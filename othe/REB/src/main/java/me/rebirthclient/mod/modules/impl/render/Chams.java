//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "G:\PortableSoft\JBY\MC_Deobf3000\1.12-MCP-Mappings"!

package me.rebirthclient.mod.modules.impl.render;

import java.awt.Color;
import me.rebirthclient.mod.modules.Category;
import me.rebirthclient.mod.modules.Module;
import me.rebirthclient.mod.modules.settings.Setting;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.client.event.RenderLivingEvent.Pre;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class Chams extends Module {
   public static Chams INSTANCE;
   private final Setting<Chams.Page> page = this.add(new Setting<>("Settings", Chams.Page.GLOBAL));
   public final Setting<Boolean> fill = this.add(new Setting<>("Fill", true, v -> this.page.getValue() == Chams.Page.GLOBAL).setParent());
   public final Setting<Boolean> xqz = this.add(new Setting<>("XQZ", true, v -> this.page.getValue() == Chams.Page.GLOBAL && this.fill.isOpen()));
   public final Setting<Boolean> wireframe = this.add(new Setting<>("Wireframe", true, v -> this.page.getValue() == Chams.Page.GLOBAL));
   public final Setting<Chams.Model> model = this.add(new Setting<>("Model", Chams.Model.XQZ, v -> this.page.getValue() == Chams.Page.GLOBAL));
   public final Setting<Boolean> self = this.add(new Setting<>("Self", true, v -> this.page.getValue() == Chams.Page.GLOBAL));
   public final Setting<Boolean> noInterp = this.add(new Setting<>("NoInterp", false, v -> this.page.getValue() == Chams.Page.GLOBAL));
   public final Setting<Boolean> sneak = this.add(new Setting<>("Sneak", false, v -> this.page.getValue() == Chams.Page.GLOBAL));
   public final Setting<Boolean> glint = this.add(new Setting<>("Glint", false, v -> this.page.getValue() == Chams.Page.GLOBAL));
   public final Setting<Float> lineWidth = this.add(new Setting<>("LineWidth", 1.0F, 0.1F, 3.0F, v -> this.page.getValue() == Chams.Page.COLORS));
   public final Setting<Color> color = this.add(new Setting<>("Color", new Color(132, 132, 241, 150), v -> this.page.getValue() == Chams.Page.COLORS));
   public final Setting<Color> lineColor = this.add(
      new Setting<>("LineColor", new Color(255, 255, 255), v -> this.page.getValue() == Chams.Page.COLORS).injectBoolean(false)
   );
   public final Setting<Color> modelColor = this.add(
      new Setting<>("ModelColor", new Color(125, 125, 213, 150), v -> this.page.getValue() == Chams.Page.COLORS).injectBoolean(false)
   );
   private final Setting<Boolean> hide = this.add(new Setting<>("Hide", false, v -> this.page.getValue() == Chams.Page.GLOBAL).setParent());
   private final Setting<Float> range = this.add(
      new Setting<>("Range", 1.5F, 1.0F, 12.0F, v -> this.hide.isOpen() && this.page.getValue() == Chams.Page.GLOBAL)
   );

   public Chams() {
      super("Chams", "Draws a pretty ESP around other players", Category.RENDER);
      INSTANCE = this;
   }

   @Override
   public String getInfo() {
      String info = null;
      if (this.fill.getValue()) {
         info = "Fill";
      } else if (this.wireframe.getValue()) {
         info = "Wireframe";
      }

      if (this.wireframe.getValue() && this.fill.getValue()) {
         info = "Both";
      }

      return info;
   }

   @SubscribeEvent
   public void onRenderPlayerEvent(net.minecraftforge.client.event.RenderPlayerEvent.Pre event) {
      event.getEntityPlayer().hurtTime = 0;
   }

   @SubscribeEvent
   public void onRenderLiving(Pre<EntityLivingBase> event) {
      if (event.getEntity() instanceof EntityPlayer && event.getEntity() != mc.player && this.hide.getValue()) {
         double dist = (double)event.getEntity().getDistance(mc.player);
         if (dist < (double)this.range.getValue().floatValue()) {
            event.setCanceled(true);
         }
      }
   }

   public static enum Model {
      XQZ,
      VANILLA,
      OFF;
   }

   public static enum Page {
      COLORS,
      GLOBAL;
   }
}
