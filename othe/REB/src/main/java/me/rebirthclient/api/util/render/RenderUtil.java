//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "G:\PortableSoft\JBY\MC_Deobf3000\1.12-MCP-Mappings"!

package me.rebirthclient.api.util.render;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Arrays;
import java.util.Objects;
import javax.imageio.ImageIO;
import me.rebirthclient.api.managers.Managers;
import me.rebirthclient.api.util.BlockUtil;
import me.rebirthclient.api.util.EntityUtil;
import me.rebirthclient.api.util.InterpolationUtil;
import me.rebirthclient.api.util.Wrapper;
import me.rebirthclient.mod.modules.impl.render.RenderSetting;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.GLU;

public class RenderUtil implements Wrapper {
   public static final ICamera camera = new Frustum();
   private static final FloatBuffer screenCoords = BufferUtils.createFloatBuffer(3);
   private static final IntBuffer viewport = BufferUtils.createIntBuffer(16);
   private static final FloatBuffer modelView = BufferUtils.createFloatBuffer(16);
   private static final FloatBuffer projection = BufferUtils.createFloatBuffer(16);

   public static void drawCircle(float x, float y, float z, float radius, Color color) {
      AxisAlignedBB bb = new AxisAlignedBB(
         (double)x - mc.getRenderManager().viewerPosX,
         (double)y - mc.getRenderManager().viewerPosY,
         (double)z - mc.getRenderManager().viewerPosZ,
         (double)(x + 1.0F) - mc.getRenderManager().viewerPosX,
         (double)(y + 1.0F) - mc.getRenderManager().viewerPosY,
         (double)(z + 1.0F) - mc.getRenderManager().viewerPosZ
      );
      camera.setPosition(Objects.requireNonNull(mc.getRenderViewEntity()).posX, mc.getRenderViewEntity().posY, mc.getRenderViewEntity().posZ);
      if (camera.isBoundingBoxInFrustum(
         new AxisAlignedBB(
            bb.minX + mc.getRenderManager().viewerPosX,
            bb.minY + mc.getRenderManager().viewerPosY,
            bb.minZ + mc.getRenderManager().viewerPosZ,
            bb.maxX + mc.getRenderManager().viewerPosX,
            bb.maxY + mc.getRenderManager().viewerPosY,
            bb.maxZ + mc.getRenderManager().viewerPosZ
         )
      )) {
         drawCircleVertices(bb, radius, color);
      }
   }

   public static void drawSexyBoxPhobosIsRetardedFuckYouESP(
      AxisAlignedBB a,
      Color boxColor,
      Color outlineColor,
      float lineWidth,
      boolean outline,
      boolean box,
      boolean colorSync,
      float alpha,
      float scale,
      float slab
   ) {
      double f = 0.5 * (double)(1.0F - scale);
      AxisAlignedBB bb = interpolateAxis(
         new AxisAlignedBB(
            a.minX + f,
            a.minY + f + (double)(1.0F - slab),
            a.minZ + f,
            a.maxX - f,
            a.maxY - f,
            a.maxZ - f
         )
      );
      float rB = (float)boxColor.getRed() / 255.0F;
      float gB = (float)boxColor.getGreen() / 255.0F;
      float bB = (float)boxColor.getBlue() / 255.0F;
      float aB = (float)boxColor.getAlpha() / 255.0F;
      float rO = (float)outlineColor.getRed() / 255.0F;
      float gO = (float)outlineColor.getGreen() / 255.0F;
      float bO = (float)outlineColor.getBlue() / 255.0F;
      float aO = (float)outlineColor.getAlpha() / 255.0F;
      if (colorSync) {
         rB = (float)Managers.COLORS.getCurrent().getRed() / 255.0F;
         gB = (float)Managers.COLORS.getCurrent().getGreen() / 255.0F;
         bB = (float)Managers.COLORS.getCurrent().getBlue() / 255.0F;
         rO = (float)Managers.COLORS.getCurrent().getRed() / 255.0F;
         gO = (float)Managers.COLORS.getCurrent().getGreen() / 255.0F;
         bO = (float)Managers.COLORS.getCurrent().getBlue() / 255.0F;
      }

      if (alpha > 1.0F) {
         alpha = 1.0F;
      }

      aB *= alpha;
      aO *= alpha;
      if (box) {
         GlStateManager.pushMatrix();
         GlStateManager.enableBlend();
         GlStateManager.disableDepth();
         GlStateManager.tryBlendFuncSeparate(770, 771, 0, 1);
         GlStateManager.disableTexture2D();
         GlStateManager.depthMask(false);
         GL11.glEnable(2848);
         GL11.glHint(3154, 4354);
         RenderGlobal.renderFilledBox(bb, rB, gB, bB, aB);
         GL11.glDisable(2848);
         GlStateManager.depthMask(true);
         GlStateManager.enableDepth();
         GlStateManager.enableTexture2D();
         GlStateManager.disableBlend();
         GlStateManager.popMatrix();
      }

      if (outline) {
         GlStateManager.pushMatrix();
         GlStateManager.enableBlend();
         GlStateManager.disableDepth();
         GlStateManager.tryBlendFuncSeparate(770, 771, 0, 1);
         GlStateManager.disableTexture2D();
         GlStateManager.depthMask(false);
         GL11.glEnable(2848);
         GL11.glHint(3154, 4354);
         GL11.glLineWidth(lineWidth);
         Tessellator tessellator = Tessellator.getInstance();
         BufferBuilder bufferbuilder = tessellator.getBuffer();
         bufferbuilder.begin(3, DefaultVertexFormats.POSITION_COLOR);
         bufferbuilder.pos(bb.minX, bb.minY, bb.minZ).color(rO, gO, bO, aO).endVertex();
         bufferbuilder.pos(bb.minX, bb.minY, bb.maxZ).color(rO, gO, bO, aO).endVertex();
         bufferbuilder.pos(bb.maxX, bb.minY, bb.maxZ).color(rO, gO, bO, aO).endVertex();
         bufferbuilder.pos(bb.maxX, bb.minY, bb.minZ).color(rO, gO, bO, aO).endVertex();
         bufferbuilder.pos(bb.minX, bb.minY, bb.minZ).color(rO, gO, bO, aO).endVertex();
         bufferbuilder.pos(bb.minX, bb.maxY, bb.minZ).color(rO, gO, bO, aO).endVertex();
         bufferbuilder.pos(bb.minX, bb.maxY, bb.maxZ).color(rO, gO, bO, aO).endVertex();
         bufferbuilder.pos(bb.minX, bb.minY, bb.maxZ).color(rO, gO, bO, aO).endVertex();
         bufferbuilder.pos(bb.maxX, bb.minY, bb.maxZ).color(rO, gO, bO, aO).endVertex();
         bufferbuilder.pos(bb.maxX, bb.maxY, bb.maxZ).color(rO, gO, bO, aO).endVertex();
         bufferbuilder.pos(bb.minX, bb.maxY, bb.maxZ).color(rO, gO, bO, aO).endVertex();
         bufferbuilder.pos(bb.maxX, bb.maxY, bb.maxZ).color(rO, gO, bO, aO).endVertex();
         bufferbuilder.pos(bb.maxX, bb.maxY, bb.minZ).color(rO, gO, bO, aO).endVertex();
         bufferbuilder.pos(bb.maxX, bb.minY, bb.minZ).color(rO, gO, bO, aO).endVertex();
         bufferbuilder.pos(bb.maxX, bb.maxY, bb.minZ).color(rO, gO, bO, aO).endVertex();
         bufferbuilder.pos(bb.minX, bb.maxY, bb.minZ).color(rO, gO, bO, aO).endVertex();
         tessellator.draw();
         GL11.glDisable(2848);
         GlStateManager.depthMask(true);
         GlStateManager.enableDepth();
         GlStateManager.enableTexture2D();
         GlStateManager.disableBlend();
         GlStateManager.popMatrix();
      }
   }

   public static void drawOutlineRect(float x, float y, float w, float h, float lineWidth, int color) {
      float right = x + w;
      float bottom = y + h;
      float alpha = (float)(color >> 24 & 0xFF) / 255.0F;
      float red = (float)(color >> 16 & 0xFF) / 255.0F;
      float green = (float)(color >> 8 & 0xFF) / 255.0F;
      float blue = (float)(color & 0xFF) / 255.0F;
      Tessellator tessellator = Tessellator.getInstance();
      BufferBuilder bufferBuilder = tessellator.getBuffer();
      GlStateManager.enableBlend();
      GlStateManager.disableTexture2D();
      GlStateManager.tryBlendFuncSeparate(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA, SourceFactor.ONE, DestFactor.ZERO);
      GlStateManager.color(red, green, blue, alpha);
      GL11.glEnable(2848);
      GlStateManager.glLineWidth(lineWidth);
      bufferBuilder.begin(1, DefaultVertexFormats.POSITION);
      bufferBuilder.pos((double)x, (double)bottom, 0.0).endVertex();
      bufferBuilder.pos((double)right, (double)bottom, 0.0).endVertex();
      bufferBuilder.pos((double)right, (double)bottom, 0.0).endVertex();
      bufferBuilder.pos((double)right, (double)y, 0.0).endVertex();
      bufferBuilder.pos((double)right, (double)y, 0.0).endVertex();
      bufferBuilder.pos((double)x, (double)y, 0.0).endVertex();
      bufferBuilder.pos((double)x, (double)y, 0.0).endVertex();
      bufferBuilder.pos((double)x, (double)bottom, 0.0).endVertex();
      tessellator.draw();
      GlStateManager.enableTexture2D();
      GlStateManager.disableBlend();
   }

