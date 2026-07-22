//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "G:\PortableSoft\JBY\MC_Deobf3000\1.12-MCP-Mappings"!

package me.rebirthclient.mod.modules.impl.render;

import java.awt.Color;
import java.util.Objects;
import me.rebirthclient.api.events.impl.Render3DEvent;
import me.rebirthclient.api.managers.Managers;
import me.rebirthclient.api.util.DamageUtil;
import me.rebirthclient.api.util.EntityUtil;
import me.rebirthclient.api.util.render.ColorUtil;
import me.rebirthclient.api.util.render.TextUtil;
import me.rebirthclient.mod.modules.Category;
import me.rebirthclient.mod.modules.Module;
import me.rebirthclient.mod.modules.settings.Setting;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemTool;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.text.TextFormatting;
import org.lwjgl.opengl.GL11;

public class NameTags extends Module {
   public static NameTags INSTANCE = new NameTags();
   public final Setting<Color> max = this.add(new Setting<>("Max", new Color(255, 255, 255)).injectBoolean(true).setParent());
   private final Setting<Boolean> noMaxText = this.add(new Setting<>("NoMaxText", true, v -> this.max.isOpen()));
   public final Setting<Color> color = this.add(new Setting<>("Color", new Color(255, 255, 255)));
   public final Setting<Color> outLine = this.add(new Setting<>("outLine", new Color(255, 255, 255)).injectBoolean(false).setParent());
   public final Setting<Color> friend = this.add(new Setting<>("Friend", new Color(155, 155, 255)).injectBoolean(true));
   public final Setting<Color> invisible = this.add(new Setting<>("Invisible", new Color(200, 200, 200)).injectBoolean(true));
   public final Setting<Color> sneak = this.add(new Setting<>("Sneaking", new Color(200, 200, 0)).injectBoolean(true));
   public final Setting<Color> rect = this.add(new Setting<>("Rectangle", new Color(0, 0, 0, 100)).injectBoolean(true));
   private final Setting<Boolean> armor = this.add(new Setting<>("Armor", true).setParent());
   private final Setting<Boolean> reversed = this.add(new Setting<>("ArmorReversed", false, v -> this.armor.isOpen()));
   private final Setting<Boolean> health = this.add(new Setting<>("Health", true));
   private final Setting<Boolean> ping = this.add(new Setting<>("Ping", true));
   private final Setting<Boolean> gamemode = this.add(new Setting<>("Gamemode", true));
   private final Setting<Boolean> entityID = this.add(new Setting<>("EntityID", false));
   private final Setting<Boolean> heldStackName = this.add(new Setting<>("StackName", false));
   private final Setting<Float> size = this.add(new Setting<>("Size", 4.5F, 0.1F, 15.0F));
   private final Setting<Boolean> scale = this.add(new Setting<>("Scale", true).setParent());
   private final Setting<Boolean> smartScale = this.add(new Setting<>("SmartScale", true, v -> this.scale.isOpen()));
   private final Setting<Float> factor = this.add(new Setting<>("Factor", 0.3F, 0.1F, 1.0F, v -> this.scale.isOpen()));
   private final Setting<Float> outLineWidth = this.add(new Setting<>("Width", 1.3F, 0.0F, 5.0F, v -> this.outLine.isOpen()));

   public NameTags() {
      super("NameTags", "Renders info about the player on a NameTag", Category.RENDER);
      INSTANCE = this;
   }

   @Override
   public void onRender3D(Render3DEvent event) {
      if (!fullNullCheck()) {
         for(EntityPlayer player : mc.world.playerEntities) {
            if (player != null && !player.equals(mc.player) && player.isEntityAlive() && (!player.isInvisible() || this.invisible.booleanValue)) {
               double x = this.interpolate(player.lastTickPosX, player.posX, event.getPartialTicks()) - mc.getRenderManager().renderPosX;
               double y = this.interpolate(player.lastTickPosY, player.posY, event.getPartialTicks()) - mc.getRenderManager().renderPosY;
               double z = this.interpolate(player.lastTickPosZ, player.posZ, event.getPartialTicks()) - mc.getRenderManager().renderPosZ;
               this.renderNameTag(player, x, y, z, event.getPartialTicks());
            }
         }
      }
   }

