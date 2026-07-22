//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "G:\PortableSoft\JBY\MC_Deobf3000\1.12-MCP-Mappings"!

package me.rebirthclient.mod.modules.impl.render;

import java.awt.Color;
import java.util.concurrent.atomic.AtomicInteger;
import me.rebirthclient.api.util.shaders.impl.fill.AquaShader;
import me.rebirthclient.api.util.shaders.impl.fill.CircleShader;
import me.rebirthclient.api.util.shaders.impl.fill.FillShader;
import me.rebirthclient.api.util.shaders.impl.fill.FlowShader;
import me.rebirthclient.api.util.shaders.impl.fill.GradientShader;
import me.rebirthclient.api.util.shaders.impl.fill.PhobosShader;
import me.rebirthclient.api.util.shaders.impl.fill.RainbowCubeShader;
import me.rebirthclient.api.util.shaders.impl.fill.SmokeShader;
import me.rebirthclient.api.util.shaders.impl.outline.AquaOutlineShader;
import me.rebirthclient.api.util.shaders.impl.outline.AstralOutlineShader;
import me.rebirthclient.api.util.shaders.impl.outline.CircleOutlineShader;
import me.rebirthclient.api.util.shaders.impl.outline.GlowShader;
import me.rebirthclient.api.util.shaders.impl.outline.GradientOutlineShader;
import me.rebirthclient.api.util.shaders.impl.outline.RainbowCubeOutlineShader;
import me.rebirthclient.api.util.shaders.impl.outline.SmokeOutlineShader;
import me.rebirthclient.mod.modules.Category;
import me.rebirthclient.mod.modules.Module;
import me.rebirthclient.mod.modules.settings.Setting;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.item.EntityEnderPearl;
import net.minecraft.entity.item.EntityExpBottle;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class Shaders extends Module {
   private final Setting<Shaders.fillShadermode> fillShader = this.add(new Setting<>("Fill Shader", Shaders.fillShadermode.None));
   private final Setting<Shaders.glowESPmode> glowESP = this.add(new Setting<>("Glow ESP", Shaders.glowESPmode.None));
   public final Setting<Shaders.Crystal1> crystal = this.add(new Setting<>("Crystals", Shaders.Crystal1.None));
   private final Setting<Shaders.Player1> player = this.add(new Setting<>("Players", Shaders.Player1.None));
   private final Setting<Shaders.Mob1> mob = this.add(new Setting<>("Mobs", Shaders.Mob1.None));
   private final Setting<Shaders.Itemsl> items = this.add(new Setting<>("Items", Shaders.Itemsl.None));
   private final Setting<Shaders.XPl> xpOrb = this.add(new Setting<>("XP", Shaders.XPl.None));
   private final Setting<Shaders.XPBl> xpBottle = this.add(new Setting<>("XPBottle", Shaders.XPBl.None));
   private final Setting<Shaders.EPl> enderPearl = this.add(new Setting<>("EnderPearl", Shaders.EPl.None));
   private final Setting<Boolean> rangeCheck = this.add(new Setting<>("Range Check", true));
   public Setting<Float> maxRange = this.add(new Setting<>("Max Range", 35.0F, 10.0F, 100.0F, object -> this.rangeCheck.getValue()));
   public Setting<Float> minRange = this.add(new Setting<>("Min range", 0.0F, 0.0F, 5.0F, object -> this.rangeCheck.getValue()));
   private final Setting<Boolean> default1 = this.add(new Setting<>("Reset Setting", false));
   private final Setting<Boolean> Fpreset = this.add(new Setting<>("FutureRainbow Preset", false));
   private final Setting<Boolean> fadeFill = this.add(
      new Setting<>(
         "Fade Fill",
         Boolean.FALSE,
         bl -> this.fillShader.getValue() == Shaders.fillShadermode.Astral || this.glowESP.getValue() == Shaders.glowESPmode.Astral
      )
   );
   private final Setting<Boolean> fadeOutline = this.add(
      new Setting<>(
         "FadeOL Fill",
         Boolean.FALSE,
         bl -> this.fillShader.getValue() == Shaders.fillShadermode.Astral || this.glowESP.getValue() == Shaders.glowESPmode.Astral
      )
   );
   public Setting<Float> duplicateOutline = this.add(new Setting<>("duplicateOutline", 1.0F, 0.0F, 20.0F));
   public Setting<Float> duplicateFill = this.add(new Setting<>("Duplicate Fill", 1.0F, 0.0F, 5.0F));
   public Setting<Float> speedOutline = this.add(new Setting<>("Speed Outline", 10.0F, 1.0F, 100.0F));
   public Setting<Float> speedFill = this.add(new Setting<>("Speed Fill", 10.0F, 1.0F, 100.0F));
   public Setting<Float> quality = this.add(new Setting<>("Shader Quality", 1.0F, 0.0F, 20.0F));
   public Setting<Float> radius = this.add(new Setting<>("Shader Radius", 1.0F, 0.0F, 5.0F));
   public Setting<Float> rad = this.add(new Setting<>("RAD Fill", 0.75F, 0.0F, 5.0F, object -> this.fillShader.getValue() == Shaders.fillShadermode.Circle));
   public Setting<Float> PI = this.add(
      new Setting<>("PI Fill", (float) Math.PI, 0.0F, 10.0F, object -> this.fillShader.getValue() == Shaders.fillShadermode.Circle)
   );
   public Setting<Float> saturationFill = this.add(
      new Setting<>("saturation", 0.4F, 0.0F, 3.0F, object -> this.fillShader.getValue() == Shaders.fillShadermode.Astral)
   );
   public Setting<Float> distfadingFill = this.add(
      new Setting<>("distfading", 0.56F, 0.0F, 1.0F, object -> this.fillShader.getValue() == Shaders.fillShadermode.Astral)
   );
   public Setting<Float> titleFill = this.add(new Setting<>("Tile", 0.45F, 0.0F, 1.3F, object -> this.fillShader.getValue() == Shaders.fillShadermode.Astral));
   public Setting<Float> stepSizeFill = this.add(
      new Setting<>("Step Size", 0.2F, 0.0F, 0.7F, object -> this.fillShader.getValue() == Shaders.fillShadermode.Astral)
   );
   public Setting<Float> volumStepsFill = this.add(
      new Setting<>("Volum Steps", 10.0F, 0.0F, 10.0F, object -> this.fillShader.getValue() == Shaders.fillShadermode.Astral)
   );
   public Setting<Float> zoomFill = this.add(new Setting<>("Zoom", 3.9F, 0.0F, 20.0F, object -> this.fillShader.getValue() == Shaders.fillShadermode.Astral));
   public Setting<Float> formuparam2Fill = this.add(
      new Setting<>("formuparam2", 0.89F, 0.0F, 1.5F, object -> this.fillShader.getValue() == Shaders.fillShadermode.Astral)
   );
   public Setting<Float> saturationOutline = this.add(
      new Setting<>("saturation", 0.4F, 0.0F, 3.0F, object -> this.glowESP.getValue() == Shaders.glowESPmode.Astral)
   );
   public Setting<Integer> maxEntities = this.add(new Setting<>("Max Entities", 100, 10, 500));
   public Setting<Integer> iterationsFill = this.add(new Setting<>("Iteration", 4, 3, 20, n -> this.fillShader.getValue() == Shaders.fillShadermode.Astral));
   public Setting<Integer> redFill = this.add(new Setting<>("Tick Regen", 0, 0, 100, n -> this.fillShader.getValue() == Shaders.fillShadermode.Astral));
   public Setting<Integer> MaxIterFill = this.add(new Setting<>("Max Iter", 5, 0, 30, n -> this.fillShader.getValue() == Shaders.fillShadermode.Aqua));
   public Setting<Integer> NUM_OCTAVESFill = this.add(new Setting<>("NUM_OCTAVES", 5, 1, 30, n -> this.fillShader.getValue() == Shaders.fillShadermode.Smoke));
   public Setting<Integer> BSTARTFIll = this.add(new Setting<>("BSTART", 0, 0, 1000, n -> this.fillShader.getValue() == Shaders.fillShadermode.RainbowCube));
   public Setting<Integer> GSTARTFill = this.add(new Setting<>("GSTART", 0, 0, 1000, n -> this.fillShader.getValue() == Shaders.fillShadermode.RainbowCube));
   public Setting<Integer> RSTARTFill = this.add(new Setting<>("RSTART", 0, 0, 1000, n -> this.fillShader.getValue() == Shaders.fillShadermode.RainbowCube));
   public Setting<Integer> WaveLenghtFIll = this.add(
      new Setting<>("Wave Lenght", 555, 0, 2000, n -> this.fillShader.getValue() == Shaders.fillShadermode.RainbowCube)
   );
   public Setting<Integer> volumStepsOutline = this.add(new Setting<>("Volum Steps", 10, 0, 10, n -> this.glowESP.getValue() == Shaders.glowESPmode.Astral));
   public Setting<Integer> iterationsOutline = this.add(new Setting<>("Iteration", 4, 3, 20, n -> this.glowESP.getValue() == Shaders.glowESPmode.Astral));
   public Setting<Integer> MaxIterOutline = this.add(new Setting<>("Max Iter", 5, 0, 30, n -> this.glowESP.getValue() == Shaders.glowESPmode.Aqua));
   public Setting<Integer> NUM_OCTAVESOutline = this.add(new Setting<>("NUM_OCTAVES", 5, 1, 30, n -> this.glowESP.getValue() == Shaders.glowESPmode.Smoke));
   public Setting<Integer> BSTARTOutline = this.add(new Setting<>("BSTART", 0, 0, 1000, n -> this.glowESP.getValue() == Shaders.glowESPmode.RainbowCube));
   public Setting<Integer> GSTARTOutline = this.add(new Setting<>("GSTART", 0, 0, 1000, n -> this.glowESP.getValue() == Shaders.glowESPmode.RainbowCube));
   public Setting<Integer> RSTARTOutline = this.add(new Setting<>("RSTART", 0, 0, 1000, n -> this.glowESP.getValue() == Shaders.glowESPmode.RainbowCube));
   public Setting<Integer> alphaValue = this.add(new Setting<>("Alpha Outline", 255, 0, 255));
   public Setting<Integer> WaveLenghtOutline = this.add(
      new Setting<>("Wave Lenght", 555, 0, 2000, n -> this.glowESP.getValue() == Shaders.glowESPmode.RainbowCube)
   );
   public Setting<Integer> redOutline = this.add(new Setting<>("Red", 0, 0, 100, n -> this.glowESP.getValue() == Shaders.glowESPmode.Astral));
   public Setting<Float> alphaFill = this.add(
      new Setting<>(
         "AlphaF",
         1.0F,
         0.0F,
         1.0F,
         object -> this.fillShader.getValue() == Shaders.fillShadermode.Astral || this.fillShader.getValue() == Shaders.fillShadermode.Smoke
      )
   );
   public Setting<Float> blueFill = this.add(new Setting<>("BlueF", 0.0F, 0.0F, 5.0F, object -> this.fillShader.getValue() == Shaders.fillShadermode.Astral));
   public Setting<Float> greenFill = this.add(new Setting<>("GreenF", 0.0F, 0.0F, 5.0F, object -> this.fillShader.getValue() == Shaders.fillShadermode.Astral));
   public Setting<Float> tauFill = this.add(
      new Setting<>("TAU", (float) (Math.PI * 2), 0.0F, 20.0F, object -> this.fillShader.getValue() == Shaders.fillShadermode.Aqua)
   );
   public Setting<Float> creepyFill = this.add(
      new Setting<>("Creepy", 1.0F, 0.0F, 20.0F, object -> this.fillShader.getValue() == Shaders.fillShadermode.Smoke)
   );
   public Setting<Float> moreGradientFill = this.add(
      new Setting<>("More Gradient", 1.0F, 0.0F, 10.0, object -> this.fillShader.getValue() == Shaders.fillShadermode.Smoke)
   );
   public Setting<Float> distfadingOutline = this.add(
      new Setting<>("distfading", 0.56F, 0.0F, 1.0F, object -> this.glowESP.getValue() == Shaders.glowESPmode.Astral)
   );
   public Setting<Float> titleOutline = this.add(new Setting<>("Tile", 0.45F, 0.0F, 1.3F, object -> this.glowESP.getValue() == Shaders.glowESPmode.Astral));
   public Setting<Float> stepSizeOutline = this.add(
      new Setting<>("Step Size", 0.19F, 0.0F, 0.7F, object -> this.glowESP.getValue() == Shaders.glowESPmode.Astral)
   );
   public Setting<Float> zoomOutline = this.add(new Setting<>("Zoom", 3.9F, 0.0F, 20.0F, object -> this.glowESP.getValue() == Shaders.glowESPmode.Astral));
   public Setting<Float> formuparam2Outline = this.add(
      new Setting<>("formuparam2", 0.89F, 0.0F, 1.5F, object -> this.glowESP.getValue() == Shaders.glowESPmode.Astral)
   );
   public Setting<Float> alphaOutline = this.add(
      new Setting<>(
         "Alpha", 1.0F, 0.0F, 1.0F, object -> this.glowESP.getValue() == Shaders.glowESPmode.Astral || this.glowESP.getValue() == Shaders.glowESPmode.Gradient
      )
   );
   public Setting<Float> blueOutline = this.add(new Setting<>("Blue", 0.0F, 0.0F, 5.0F, object -> this.glowESP.getValue() == Shaders.glowESPmode.Astral));
   public Setting<Float> greenOutline = this.add(new Setting<>("Green", 0.0F, 0.0F, 5.0F, object -> this.glowESP.getValue() == Shaders.glowESPmode.Astral));
   public Setting<Float> tauOutline = this.add(
      new Setting<>("TAU", (float) (Math.PI * 2), 0.0F, 20.0F, object -> this.glowESP.getValue() == Shaders.glowESPmode.Aqua)
   );
   public Setting<Float> creepyOutline = this.add(
      new Setting<>("Gradient Creepy", 1.0F, 0.0F, 20.0F, object -> this.glowESP.getValue() == Shaders.glowESPmode.Gradient)
   );
   public Setting<Float> moreGradientOutline = this.add(
      new Setting<>("More Gradient", 1.0F, 0.0F, 10.0F, object -> this.glowESP.getValue() == Shaders.glowESPmode.Gradient)
   );
   public Setting<Float> radOutline = this.add(
      new Setting<>("RAD Outline", 0.75F, 0.0F, 5.0F, object -> this.glowESP.getValue() == Shaders.glowESPmode.Circle)
   );
   public Setting<Float> PIOutline = this.add(
      new Setting<>("PI Outline", (float) Math.PI, 0.0F, 10.0F, object -> this.glowESP.getValue() == Shaders.glowESPmode.Circle)
   );
   public Setting<Color> colorImgOutline = this.add(
      new Setting<>(
         "ColorImgOutline",
         new Color(0, 0, 0, 255),
         n -> this.fillShader.getValue() == Shaders.fillShadermode.RainbowCube || this.glowESP.getValue() == Shaders.glowESPmode.RainbowCube
      )
   );
   public Setting<Color> thirdColorImgOutline = this.add(
      new Setting<>(
         "ThirdColorImg",
         new Color(0, 0, 0, 255),
         n -> this.fillShader.getValue() == Shaders.fillShadermode.Smoke || this.glowESP.getValue() == Shaders.glowESPmode.Smoke
      )
   );
   public Setting<Color> colorESP = this.add(new Setting<>("ColorESP", new Color(0, 0, 0, 255)));
   public Setting<Color> colorImgFill = this.add(new Setting<>("ColorImgFill", new Color(0, 0, 0, 255)));
   public Setting<Color> thirdColorImgFIll = this.add(
      new Setting<>(
         "SmokeImgFill",
         new Color(0, 0, 0, 255),
         n -> this.fillShader.getValue() == Shaders.fillShadermode.Smoke || this.glowESP.getValue() == Shaders.glowESPmode.Smoke
      )
   );
   public Setting<Color> secondColorImgFill = this.add(
      new Setting<>(
         "SmokeFill",
         new Color(0, 0, 0, 255),
         n -> this.fillShader.getValue() == Shaders.fillShadermode.Smoke || this.glowESP.getValue() == Shaders.glowESPmode.Smoke
      )
   );
   public boolean notShader = true;
   public static Shaders INSTANCE;

   @SubscribeEvent
   public void onRenderGameOverlay(RenderGameOverlayEvent renderGameOverlayEvent) {
      if (!fullNullCheck()) {
         if (renderGameOverlayEvent.getType() == ElementType.HOTBAR) {
            if (mc.world == null || mc.player == null) {
               return;
            }

            GlStateManager.pushMatrix();
            this.notShader = false;
            Color color = new Color(
               this.colorImgFill.getValue().getRed(),
               this.colorImgFill.getValue().getGreen(),
               this.colorImgFill.getValue().getBlue(),
               this.colorImgFill.getValue().getAlpha()
            );
            Color color2 = new Color(
               this.colorESP.getValue().getRed(), this.colorESP.getValue().getGreen(), this.colorESP.getValue().getBlue(), this.colorESP.getValue().getAlpha()
            );
            Color color3 = new Color(
               this.secondColorImgFill.getValue().getRed(),
               this.secondColorImgFill.getValue().getGreen(),
               this.secondColorImgFill.getValue().getBlue(),
               this.secondColorImgFill.getValue().getAlpha()
            );
            Color color4 = new Color(
               this.thirdColorImgOutline.getValue().getRed(),
               this.thirdColorImgOutline.getValue().getGreen(),
               this.thirdColorImgOutline.getValue().getBlue(),
               this.thirdColorImgOutline.getValue().getAlpha()
            );
            Color color5 = new Color(
               this.thirdColorImgFIll.getValue().getRed(),
               this.thirdColorImgFIll.getValue().getGreen(),
               this.thirdColorImgFIll.getValue().getBlue(),
               this.thirdColorImgFIll.getValue().getAlpha()
            );
            Color color6 = new Color(
               this.colorImgOutline.getValue().getRed(),
               this.colorImgOutline.getValue().getGreen(),
               this.colorImgOutline.getValue().getBlue(),
               this.colorImgOutline.getValue().getAlpha()
            );
            if (this.glowESP.getValue() != Shaders.glowESPmode.None && this.fillShader.getValue() != Shaders.fillShadermode.None) {
               this.getFill();
               switch((Shaders.fillShadermode)this.fillShader.getValue()) {
                  case Astral:
                     FlowShader.INSTANCE.startDraw(renderGameOverlayEvent.getPartialTicks());
                     this.renderPlayersFill(renderGameOverlayEvent.getPartialTicks());
                     FlowShader.INSTANCE
                        .stopDraw(
                           Color.WHITE,
                           1.0F,
                           1.0F,
                           this.duplicateFill.getValue(),
                           this.redFill.getValue().floatValue(),
                           this.greenFill.getValue(),
                           this.blueFill.getValue(),
                           this.alphaFill.getValue(),
                           this.iterationsFill.getValue(),
                           this.formuparam2Fill.getValue(),
                           this.zoomFill.getValue(),
                           this.volumStepsFill.getValue(),
                           this.stepSizeFill.getValue(),
                           this.titleFill.getValue(),
                           this.distfadingFill.getValue(),
                           this.saturationFill.getValue(),
                           0.0F,
                           this.fadeFill.getValue() ? 1 : 0
                        );
                     FlowShader.INSTANCE.update((double)(this.speedFill.getValue() / 1000.0F));
                     break;
                  case Aqua:
                     AquaShader.INSTANCE.startDraw(renderGameOverlayEvent.getPartialTicks());
                     this.renderPlayersFill(renderGameOverlayEvent.getPartialTicks());
                     AquaShader.INSTANCE
                        .stopDraw(color, 1.0F, 1.0F, this.duplicateFill.getValue(), this.MaxIterFill.getValue(), (double)this.tauFill.getValue().floatValue());
                     AquaShader.INSTANCE.update((double)(this.speedFill.getValue() / 1000.0F));
                     break;
                  case Smoke:
                     SmokeShader.INSTANCE.startDraw(renderGameOverlayEvent.getPartialTicks());
                     this.renderPlayersFill(renderGameOverlayEvent.getPartialTicks());
                     SmokeShader.INSTANCE
                        .stopDraw(Color.WHITE, 1.0F, 1.0F, this.duplicateFill.getValue(), color, color3, color5, this.NUM_OCTAVESFill.getValue());
                     SmokeShader.INSTANCE.update((double)(this.speedFill.getValue() / 1000.0F));
                     break;
                  case RainbowCube:
                     RainbowCubeShader.INSTANCE.startDraw(renderGameOverlayEvent.getPartialTicks());
                     this.renderPlayersFill(renderGameOverlayEvent.getPartialTicks());
                     RainbowCubeShader.INSTANCE
                        .stopDraw(
                           Color.WHITE,
                           1.0F,
                           1.0F,
                           this.duplicateFill.getValue(),
                           color,
                           this.WaveLenghtFIll.getValue(),
                           this.RSTARTFill.getValue(),
                           this.GSTARTFill.getValue(),
                           this.BSTARTFIll.getValue()
                        );
                     RainbowCubeShader.INSTANCE.update((double)(this.speedFill.getValue() / 1000.0F));
                     break;
                  case Gradient:
                     GradientShader.INSTANCE.startDraw(renderGameOverlayEvent.getPartialTicks());
                     this.renderPlayersFill(renderGameOverlayEvent.getPartialTicks());
                     GradientShader.INSTANCE
                        .stopDraw(
                           color2,
                           1.0F,
                           1.0F,
                           this.duplicateFill.getValue(),
                           this.moreGradientFill.getValue(),
                           this.creepyFill.getValue(),
                           this.alphaFill.getValue(),
                           this.NUM_OCTAVESFill.getValue()
                        );
                     GradientShader.INSTANCE.update((double)(this.speedFill.getValue() / 1000.0F));
                     break;
                  case Fill:
                     FillShader.INSTANCE.startDraw(renderGameOverlayEvent.getPartialTicks());
                     this.renderPlayersFill(renderGameOverlayEvent.getPartialTicks());
                     FillShader.INSTANCE.stopDraw(color);
                     FillShader.INSTANCE.update((double)(this.speedFill.getValue() / 1000.0F));
                     break;
                  case Circle:
                     CircleShader.INSTANCE.startDraw(renderGameOverlayEvent.getPartialTicks());
                     this.renderPlayersFill(renderGameOverlayEvent.getPartialTicks());
                     CircleShader.INSTANCE.stopDraw(this.duplicateFill.getValue(), color, this.PI.getValue(), this.rad.getValue());
                     CircleShader.INSTANCE.update((double)(this.speedFill.getValue() / 1000.0F));
                     break;
                  case Phobos:
                     PhobosShader.INSTANCE.startDraw(renderGameOverlayEvent.getPartialTicks());
                     this.renderPlayersFill(renderGameOverlayEvent.getPartialTicks());
                     PhobosShader.INSTANCE
                        .stopDraw(color, 1.0F, 1.0F, this.duplicateFill.getValue(), this.MaxIterFill.getValue(), (double)this.tauFill.getValue().floatValue());
                     PhobosShader.INSTANCE.update((double)(this.speedFill.getValue() / 1000.0F));
               }

               switch((Shaders.glowESPmode)this.glowESP.getValue()) {
                  case Color:
                     GlowShader.INSTANCE.startDraw(renderGameOverlayEvent.getPartialTicks());
                     this.renderPlayersOutline(renderGameOverlayEvent.getPartialTicks());
                     GlowShader.INSTANCE.stopDraw(color2, this.radius.getValue(), this.quality.getValue(), false, this.alphaValue.getValue());
                     break;
                  case RainbowCube:
                     RainbowCubeOutlineShader.INSTANCE.startDraw(renderGameOverlayEvent.getPartialTicks());
                     this.renderPlayersOutline(renderGameOverlayEvent.getPartialTicks());
                     RainbowCubeOutlineShader.INSTANCE
                        .stopDraw(
                           color2,
                           this.radius.getValue(),
                           this.quality.getValue(),
                           false,
                           this.alphaValue.getValue(),
                           this.duplicateOutline.getValue(),
                           color6,
                           this.WaveLenghtOutline.getValue(),
                           this.RSTARTOutline.getValue(),
                           this.GSTARTOutline.getValue(),
                           this.BSTARTOutline.getValue()
                        );
                     RainbowCubeOutlineShader.INSTANCE.update((double)(this.speedOutline.getValue() / 1000.0F));
                     break;
                  case Gradient:
                     GradientOutlineShader.INSTANCE.startDraw(renderGameOverlayEvent.getPartialTicks());
                     this.renderPlayersOutline(renderGameOverlayEvent.getPartialTicks());
                     GradientOutlineShader.INSTANCE
                        .stopDraw(
                           color2,
                           this.radius.getValue(),
                           this.quality.getValue(),
                           false,
                           this.alphaValue.getValue(),
                           this.duplicateOutline.getValue(),
                           this.moreGradientOutline.getValue(),
                           this.creepyOutline.getValue(),
                           this.alphaOutline.getValue(),
                           this.NUM_OCTAVESOutline.getValue()
                        );
                     GradientOutlineShader.INSTANCE.update((double)(this.speedOutline.getValue() / 1000.0F));
                     break;
                  case Astral:
                     AstralOutlineShader.INSTANCE.startDraw(renderGameOverlayEvent.getPartialTicks());
                     this.renderPlayersOutline(renderGameOverlayEvent.getPartialTicks());
                     AstralOutlineShader.INSTANCE
                        .stopDraw(
                           color2,
                           this.radius.getValue(),
                           this.quality.getValue(),
                           false,
                           this.alphaValue.getValue(),
                           this.duplicateOutline.getValue(),
                           this.redOutline.getValue().floatValue(),
                           this.greenOutline.getValue(),
                           this.blueOutline.getValue(),
                           this.alphaOutline.getValue(),
                           this.iterationsOutline.getValue(),
                           this.formuparam2Outline.getValue(),
                           this.zoomOutline.getValue(),
                           (float)this.volumStepsOutline.getValue().intValue(),
                           this.stepSizeOutline.getValue(),
                           this.titleOutline.getValue(),
                           this.distfadingOutline.getValue(),
                           this.saturationOutline.getValue(),
                           0.0F,
                           this.fadeOutline.getValue() ? 1 : 0
                        );
                     AstralOutlineShader.INSTANCE.update((double)(this.speedOutline.getValue() / 1000.0F));
                     break;
                  case Aqua:
                     AquaOutlineShader.INSTANCE.startDraw(renderGameOverlayEvent.getPartialTicks());
                     this.renderPlayersOutline(renderGameOverlayEvent.getPartialTicks());
                     AquaOutlineShader.INSTANCE
                        .stopDraw(
                           color2,
                           this.radius.getValue(),
                           this.quality.getValue(),
                           false,
                           this.alphaValue.getValue(),
                           this.duplicateOutline.getValue(),
                           this.MaxIterOutline.getValue(),
                           (double)this.tauOutline.getValue().floatValue()
                        );
                     AquaOutlineShader.INSTANCE.update((double)(this.speedOutline.getValue() / 1000.0F));
                     break;
                  case Circle:
                     CircleOutlineShader.INSTANCE.startDraw(renderGameOverlayEvent.getPartialTicks());
                     this.renderPlayersOutline(renderGameOverlayEvent.getPartialTicks());
                     CircleOutlineShader.INSTANCE
                        .stopDraw(
                           color2,
                           this.radius.getValue(),
                           this.quality.getValue(),
                           false,
                           this.alphaValue.getValue(),
                           this.duplicateOutline.getValue(),
                           this.PIOutline.getValue(),
                           this.radOutline.getValue()
                        );
                     CircleOutlineShader.INSTANCE.update((double)(this.speedOutline.getValue() / 1000.0F));
                     break;
                  case Smoke:
                     SmokeOutlineShader.INSTANCE.startDraw(renderGameOverlayEvent.getPartialTicks());
                     this.renderPlayersOutline(renderGameOverlayEvent.getPartialTicks());
                     SmokeOutlineShader.INSTANCE
                        .stopDraw(
                           color2,
                           this.radius.getValue(),
                           this.quality.getValue(),
                           false,
                           this.alphaValue.getValue(),
                           this.duplicateOutline.getValue(),
                           color3,
                           color4,
                           this.NUM_OCTAVESOutline.getValue()
                        );
                     SmokeOutlineShader.INSTANCE.update((double)(this.speedOutline.getValue() / 1000.0F));
               }
            } else {
               switch((Shaders.glowESPmode)this.glowESP.getValue()) {
                  case Color:
                     GlowShader.INSTANCE.startDraw(renderGameOverlayEvent.getPartialTicks());
                     this.renderPlayersOutline(renderGameOverlayEvent.getPartialTicks());
                     GlowShader.INSTANCE.stopDraw(color2, this.radius.getValue(), this.quality.getValue(), false, this.alphaValue.getValue());
                     break;
                  case RainbowCube:
                     RainbowCubeOutlineShader.INSTANCE.startDraw(renderGameOverlayEvent.getPartialTicks());
                     this.renderPlayersOutline(renderGameOverlayEvent.getPartialTicks());
                     RainbowCubeOutlineShader.INSTANCE
                        .stopDraw(
                           color2,
                           this.radius.getValue(),
                           this.quality.getValue(),
                           false,
                           this.alphaValue.getValue(),
                           this.duplicateOutline.getValue(),
                           color6,
                           this.WaveLenghtOutline.getValue(),
                           this.RSTARTOutline.getValue(),
                           this.GSTARTOutline.getValue(),
                           this.BSTARTOutline.getValue()
                        );
                     RainbowCubeOutlineShader.INSTANCE.update((double)(this.speedOutline.getValue() / 1000.0F));
                     break;
                  case Gradient:
                     GradientOutlineShader.INSTANCE.startDraw(renderGameOverlayEvent.getPartialTicks());
                     this.renderPlayersOutline(renderGameOverlayEvent.getPartialTicks());
                     GradientOutlineShader.INSTANCE
                        .stopDraw(
                           color2,
                           this.radius.getValue(),
                           this.quality.getValue(),
                           false,
                           this.alphaValue.getValue(),
                           this.duplicateOutline.getValue(),
                           this.moreGradientOutline.getValue(),
                           this.creepyOutline.getValue(),
                           this.alphaOutline.getValue(),
                           this.NUM_OCTAVESOutline.getValue()
                        );
                     GradientOutlineShader.INSTANCE.update((double)(this.speedOutline.getValue() / 1000.0F));
                     break;
                  case Astral:
                     AstralOutlineShader.INSTANCE.startDraw(renderGameOverlayEvent.getPartialTicks());
                     this.renderPlayersOutline(renderGameOverlayEvent.getPartialTicks());
                     AstralOutlineShader.INSTANCE
                        .stopDraw(
                           color2,
                           this.radius.getValue(),
                           this.quality.getValue(),
                           false,
                           this.alphaValue.getValue(),
                           this.duplicateOutline.getValue(),
                           this.redOutline.getValue().floatValue(),
                           this.greenOutline.getValue(),
                           this.blueOutline.getValue(),
                           this.alphaOutline.getValue(),
                           this.iterationsOutline.getValue(),
                           this.formuparam2Outline.getValue(),
                           this.zoomOutline.getValue(),
                           (float)this.volumStepsOutline.getValue().intValue(),
                           this.stepSizeOutline.getValue(),
                           this.titleOutline.getValue(),
                           this.distfadingOutline.getValue(),
                           this.saturationOutline.getValue(),
                           0.0F,
                           this.fadeOutline.getValue() ? 1 : 0
                        );
                     AstralOutlineShader.INSTANCE.update((double)(this.speedOutline.getValue() / 1000.0F));
                     break;
                  case Aqua:
                     AquaOutlineShader.INSTANCE.startDraw(renderGameOverlayEvent.getPartialTicks());
                     this.renderPlayersOutline(renderGameOverlayEvent.getPartialTicks());
                     AquaOutlineShader.INSTANCE
                        .stopDraw(
                           color2,
                           this.radius.getValue(),
                           this.quality.getValue(),
                           false,
                           this.alphaValue.getValue(),
                           this.duplicateOutline.getValue(),
                           this.MaxIterOutline.getValue(),
                           (double)this.tauOutline.getValue().floatValue()
                        );
                     AquaOutlineShader.INSTANCE.update((double)(this.speedOutline.getValue() / 1000.0F));
                     break;
                  case Circle:
                     CircleOutlineShader.INSTANCE.startDraw(renderGameOverlayEvent.getPartialTicks());
                     this.renderPlayersOutline(renderGameOverlayEvent.getPartialTicks());
                     CircleOutlineShader.INSTANCE
                        .stopDraw(
                           color2,
                           this.radius.getValue(),
                           this.quality.getValue(),
                           false,
                           this.alphaValue.getValue(),
                           this.duplicateOutline.getValue(),
                           this.PIOutline.getValue(),
                           this.radOutline.getValue()
                        );
                     CircleOutlineShader.INSTANCE.update((double)(this.speedOutline.getValue() / 1000.0F));
                     break;
                  case Smoke:
                     SmokeOutlineShader.INSTANCE.startDraw(renderGameOverlayEvent.getPartialTicks());
                     this.renderPlayersOutline(renderGameOverlayEvent.getPartialTicks());
                     SmokeOutlineShader.INSTANCE
                        .stopDraw(
                           color2,
                           this.radius.getValue(),
                           this.quality.getValue(),
                           false,
                           this.alphaValue.getValue(),
                           this.duplicateOutline.getValue(),
                           color3,
                           color4,
                           this.NUM_OCTAVESOutline.getValue()
                        );
                     SmokeOutlineShader.INSTANCE.update((double)(this.speedOutline.getValue() / 1000.0F));
               }

               switch((Shaders.fillShadermode)this.fillShader.getValue()) {
                  case Astral:
                     FlowShader.INSTANCE.startDraw(renderGameOverlayEvent.getPartialTicks());
                     this.renderPlayersFill(renderGameOverlayEvent.getPartialTicks());
                     FlowShader.INSTANCE
                        .stopDraw(
                           Color.WHITE,
                           1.0F,
                           1.0F,
                           this.duplicateFill.getValue(),
                           this.redFill.getValue().floatValue(),
                           this.greenFill.getValue(),
                           this.blueFill.getValue(),
                           this.alphaFill.getValue(),
                           this.iterationsFill.getValue(),
                           this.formuparam2Fill.getValue(),
                           this.zoomFill.getValue(),
                           this.volumStepsFill.getValue(),
                           this.stepSizeFill.getValue(),
                           this.titleFill.getValue(),
                           this.distfadingFill.getValue(),
                           this.saturationFill.getValue(),
                           0.0F,
                           this.fadeFill.getValue() ? 1 : 0
                        );
                     FlowShader.INSTANCE.update((double)(this.speedFill.getValue() / 1000.0F));
                     break;
                  case Aqua:
                     AquaShader.INSTANCE.startDraw(renderGameOverlayEvent.getPartialTicks());
                     this.renderPlayersFill(renderGameOverlayEvent.getPartialTicks());
                     AquaShader.INSTANCE
                        .stopDraw(color, 1.0F, 1.0F, this.duplicateFill.getValue(), this.MaxIterFill.getValue(), (double)this.tauFill.getValue().floatValue());
                     AquaShader.INSTANCE.update((double)(this.speedFill.getValue() / 1000.0F));
                     break;
                  case Smoke:
                     SmokeShader.INSTANCE.startDraw(renderGameOverlayEvent.getPartialTicks());
                     this.renderPlayersFill(renderGameOverlayEvent.getPartialTicks());
                     SmokeShader.INSTANCE
                        .stopDraw(Color.WHITE, 1.0F, 1.0F, this.duplicateFill.getValue(), color, color3, color5, this.NUM_OCTAVESFill.getValue());
                     SmokeShader.INSTANCE.update((double)(this.speedFill.getValue() / 1000.0F));
                     break;
                  case RainbowCube:
                     RainbowCubeShader.INSTANCE.startDraw(renderGameOverlayEvent.getPartialTicks());
                     this.renderPlayersFill(renderGameOverlayEvent.getPartialTicks());
                     RainbowCubeShader.INSTANCE
                        .stopDraw(
                           Color.WHITE,
                           1.0F,
                           1.0F,
                           this.duplicateFill.getValue(),
                           color,
                           this.WaveLenghtFIll.getValue(),
                           this.RSTARTFill.getValue(),
                           this.GSTARTFill.getValue(),
                           this.BSTARTFIll.getValue()
                        );
                     RainbowCubeShader.INSTANCE.update((double)(this.speedFill.getValue() / 1000.0F));
                     break;
                  case Gradient:
                     GradientShader.INSTANCE.startDraw(renderGameOverlayEvent.getPartialTicks());
                     this.renderPlayersFill(renderGameOverlayEvent.getPartialTicks());
                     GradientShader.INSTANCE
                        .stopDraw(
                           color2,
                           1.0F,
                           1.0F,
                           this.duplicateFill.getValue(),
                           this.moreGradientFill.getValue(),
                           this.creepyFill.getValue(),
                           this.alphaFill.getValue(),
                           this.NUM_OCTAVESFill.getValue()
                        );
                     GradientShader.INSTANCE.update((double)(this.speedFill.getValue() / 1000.0F));
                     break;
                  case Fill:
                     FillShader.INSTANCE.startDraw(renderGameOverlayEvent.getPartialTicks());
                     this.renderPlayersFill(renderGameOverlayEvent.getPartialTicks());
                     FillShader.INSTANCE.stopDraw(color);
                     FillShader.INSTANCE.update((double)(this.speedFill.getValue() / 1000.0F));
                     break;
                  case Circle:
                     CircleShader.INSTANCE.startDraw(renderGameOverlayEvent.getPartialTicks());
                     this.renderPlayersFill(renderGameOverlayEvent.getPartialTicks());
                     CircleShader.INSTANCE.stopDraw(this.duplicateFill.getValue(), color, this.PI.getValue(), this.rad.getValue());
                     CircleShader.INSTANCE.update((double)(this.speedFill.getValue() / 1000.0F));
                     break;
                  case Phobos:
                     PhobosShader.INSTANCE.startDraw(renderGameOverlayEvent.getPartialTicks());
                     this.renderPlayersFill(renderGameOverlayEvent.getPartialTicks());
                     PhobosShader.INSTANCE
                        .stopDraw(color, 1.0F, 1.0F, this.duplicateFill.getValue(), this.MaxIterFill.getValue(), (double)this.tauFill.getValue().floatValue());
                     PhobosShader.INSTANCE.update((double)(this.speedFill.getValue() / 1000.0F));
               }
            }

            this.notShader = true;
            GlStateManager.popMatrix();
         }
      }
   }

   void renderPlayersFill(float f) {
      boolean bl = this.rangeCheck.getValue();
      double d = (double)(this.minRange.getValue() * this.minRange.getValue());
      double d2 = (double)(this.maxRange.getValue() * this.maxRange.getValue());
      AtomicInteger atomicInteger = new AtomicInteger();
      int n = this.maxEntities.getValue();

      try {
         mc.world
            .loadedEntityList
            .stream()
            .filter(
               entity -> {
                  if (atomicInteger.getAndIncrement() > n) {
                     return false;
                  } else {
                     return entity instanceof EntityPlayer
                        ? (this.player.getValue() == Shaders.Player1.Fill || this.player.getValue() == Shaders.Player1.Both)
                           && (entity != mc.player || mc.gameSettings.thirdPersonView != 0)
                        : (
                           entity instanceof EntityEnderPearl
                              ? this.enderPearl.getValue() == Shaders.EPl.Fill || this.enderPearl.getValue() == Shaders.EPl.Both
                              : (
                                 entity instanceof EntityExpBottle
                                    ? this.xpBottle.getValue() == Shaders.XPBl.Fill || this.xpBottle.getValue() == Shaders.XPBl.Both
                                    : (
                                       entity instanceof EntityXPOrb
                                          ? this.xpOrb.getValue() == Shaders.XPl.Fill || this.xpOrb.getValue() == Shaders.XPl.Both
                                          : (
                                             entity instanceof EntityItem
                                                ? this.items.getValue() == Shaders.Itemsl.Fill || this.items.getValue() == Shaders.Itemsl.Both
                                                : (
                                                   entity instanceof EntityCreature
                                                      ? this.mob.getValue() == Shaders.Mob1.Fill || this.mob.getValue() == Shaders.Mob1.Both
                                                      : entity instanceof EntityEnderCrystal
                                                         && (
                                                            this.crystal.getValue() == Shaders.Crystal1.Fill
                                                               || this.crystal.getValue() == Shaders.Crystal1.Both
                                                         )
                                                )
                                          )
                                    )
                              )
                        );
                  }
               }
            )
            .filter(entity -> {
               if (!bl) {
                  return true;
               } else {
                  double d3 = mc.player.getDistanceSq(entity);
                  return d3 > d && d3 < d2;
               }
            })
            .forEach(entity -> mc.getRenderManager().renderEntityStatic(entity, f, true));
      } catch (Exception var10) {
      }
   }

   void renderPlayersOutline(float f) {
      boolean bl = this.rangeCheck.getValue();
      double d = (double)(this.minRange.getValue() * this.minRange.getValue());
      double d2 = (double)(this.maxRange.getValue() * this.maxRange.getValue());
      AtomicInteger atomicInteger = new AtomicInteger();
      int n = this.maxEntities.getValue();
      mc.world
         .addEntityToWorld(
            -1000,
            new EntityXPOrb(mc.world, mc.player.posX, mc.player.posY + 1000000.0, mc.player.posZ, 1)
         );
      mc.world
         .loadedEntityList
         .stream()
         .filter(
            entity -> {
               if (atomicInteger.getAndIncrement() > n) {
                  return false;
               } else {
                  return entity instanceof EntityPlayer
                     ? (this.player.getValue() == Shaders.Player1.Outline || this.player.getValue() == Shaders.Player1.Both)
                        && (entity != mc.player || mc.gameSettings.thirdPersonView != 0)
                     : (
                        entity instanceof EntityEnderPearl
                           ? this.enderPearl.getValue() == Shaders.EPl.Outline || this.enderPearl.getValue() == Shaders.EPl.Both
                           : (
                              entity instanceof EntityExpBottle
                                 ? this.xpBottle.getValue() == Shaders.XPBl.Outline || this.xpBottle.getValue() == Shaders.XPBl.Both
                                 : (
                                    entity instanceof EntityXPOrb
                                       ? this.xpOrb.getValue() == Shaders.XPl.Outline || this.xpOrb.getValue() == Shaders.XPl.Both
                                       : (
                                          entity instanceof EntityItem
                                             ? this.items.getValue() == Shaders.Itemsl.Outline || this.items.getValue() == Shaders.Itemsl.Both
                                             : (
                                                entity instanceof EntityCreature
                                                   ? this.mob.getValue() == Shaders.Mob1.Outline || this.mob.getValue() == Shaders.Mob1.Both
                                                   : entity instanceof EntityEnderCrystal
                                                      && (
                                                         this.crystal.getValue() == Shaders.Crystal1.Outline
                                                            || this.crystal.getValue() == Shaders.Crystal1.Both
                                                      )
                                             )
                                       )
                                 )
                           )
                     );
               }
            }
         )
         .filter(entity -> {
            if (!bl) {
               return true;
            } else {
               double d3 = mc.player.getDistanceSq(entity);
               return d3 > d && d3 < d2 || entity.getEntityId() == -1000;
            }
         })
         .forEach(entity -> mc.getRenderManager().renderEntityStatic(entity, f, true));
      mc.world.removeEntityFromWorld(-1000);
   }

   public Shaders() {
      super("Shaders", "test", Category.RENDER);
      INSTANCE = this;
   }

   @Override
   public void onTick() {
      if (this.Fpreset.getValue()) {
         this.fillShader.setValue(Shaders.fillShadermode.None);
         this.glowESP.setValue(Shaders.glowESPmode.Gradient);
         this.player.setValue(Shaders.Player1.Outline);
         this.crystal.setValue(Shaders.Crystal1.Outline);
         this.duplicateOutline.setValue(2.0F);
         this.speedOutline.setValue(30.0F);
         this.quality.setValue(0.6F);
         this.radius.setValue(1.7F);
         this.creepyOutline.setValue(1.0F);
         this.moreGradientOutline.setValue(1.0F);
         this.Fpreset.setValue(false);
      }

      if (this.default1.getValue()) {
         this.fillShader.setValue(Shaders.fillShadermode.None);
         this.glowESP.setValue(Shaders.glowESPmode.None);
         this.rangeCheck.setValue(true);
         this.maxRange.setValue(35.0F);
         this.minRange.setValue(0.0F);
         this.crystal.setValue(Shaders.Crystal1.None);
         this.player.setValue(Shaders.Player1.None);
         this.mob.setValue(Shaders.Mob1.None);
         this.items.setValue(Shaders.Itemsl.None);
         this.fadeFill.setValue(false);
         this.fadeOutline.setValue(false);
         this.duplicateOutline.setValue(1.0F);
         this.duplicateFill.setValue(1.0F);
         this.speedOutline.setValue(10.0F);
         this.speedFill.setValue(10.0F);
         this.quality.setValue(1.0F);
         this.radius.setValue(1.0F);
         this.rad.setValue(0.75F);
         this.PI.setValue((float) Math.PI);
         this.saturationFill.setValue(0.4F);
         this.distfadingFill.setValue(0.56F);
         this.titleFill.setValue(0.45F);
         this.stepSizeFill.setValue(0.2F);
         this.volumStepsFill.setValue(10.0F);
         this.zoomFill.setValue(3.9F);
         this.formuparam2Fill.setValue(0.89F);
         this.saturationOutline.setValue(0.4F);
         this.maxEntities.setValue(100);
         this.iterationsFill.setValue(4);
         this.redFill.setValue(0);
         this.MaxIterFill.setValue(5);
         this.NUM_OCTAVESFill.setValue(5);
         this.BSTARTFIll.setValue(0);
         this.GSTARTFill.setValue(0);
         this.RSTARTFill.setValue(0);
         this.WaveLenghtFIll.setValue(555);
         this.volumStepsOutline.setValue(10);
         this.iterationsOutline.setValue(4);
         this.MaxIterOutline.setValue(5);
         this.NUM_OCTAVESOutline.setValue(5);
         this.BSTARTOutline.setValue(0);
         this.GSTARTOutline.setValue(0);
         this.RSTARTOutline.setValue(0);
         this.alphaValue.setValue(255);
         this.WaveLenghtOutline.setValue(555);
         this.redOutline.setValue(0);
         this.alphaFill.setValue(1.0F);
         this.blueFill.setValue(0.0F);
         this.greenFill.setValue(0.0F);
         this.tauFill.setValue((float) (Math.PI * 2));
         this.creepyFill.setValue(1.0F);
         this.moreGradientFill.setValue(1.0F);
         this.distfadingOutline.setValue(0.56F);
         this.titleOutline.setValue(0.45F);
         this.stepSizeOutline.setValue(0.19F);
         this.zoomOutline.setValue(3.9F);
         this.formuparam2Outline.setValue(0.89F);
         this.alphaOutline.setValue(1.0F);
         this.blueOutline.setValue(0.0F);
         this.greenOutline.setValue(0.0F);
         this.tauOutline.setValue(0.0F);
         this.creepyOutline.setValue(1.0F);
         this.moreGradientOutline.setValue(1.0F);
         this.radOutline.setValue(0.75F);
         this.PIOutline.setValue((float) Math.PI);
         this.default1.setValue(false);
      }
   }

   void getFill() {
      switch((Shaders.fillShadermode)this.fillShader.getValue()) {
         case Astral:
            FlowShader.INSTANCE.update((double)(this.speedFill.getValue() / 1000.0F));
            break;
         case Aqua:
            AquaShader.INSTANCE.update((double)(this.speedFill.getValue() / 1000.0F));
            break;
         case Smoke:
            SmokeShader.INSTANCE.update((double)(this.speedFill.getValue() / 1000.0F));
            break;
         case RainbowCube:
            RainbowCubeShader.INSTANCE.update((double)(this.speedFill.getValue() / 1000.0F));
            break;
         case Gradient:
            GradientShader.INSTANCE.update((double)(this.speedFill.getValue() / 1000.0F));
            break;
         case Fill:
            FillShader.INSTANCE.update((double)(this.speedFill.getValue() / 1000.0F));
            break;
         case Circle:
            CircleShader.INSTANCE.update((double)(this.speedFill.getValue() / 1000.0F));
            break;
         case Phobos:
            PhobosShader.INSTANCE.update((double)(this.speedFill.getValue() / 1000.0F));
      }
   }

   public static enum Crystal1 {
      None,
      Fill,
      Outline,
      Both;
   }

   public static enum EPl {
      None,
      Fill,
      Outline,
      Both;
   }

   public static enum Itemsl {
      None,
      Fill,
      Outline,
      Both;
   }

   public static enum Mob1 {
      None,
      Fill,
      Outline,
      Both;
   }

   public static enum Player1 {
      None,
      Fill,
      Outline,
      Both;
   }

   public static enum XPBl {
      None,
      Fill,
      Outline,
      Both;
   }

   public static enum XPl {
      None,
      Fill,
      Outline,
      Both;
   }

   public static enum fillShadermode {
      Astral,
      Aqua,
      Smoke,
      RainbowCube,
      Gradient,
      Fill,
      Circle,
      Phobos,
      None;
   }

   public static enum glowESPmode {
      None,
      Color,
      Astral,
      RainbowCube,
      Gradient,
      Circle,
      Smoke,
      Aqua;
   }
}