   public static void drawRect2(double left, double top, double right, double bottom, int color) {
      GlStateManager.pushMatrix();
      if (left < right) {
         double i = left;
         left = right;
         right = i;
      }

      if (top < bottom) {
         double j = top;
         top = bottom;
         bottom = j;
      }

      float f3 = (float)(color >> 24 & 0xFF) / 255.0F;
      float f = (float)(color >> 16 & 0xFF) / 255.0F;
      float f1 = (float)(color >> 8 & 0xFF) / 255.0F;
      float f2 = (float)(color & 0xFF) / 255.0F;
      Tessellator tessellator = Tessellator.getInstance();
      BufferBuilder bufferbuilder = tessellator.getBuffer();
      GlStateManager.enableBlend();
      GlStateManager.disableTexture2D();
      GlStateManager.tryBlendFuncSeparate(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA, SourceFactor.ONE, DestFactor.ZERO);
      GlStateManager.color(f, f1, f2, f3);
      bufferbuilder.begin(7, DefaultVertexFormats.POSITION);
      bufferbuilder.pos(left, bottom, 0.0).endVertex();
      bufferbuilder.pos(right, bottom, 0.0).endVertex();
      bufferbuilder.pos(right, top, 0.0).endVertex();
      bufferbuilder.pos(left, top, 0.0).endVertex();
      tessellator.draw();
      GlStateManager.enableTexture2D();
      GlStateManager.disableBlend();
      GlStateManager.popMatrix();
   }

   public static void glScissor(float x, float y, float x1, float y1, ScaledResolution sr) {
      GL11.glScissor(
         (int)(x * (float)sr.getScaleFactor()),
         (int)((float)mc.displayHeight - y1 * (float)sr.getScaleFactor()),
         (int)((x1 - x) * (float)sr.getScaleFactor()),
         (int)((y1 - y) * (float)sr.getScaleFactor())
      );
   }

   public static void drawCircleVertices(AxisAlignedBB bb, float radius, Color color) {
      float r = (float)color.getRed() / 255.0F;
      float g = (float)color.getGreen() / 255.0F;
      float b = (float)color.getBlue() / 255.0F;
      float a = (float)color.getAlpha() / 255.0F;
      Tessellator tessellator = Tessellator.getInstance();
      BufferBuilder buffer = tessellator.getBuffer();
      GlStateManager.pushMatrix();
      GlStateManager.enableBlend();
      GlStateManager.disableDepth();
      GlStateManager.tryBlendFuncSeparate(770, 771, 0, 1);
      GlStateManager.disableTexture2D();
      GlStateManager.depthMask(false);
      GL11.glEnable(2848);
      GL11.glHint(3154, 4354);
      GL11.glLineWidth(1.0F);

      for(int i = 0; i < 360; ++i) {
         buffer.begin(3, DefaultVertexFormats.POSITION_COLOR);
         buffer.pos(
               bb.getCenter().x + Math.sin((double)i * 3.1415926 / 180.0) * (double)radius,
               bb.minY,
               bb.getCenter().z + Math.cos((double)i * 3.1415926 / 180.0) * (double)radius
            )
            .color(r, g, b, a)
            .endVertex();
         buffer.pos(
               bb.getCenter().x + Math.sin((double)(i + 1) * 3.1415926 / 180.0) * (double)radius,
               bb.minY,
               bb.getCenter().z + Math.cos((double)(i + 1) * 3.1415926 / 180.0) * (double)radius
            )
            .color(r, g, b, a)
            .endVertex();
         tessellator.draw();
      }

      GL11.glDisable(2848);
      GlStateManager.depthMask(true);
      GlStateManager.enableDepth();
      GlStateManager.enableTexture2D();
      GlStateManager.disableBlend();
      GlStateManager.popMatrix();
   }

   public static void drawText(AxisAlignedBB axisAlignedBB, String text) {
      if (axisAlignedBB != null && text != null) {
         double x = axisAlignedBB.minX + (axisAlignedBB.maxX - axisAlignedBB.minX) / 2.0 - mc.getRenderManager().renderPosX;
         double y = axisAlignedBB.minY + (axisAlignedBB.maxY - axisAlignedBB.minY) / 2.0 - mc.getRenderManager().renderPosY - 1.5;
         double z = axisAlignedBB.minZ + (axisAlignedBB.maxZ - axisAlignedBB.minZ) / 2.0 - mc.getRenderManager().renderPosZ;
         drawText(text, x, y, z, new Color(255, 255, 255, 255));
      }
   }

   public static void drawText(AxisAlignedBB axisAlignedBB, String text, Color color) {
      if (axisAlignedBB != null && text != null) {
         double x = axisAlignedBB.minX + (axisAlignedBB.maxX - axisAlignedBB.minX) / 2.0 - mc.getRenderManager().renderPosX;
         double y = axisAlignedBB.minY + (axisAlignedBB.maxY - axisAlignedBB.minY) / 2.0 - mc.getRenderManager().renderPosY - 1.5;
         double z = axisAlignedBB.minZ + (axisAlignedBB.maxZ - axisAlignedBB.minZ) / 2.0 - mc.getRenderManager().renderPosZ;
         drawText(text, x, y, z, color);
      }
   }

   public static void drawText(String text, double x, double y, double z, Color color) {
      Entity camera = mc.getRenderViewEntity();
      if (camera != null) {
         double originalPositionX = camera.posX;
         double originalPositionY = camera.posY;
         double originalPositionZ = camera.posZ;
         camera.posX = camera.prevPosX;
         camera.posY = camera.prevPosY;
         camera.posZ = camera.prevPosZ;
         int width = Managers.TEXT.getMCStringWidth(text) / 2;
         double scale = 0.027999999999999997;
         GlStateManager.pushMatrix();
         RenderHelper.enableStandardItemLighting();
         GlStateManager.enablePolygonOffset();
         GlStateManager.doPolygonOffset(1.0F, -1500000.0F);
         GlStateManager.disableLighting();
         GlStateManager.translate((float)x, (float)y + 1.4F, (float)z);
         GlStateManager.rotate(-mc.getRenderManager().playerViewY, 0.0F, 1.0F, 0.0F);
         float var10001 = mc.gameSettings.thirdPersonView == 2 ? -1.0F : 1.0F;
         GlStateManager.rotate(mc.getRenderManager().playerViewX, var10001, 0.0F, 0.0F);
         GlStateManager.scale(-scale, -scale, scale);
         GlStateManager.disableDepth();
         Managers.TEXT.drawMCString(text, (float)(-width), (float)(-(Managers.TEXT.getFontHeight() - 1)), ColorUtil.toRGBA(color), true);
         GlStateManager.enableDepth();
         camera.posX = originalPositionX;
         camera.posY = originalPositionY;
         camera.posZ = originalPositionZ;
         GlStateManager.disablePolygonOffset();
         GlStateManager.doPolygonOffset(1.0F, 1500000.0F);
         GlStateManager.popMatrix();
      }
   }

   public static void drawBoxESP(
      BlockPos pos, Color color, boolean secondC, Color secondColor, float lineWidth, boolean outline, boolean box, int boxAlpha, boolean air
   ) {
      if (box) {
         drawBox(pos, new Color(color.getRed(), color.getGreen(), color.getBlue(), boxAlpha));
      }

      if (outline) {
         drawBlockOutline(pos, secondC ? secondColor : color, lineWidth, air);
      }
   }

   public static AxisAlignedBB interpolateAxis(AxisAlignedBB bb) {
      return new AxisAlignedBB(
         bb.minX - mc.getRenderManager().viewerPosX,
         bb.minY - mc.getRenderManager().viewerPosY,
         bb.minZ - mc.getRenderManager().viewerPosZ,
         bb.maxX - mc.getRenderManager().viewerPosX,
         bb.maxY - mc.getRenderManager().viewerPosY,
         bb.maxZ - mc.getRenderManager().viewerPosZ
      );
   }

   public static void drawRectangleCorrectly(int x, int y, int w, int h, int color) {
      GL11.glLineWidth(1.0F);
      Gui.drawRect(x, y, x + w, y + h, color);
   }

   public static void drawBBFill(AxisAlignedBB BB, Color color, int alpha) {
      AxisAlignedBB bb = new AxisAlignedBB(
         BB.minX - mc.getRenderManager().viewerPosX,
         BB.minY - mc.getRenderManager().viewerPosY,
         BB.minZ - mc.getRenderManager().viewerPosZ,
         BB.maxX - mc.getRenderManager().viewerPosX,
         BB.maxY - mc.getRenderManager().viewerPosY,
         BB.maxZ - mc.getRenderManager().viewerPosZ
      );
      camera.setPosition(Objects.requireNonNull(mc.getRenderViewEntity()).posX, mc.getRenderViewEntity().posY, mc.getRenderViewEntity().posZ);
      if (camera.isBoundingBoxInFrustum(
         new AxisAlignedBB(
            bb.minX + mc.getRenderManager().viewerPosX,
            bb.minY + mc.getRenderManager().viewerPosY,
            bb.minZ + mc.getRenderManager().viewerPosZ,
            bb.maxX + mc.getRenderManager().viewerPosX,
            bb.maxY + mc.getRenderManager().viewerPosY,
            bb.maxZ + mc.getRenderManager().viewerPosZ
         )
      )) {
         GlStateManager.pushMatrix();
         GlStateManager.enableBlend();
         GlStateManager.disableDepth();
         GlStateManager.tryBlendFuncSeparate(770, 771, 0, 1);
         GlStateManager.disableTexture2D();
         GlStateManager.depthMask(false);
         GL11.glEnable(2848);
         GL11.glHint(3154, 4354);
         RenderGlobal.renderFilledBox(
            bb, (float)color.getRed() / 255.0F, (float)color.getGreen() / 255.0F, (float)color.getBlue() / 255.0F, (float)alpha / 255.0F
         );
         GL11.glDisable(2848);
         GlStateManager.depthMask(true);
         GlStateManager.enableDepth();
         GlStateManager.enableTexture2D();
         GlStateManager.disableBlend();
         GlStateManager.popMatrix();
      }
   }