   private void renderNameTag(EntityPlayer player, double x, double y, double z, float delta) {
      double tempY = y + (player.isSneaking() ? 0.5 : 0.7);
      Entity camera = mc.getRenderViewEntity();

      assert camera != null;

      double originalPositionX = camera.posX;
      double originalPositionY = camera.posY;
      double originalPositionZ = camera.posZ;
      camera.posX = this.interpolate(camera.prevPosX, camera.posX, delta);
      camera.posY = this.interpolate(camera.prevPosY, camera.posY, delta);
      camera.posZ = this.interpolate(camera.prevPosZ, camera.posZ, delta);
      String displayTag = this.getDisplayTag(player);
      double distance = camera.getDistance(
         x + mc.getRenderManager().viewerPosX, y + mc.getRenderManager().viewerPosY, z + mc.getRenderManager().viewerPosZ
      );
      int width = Managers.TEXT.getMCStringWidth(displayTag) / 2;
      double scale = (0.0018 + (double)this.size.getValue().floatValue() * distance * (double)this.factor.getValue().floatValue()) / 1000.0;
      if (distance <= 6.0 && this.smartScale.getValue()) {
         scale = (0.0018 + (double)(this.size.getValue() + 2.0F) * distance * (double)this.factor.getValue().floatValue()) / 1000.0;
      }

      if (distance <= 4.0 && this.smartScale.getValue()) {
         scale = (0.0018 + (double)(this.size.getValue() + 4.0F) * distance * (double)this.factor.getValue().floatValue()) / 1000.0;
      }

      if (!this.scale.getValue()) {
         scale = (double)this.size.getValue().floatValue() / 100.0;
      }

      GlStateManager.pushMatrix();
      RenderHelper.enableStandardItemLighting();
      GlStateManager.enablePolygonOffset();
      GlStateManager.doPolygonOffset(1.0F, -1500000.0F);
      GlStateManager.disableLighting();
      GlStateManager.translate((float)x, (float)tempY + 1.4F, (float)z);
      GlStateManager.rotate(-mc.getRenderManager().playerViewY, 0.0F, 1.0F, 0.0F);
      float var10001 = mc.gameSettings.thirdPersonView == 2 ? -1.0F : 1.0F;
      GlStateManager.rotate(mc.getRenderManager().playerViewX, var10001, 0.0F, 0.0F);
      GlStateManager.scale(-scale, -scale, scale);
      GlStateManager.disableDepth();
      GlStateManager.enableBlend();
      GlStateManager.enableBlend();
      if (this.rect.booleanValue) {
         this.drawRect((float)(-width - 2), (float)(-(mc.fontRenderer.FONT_HEIGHT + 1)), (float)width + 2.0F, 1.5F, this.rect.getValue().getRGB());
      } else if (!this.outLine.booleanValue) {
         this.drawRect(0.0F, 0.0F, 0.0F, 0.0F, this.rect.getValue().getRGB());
      }

      if (this.outLine.booleanValue) {
         this.drawOutlineRect((float)(-width - 2), (float)(-(mc.fontRenderer.FONT_HEIGHT + 1)), (float)width + 2.0F, 1.5F, this.getOutlineColor());
      }

      GlStateManager.disableBlend();
      ItemStack renderMainHand = player.getHeldItemMainhand().copy();
      if (renderMainHand.hasEffect() && (renderMainHand.getItem() instanceof ItemTool || renderMainHand.getItem() instanceof ItemArmor)) {
         renderMainHand.stackSize = 1;
      }

      if (this.heldStackName.getValue() && !renderMainHand.isEmpty && renderMainHand.getItem() != Items.AIR) {
         String stackName = renderMainHand.getDisplayName();
         int stackNameWidth = Managers.TEXT.getMCStringWidth(stackName) / 2;
         GL11.glPushMatrix();
         GL11.glScalef(0.75F, 0.75F, 0.0F);
         Managers.TEXT.drawMCString(stackName, (float)(-stackNameWidth), -(this.getBiggestArmorTag(player) + 20.0F), -1, true);
         GL11.glScalef(1.5F, 1.5F, 1.0F);
         GL11.glPopMatrix();
      }

      if (this.armor.getValue()) {
         GlStateManager.pushMatrix();
         int xOffset = -6;

         for(ItemStack armourStack : player.inventory.armorInventory) {
            if (armourStack != null) {
               xOffset -= 8;
            }
         }

         xOffset -= 8;
         ItemStack renderOffhand = player.getHeldItemOffhand().copy();
         if (renderOffhand.hasEffect() && (renderOffhand.getItem() instanceof ItemTool || renderOffhand.getItem() instanceof ItemArmor)) {
            renderOffhand.stackSize = 1;
         }

         this.renderItemStack(renderOffhand, xOffset);
         xOffset += 16;
         if (this.reversed.getValue()) {
            for(int index = 0; index <= 3; ++index) {
               ItemStack armourStack = (ItemStack)player.inventory.armorInventory.get(index);
               if (armourStack.getItem() != Items.AIR) {
                  armourStack.copy();
                  this.renderItemStack(armourStack, xOffset);
                  xOffset += 16;
               }
            }
         } else {
            for(int index = 3; index >= 0; --index) {
               ItemStack armourStack = (ItemStack)player.inventory.armorInventory.get(index);
               if (armourStack.getItem() != Items.AIR) {
                  armourStack.copy();
                  this.renderItemStack(armourStack, xOffset);
                  xOffset += 16;
               }
            }
         }

         this.renderItemStack(renderMainHand, xOffset);
         GlStateManager.popMatrix();
      }

      Managers.TEXT.drawMCString(displayTag, (float)(-width), (float)(-(mc.fontRenderer.FONT_HEIGHT - 1)), this.getDisplayColor(player), true);
      camera.posX = originalPositionX;
      camera.posY = originalPositionY;
      camera.posZ = originalPositionZ;
      GlStateManager.enableDepth();
      GlStateManager.disableBlend();
      GlStateManager.disablePolygonOffset();
      GlStateManager.doPolygonOffset(1.0F, 1500000.0F);
      GlStateManager.popMatrix();
   }

