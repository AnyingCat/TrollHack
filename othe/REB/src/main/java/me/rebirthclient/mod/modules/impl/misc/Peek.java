//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "G:\PortableSoft\JBY\MC_Deobf3000\1.12-MCP-Mappings"!

package me.rebirthclient.mod.modules.impl.misc;

import java.awt.Color;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import me.rebirthclient.api.events.impl.Render2DEvent;
import me.rebirthclient.api.managers.Managers;
import me.rebirthclient.api.util.Timer;
import me.rebirthclient.api.util.render.ColorUtil;
import me.rebirthclient.mod.modules.Category;
import me.rebirthclient.mod.modules.Module;
import me.rebirthclient.mod.modules.impl.client.ClickGui;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemShulkerBox;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class Peek extends Module {
   private final Map<EntityPlayer, ItemStack> spiedPlayers = new ConcurrentHashMap();
   private final Map<EntityPlayer, Timer> playerTimers = new ConcurrentHashMap<>();

   public Peek() {
      super("Peek", "Allows you to peek into your enemy's shulkerboxes", Category.MISC);
   }

   @Override
   public void onUpdate() {
      for(EntityPlayer player : mc.world.playerEntities) {
         if (player != null && player.getHeldItemMainhand().getItem() instanceof ItemShulkerBox && mc.player != player) {
            ItemStack stack = player.getHeldItemMainhand();
            this.spiedPlayers.put(player, stack);
         }
      }
   }

   @Override
   public void onRender2D(Render2DEvent event) {
      if (!fullNullCheck()) {
         int x = Managers.TEXT.scaledWidth / 2 - 78;
         int y = 24;

         for(EntityPlayer player : mc.world.playerEntities) {
            if (this.spiedPlayers.get(player) != null) {
               Timer playerTimer;
               if (player.getHeldItemMainhand() == null || !(player.getHeldItemMainhand().getItem() instanceof ItemShulkerBox)) {
                  playerTimer = this.playerTimers.get(player);
                  if (playerTimer == null) {
                     Timer timer = new Timer();
                     timer.reset();
                     this.playerTimers.put(player, timer);
                  } else if (playerTimer.passedS(3.0)) {
                     continue;
                  }
               } else if (player.getHeldItemMainhand().getItem() instanceof ItemShulkerBox && (playerTimer = this.playerTimers.get(player)) != null) {
                  playerTimer.reset();
                  this.playerTimers.put(player, playerTimer);
               }

               ItemStack stack = (ItemStack)this.spiedPlayers.get(player);
               this.renderShulkerToolTip(stack, x, y, player.getName());
            }
         }
      }
   }

   @SubscribeEvent(
      priority = EventPriority.HIGHEST
   )
   public void makeTooltip(ItemTooltipEvent event) {
   }

   private void renderShulkerToolTip(ItemStack stack, int x, int y, String name) {
      NBTTagCompound tagCompound = stack.getTagCompound();
      NBTTagCompound blockEntityTag;
      if (tagCompound != null
         && tagCompound.hasKey("BlockEntityTag", 10)
         && (blockEntityTag = tagCompound.getCompoundTag("BlockEntityTag")).hasKey("Items", 9)) {
         GlStateManager.enableTexture2D();
         GlStateManager.disableLighting();
         GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
         GlStateManager.enableBlend();
         GlStateManager.tryBlendFuncSeparate(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA, SourceFactor.ONE, DestFactor.ZERO);
         mc.getTextureManager().bindTexture(new ResourceLocation("textures/rebirth/constant/ingame/container.png"));
         this.drawTexturedRect(x, y, 0, 16);
         this.drawTexturedRect(x, y + 16, 16, 57);
         this.drawTexturedRect(x, y + 16 + 54, 160, 8);
         GlStateManager.disableDepth();
         Color color = new Color(
            ClickGui.INSTANCE.color.getValue().getRed(), ClickGui.INSTANCE.color.getValue().getGreen(), ClickGui.INSTANCE.color.getValue().getBlue(), 200
         );
         Managers.TEXT.drawStringWithShadow(name == null ? stack.getDisplayName() : name, (float)(x + 8), (float)(y + 6), ColorUtil.toRGBA(color));
         GlStateManager.enableDepth();
         RenderHelper.enableGUIStandardItemLighting();
         GlStateManager.enableRescaleNormal();
         GlStateManager.enableColorMaterial();
         GlStateManager.enableLighting();
         NonNullList nonnulllist = NonNullList.withSize(27, ItemStack.EMPTY);
         ItemStackHelper.loadAllItems(blockEntityTag, nonnulllist);

         for(int i = 0; i < nonnulllist.size(); ++i) {
            int iX = x + i % 9 * 18 + 8;
            int iY = y + i / 9 * 18 + 18;
            ItemStack itemStack = (ItemStack)nonnulllist.get(i);
            mc.getItemRenderer().itemRenderer.zLevel = 501.0F;
            mc.getRenderItem().renderItemAndEffectIntoGUI(itemStack, iX, iY);
            mc.getRenderItem().renderItemOverlayIntoGUI(mc.fontRenderer, itemStack, iX, iY, null);
            mc.getItemRenderer().itemRenderer.zLevel = 0.0F;
         }

         GlStateManager.disableLighting();
         GlStateManager.disableBlend();
         GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
      }
   }

   private void drawTexturedRect(int x, int y, int textureY, int height) {
      Tessellator tessellator = Tessellator.getInstance();
      BufferBuilder BufferBuilder2 = tessellator.getBuffer();
      BufferBuilder2.begin(7, DefaultVertexFormats.POSITION_TEX);
      BufferBuilder2.pos((double)x, (double)(y + height), 500.0)
         .tex(0.0, (double)((float)(textureY + height) * 0.00390625F))
         .endVertex();
      BufferBuilder2.pos((double)(x + 176), (double)(y + height), 500.0)
         .tex(0.6875, (double)((float)(textureY + height) * 0.00390625F))
         .endVertex();
      BufferBuilder2.pos((double)(x + 176), (double)y, 500.0).tex(0.6875, (double)((float)textureY * 0.00390625F)).endVertex();
      BufferBuilder2.pos((double)x, (double)y, 500.0).tex(0.0, (double)((float)textureY * 0.00390625F)).endVertex();
      tessellator.draw();
   }
}