   public static void drawBBBox(AxisAlignedBB a, Color color, int alpha) {
      AxisAlignedBB bb = interpolateAxis(
         new AxisAlignedBB(a.minX, a.minY, a.minZ, a.maxX, a.maxY, a.maxZ)
      );
      float rO = (float)color.getRed() / 255.0F;
      float gO = (float)color.getGreen() / 255.0F;
      float bO = (float)color.getBlue() / 255.0F;
      float aO = (float)alpha / 255.0F;
      GlStateManager.pushMatrix();
      GlStateManager.enableBlend();
      GlStateManager.disableDepth();
      GlStateManager.tryBlendFuncSeparate(770, 771, 0, 1);
      GlStateManager.disableTexture2D();
      GlStateManager.depthMask(false);
      GL11.glEnable(2848);
      GL11.glHint(3154, 4354);
      GL11.glLineWidth(RenderSetting.INSTANCE.outlineWidth.getValue());
      Tessellator tessellator = Tessellator.getInstance();
      BufferBuilder bufferbuilder = tessellator.getBuffer();
      bufferbuilder.begin(3, DefaultVertexFormats.POSITION_COLOR);
      bufferbuilder.pos(bb.minX, bb.minY, bb.minZ).color(rO, gO, bO, aO).endVertex();
      bufferbuilder.pos(bb.minX, bb.minY, bb.maxZ).color(rO, gO, bO, aO).endVertex();
      bufferbuilder.pos(bb.maxX, bb.minY, bb.maxZ).color(rO, gO, bO, aO).endVertex();
      bufferbuilder.pos(bb.maxX, bb.minY, bb.minZ).color(rO, gO, bO, aO).endVertex();
      bufferbuilder.pos(bb.minX, bb.minY, bb.minZ).color(rO, gO, bO, aO).endVertex();
      bufferbuilder.pos(bb.minX, bb.maxY, bb.minZ).color(rO, gO, bO, aO).endVertex();
      bufferbuilder.pos(bb.minX, bb.maxY, bb.maxZ).color(rO, gO, bO, aO).endVertex();
      bufferbuilder.pos(bb.minX, bb.minY, bb.maxZ).color(rO, gO, bO, aO).endVertex();
      bufferbuilder.pos(bb.maxX, bb.minY, bb.maxZ).color(rO, gO, bO, aO).endVertex();
      bufferbuilder.pos(bb.maxX, bb.maxY, bb.maxZ).color(rO, gO, bO, aO).endVertex();
      bufferbuilder.pos(bb.minX, bb.maxY, bb.maxZ).color(rO, gO, bO, aO).endVertex();
      bufferbuilder.pos(bb.maxX, bb.maxY, bb.maxZ).color(rO, gO, bO, aO).endVertex();
      bufferbuilder.pos(bb.maxX, bb.maxY, bb.minZ).color(rO, gO, bO, aO).endVertex();
      bufferbuilder.pos(bb.maxX, bb.minY, bb.minZ).color(rO, gO, bO, aO).endVertex();
      bufferbuilder.pos(bb.maxX, bb.maxY, bb.minZ).color(rO, gO, bO, aO).endVertex();
      bufferbuilder.pos(bb.minX, bb.maxY, bb.minZ).color(rO, gO, bO, aO).endVertex();
      tessellator.draw();
      GL11.glDisable(2848);
      GlStateManager.depthMask(true);
      GlStateManager.enableDepth();
      GlStateManager.enableTexture2D();
      GlStateManager.disableBlend();
      GlStateManager.popMatrix();
   }

   public static void drawBoxESP(
      BlockPos pos, Color color, boolean secondC, Color secondColor, float lineWidth, boolean outline, boolean box, int boxAlpha, boolean air, double height
   ) {
      if (box) {
         drawBox(pos, new Color(color.getRed(), color.getGreen(), color.getBlue(), boxAlpha), height, false, false, 0);
      }

      if (outline) {
         drawBlockOutline(pos, secondC ? secondColor : color, lineWidth, air, height, false, false, 0, false);
      }
   }

   public static void drawBox(BlockPos pos, Color color, double height, boolean fade, boolean invertFade, int alpha) {
      if (fade) {
         Color endColor = new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
         drawFadingBox(pos, invertFade ? endColor : color, invertFade ? color : endColor, height);
      } else {
         AxisAlignedBB bb = new AxisAlignedBB(
            (double)pos.getX() - mc.getRenderManager().viewerPosX,
            (double)pos.getY() - mc.getRenderManager().viewerPosY,
            (double)pos.getZ() - mc.getRenderManager().viewerPosZ,
            (double)(pos.getX() + 1) - mc.getRenderManager().viewerPosX,
            (double)(pos.getY() + 1) - mc.getRenderManager().viewerPosY + height,
            (double)(pos.getZ() + 1) - mc.getRenderManager().viewerPosZ
         );
         camera.setPosition(Objects.requireNonNull(mc.getRenderViewEntity()).posX, mc.getRenderViewEntity().posY, mc.getRenderViewEntity().posZ);
         if (camera.isBoundingBoxInFrustum(
            new AxisAlignedBB(
               bb.minX + mc.getRenderManager().viewerPosX,
               bb.minY + mc.getRenderManager().viewerPosY,
               bb.minZ + mc.getRenderManager().viewerPosZ,
               bb.maxX + mc.getRenderManager().viewerPosX,
               bb.maxY + mc.getRenderManager().viewerPosY,
               bb.maxZ + mc.getRenderManager().viewerPosZ
            )
         )) {
            GlStateManager.pushMatrix();
            GlStateManager.enableBlend();
            GlStateManager.disableDepth();
            GlStateManager.tryBlendFuncSeparate(770, 771, 0, 1);
            GlStateManager.disableTexture2D();
            GlStateManager.depthMask(false);
            GL11.glEnable(2848);
            GL11.glHint(3154, 4354);
            RenderGlobal.renderFilledBox(
               bb, (float)color.getRed() / 255.0F, (float)color.getGreen() / 255.0F, (float)color.getBlue() / 255.0F, (float)color.getAlpha() / 255.0F
            );
            GL11.glDisable(2848);
            GlStateManager.depthMask(true);
            GlStateManager.enableDepth();
            GlStateManager.enableTexture2D();
            GlStateManager.disableBlend();
            GlStateManager.popMatrix();
         }
      }
   }

   public static void drawFadingBox(BlockPos pos, Color startColor, Color endColor, double height) {
      for(EnumFacing face : EnumFacing.values()) {
         if (face != EnumFacing.UP) {
            drawFadingSide(pos, face, startColor, endColor, height);
         }
      }
   }

   public static void drawFadingSide(BlockPos pos, EnumFacing face, Color startColor, Color endColor, double height) {
      Tessellator tessellator = Tessellator.getInstance();
      BufferBuilder builder = tessellator.getBuffer();
      IBlockState state = BlockUtil.getState(pos);
      Vec3d interp = InterpolationUtil.getInterpolatedPos(mc.player, mc.getRenderPartialTicks(), false);
      AxisAlignedBB bb = state.getSelectedBoundingBox(mc.world, pos)
         .grow(0.002F)
         .offset(-interp.x, -interp.y, -interp.z)
         .expand(0.0, 0.0, 0.0);
      float red = (float)startColor.getRed() / 255.0F;
      float green = (float)startColor.getGreen() / 255.0F;
      float blue = (float)startColor.getBlue() / 255.0F;
      float alpha = (float)startColor.getAlpha() / 255.0F;
      float red2 = (float)endColor.getRed() / 255.0F;
      float green2 = (float)endColor.getGreen() / 255.0F;
      float blue2 = (float)endColor.getBlue() / 255.0F;
      float alpha2 = (float)endColor.getAlpha() / 255.0F;
      double x1 = 0.0;
      double y1 = 0.0;
      double z1 = 0.0;
      double x2 = 0.0;
      double y2 = 0.0;
      double z2 = 0.0;
      if (face == EnumFacing.DOWN) {
         x1 = bb.minX;
         x2 = bb.maxX;
         y1 = bb.minY;
         y2 = bb.minY;
         z1 = bb.minZ;
         z2 = bb.maxZ;
      } else if (face == EnumFacing.UP) {
         x1 = bb.minX;
         x2 = bb.maxX;
         y1 = bb.maxY + height;
         y2 = bb.maxY + height;
         z1 = bb.minZ;
         z2 = bb.maxZ;
      } else if (face == EnumFacing.EAST) {
         x1 = bb.maxX;
         x2 = bb.maxX;
         y1 = bb.minY;
         y2 = bb.maxY + height;
         z1 = bb.minZ;
         z2 = bb.maxZ;
      } else if (face == EnumFacing.WEST) {
         x1 = bb.minX;
         x2 = bb.minX;
         y1 = bb.minY;
         y2 = bb.maxY + height;
         z1 = bb.minZ;
         z2 = bb.maxZ;
      } else if (face == EnumFacing.SOUTH) {
         x1 = bb.minX;
         x2 = bb.maxX;
         y1 = bb.minY;
         y2 = bb.maxY + height;
         z1 = bb.maxZ;
         z2 = bb.maxZ;
      } else if (face == EnumFacing.NORTH) {
         x1 = bb.minX;
         x2 = bb.maxX;
         y1 = bb.minY;
         y2 = bb.maxY + height;
         z1 = bb.minZ;
         z2 = bb.minZ;
      }

      GlStateManager.pushMatrix();
      GlStateManager.disableDepth();
      GlStateManager.disableTexture2D();
      GlStateManager.enableBlend();
      GlStateManager.disableAlpha();
      GlStateManager.depthMask(false);
      builder.begin(5, DefaultVertexFormats.POSITION_COLOR);
      if (face == EnumFacing.EAST || face == EnumFacing.WEST || face == EnumFacing.NORTH || face == EnumFacing.SOUTH) {
         buildPosColor(builder, red, green, blue, alpha, red2, green2, blue2, alpha2, x1, y1, z1, x2, y2, z2);
      } else if (face == EnumFacing.UP) {
         buildPosColor(builder, red2, green2, blue2, alpha2, x1, y1, z1, x2, y2, z2);
      } else if (face == EnumFacing.DOWN) {
         buildPosColor(builder, red, green, blue, alpha, x1, y1, z1, x2, y2, z2);
      }

      tessellator.draw();
      GlStateManager.depthMask(true);
      GlStateManager.disableBlend();
      GlStateManager.enableAlpha();
      GlStateManager.enableTexture2D();
      GlStateManager.enableDepth();
      GlStateManager.popMatrix();
   }