   private int getDisplayColor(EntityPlayer player) {
      int displaycolor = ColorUtil.toRGBA(this.color.getValue());
      if (Managers.FRIENDS.isFriend(player) && this.friend.booleanValue) {
         return ColorUtil.toRGBA(this.friend.getValue());
      } else {
         if (player.isInvisible() && this.invisible.booleanValue) {
            displaycolor = ColorUtil.toRGBA(this.invisible.getValue());
         } else if (player.isSneaking() && this.sneak.booleanValue) {
            displaycolor = ColorUtil.toRGBA(this.sneak.getValue());
         }

         return displaycolor;
      }
   }

   private int getOutlineColor() {
      return ColorUtil.toRGBA(this.outLine.getValue());
   }

   private void renderItemStack(ItemStack stack, int x) {
      GlStateManager.pushMatrix();
      GlStateManager.depthMask(true);
      GlStateManager.clear(256);
      RenderHelper.enableStandardItemLighting();
      mc.getRenderItem().zLevel = -150.0F;
      GlStateManager.disableAlpha();
      GlStateManager.enableDepth();
      GlStateManager.disableCull();
      mc.getRenderItem().renderItemAndEffectIntoGUI(stack, x, -26);
      mc.getRenderItem().renderItemOverlays(mc.fontRenderer, stack, x, -26);
      mc.getRenderItem().zLevel = 0.0F;
      RenderHelper.disableStandardItemLighting();
      GlStateManager.enableCull();
      GlStateManager.enableAlpha();
      GlStateManager.scale(0.5F, 0.5F, 0.5F);
      GlStateManager.disableDepth();
      this.renderEnchantmentText(stack, x);
      GlStateManager.enableDepth();
      GlStateManager.scale(2.0F, 2.0F, 2.0F);
      GlStateManager.popMatrix();
   }

