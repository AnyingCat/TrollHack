//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "G:\PortableSoft\JBY\MC_Deobf3000\1.12-MCP-Mappings"!

package me.rebirthclient.mod.modules.impl.player;

import com.mojang.realmsclient.gui.ChatFormatting;
import java.awt.Color;
import me.rebirthclient.api.events.impl.PacketEvent;
import me.rebirthclient.api.events.impl.Render3DEvent;
import me.rebirthclient.api.util.CopyOfPlayer;
import me.rebirthclient.api.util.EntityUtil;
import me.rebirthclient.api.util.render.ColorUtil;
import me.rebirthclient.api.util.render.RenderUtil;
import me.rebirthclient.api.util.render.entity.StaticModelPlayer;
import me.rebirthclient.mod.modules.Category;
import me.rebirthclient.mod.modules.Module;
import me.rebirthclient.mod.modules.impl.exploit.BlockClip;
import me.rebirthclient.mod.modules.impl.exploit.Clip;
import me.rebirthclient.mod.modules.settings.Setting;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.play.server.SPacketPlayerPosLook;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;

public class FlagDetect extends Module {
   private final Setting<Boolean> notify = this.add(new Setting<>("ChatNotify", true));
   private final Setting<Boolean> chams = this.add(new Setting<>("Chams", true).setParent());
   private final Setting<Integer> fadeTime = this.add(new Setting<>("FadeTime", 15, 1, 50, v -> this.chams.isOpen()));
   private final Setting<Color> color = this.add(new Setting<>("Color", new Color(190, 0, 0, 100), v -> this.chams.isOpen()));
   private final Setting<Color> lineColor = this.add(new Setting<>("LineColor", new Color(255, 255, 255, 120), v -> this.chams.isOpen()).injectBoolean(false));
   private CopyOfPlayer player;

   public FlagDetect() {
      super("FlagDetect", "Detects & notifies you when your player is being flagged", Category.PLAYER);
   }

   @SubscribeEvent
   public void onPacket(PacketEvent event) {
      if (!fullNullCheck() && spawnCheck() && !Clip.INSTANCE.isOn() && !BlockClip.INSTANCE.isOn() && event.getPacket() instanceof SPacketPlayerPosLook) {
         if (this.notify.getValue()) {
            this.sendMessageWithID(ChatFormatting.RED + "Server lagged you back!", -123);
         }

         if (this.chams.getValue()) {
            this.player = new CopyOfPlayer(
               EntityUtil.getCopiedPlayer(mc.player),
               System.currentTimeMillis(),
               mc.player.posX,
               mc.player.posY,
               mc.player.posZ,
               mc.player.getSkinType().equals("slim")
            );
         }
      }
   }

   @Override
   public void onRender3D(Render3DEvent event) {
      if (!fullNullCheck() && this.chams.getValue() && this.player != null) {
         EntityPlayer player = this.player.getPlayer();
         StaticModelPlayer model = this.player.getModel();
         double x = this.player.getX() - mc.getRenderManager().viewerPosX;
         double y = this.player.getY() - mc.getRenderManager().viewerPosY;
         double z = this.player.getZ() - mc.getRenderManager().viewerPosZ;
         GL11.glPushMatrix();
         GL11.glPushAttrib(1048575);
         GL11.glDisable(3553);
         GL11.glDisable(2896);
         GL11.glDisable(2929);
         GL11.glEnable(2848);
         GL11.glEnable(3042);
         GlStateManager.blendFunc(770, 771);
         GlStateManager.translate(x, y, z);
         GlStateManager.rotate(180.0F - model.getYaw(), 0.0F, 1.0F, 0.0F);
         Color boxColor = this.color.getValue();
         Color outlineColor = this.lineColor.booleanValue ? this.lineColor.getValue() : this.color.getValue();
         float maxBoxAlpha = (float)boxColor.getAlpha();
         float maxOutlineAlpha = (float)outlineColor.getAlpha();
         float alphaBoxAmount = maxBoxAlpha / (float)(this.fadeTime.getValue() * 100);
         float alphaOutlineAmount = maxOutlineAlpha / (float)(this.fadeTime.getValue() * 100);
         int fadeBoxAlpha = MathHelper.clamp(
            (int)(alphaBoxAmount * (float)(this.player.getTime() + (long)(this.fadeTime.getValue() * 100) - System.currentTimeMillis())), 0, (int)maxBoxAlpha
         );
         int fadeOutlineAlpha = MathHelper.clamp(
            (int)(alphaOutlineAmount * (float)(this.player.getTime() + (long)(this.fadeTime.getValue() * 100) - System.currentTimeMillis())),
            0,
            (int)maxOutlineAlpha
         );
         Color box = ColorUtil.injectAlpha(boxColor, fadeBoxAlpha);
         Color line = ColorUtil.injectAlpha(outlineColor, fadeOutlineAlpha);
         GlStateManager.enableRescaleNormal();
         GlStateManager.scale(-1.0F, -1.0F, 1.0F);
         double widthX = player.getEntityBoundingBox().maxX - player.getRenderBoundingBox().minX + 1.0;
         double widthZ = player.getEntityBoundingBox().maxZ - player.getEntityBoundingBox().minZ + 1.0;
         GlStateManager.scale(widthX, (double)player.height, widthZ);
         GlStateManager.translate(0.0F, -1.501F, 0.0F);
         RenderUtil.glColor(box);
         GL11.glPolygonMode(1032, 6914);
         model.render(0.0625F);
         RenderUtil.glColor(line);
         GL11.glLineWidth(0.8F);
         GL11.glPolygonMode(1032, 6913);
         model.render(0.0625F);
         GL11.glPopAttrib();
         GL11.glPopMatrix();
         if (this.player.getTime() + (long)(this.fadeTime.getValue() * 100) < System.currentTimeMillis()) {
            this.player = null;
         }
      }
   }
}