   private static void buildPosColor(
      BufferBuilder builder,
      float red,
      float green,
      float blue,
      float alpha,
      float red2,
      float green2,
      float blue2,
      float alpha2,
      double x1,
      double y1,
      double z1,
      double x2,
      double y2,
      double z2
   ) {
      builder.pos(x1, y1, z1).color(red, green, blue, alpha).endVertex();
      builder.pos(x1, y1, z1).color(red, green, blue, alpha).endVertex();
      builder.pos(x1, y1, z1).color(red, green, blue, alpha).endVertex();
      builder.pos(x1, y1, z2).color(red, green, blue, alpha).endVertex();
      builder.pos(x1, y2, z1).color(red2, green2, blue2, alpha2).endVertex();
      builder.pos(x1, y2, z2).color(red2, green2, blue2, alpha2).endVertex();
      builder.pos(x1, y2, z2).color(red2, green2, blue2, alpha2).endVertex();
      builder.pos(x1, y1, z2).color(red, green, blue, alpha).endVertex();
      builder.pos(x2, y2, z2).color(red2, green2, blue2, alpha2).endVertex();
      builder.pos(x2, y1, z2).color(red, green, blue, alpha).endVertex();
      builder.pos(x2, y1, z2).color(red, green, blue, alpha).endVertex();
      builder.pos(x2, y1, z1).color(red, green, blue, alpha).endVertex();
      builder.pos(x2, y2, z2).color(red2, green2, blue2, alpha2).endVertex();
      builder.pos(x2, y2, z1).color(red2, green2, blue2, alpha2).endVertex();
      builder.pos(x2, y2, z1).color(red2, green2, blue2, alpha2).endVertex();
      builder.pos(x2, y1, z1).color(red, green, blue, alpha).endVertex();
      builder.pos(x1, y2, z1).color(red2, green2, blue2, alpha2).endVertex();
      builder.pos(x1, y1, z1).color(red, green, blue, alpha).endVertex();
      builder.pos(x1, y1, z1).color(red, green, blue, alpha).endVertex();
      builder.pos(x2, y1, z1).color(red, green, blue, alpha).endVertex();
      builder.pos(x1, y1, z2).color(red, green, blue, alpha).endVertex();
      builder.pos(x2, y1, z2).color(red, green, blue, alpha).endVertex();
      builder.pos(x2, y1, z2).color(red, green, blue, alpha).endVertex();
      builder.pos(x1, y2, z1).color(red2, green2, blue2, alpha2).endVertex();
      builder.pos(x1, y2, z1).color(red2, green2, blue2, alpha2).endVertex();
      builder.pos(x1, y2, z2).color(red2, green2, blue2, alpha2).endVertex();
      builder.pos(x2, y2, z1).color(red2, green2, blue2, alpha2).endVertex();
      builder.pos(x2, y2, z2).color(red2, green2, blue2, alpha2).endVertex();
      builder.pos(x2, y2, z2).color(red2, green2, blue2, alpha2).endVertex();
      builder.pos(x2, y2, z2).color(red2, green2, blue2, alpha2).endVertex();
   }

   private static void buildPosColor(
      BufferBuilder builder, float red2, float green2, float blue2, float alpha2, double x1, double y1, double z1, double x2, double y2, double z2
   ) {
      buildPosColor(builder, red2, green2, blue2, alpha2, red2, green2, blue2, alpha2, x1, y1, z1, x2, y2, z2);
   }

   public static void drawBlockOutline(
      BlockPos pos, Color color, float linewidth, boolean air, double height, boolean fade, boolean invertFade, int alpha, boolean depth
   ) {
      if (fade) {
         Color endColor = new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
         drawFadingOutline(pos, invertFade ? color : endColor, invertFade ? endColor : color, linewidth, height);
      } else {
         IBlockState state = mc.world.getBlockState(pos);
         if ((air || state.getMaterial() != Material.AIR) && mc.world.getWorldBorder().contains(pos)) {
            AxisAlignedBB blockAxis = new AxisAlignedBB(
               (double)pos.getX() - mc.getRenderManager().viewerPosX,
               (double)pos.getY() - mc.getRenderManager().viewerPosY,
               (double)pos.getZ() - mc.getRenderManager().viewerPosZ,
               (double)(pos.getX() + 1) - mc.getRenderManager().viewerPosX,
               (double)(pos.getY() + 1) - mc.getRenderManager().viewerPosY + height,
               (double)(pos.getZ() + 1) - mc.getRenderManager().viewerPosZ
            );
            drawBlockOutline(blockAxis.grow(0.002F), color, linewidth, depth);
         }
      }
   }

   public static void drawBlockOutline(BlockPos pos, Color color, float linewidth, boolean air) {
      IBlockState iblockstate = mc.world.getBlockState(pos);
      if ((air || iblockstate.getMaterial() != Material.AIR) && mc.world.getWorldBorder().contains(pos)) {
         Vec3d interp = EntityUtil.interpolateEntity(mc.player, mc.getRenderPartialTicks());
         drawBlockOutline(
            iblockstate.getSelectedBoundingBox(mc.world, pos)
               .grow(0.002F)
               .offset(-interp.x, -interp.y, -interp.z),
            color,
            linewidth
         );
      }
   }

   public static void drawBlockOutline(AxisAlignedBB bb, Color color, float linewidth) {
      float red = (float)color.getRed() / 255.0F;
      float green = (float)color.getGreen() / 255.0F;
      float blue = (float)color.getBlue() / 255.0F;
      float alpha = (float)color.getAlpha() / 255.0F;
      GlStateManager.pushMatrix();
      GlStateManager.enableBlend();
      GlStateManager.disableDepth();
      GlStateManager.tryBlendFuncSeparate(770, 771, 0, 1);
      GlStateManager.disableTexture2D();
      GlStateManager.depthMask(false);
      GL11.glEnable(2848);
      GL11.glHint(3154, 4354);
      GL11.glLineWidth(linewidth);
      Tessellator tessellator = Tessellator.getInstance();
      BufferBuilder bufferbuilder = tessellator.getBuffer();
      bufferbuilder.begin(3, DefaultVertexFormats.POSITION_COLOR);
      bufferbuilder.pos(bb.minX, bb.minY, bb.minZ).color(red, green, blue, alpha).endVertex();
      bufferbuilder.pos(bb.minX, bb.minY, bb.maxZ).color(red, green, blue, alpha).endVertex();
      bufferbuilder.pos(bb.maxX, bb.minY, bb.maxZ).color(red, green, blue, alpha).endVertex();
      bufferbuilder.pos(bb.maxX, bb.minY, bb.minZ).color(red, green, blue, alpha).endVertex();
      bufferbuilder.pos(bb.minX, bb.minY, bb.minZ).color(red, green, blue, alpha).endVertex();
      bufferbuilder.pos(bb.minX, bb.maxY, bb.minZ).color(red, green, blue, alpha).endVertex();
      bufferbuilder.pos(bb.minX, bb.maxY, bb.maxZ).color(red, green, blue, alpha).endVertex();
      bufferbuilder.pos(bb.minX, bb.minY, bb.maxZ).color(red, green, blue, alpha).endVertex();
      bufferbuilder.pos(bb.maxX, bb.minY, bb.maxZ).color(red, green, blue, alpha).endVertex();
      bufferbuilder.pos(bb.maxX, bb.maxY, bb.maxZ).color(red, green, blue, alpha).endVertex();
      bufferbuilder.pos(bb.minX, bb.maxY, bb.maxZ).color(red, green, blue, alpha).endVertex();
      bufferbuilder.pos(bb.maxX, bb.maxY, bb.maxZ).color(red, green, blue, alpha).endVertex();
      bufferbuilder.pos(bb.maxX, bb.maxY, bb.minZ).color(red, green, blue, alpha).endVertex();
      bufferbuilder.pos(bb.maxX, bb.minY, bb.minZ).color(red, green, blue, alpha).endVertex();
      bufferbuilder.pos(bb.maxX, bb.maxY, bb.minZ).color(red, green, blue, alpha).endVertex();
      bufferbuilder.pos(bb.minX, bb.maxY, bb.minZ).color(red, green, blue, alpha).endVertex();
      tessellator.draw();
      GL11.glDisable(2848);
      GlStateManager.depthMask(true);
      GlStateManager.enableDepth();
      GlStateManager.enableTexture2D();
      GlStateManager.disableBlend();
      GlStateManager.popMatrix();
   }