   private void renderEnchantmentText(ItemStack stack, int x) {
      int enchantmentY = -34;
      if (stack.getItem() == Items.GOLDEN_APPLE && stack.hasEffect()) {
         Managers.TEXT.drawMCString("god", (float)(x * 2), (float)enchantmentY, -3977919, true);
         enchantmentY -= 8;
      }

      NBTTagList enchants;
      if ((enchants = stack.getEnchantmentTagList()).tagCount() > 2 && this.max.booleanValue) {
         if (this.noMaxText.getValue()) {
            Managers.TEXT.drawMCString("", (float)(x * 2), (float)enchantmentY, ColorUtil.toRGBA(this.max.getValue()), true);
         } else {
            Managers.TEXT.drawMCString("max", (float)(x * 2), (float)enchantmentY, ColorUtil.toRGBA(this.max.getValue()), true);
         }

         enchantmentY -= 8;
      } else {
         for(int index = 0; index < enchants.tagCount(); ++index) {
            short id = enchants.getCompoundTagAt(index).getShort("id");
            short level = enchants.getCompoundTagAt(index).getShort("lvl");
            Enchantment enc = Enchantment.getEnchantmentByID(id);
            if (enc != null) {
               String encName = enc.isCurse()
                  ? TextFormatting.RED + enc.getTranslatedName(level).substring(0, 4).toLowerCase()
                  : enc.getTranslatedName(level).substring(0, 2).toLowerCase();
               encName = encName + level;
               Managers.TEXT.drawMCString(encName, (float)(x * 2), (float)enchantmentY, -1, true);
               enchantmentY -= 8;
            }
         }
      }

      if (DamageUtil.hasDurability(stack)) {
         float green = ((float)stack.getMaxDamage() - (float)stack.getItemDamage()) / (float)stack.getMaxDamage();
         float red = 1.0F - green;
         int dmg = 100 - (int)(red * 100.0F);
         String color = dmg >= 60 ? TextUtil.GREEN : (dmg >= 25 ? TextUtil.YELLOW : TextUtil.RED);
         Managers.TEXT.drawMCString(color + dmg + "%", (float)(x * 2), (float)enchantmentY, -1, true);
      }
   }

   private float getBiggestArmorTag(EntityPlayer player) {
      float enchantmentY = 0.0F;
      boolean arm = false;

      for(ItemStack stack : player.inventory.armorInventory) {
         float encY = 0.0F;
         if (stack != null) {
            NBTTagList enchants = stack.getEnchantmentTagList();

            for(int index = 0; index < enchants.tagCount(); ++index) {
               short id = enchants.getCompoundTagAt(index).getShort("id");
               Enchantment enc = Enchantment.getEnchantmentByID(id);
               if (enc != null) {
                  encY += 8.0F;
                  arm = true;
               }
            }
         }

         if (encY > enchantmentY) {
            enchantmentY = encY;
         }
      }

      ItemStack renderMainHand = player.getHeldItemMainhand().copy();
      if (renderMainHand.hasEffect()) {
         float encY = 0.0F;
         NBTTagList enchants = renderMainHand.getEnchantmentTagList();

         for(int index2 = 0; index2 < enchants.tagCount(); ++index2) {
            short id = enchants.getCompoundTagAt(index2).getShort("id");
            Enchantment enc2 = Enchantment.getEnchantmentByID(id);
            if (enc2 != null) {
               encY += 8.0F;
               arm = true;
            }
         }

         if (encY > enchantmentY) {
            enchantmentY = encY;
         }
      }

      ItemStack renderOffHand;
      if ((renderOffHand = player.getHeldItemOffhand().copy()).hasEffect()) {
         float encY = 0.0F;
         NBTTagList enchants = renderOffHand.getEnchantmentTagList();

         for(int index = 0; index < enchants.tagCount(); ++index) {
            short id = enchants.getCompoundTagAt(index).getShort("id");
            Enchantment enc = Enchantment.getEnchantmentByID(id);
            if (enc != null) {
               encY += 8.0F;
               arm = true;
            }
         }

         if (encY > enchantmentY) {
            enchantmentY = encY;
         }
      }

      return (float)(arm ? 0 : 20) + enchantmentY;
   }