   public static void drawBlockOutline(AxisAlignedBB bb, Color color, float linewidth, boolean depth) {
      float red = (float)color.getRed() / 255.0F;
      float green = (float)color.getGreen() / 255.0F;
      float blue = (float)color.getBlue() / 255.0F;
      float alpha = (float)color.getAlpha() / 255.0F;
      GlStateManager.pushMatrix();
      GlStateManager.enableBlend();
      if (depth) {
         GlStateManager.enableDepth();
         GlStateManager.tryBlendFuncSeparate(770, 771, 0, 1);
         GlStateManager.disableTexture2D();
         GlStateManager.depthMask(true);
      } else {
         GlStateManager.disableDepth();
         GlStateManager.tryBlendFuncSeparate(770, 771, 0, 1);
         GlStateManager.disableTexture2D();
         GlStateManager.depthMask(false);
      }

      GL11.glEnable(2848);
      GL11.glHint(3154, 4354);
      GL11.glLineWidth(linewidth);
      Tessellator tessellator = Tessellator.getInstance();
      BufferBuilder bufferbuilder = tessellator.getBuffer();
      bufferbuilder.begin(3, DefaultVertexFormats.POSITION_COLOR);
      bufferbuilder.pos(bb.minX, bb.minY, bb.minZ).color(red, green, blue, alpha).endVertex();
      bufferbuilder.pos(bb.minX, bb.minY, bb.maxZ).color(red, green, blue, alpha).endVertex();
      bufferbuilder.pos(bb.maxX, bb.minY, bb.maxZ).color(red, green, blue, alpha).endVertex();
      bufferbuilder.pos(bb.maxX, bb.minY, bb.minZ).color(red, green, blue, alpha).endVertex();
      bufferbuilder.pos(bb.minX, bb.minY, bb.minZ).color(red, green, blue, alpha).endVertex();
      bufferbuilder.pos(bb.minX, bb.maxY, bb.minZ).color(red, green, blue, alpha).endVertex();
      bufferbuilder.pos(bb.minX, bb.maxY, bb.maxZ).color(red, green, blue, alpha).endVertex();
      bufferbuilder.pos(bb.minX, bb.minY, bb.maxZ).color(red, green, blue, alpha).endVertex();
      bufferbuilder.pos(bb.maxX, bb.minY, bb.maxZ).color(red, green, blue, alpha).endVertex();
      bufferbuilder.pos(bb.maxX, bb.maxY, bb.maxZ).color(red, green, blue, alpha).endVertex();
      bufferbuilder.pos(bb.minX, bb.maxY, bb.maxZ).color(red, green, blue, alpha).endVertex();
      bufferbuilder.pos(bb.maxX, bb.maxY, bb.maxZ).color(red, green, blue, alpha).endVertex();
      bufferbuilder.pos(bb.maxX, bb.maxY, bb.minZ).color(red, green, blue, alpha).endVertex();
      bufferbuilder.pos(bb.maxX, bb.minY, bb.minZ).color(red, green, blue, alpha).endVertex();
      bufferbuilder.pos(bb.maxX, bb.maxY, bb.minZ).color(red, green, blue, alpha).endVertex();
      bufferbuilder.pos(bb.minX, bb.maxY, bb.minZ).color(red, green, blue, alpha).endVertex();
      tessellator.draw();
      GL11.glDisable(2848);
      GlStateManager.depthMask(true);
      GlStateManager.enableDepth();
      GlStateManager.enableTexture2D();
      GlStateManager.disableBlend();
      GlStateManager.popMatrix();
   }

   public static void drawFadingOutline(BlockPos pos, Color startColor, Color endColor, float linewidth, double height) {
      IBlockState iblockstate = mc.world.getBlockState(pos);
      Vec3d interp = InterpolationUtil.getInterpolatedPos(mc.player, mc.getRenderPartialTicks(), false);
      drawFadingOutline(
         iblockstate.getSelectedBoundingBox(mc.world, pos)
            .grow(0.002F)
            .offset(-interp.x, -interp.y, -interp.z)
            .expand(0.0, 0.0, 0.0),
         startColor,
         endColor,
         linewidth,
         height
      );
   }

   public static void drawFadingOutline(AxisAlignedBB bb, Color startColor, Color endColor, float linewidth, double height) {
      float red = (float)startColor.getRed() / 255.0F;
      float green = (float)startColor.getGreen() / 255.0F;
      float blue = (float)startColor.getBlue() / 255.0F;
      float alpha = (float)startColor.getAlpha() / 255.0F;
      float red2 = (float)endColor.getRed() / 255.0F;
      float green2 = (float)endColor.getGreen() / 255.0F;
      float blue2 = (float)endColor.getBlue() / 255.0F;
      float alpha2 = (float)endColor.getAlpha() / 255.0F;
      GlStateManager.pushMatrix();
      GlStateManager.enableBlend();
      GlStateManager.disableDepth();
      GlStateManager.tryBlendFuncSeparate(770, 771, 0, 1);
      GlStateManager.disableTexture2D();
      GlStateManager.depthMask(false);
      GL11.glEnable(2848);
      GL11.glHint(3154, 4354);
      GL11.glLineWidth(linewidth);
      Tessellator tessellator = Tessellator.getInstance();
      BufferBuilder bufferbuilder = tessellator.getBuffer();
      bufferbuilder.begin(3, DefaultVertexFormats.POSITION_COLOR);
      bufferbuilder.pos(bb.minX, bb.minY, bb.minZ).color(red2, green2, blue2, alpha2).endVertex();
      bufferbuilder.pos(bb.minX, bb.minY, bb.maxZ).color(red2, green2, blue2, alpha2).endVertex();
      bufferbuilder.pos(bb.maxX, bb.minY, bb.maxZ).color(red2, green2, blue2, alpha2).endVertex();
      bufferbuilder.pos(bb.maxX, bb.minY, bb.minZ).color(red2, green2, blue2, alpha2).endVertex();
      bufferbuilder.pos(bb.minX, bb.minY, bb.minZ).color(red2, green2, blue2, alpha2).endVertex();
      bufferbuilder.pos(bb.minX, bb.maxY + height, bb.minZ).color(red, green, blue, alpha).endVertex();
      bufferbuilder.pos(bb.minX, bb.maxY + height, bb.maxZ).color(red, green, blue, alpha).endVertex();
      bufferbuilder.pos(bb.minX, bb.minY, bb.maxZ).color(red2, green2, blue2, alpha2).endVertex();
      bufferbuilder.pos(bb.maxX, bb.minY, bb.maxZ).color(red2, green2, blue2, alpha2).endVertex();
      bufferbuilder.pos(bb.maxX, bb.maxY + height, bb.maxZ).color(red, green, blue, alpha).endVertex();
      bufferbuilder.pos(bb.minX, bb.maxY + height, bb.maxZ).color(red, green, blue, alpha).endVertex();
      bufferbuilder.pos(bb.maxX, bb.maxY + height, bb.maxZ).color(red, green, blue, alpha).endVertex();
      bufferbuilder.pos(bb.maxX, bb.maxY + height, bb.minZ).color(red, green, blue, alpha).endVertex();
      bufferbuilder.pos(bb.maxX, bb.minY, bb.minZ).color(red2, green2, blue2, alpha2).endVertex();
      bufferbuilder.pos(bb.maxX, bb.maxY + height, bb.minZ).color(red, green, blue, alpha).endVertex();
      bufferbuilder.pos(bb.minX, bb.maxY + height, bb.minZ).color(red, green, blue, alpha).endVertex();
      tessellator.draw();
      GL11.glDisable(2848);
      GlStateManager.depthMask(true);
      GlStateManager.enableDepth();
      GlStateManager.enableTexture2D();
      GlStateManager.disableBlend();
      GlStateManager.popMatrix();
   }

   public static void drawSelectionBoxESP(
      BlockPos pos, Color color, boolean secondC, Color secondColor, float lineWidth, boolean outline, boolean box, int boxAlpha, boolean depth
   ) {
      AxisAlignedBB bbPos = mc.world.getBlockState(pos).getSelectedBoundingBox(mc.world, pos).grow(0.002);
      AxisAlignedBB bb = InterpolationUtil.getInterpolatedAxis(bbPos);
      camera.setPosition(Objects.requireNonNull(mc.getRenderViewEntity()).posX, mc.getRenderViewEntity().posY, mc.getRenderViewEntity().posZ);
      if (camera.isBoundingBoxInFrustum(bbPos)) {
         GlStateManager.pushMatrix();
         GlStateManager.enableBlend();
         if (depth) {
            GlStateManager.enableDepth();
            GlStateManager.tryBlendFuncSeparate(770, 771, 0, 1);
            GlStateManager.disableTexture2D();
            GlStateManager.depthMask(true);
         } else {
            GlStateManager.disableDepth();
            GlStateManager.tryBlendFuncSeparate(770, 771, 0, 1);
            GlStateManager.disableTexture2D();
            GlStateManager.depthMask(false);
         }

         GL11.glEnable(2848);
         GL11.glHint(3154, 4354);
         GL11.glLineWidth(lineWidth);
         if (box) {
            RenderGlobal.renderFilledBox(
               bb, (float)color.getRed() / 255.0F, (float)color.getGreen() / 255.0F, (float)color.getBlue() / 255.0F, (float)boxAlpha / 255.0F
            );
         }

         if (outline) {
            drawBlockOutline(bb, secondC ? secondColor : color, lineWidth, depth);
         }

         GL11.glDisable(2848);
         GlStateManager.depthMask(true);
         GlStateManager.enableDepth();
         GlStateManager.enableTexture2D();
         GlStateManager.disableBlend();
         GlStateManager.popMatrix();
      }
   }

   public static void testESP(AxisAlignedBB bbPos, Color color, float lineWidth, boolean outline, boolean box, int alpha, boolean depth) {
      AxisAlignedBB bb = InterpolationUtil.getInterpolatedAxis(bbPos);
      camera.setPosition(Objects.requireNonNull(mc.getRenderViewEntity()).posX, mc.getRenderViewEntity().posY, mc.getRenderViewEntity().posZ);
      if (camera.isBoundingBoxInFrustum(bbPos)) {
         GlStateManager.pushMatrix();
         GlStateManager.enableBlend();
         if (depth) {
            GlStateManager.enableDepth();
            GlStateManager.tryBlendFuncSeparate(770, 771, 0, 1);
            GlStateManager.disableTexture2D();
            GlStateManager.depthMask(true);
         } else {
            GlStateManager.disableDepth();
            GlStateManager.tryBlendFuncSeparate(770, 771, 0, 1);
            GlStateManager.disableTexture2D();
            GlStateManager.depthMask(false);
         }

         GL11.glEnable(2848);
         GL11.glHint(3154, 4354);
         GL11.glLineWidth(lineWidth);
         if (box) {
            RenderGlobal.renderFilledBox(
               bb, (float)color.getRed() / 255.0F, (float)color.getGreen() / 255.0F, (float)color.getBlue() / 255.0F, (float)alpha / 255.0F
            );
         }

         if (outline) {
            drawBlockOutline(bb, ColorUtil.injectAlpha(color, alpha), lineWidth, false);
         }

         GL11.glDisable(2848);
         GlStateManager.depthMask(true);
         GlStateManager.enableDepth();
         GlStateManager.enableTexture2D();
         GlStateManager.disableBlend();
         GlStateManager.popMatrix();
      }
   }

   public static void drawBoxESP(BlockPos pos, Color color, int boxAlpha) {
      drawBox(pos, new Color(color.getRed(), color.getGreen(), color.getBlue(), boxAlpha));
   }

   public static void drawBox(AxisAlignedBB bb, Color color) {
      camera.setPosition(Objects.requireNonNull(mc.getRenderViewEntity()).posX, mc.getRenderViewEntity().posY, mc.getRenderViewEntity().posZ);
      if (camera.isBoundingBoxInFrustum(
         new AxisAlignedBB(
            bb.minX + mc.getRenderManager().viewerPosX,
            bb.minY + mc.getRenderManager().viewerPosY,
            bb.minZ + mc.getRenderManager().viewerPosZ,
            bb.maxX + mc.getRenderManager().viewerPosX,
            bb.maxY + mc.getRenderManager().viewerPosY,
            bb.maxZ + mc.getRenderManager().viewerPosZ
         )
      )) {
         GlStateManager.pushMatrix();
         GlStateManager.enableBlend();
         GlStateManager.disableDepth();
         GlStateManager.tryBlendFuncSeparate(770, 771, 0, 1);
         GlStateManager.disableTexture2D();
         GlStateManager.depthMask(false);
         GL11.glEnable(2848);
         GL11.glHint(3154, 4354);
         RenderGlobal.renderFilledBox(
            bb, (float)color.getRed() / 255.0F, (float)color.getGreen() / 255.0F, (float)color.getBlue() / 255.0F, (float)color.getAlpha() / 255.0F
         );
         GL11.glDisable(2848);
         GlStateManager.depthMask(true);
         GlStateManager.enableDepth();
         GlStateManager.enableTexture2D();
         GlStateManager.disableBlend();
         GlStateManager.popMatrix();
      }
   }

   public static void drawBox(BlockPos pos, Color color) {
      AxisAlignedBB bb = new AxisAlignedBB(
         (double)pos.getX() - mc.getRenderManager().viewerPosX,
         (double)pos.getY() - mc.getRenderManager().viewerPosY,
         (double)pos.getZ() - mc.getRenderManager().viewerPosZ,
         (double)(pos.getX() + 1) - mc.getRenderManager().viewerPosX,
         (double)(pos.getY() + 1) - mc.getRenderManager().viewerPosY,
         (double)(pos.getZ() + 1) - mc.getRenderManager().viewerPosZ
      );
      camera.setPosition(Objects.requireNonNull(mc.getRenderViewEntity()).posX, mc.getRenderViewEntity().posY, mc.getRenderViewEntity().posZ);
      if (camera.isBoundingBoxInFrustum(
         new AxisAlignedBB(
            bb.minX + mc.getRenderManager().viewerPosX,
            bb.minY + mc.getRenderManager().viewerPosY,
            bb.minZ + mc.getRenderManager().viewerPosZ,
            bb.maxX + mc.getRenderManager().viewerPosX,
            bb.maxY + mc.getRenderManager().viewerPosY,
            bb.maxZ + mc.getRenderManager().viewerPosZ
         )
      )) {
         GlStateManager.pushMatrix();
         GlStateManager.enableBlend();
         GlStateManager.disableDepth();
         GlStateManager.tryBlendFuncSeparate(770, 771, 0, 1);
         GlStateManager.disableTexture2D();
         GlStateManager.depthMask(false);
         GL11.glEnable(2848);
         GL11.glHint(3154, 4354);
         RenderGlobal.renderFilledBox(
            bb, (float)color.getRed() / 255.0F, (float)color.getGreen() / 255.0F, (float)color.getBlue() / 255.0F, (float)color.getAlpha() / 255.0F
         );
         GL11.glDisable(2848);
         GlStateManager.depthMask(true);
         GlStateManager.enableDepth();
         GlStateManager.enableTexture2D();
         GlStateManager.disableBlend();
         GlStateManager.popMatrix();
      }
   }

   public static void drawBoxESP(
      AxisAlignedBB bbPos, Color color, boolean secondC, Color secondColor, float lineWidth, boolean outline, boolean box, int boxAlpha, boolean depth
   ) {
      AxisAlignedBB axisAlignedBB = bbPos.offset(
         -mc.getRenderManager().viewerPosX, -mc.getRenderManager().viewerPosY, -mc.getRenderManager().viewerPosZ
      );
      camera.setPosition(Objects.requireNonNull(mc.getRenderViewEntity()).posX, mc.getRenderViewEntity().posY, mc.getRenderViewEntity().posZ);
      if (camera.isBoundingBoxInFrustum(bbPos)) {
         GlStateManager.pushMatrix();
         GlStateManager.enableBlend();
         if (depth) {
            GlStateManager.enableDepth();
            GlStateManager.tryBlendFuncSeparate(770, 771, 0, 1);
            GlStateManager.disableTexture2D();
            GlStateManager.depthMask(true);
         } else {
            GlStateManager.disableDepth();
            GlStateManager.tryBlendFuncSeparate(770, 771, 0, 1);
            GlStateManager.disableTexture2D();
            GlStateManager.depthMask(false);
         }

         GL11.glEnable(2848);
         GL11.glHint(3154, 4354);
         GL11.glLineWidth(lineWidth);
         if (box) {
            RenderGlobal.renderFilledBox(
               axisAlignedBB, (float)color.getRed() / 255.0F, (float)color.getGreen() / 255.0F, (float)color.getBlue() / 255.0F, (float)boxAlpha / 255.0F
            );
         }

         if (outline) {
            drawBlockOutline(axisAlignedBB, secondC ? secondColor : color, lineWidth, depth);
         }

         GL11.glDisable(2848);
         GlStateManager.depthMask(true);
         GlStateManager.enableDepth();
         GlStateManager.enableTexture2D();
         GlStateManager.disableBlend();
         GlStateManager.popMatrix();
      }
   }

   public static void drawBlockWireframe(BlockPos pos, Color color, float lineWidth, double height, boolean onlyBottom) {
      GlStateManager.pushMatrix();
      GlStateManager.enableBlend();
      GlStateManager.disableDepth();
      GlStateManager.tryBlendFuncSeparate(770, 771, 0, 1);
      GlStateManager.disableTexture2D();
      GlStateManager.depthMask(false);
      GL11.glEnable(2848);
      GL11.glHint(3154, 4354);
      GL11.glLineWidth(lineWidth);
      double x = (double)pos.getX() - mc.getRenderManager().viewerPosX;
      double y = (double)pos.getY() - mc.getRenderManager().viewerPosY;
      double z = (double)pos.getZ() - mc.getRenderManager().viewerPosZ;
      int red = color.getRed();
      int green = color.getGreen();
      int blue = color.getBlue();
      int alpha = color.getAlpha();
      AxisAlignedBB bb = new AxisAlignedBB(x, y, z, x + 1.0, y + 1.0 + height, z + 1.0);
      Tessellator tessellator = Tessellator.getInstance();
      BufferBuilder bufferbuilder = tessellator.getBuffer();
      bufferbuilder.begin(1, DefaultVertexFormats.POSITION_COLOR);
      bufferbuilder.pos(bb.minX, bb.minY, bb.minZ).color(red, green, blue, alpha).endVertex();
      bufferbuilder.pos(bb.maxX, bb.minY, bb.maxZ).color(red, green, blue, alpha).endVertex();
      bufferbuilder.pos(bb.minX, bb.minY, bb.maxZ).color(red, green, blue, alpha).endVertex();
      bufferbuilder.pos(bb.maxX, bb.minY, bb.minZ).color(red, green, blue, alpha).endVertex();
      if (!onlyBottom) {
         bufferbuilder.pos(bb.minX, bb.maxY, bb.minZ).color(red, green, blue, alpha).endVertex();
         bufferbuilder.pos(bb.maxX, bb.maxY, bb.maxZ).color(red, green, blue, alpha).endVertex();
         bufferbuilder.pos(bb.minX, bb.maxY, bb.maxZ).color(red, green, blue, alpha).endVertex();
         bufferbuilder.pos(bb.maxX, bb.maxY, bb.minZ).color(red, green, blue, alpha).endVertex();
         bufferbuilder.pos(bb.minX, bb.minY, bb.minZ).color(red, green, blue, alpha).endVertex();
         bufferbuilder.pos(bb.minX, bb.maxY, bb.maxZ).color(red, green, blue, alpha).endVertex();
         bufferbuilder.pos(bb.minX, bb.minY, bb.maxZ).color(red, green, blue, alpha).endVertex();
         bufferbuilder.pos(bb.minX, bb.maxY, bb.minZ).color(red, green, blue, alpha).endVertex();
         bufferbuilder.pos(bb.maxX, bb.minY, bb.minZ).color(red, green, blue, alpha).endVertex();
         bufferbuilder.pos(bb.maxX, bb.maxY, bb.maxZ).color(red, green, blue, alpha).endVertex();
         bufferbuilder.pos(bb.maxX, bb.minY, bb.maxZ).color(red, green, blue, alpha).endVertex();
         bufferbuilder.pos(bb.maxX, bb.maxY, bb.minZ).color(red, green, blue, alpha).endVertex();
         bufferbuilder.pos(bb.minX, bb.minY, bb.minZ).color(red, green, blue, alpha).endVertex();
         bufferbuilder.pos(bb.maxX, bb.maxY, bb.minZ).color(red, green, blue, alpha).endVertex();
         bufferbuilder.pos(bb.maxX, bb.minY, bb.minZ).color(red, green, blue, alpha).endVertex();
         bufferbuilder.pos(bb.minX, bb.maxY, bb.minZ).color(red, green, blue, alpha).endVertex();
         bufferbuilder.pos(bb.minX, bb.minY, bb.maxZ).color(red, green, blue, alpha).endVertex();
         bufferbuilder.pos(bb.maxX, bb.maxY, bb.maxZ).color(red, green, blue, alpha).endVertex();
         bufferbuilder.pos(bb.maxX, bb.minY, bb.maxZ).color(red, green, blue, alpha).endVertex();
         bufferbuilder.pos(bb.minX, bb.maxY, bb.maxZ).color(red, green, blue, alpha).endVertex();
      }

      tessellator.draw();
      GL11.glDisable(2848);
      GlStateManager.depthMask(true);
      GlStateManager.enableDepth();
      GlStateManager.enableTexture2D();
      GlStateManager.disableBlend();
      GlStateManager.popMatrix();
   }