   private String getDisplayTag(EntityPlayer player) {
      String name = player.getDisplayName().getFormattedText();
      if (name.contains(mc.getSession().getUsername())) {
         name = "You";
      }

      float health = EntityUtil.getHealth(player);
      String color = health > 18.0F
         ? TextUtil.GREEN
         : (health > 16.0F ? TextUtil.DARK_GREEN : (health > 12.0F ? TextUtil.YELLOW : (health > 8.0F ? TextUtil.RED : TextUtil.DARK_RED)));
      String pingStr = "";
      if (this.ping.getValue()) {
         try {
            int responseTime = ((NetHandlerPlayClient)Objects.requireNonNull(mc.getConnection())).getPlayerInfo(player.getUniqueID()).getResponseTime();
            pingStr = pingStr + responseTime + "ms ";
         } catch (Exception var8) {
         }
      }

      String idString = "";
      if (this.entityID.getValue()) {
         idString = idString + "ID: " + player.getEntityId() + " ";
      }

      String gameModeStr = "";
      if (this.gamemode.getValue()) {
         gameModeStr = player.isCreative()
            ? gameModeStr + "[C] "
            : (!player.isSpectator() && !player.isInvisible() ? gameModeStr + "[S] " : gameModeStr + "[I] ");
      }

      if (this.health.getValue()) {
         name = Math.floor((double)health) == (double)health
            ? name + color + " " + (health > 0.0F ? (int)Math.floor((double)health) : "dead")
            : name + color + " " + (health > 0.0F ? (int)health : "dead");
      }

      return " " + pingStr + idString + gameModeStr + name + " ";
   }

   private double interpolate(double previous, double current, float delta) {
      return previous + (current - previous) * (double)delta;
   }

   public void drawOutlineRect(float x, float y, float w, float h, int color) {
      float alpha = (float)(color >> 24 & 0xFF) / 255.0F;
      float red = (float)(color >> 16 & 0xFF) / 255.0F;
      float green = (float)(color >> 8 & 0xFF) / 255.0F;
      float blue = (float)(color & 0xFF) / 255.0F;
      Tessellator tessellator = Tessellator.getInstance();
      BufferBuilder bufferbuilder = tessellator.getBuffer();
      GlStateManager.enableBlend();
      GlStateManager.disableTexture2D();
      GlStateManager.glLineWidth(this.outLineWidth.getValue());
      GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
      bufferbuilder.begin(2, DefaultVertexFormats.POSITION_COLOR);
      bufferbuilder.pos((double)x, (double)h, 0.0).color(red, green, blue, alpha).endVertex();
      bufferbuilder.pos((double)w, (double)h, 0.0).color(red, green, blue, alpha).endVertex();
      bufferbuilder.pos((double)w, (double)y, 0.0).color(red, green, blue, alpha).endVertex();
      bufferbuilder.pos((double)x, (double)y, 0.0).color(red, green, blue, alpha).endVertex();
      tessellator.draw();
      GlStateManager.enableTexture2D();
      GlStateManager.disableBlend();
   }

   public void drawRect(float x, float y, float w, float h, int color) {
      float alpha = (float)(color >> 24 & 0xFF) / 255.0F;
      float red = (float)(color >> 16 & 0xFF) / 255.0F;
      float green = (float)(color >> 8 & 0xFF) / 255.0F;
      float blue = (float)(color & 0xFF) / 255.0F;
      Tessellator tessellator = Tessellator.getInstance();
      BufferBuilder bufferbuilder = tessellator.getBuffer();
      GlStateManager.enableBlend();
      GlStateManager.disableTexture2D();
      GlStateManager.glLineWidth(this.outLineWidth.getValue());
      GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
      bufferbuilder.begin(7, DefaultVertexFormats.POSITION_COLOR);
      bufferbuilder.pos((double)x, (double)h, 0.0).color(red, green, blue, alpha).endVertex();
      bufferbuilder.pos((double)w, (double)h, 0.0).color(red, green, blue, alpha).endVertex();
      bufferbuilder.pos((double)w, (double)y, 0.0).color(red, green, blue, alpha).endVertex();
      bufferbuilder.pos((double)x, (double)y, 0.0).color(red, green, blue, alpha).endVertex();
      tessellator.draw();
      GlStateManager.enableTexture2D();
      GlStateManager.disableBlend();
   }
}