   public static void drawEntityBoxESP(
      Entity entity, Color color, boolean secondC, Color secondColor, float lineWidth, boolean outline, boolean box, int boxAlpha
   ) {
      Vec3d interp = InterpolationUtil.getInterpolatedPos(entity, mc.getRenderPartialTicks(), true);
      AxisAlignedBB bb = new AxisAlignedBB(
         entity.getEntityBoundingBox().minX - 0.05 - entity.posX + interp.x,
         entity.getEntityBoundingBox().minY - 0.0 - entity.posY + interp.y,
         entity.getEntityBoundingBox().minZ - 0.05 - entity.posZ + interp.z,
         entity.getEntityBoundingBox().maxX + 0.05 - entity.posX + interp.x,
         entity.getEntityBoundingBox().maxY + 0.1 - entity.posY + interp.y,
         entity.getEntityBoundingBox().maxZ + 0.05 - entity.posZ + interp.z
      );
      GlStateManager.pushMatrix();
      GlStateManager.enableBlend();
      GlStateManager.disableDepth();
      GlStateManager.tryBlendFuncSeparate(770, 771, 0, 1);
      GlStateManager.disableTexture2D();
      GlStateManager.depthMask(false);
      GL11.glEnable(2848);
      GL11.glHint(3154, 4354);
      GL11.glLineWidth(lineWidth);
      if (entity instanceof EntityPlayer && Managers.FRIENDS.isFriend(entity.getName())) {
         color = Managers.COLORS.getFriendColor(color.getAlpha());
      }

      if (box) {
         RenderGlobal.renderFilledBox(
            bb, (float)color.getRed() / 255.0F, (float)color.getGreen() / 255.0F, (float)color.getBlue() / 255.0F, (float)boxAlpha / 255.0F
         );
      }

      GL11.glDisable(2848);
      GlStateManager.depthMask(true);
      GlStateManager.enableDepth();
      GlStateManager.enableTexture2D();
      GlStateManager.disableBlend();
      GlStateManager.popMatrix();
      if (outline) {
         drawBlockOutline(bb, secondC ? secondColor : color, lineWidth, false);
      }
   }

   public static void drawLine(float x, float y, float x1, float y1, float thickness, int hex) {
      float red = (float)(hex >> 16 & 0xFF) / 255.0F;
      float green = (float)(hex >> 8 & 0xFF) / 255.0F;
      float blue = (float)(hex & 0xFF) / 255.0F;
      float alpha = (float)(hex >> 24 & 0xFF) / 255.0F;
      GlStateManager.pushMatrix();
      GlStateManager.disableTexture2D();
      GlStateManager.enableBlend();
      GlStateManager.disableAlpha();
      GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
      GlStateManager.shadeModel(7425);
      GL11.glLineWidth(thickness);
      GL11.glEnable(2848);
      GL11.glHint(3154, 4354);
      Tessellator tessellator = Tessellator.getInstance();
      BufferBuilder bufferbuilder = tessellator.getBuffer();
      bufferbuilder.begin(3, DefaultVertexFormats.POSITION_COLOR);
      bufferbuilder.pos((double)x, (double)y, 0.0).color(red, green, blue, alpha).endVertex();
      bufferbuilder.pos((double)x1, (double)y1, 0.0).color(red, green, blue, alpha).endVertex();
      tessellator.draw();
      GlStateManager.shadeModel(7424);
      GL11.glDisable(2848);
      GlStateManager.disableBlend();
      GlStateManager.enableAlpha();
      GlStateManager.enableTexture2D();
      GlStateManager.popMatrix();
   }

   public static void drawGradientLine(float x, float y, float x1, float y1, float thickness, int hex, int secondHex) {
      float red = (float)(hex >> 16 & 0xFF) / 255.0F;
      float green = (float)(hex >> 8 & 0xFF) / 255.0F;
      float blue = (float)(hex & 0xFF) / 255.0F;
      float alpha = (float)(hex >> 24 & 0xFF) / 255.0F;
      float red2 = (float)(secondHex >> 16 & 0xFF) / 255.0F;
      float green2 = (float)(secondHex >> 8 & 0xFF) / 255.0F;
      float blue2 = (float)(secondHex & 0xFF) / 255.0F;
      float alpha2 = (float)(secondHex >> 24 & 0xFF) / 255.0F;
      GlStateManager.pushMatrix();
      GlStateManager.disableTexture2D();
      GlStateManager.enableBlend();
      GlStateManager.disableAlpha();
      GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
      GlStateManager.shadeModel(7425);
      GL11.glLineWidth(thickness);
      GL11.glEnable(2848);
      GL11.glHint(3154, 4354);
      Tessellator tessellator = Tessellator.getInstance();
      BufferBuilder bufferbuilder = tessellator.getBuffer();
      bufferbuilder.begin(3, DefaultVertexFormats.POSITION_COLOR);
      bufferbuilder.pos((double)x, (double)y, 0.0).color(red, green, blue, alpha).endVertex();
      bufferbuilder.pos((double)x, (double)y1, 0.0).color(red, green, blue, alpha).endVertex();
      bufferbuilder.pos((double)x1, (double)y1, 0.0).color(red2, green2, blue2, alpha2).endVertex();
      bufferbuilder.pos((double)x1, (double)y, 0.0).color(red2, green2, blue2, alpha2).endVertex();
      tessellator.draw();
      GlStateManager.shadeModel(7424);
      GL11.glDisable(2848);
      GlStateManager.disableBlend();
      GlStateManager.enableAlpha();
      GlStateManager.enableTexture2D();
      GlStateManager.popMatrix();
   }

   public static void drawRect(float x, float y, float w, float h, int color) {
      float alpha = (float)(color >> 24 & 0xFF) / 255.0F;
      float red = (float)(color >> 16 & 0xFF) / 255.0F;
      float green = (float)(color >> 8 & 0xFF) / 255.0F;
      float blue = (float)(color & 0xFF) / 255.0F;
      Tessellator tessellator = Tessellator.getInstance();
      BufferBuilder bufferbuilder = tessellator.getBuffer();
      GlStateManager.enableBlend();
      GlStateManager.disableTexture2D();
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

   public static void drawModalRect(int x, int y, float u, float v, int uWidth, int vHeight, int width, int height, float tileWidth, float tileHeight) {
      Gui.drawScaledCustomSizeModalRect(x, y, u, v, uWidth, vHeight, width, height, tileWidth, tileHeight);
   }

   public static void drawVGradientRect(float left, float top, float right, float bottom, int startColor, int endColor) {
      float f = (float)(startColor >> 24 & 0xFF) / 255.0F;
      float f1 = (float)(startColor >> 16 & 0xFF) / 255.0F;
      float f2 = (float)(startColor >> 8 & 0xFF) / 255.0F;
      float f3 = (float)(startColor & 0xFF) / 255.0F;
      float f4 = (float)(endColor >> 24 & 0xFF) / 255.0F;
      float f5 = (float)(endColor >> 16 & 0xFF) / 255.0F;
      float f6 = (float)(endColor >> 8 & 0xFF) / 255.0F;
      float f7 = (float)(endColor & 0xFF) / 255.0F;
      GlStateManager.disableTexture2D();
      GlStateManager.enableBlend();
      GlStateManager.disableAlpha();
      GlStateManager.tryBlendFuncSeparate(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA, SourceFactor.ONE, DestFactor.ZERO);
      GlStateManager.shadeModel(7425);
      Tessellator tessellator = Tessellator.getInstance();
      BufferBuilder bufferbuilder = tessellator.getBuffer();
      bufferbuilder.begin(7, DefaultVertexFormats.POSITION_COLOR);
      bufferbuilder.pos((double)right, (double)top, 0.0).color(f1, f2, f3, f).endVertex();
      bufferbuilder.pos((double)left, (double)top, 0.0).color(f1, f2, f3, f).endVertex();
      bufferbuilder.pos((double)left, (double)bottom, 0.0).color(f5, f6, f7, f4).endVertex();
      bufferbuilder.pos((double)right, (double)bottom, 0.0).color(f5, f6, f7, f4).endVertex();
      tessellator.draw();
      GlStateManager.shadeModel(7424);
      GlStateManager.disableBlend();
      GlStateManager.enableAlpha();
      GlStateManager.enableTexture2D();
   }

   public static void drawHGradientRect(float left, float top, float right, float bottom, int startColor, int endColor) {
      float f1 = (float)(startColor >> 16 & 0xFF) / 255.0F;
      float f2 = (float)(startColor >> 8 & 0xFF) / 255.0F;
      float f3 = (float)(startColor & 0xFF) / 255.0F;
      float f4 = (float)(endColor >> 24 & 0xFF) / 255.0F;
      float f5 = (float)(endColor >> 16 & 0xFF) / 255.0F;
      float f6 = (float)(endColor >> 8 & 0xFF) / 255.0F;
      float f7 = (float)(endColor & 0xFF) / 255.0F;
      GlStateManager.disableTexture2D();
      GlStateManager.enableBlend();
      GlStateManager.disableAlpha();
      GlStateManager.tryBlendFuncSeparate(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA, SourceFactor.ONE, DestFactor.ZERO);
      GlStateManager.shadeModel(7425);
      Tessellator tessellator = Tessellator.getInstance();
      BufferBuilder bufferbuilder = tessellator.getBuffer();
      bufferbuilder.begin(7, DefaultVertexFormats.POSITION_COLOR);
      bufferbuilder.pos((double)left, (double)top, 0.0).color(f1, f2, f3, f4).endVertex();
      bufferbuilder.pos((double)left, (double)bottom, 0.0).color(f1, f2, f3, f4).endVertex();
      bufferbuilder.pos((double)right, (double)bottom, 0.0).color(f5, f6, f7, f4).endVertex();
      bufferbuilder.pos((double)right, (double)top, 0.0).color(f5, f6, f7, f4).endVertex();
      tessellator.draw();
      GlStateManager.shadeModel(7424);
      GlStateManager.disableBlend();
      GlStateManager.enableAlpha();
      GlStateManager.enableTexture2D();
   }

   public static void drawGlow(double x, double y, double x1, double y1, int color) {
      GlStateManager.disableTexture2D();
      GlStateManager.enableBlend();
      GlStateManager.disableAlpha();
      GlStateManager.tryBlendFuncSeparate(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA, SourceFactor.ONE, DestFactor.ZERO);
      GlStateManager.shadeModel(7425);
      drawVGradientRect(
         (float)((int)x),
         (float)((int)y),
         (float)((int)x1),
         (float)((int)(y + (y1 - y) / 2.0)),
         ColorUtil.toRGBA(new Color(color).getRed(), new Color(color).getGreen(), new Color(color).getBlue(), 0),
         color
      );
      drawVGradientRect(
         (float)((int)x),
         (float)((int)(y + (y1 - y) / 2.0)),
         (float)((int)x1),
         (float)((int)y1),
         color,
         ColorUtil.toRGBA(new Color(color).getRed(), new Color(color).getGreen(), new Color(color).getBlue(), 0)
      );
      int radius = (int)((y1 - y) / 2.0);
      drawPolygonPart(
         x, y + (y1 - y) / 2.0, radius, 0, color, ColorUtil.toRGBA(new Color(color).getRed(), new Color(color).getGreen(), new Color(color).getBlue(), 0)
      );
      drawPolygonPart(
         x, y + (y1 - y) / 2.0, radius, 1, color, ColorUtil.toRGBA(new Color(color).getRed(), new Color(color).getGreen(), new Color(color).getBlue(), 0)
      );
      drawPolygonPart(
         x1, y + (y1 - y) / 2.0, radius, 2, color, ColorUtil.toRGBA(new Color(color).getRed(), new Color(color).getGreen(), new Color(color).getBlue(), 0)
      );
      drawPolygonPart(
         x1, y + (y1 - y) / 2.0, radius, 3, color, ColorUtil.toRGBA(new Color(color).getRed(), new Color(color).getGreen(), new Color(color).getBlue(), 0)
      );
      GlStateManager.shadeModel(7424);
      GlStateManager.disableBlend();
      GlStateManager.enableAlpha();
      GlStateManager.enableTexture2D();
   }

   public static void drawPolygonPart(double x, double y, int radius, int part, int color, int endcolor) {
      float alpha = (float)(color >> 24 & 0xFF) / 255.0F;
      float red = (float)(color >> 16 & 0xFF) / 255.0F;
      float green = (float)(color >> 8 & 0xFF) / 255.0F;
      float blue = (float)(color & 0xFF) / 255.0F;
      float alpha1 = (float)(endcolor >> 24 & 0xFF) / 255.0F;
      float red1 = (float)(endcolor >> 16 & 0xFF) / 255.0F;
      float green1 = (float)(endcolor >> 8 & 0xFF) / 255.0F;
      float blue1 = (float)(endcolor & 0xFF) / 255.0F;
      GlStateManager.disableTexture2D();
      GlStateManager.enableBlend();
      GlStateManager.disableAlpha();
      GlStateManager.tryBlendFuncSeparate(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA, SourceFactor.ONE, DestFactor.ZERO);
      GlStateManager.shadeModel(7425);
      Tessellator tessellator = Tessellator.getInstance();
      BufferBuilder bufferbuilder = tessellator.getBuffer();
      bufferbuilder.begin(6, DefaultVertexFormats.POSITION_COLOR);
      bufferbuilder.pos(x, y, 0.0).color(red, green, blue, alpha).endVertex();
      double TWICE_PI = Math.PI * 2;

      for(int i = part * 90; i <= part * 90 + 90; ++i) {
         double angle = (Math.PI * 2) * (double)i / 360.0 + Math.toRadians(180.0);
         bufferbuilder.pos(x + Math.sin(angle) * (double)radius, y + Math.cos(angle) * (double)radius, 0.0)
            .color(red1, green1, blue1, alpha1)
            .endVertex();
      }

      tessellator.draw();
      GlStateManager.shadeModel(7424);
      GlStateManager.disableBlend();
      GlStateManager.enableAlpha();
      GlStateManager.enableTexture2D();
   }

   public static void drawCircle(float x, float y, float radius, int color) {
      float alpha = (float)(color >> 24 & 0xFF) / 255.0F;
      float red = (float)(color >> 16 & 0xFF) / 255.0F;
      float green = (float)(color >> 8 & 0xFF) / 255.0F;
      float blue = (float)(color & 0xFF) / 255.0F;
      GlStateManager.pushMatrix();
      GlStateManager.enableBlend();
      GlStateManager.disableTexture2D();
      GlStateManager.tryBlendFuncSeparate(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA, SourceFactor.ONE, DestFactor.ZERO);
      GL11.glColor4f(red, green, blue, alpha);
      GL11.glBegin(9);

      for(int i = 0; i <= 360; ++i) {
         GL11.glVertex2d(
            (double)x + Math.sin((double)i * 3.141526 / 180.0) * (double)radius, (double)y + Math.cos((double)i * 3.141526 / 180.0) * (double)radius
         );
      }

      GL11.glEnd();
      GlStateManager.resetColor();
      GlStateManager.enableTexture2D();
      GlStateManager.disableBlend();
      GlStateManager.popMatrix();
   }

   public static void drawNameTagOutline(float x, float y, float width, float height, float lineWidth, int color, int secondColor, boolean rainbow) {
      GlStateManager.enableBlend();
      GlStateManager.disableTexture2D();
      GlStateManager.tryBlendFuncSeparate(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA, SourceFactor.ONE, DestFactor.ZERO);
      x *= 2.0F;
      width *= 2.0F;
      y *= 2.0F;
      height *= 2.0F;
      GL11.glScalef(0.5F, 0.5F, 0.5F);
      drawLine(x, y, x, height, lineWidth, rainbow ? ColorUtil.rainbow(5000).getRGB() : color);
      drawLine(width, y, width, height, lineWidth, rainbow ? ColorUtil.rainbow(1000).getRGB() : secondColor);
      drawGradientLine(x, y, width, y, lineWidth, rainbow ? ColorUtil.rainbow(5000).getRGB() : color, rainbow ? ColorUtil.rainbow(1000).getRGB() : secondColor);
      drawGradientLine(
         x, height, width, height, lineWidth, rainbow ? ColorUtil.rainbow(5000).getRGB() : color, rainbow ? ColorUtil.rainbow(1000).getRGB() : secondColor
      );
      GL11.glScalef(2.0F, 2.0F, 2.0F);
      GlStateManager.enableTexture2D();
      GlStateManager.disableBlend();
   }

   public static void drawArrowPointer(float x, float y, float size, float widthDiv, float heightDiv, boolean outline, float outlineWidth, int color) {
      boolean blend = GL11.glIsEnabled(3042);
      float alpha = (float)(color >> 24 & 0xFF) / 255.0F;
      GL11.glEnable(3042);
      GL11.glDisable(3553);
      GL11.glBlendFunc(770, 771);
      GL11.glEnable(2848);
      GL11.glPushMatrix();
      glColor(color);
      GL11.glBegin(7);
      GL11.glVertex2d((double)x, (double)y);
      GL11.glVertex2d((double)(x - size / widthDiv), (double)(y + size));
      GL11.glVertex2d((double)x, (double)(y + size / heightDiv));
      GL11.glVertex2d((double)(x + size / widthDiv), (double)(y + size));
      GL11.glVertex2d((double)x, (double)y);
      GL11.glEnd();
      if (outline) {
         GL11.glLineWidth(outlineWidth);
         GL11.glColor4f(0.0F, 0.0F, 0.0F, alpha);
         GL11.glBegin(2);
         GL11.glVertex2d((double)x, (double)y);
         GL11.glVertex2d((double)(x - size / widthDiv), (double)(y + size));
         GL11.glVertex2d((double)x, (double)(y + size / heightDiv));
         GL11.glVertex2d((double)(x + size / widthDiv), (double)(y + size));
         GL11.glVertex2d((double)x, (double)y);
         GL11.glEnd();
      }

      GL11.glPopMatrix();
      GL11.glEnable(3553);
      if (!blend) {
         GL11.glDisable(3042);
      }

      GL11.glDisable(2848);
   }

   public static ByteBuffer readImageToBuffer(InputStream in) throws IOException {
      BufferedImage bufferedimage = ImageIO.read(in);
      int[] pixelIndex = bufferedimage.getRGB(0, 0, bufferedimage.getWidth(), bufferedimage.getHeight(), null, 0, bufferedimage.getWidth());
      ByteBuffer bytebuffer = ByteBuffer.allocate(4 * pixelIndex.length);
      Arrays.stream(pixelIndex).map(i -> i << 8 | i >> 24 & 0xFF).forEach(bytebuffer::putInt);
      ((Buffer)bytebuffer).flip();
      return bytebuffer;
   }

   public static Vec3d get2DPos(double x, double y, double z) {
      GL11.glGetFloat(2982, modelView);
      GL11.glGetFloat(2983, projection);
      GL11.glGetInteger(2978, viewport);
      boolean out = GLU.gluProject((float)x, (float)y, (float)z, modelView, projection, viewport, screenCoords);
      return out ? new Vec3d((double)screenCoords.get(0), (double)((float)Display.getHeight() - screenCoords.get(1)), (double)screenCoords.get(2)) : null;
   }

   public static void glColor(Color color) {
      GL11.glColor4f((float)color.getRed() / 255.0F, (float)color.getGreen() / 255.0F, (float)color.getBlue() / 255.0F, (float)color.getAlpha() / 255.0F);
   }

   public static void glColor(int color) {
      float red = (float)(color >> 16 & 0xFF) / 255.0F;
      float green = (float)(color >> 8 & 0xFF) / 255.0F;
      float blue = (float)(color & 0xFF) / 255.0F;
      float alpha = (float)(color >> 24 & 0xFF) / 255.0F;
      GL11.glColor4f(red, green, blue, alpha);
   }
}
