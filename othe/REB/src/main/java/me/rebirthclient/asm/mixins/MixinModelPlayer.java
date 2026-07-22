//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "G:\PortableSoft\JBY\MC_Deobf3000\1.12-MCP-Mappings"!

package me.rebirthclient.asm.mixins;

import java.awt.Color;
import me.rebirthclient.api.managers.Managers;
import me.rebirthclient.mod.modules.impl.render.Models;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelBox;
import net.minecraft.client.model.ModelPlayer;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({ModelPlayer.class})
public class MixinModelPlayer extends ModelBiped {
   @Shadow
   public ModelRenderer bipedLeftArmwear;
   @Shadow
   public ModelRenderer bipedRightArmwear;
   @Shadow
   public ModelRenderer bipedLeftLegwear;
   @Shadow
   public ModelRenderer bipedRightLegwear;
   @Shadow
   public ModelRenderer bipedBodyWear;
   public ModelRenderer left_leg;
   public ModelRenderer right_leg;
   public ModelRenderer body = new ModelRenderer(this);
   public ModelRenderer eye;
   public ModelRenderer rabbitBone;
   public ModelRenderer rabbitRleg;
   public ModelRenderer rabbitLarm;
   public ModelRenderer rabbitRarm;
   public ModelRenderer rabbitLleg;
   public ModelRenderer rabbitHead;
   public ModelRenderer fredhead;
   public ModelRenderer armLeft;
   public ModelRenderer legRight;
   public ModelRenderer legLeft;
   public ModelRenderer armRight;
   public ModelRenderer fredbody;
   public ModelRenderer armLeftpad2;
   public ModelRenderer torso;
   public ModelRenderer earRightpad_1;
   public ModelRenderer armRightpad2;
   public ModelRenderer legLeftpad;
   public ModelRenderer hat;
   public ModelRenderer legLeftpad2;
   public ModelRenderer armRight2;
   public ModelRenderer legRight2;
   public ModelRenderer earRightpad;
   public ModelRenderer armLeft2;
   public ModelRenderer frednose;
   public ModelRenderer earLeft;
   public ModelRenderer footRight;
   public ModelRenderer legRightpad2;
   public ModelRenderer legRightpad;
   public ModelRenderer armLeftpad;
   public ModelRenderer legLeft2;
   public ModelRenderer footLeft;
   public ModelRenderer hat2;
   public ModelRenderer armRightpad;
   public ModelRenderer earRight;
   public ModelRenderer crotch;
   public ModelRenderer jaw;
   public ModelRenderer handRight;
   public ModelRenderer handLeft;

   public MixinModelPlayer(float modelSize, boolean smallArmsIn) {
      super(modelSize, 0.0F, 64, 64);
      this.body.setRotationPoint(0.0F, 0.0F, 0.0F);
      this.body.setTextureOffset(34, 8).addBox(-4.0F, 6.0F, -3.0F, 8, 12, 6);
      this.body.setTextureOffset(15, 10).addBox(-3.0F, 9.0F, 3.0F, 6, 8, 3);
      this.body.setTextureOffset(26, 0).addBox(-3.0F, 5.0F, -3.0F, 6, 1, 6);
      this.eye = new ModelRenderer(this);
      this.eye.setTextureOffset(0, 10).addBox(-3.0F, 7.0F, -4.0F, 6, 4, 1);
      this.left_leg = new ModelRenderer(this);
      this.left_leg.setRotationPoint(-2.0F, 18.0F, 0.0F);
      this.left_leg.setTextureOffset(0, 0).addBox(2.9F, 0.0F, -1.5F, 3, 6, 3, 0.0F);
      this.right_leg = new ModelRenderer(this);
      this.right_leg.setRotationPoint(2.0F, 18.0F, 0.0F);
      this.right_leg.setTextureOffset(13, 0).addBox(-5.9F, 0.0F, -1.5F, 3, 6, 3);
      ModelRenderer footRight = new ModelRenderer(this, 22, 39);
      this.footRight = footRight;
      footRight.setRotationPoint(0.0F, 8.0F, 0.0F);
      this.footRight.addBox(-2.5F, 0.0F, -6.0F, 5, 3, 8, 0.0F);
      this.setRotationAngle(this.footRight, -0.034906585F, 0.0F, 0.0F);
      ModelRenderer earRight = new ModelRenderer(this, 8, 0);
      this.earRight = earRight;
      earRight.setRotationPoint(-4.5F, -5.5F, 0.0F);
      this.earRight.addBox(-1.0F, -3.0F, -0.5F, 2, 3, 1, 0.0F);
      this.setRotationAngle(this.earRight, 0.05235988F, 0.0F, (float) (-Math.PI / 3));
      ModelRenderer legLeftpad = new ModelRenderer(this, 48, 39);
      this.legLeftpad = legLeftpad;
      legLeftpad.setRotationPoint(0.0F, 0.5F, 0.0F);
      this.legLeftpad.addBox(-3.0F, 0.0F, -3.0F, 6, 9, 6, 0.0F);
      ModelRenderer earRightpad_1 = new ModelRenderer(this, 40, 39);
      this.earRightpad_1 = earRightpad_1;
      earRightpad_1.setRotationPoint(0.0F, -1.0F, 0.0F);
      this.earRightpad_1.addBox(-2.0F, -5.0F, -1.0F, 4, 4, 2, 0.0F);
      ModelRenderer legLeft = new ModelRenderer(this, 54, 10);
      this.legLeft = legLeft;
      legLeft.setRotationPoint(3.3F, 12.5F, 0.0F);
      this.legLeft.addBox(-1.0F, 0.0F, -1.0F, 2, 10, 2, 0.0F);
      ModelRenderer armRightpad2 = new ModelRenderer(this, 0, 26);
      this.armRightpad2 = armRightpad2;
      armRightpad2.setRotationPoint(0.0F, 0.5F, 0.0F);
      this.armRightpad2.addBox(-2.5F, 0.0F, -2.5F, 5, 7, 5, 0.0F);
      ModelRenderer handLeft = new ModelRenderer(this, 58, 56);
      this.handLeft = handLeft;
      handLeft.setRotationPoint(0.0F, 8.0F, 0.0F);
      this.handLeft.addBox(-2.0F, 0.0F, -2.5F, 4, 4, 5, 0.0F);
      this.setRotationAngle(this.handLeft, 0.0F, 0.0F, 0.05235988F);
      ModelRenderer armLeft = new ModelRenderer(this, 62, 10);
      this.armLeft = armLeft;
      armLeft.setRotationPoint(6.5F, -8.0F, 0.0F);
      this.armLeft.addBox(-1.0F, 0.0F, -1.0F, 2, 10, 2, 0.0F);
      this.setRotationAngle(this.armLeft, 0.0F, 0.0F, (float) (-Math.PI / 12));
      ModelRenderer legRight = new ModelRenderer(this, 90, 8);
      this.legRight = legRight;
      legRight.setRotationPoint(-3.3F, 12.5F, 0.0F);
      this.legRight.addBox(-1.0F, 0.0F, -1.0F, 2, 10, 2, 0.0F);
      ModelRenderer armLeft2 = new ModelRenderer(this, 90, 48);
      this.armLeft2 = armLeft2;
      armLeft2.setRotationPoint(0.0F, 9.6F, 0.0F);
      this.armLeft2.addBox(-1.0F, 0.0F, -1.0F, 2, 8, 2, 0.0F);
      this.setRotationAngle(this.armLeft2, (float) (-Math.PI / 18), 0.0F, 0.0F);
      ModelRenderer legRight2 = new ModelRenderer(this, 20, 35);
      this.legRight2 = legRight2;
      legRight2.setRotationPoint(0.0F, 9.6F, 0.0F);
      this.legRight2.addBox(-1.0F, 0.0F, -1.0F, 2, 8, 2, 0.0F);
      this.setRotationAngle(this.legRight2, 0.034906585F, 0.0F, 0.0F);
      ModelRenderer armLeftpad2 = new ModelRenderer(this, 0, 58);
      this.armLeftpad2 = armLeftpad2;
      armLeftpad2.setRotationPoint(0.0F, 0.5F, 0.0F);
      this.armLeftpad2.addBox(-2.5F, 0.0F, -2.5F, 5, 7, 5, 0.0F);
      ModelRenderer legLeft2 = new ModelRenderer(this, 72, 48);
      this.legLeft2 = legLeft2;
      legLeft2.setRotationPoint(0.0F, 9.6F, 0.0F);
      this.legLeft2.addBox(-1.0F, 0.0F, -1.0F, 2, 8, 2, 0.0F);
      this.setRotationAngle(this.legLeft2, 0.034906585F, 0.0F, 0.0F);
      ModelRenderer hat = new ModelRenderer(this, 70, 24);
      this.hat = hat;
      hat.setRotationPoint(0.0F, -8.4F, 0.0F);
      this.hat.addBox(-3.0F, -0.5F, -3.0F, 6, 1, 6, 0.0F);
      this.setRotationAngle(this.hat, (float) (-Math.PI / 180.0), 0.0F, 0.0F);
      ModelRenderer earRightpad = new ModelRenderer(this, 85, 0);
      this.earRightpad = earRightpad;
      earRightpad.setRotationPoint(0.0F, -1.0F, 0.0F);
      this.earRightpad.addBox(-2.0F, -5.0F, -1.0F, 4, 4, 2, 0.0F);
      ModelRenderer crotch = new ModelRenderer(this, 56, 0);
      this.crotch = crotch;
      crotch.setRotationPoint(0.0F, 9.5F, 0.0F);
      this.crotch.addBox(-5.5F, 0.0F, -3.5F, 11, 3, 7, 0.0F);
      ModelRenderer torso = new ModelRenderer(this, 8, 0);
      this.torso = torso;
      torso.setRotationPoint(0.0F, 0.0F, 0.0F);
      this.torso.addBox(-6.0F, -9.0F, -4.0F, 12, 18, 8, 0.0F);
      this.setRotationAngle(this.torso, (float) (Math.PI / 180.0), 0.0F, 0.0F);
      ModelRenderer armRight2 = new ModelRenderer(this, 90, 20);
      this.armRight2 = armRight2;
      armRight2.setRotationPoint(0.0F, 9.6F, 0.0F);
      this.armRight2.addBox(-1.0F, 0.0F, -1.0F, 2, 8, 2, 0.0F);
      this.setRotationAngle(this.armRight2, (float) (-Math.PI / 18), 0.0F, 0.0F);
      ModelRenderer handRight = new ModelRenderer(this, 20, 26);
      this.handRight = handRight;
      handRight.setRotationPoint(0.0F, 8.0F, 0.0F);
      this.handRight.addBox(-2.0F, 0.0F, -2.5F, 4, 4, 5, 0.0F);
      this.setRotationAngle(this.handRight, 0.0F, 0.0F, -0.05235988F);
      ModelRenderer fredbody = new ModelRenderer(this, 0, 0);
      this.fredbody = fredbody;
      fredbody.setRotationPoint(0.0F, -9.0F, 0.0F);
      this.fredbody.addBox(-1.0F, -14.0F, -1.0F, 2, 24, 2, 0.0F);
      ModelRenderer fredhead = new ModelRenderer(this, 39, 22);
      this.fredhead = fredhead;
      fredhead.setRotationPoint(0.0F, -13.0F, -0.5F);
      this.fredhead.addBox(-5.5F, -8.0F, -4.5F, 11, 8, 9, 0.0F);
      ModelRenderer legRightpad = new ModelRenderer(this, 73, 33);
      this.legRightpad = legRightpad;
      legRightpad.setRotationPoint(0.0F, 0.5F, 0.0F);
      this.legRightpad.addBox(-3.0F, 0.0F, -3.0F, 6, 9, 6, 0.0F);
      ModelRenderer frednose = new ModelRenderer(this, 17, 67);
      this.frednose = frednose;
      frednose.setRotationPoint(0.0F, -2.0F, -4.5F);
      this.frednose.addBox(-4.0F, -2.0F, -3.0F, 8, 4, 3, 0.0F);
      ModelRenderer legLeftpad2 = new ModelRenderer(this, 16, 50);
      this.legLeftpad2 = legLeftpad2;
      legLeftpad2.setRotationPoint(0.0F, 0.5F, 0.0F);
      this.legLeftpad2.addBox(-2.5F, 0.0F, -3.0F, 5, 7, 6, 0.0F);
      ModelRenderer armRightpad3 = new ModelRenderer(this, 70, 10);
      this.armRightpad = armRightpad3;
      armRightpad3.setRotationPoint(0.0F, 0.5F, 0.0F);
      this.armRightpad.addBox(-2.5F, 0.0F, -2.5F, 5, 9, 5, 0.0F);
      ModelRenderer armLeftpad3 = new ModelRenderer(this, 38, 54);
      this.armLeftpad = armLeftpad3;
      armLeftpad3.setRotationPoint(0.0F, 0.5F, 0.0F);
      this.armLeftpad.addBox(-2.5F, 0.0F, -2.5F, 5, 9, 5, 0.0F);
      ModelRenderer hat2 = new ModelRenderer(this, 78, 61);
      this.hat2 = hat2;
      hat2.setRotationPoint(0.0F, 0.1F, 0.0F);
      this.hat2.addBox(-2.0F, -4.0F, -2.0F, 4, 4, 4, 0.0F);
      this.setRotationAngle(this.hat2, (float) (-Math.PI / 180.0), 0.0F, 0.0F);
      ModelRenderer legRightpad2 = new ModelRenderer(this, 0, 39);
      this.legRightpad2 = legRightpad2;
      legRightpad2.setRotationPoint(0.0F, 0.5F, 0.0F);
      this.legRightpad2.addBox(-2.5F, 0.0F, -3.0F, 5, 7, 6, 0.0F);
      ModelRenderer jaw = new ModelRenderer(this, 49, 65);
      this.jaw = jaw;
      jaw.setRotationPoint(0.0F, 0.5F, 0.0F);
      this.jaw.addBox(-5.0F, 0.0F, -4.5F, 10, 3, 9, 0.0F);
      this.setRotationAngle(this.jaw, 0.08726646F, 0.0F, 0.0F);
      ModelRenderer armRight3 = new ModelRenderer(this, 48, 0);
      this.armRight = armRight3;
      armRight3.setRotationPoint(-6.5F, -8.0F, 0.0F);
      this.armRight.addBox(-1.0F, 0.0F, -1.0F, 2, 10, 2, 0.0F);
      this.setRotationAngle(this.armRight, 0.0F, 0.0F, (float) (Math.PI / 12));
      ModelRenderer footLeft = new ModelRenderer(this, 72, 50);
      this.footLeft = footLeft;
      footLeft.setRotationPoint(0.0F, 8.0F, 0.0F);
      this.footLeft.addBox(-2.5F, 0.0F, -6.0F, 5, 3, 8, 0.0F);
      this.setRotationAngle(this.footLeft, -0.034906585F, 0.0F, 0.0F);
      ModelRenderer earLeft = new ModelRenderer(this, 40, 0);
      this.earLeft = earLeft;
      earLeft.setRotationPoint(4.5F, -5.5F, 0.0F);
      this.earLeft.addBox(-1.0F, -3.0F, -0.5F, 2, 3, 1, 0.0F);
      this.setRotationAngle(this.earLeft, 0.05235988F, 0.0F, (float) (Math.PI / 3));
      this.legRight2.addChild(this.footRight);
      this.fredhead.addChild(this.earRight);
      this.legLeft.addChild(this.legLeftpad);
      this.earLeft.addChild(this.earRightpad_1);
      this.fredbody.addChild(this.legLeft);
      this.armRight2.addChild(this.armRightpad2);
      this.armLeft2.addChild(this.handLeft);
      this.fredbody.addChild(this.armLeft);
      this.fredbody.addChild(this.legRight);
      this.armLeft.addChild(this.armLeft2);
      this.legRight.addChild(this.legRight2);
      this.armLeft2.addChild(this.armLeftpad2);
      this.legLeft.addChild(this.legLeft2);
      this.fredhead.addChild(this.hat);
      this.earRight.addChild(this.earRightpad);
      this.fredbody.addChild(this.crotch);
      this.fredbody.addChild(this.torso);
      this.armRight.addChild(this.armRight2);
      this.armRight2.addChild(this.handRight);
      this.fredbody.addChild(this.fredhead);
      this.legRight.addChild(this.legRightpad);
      this.fredhead.addChild(this.frednose);
      this.legLeft2.addChild(this.legLeftpad2);
      this.armRight.addChild(this.armRightpad);
      this.armLeft.addChild(this.armLeftpad);
      this.hat.addChild(this.hat2);
      this.legRight2.addChild(this.legRightpad2);
      this.fredhead.addChild(this.jaw);
      this.fredbody.addChild(this.armRight);
      this.legLeft2.addChild(this.footLeft);
      this.fredhead.addChild(this.earLeft);
   }

   public void generatemodel() {
      this.body = new ModelRenderer(this);
      this.body.setRotationPoint(0.0F, 0.0F, 0.0F);
      this.body.setTextureOffset(34, 8).addBox(-4.0F, 6.0F, -3.0F, 8, 12, 6);
      this.body.setTextureOffset(15, 10).addBox(-3.0F, 9.0F, 3.0F, 6, 8, 3);
      this.body.setTextureOffset(26, 0).addBox(-3.0F, 5.0F, -3.0F, 6, 1, 6);
      this.eye = new ModelRenderer(this);
      this.eye.setTextureOffset(0, 10).addBox(-3.0F, 7.0F, -4.0F, 6, 4, 1);
      this.left_leg = new ModelRenderer(this);
      this.left_leg.setRotationPoint(-2.0F, 18.0F, 0.0F);
      this.left_leg.setTextureOffset(0, 0).addBox(2.9F, 0.0F, -1.5F, 3, 6, 3, 0.0F);
      this.right_leg = new ModelRenderer(this);
      this.right_leg.setRotationPoint(2.0F, 18.0F, 0.0F);
      this.right_leg.setTextureOffset(13, 0).addBox(-5.9F, 0.0F, -1.5F, 3, 6, 3);
      (this.rabbitBone = new ModelRenderer(this)).setRotationPoint(0.0F, 24.0F, 0.0F);
      this.rabbitBone.cubeList.add(new ModelBox(this.rabbitBone, 28, 45, -5.0F, -13.0F, -5.0F, 10, 11, 8, 0.0F, false));
      (this.rabbitRleg = new ModelRenderer(this)).setRotationPoint(-3.0F, -2.0F, -1.0F);
      this.rabbitBone.addChild(this.rabbitRleg);
      this.rabbitRleg.cubeList.add(new ModelBox(this.rabbitRleg, 0, 0, -2.0F, 0.0F, -2.0F, 4, 2, 4, 0.0F, false));
      (this.rabbitLarm = new ModelRenderer(this)).setRotationPoint(5.0F, -13.0F, -1.0F);
      this.setRotationAngle(this.rabbitLarm, 0.0F, 0.0F, -0.0873F);
      this.rabbitBone.addChild(this.rabbitLarm);
      this.rabbitLarm.cubeList.add(new ModelBox(this.rabbitLarm, 0, 0, 0.0F, 0.0F, -2.0F, 2, 8, 4, 0.0F, false));
      (this.rabbitRarm = new ModelRenderer(this)).setRotationPoint(-5.0F, -13.0F, -1.0F);
      this.setRotationAngle(this.rabbitRarm, 0.0F, 0.0F, 0.0873F);
      this.rabbitBone.addChild(this.rabbitRarm);
      this.rabbitRarm.cubeList.add(new ModelBox(this.rabbitRarm, 0, 0, -2.0F, 0.0F, -2.0F, 2, 8, 4, 0.0F, false));
      (this.rabbitLleg = new ModelRenderer(this)).setRotationPoint(3.0F, -2.0F, -1.0F);
      this.rabbitBone.addChild(this.rabbitLleg);
      this.rabbitLleg.cubeList.add(new ModelBox(this.rabbitLleg, 0, 0, -2.0F, 0.0F, -2.0F, 4, 2, 4, 0.0F, false));
      (this.rabbitHead = new ModelRenderer(this)).setRotationPoint(0.0F, -14.0F, -1.0F);
      this.rabbitBone.addChild(this.rabbitHead);
      this.rabbitHead.cubeList.add(new ModelBox(this.rabbitHead, 0, 0, -3.0F, 0.0F, -4.0F, 6, 1, 6, 0.0F, false));
      this.rabbitHead.cubeList.add(new ModelBox(this.rabbitHead, 56, 0, -5.0F, -9.0F, -5.0F, 2, 3, 2, 0.0F, false));
      this.rabbitHead.cubeList.add(new ModelBox(this.rabbitHead, 56, 0, 3.0F, -9.0F, -5.0F, 2, 3, 2, 0.0F, true));
      this.rabbitHead.cubeList.add(new ModelBox(this.rabbitHead, 0, 45, -4.0F, -11.0F, -4.0F, 8, 11, 8, 0.0F, false));
      this.rabbitHead.cubeList.add(new ModelBox(this.rabbitHead, 46, 0, 1.0F, -20.0F, 0.0F, 3, 9, 1, 0.0F, false));
      this.rabbitHead.cubeList.add(new ModelBox(this.rabbitHead, 46, 0, -4.0F, -20.0F, 0.0F, 3, 9, 1, 0.0F, false));
      this.textureWidth = 100;
      this.textureHeight = 80;
      ModelRenderer footRight = new ModelRenderer(this, 22, 39);
      this.footRight = footRight;
      footRight.setRotationPoint(0.0F, 8.0F, 0.0F);
      this.footRight.addBox(-2.5F, 0.0F, -6.0F, 5, 3, 8, 0.0F);
      this.setRotationAngle(this.footRight, -0.034906585F, 0.0F, 0.0F);
      ModelRenderer earRight = new ModelRenderer(this, 8, 0);
      this.earRight = earRight;
      earRight.setRotationPoint(-4.5F, -5.5F, 0.0F);
      this.earRight.addBox(-1.0F, -3.0F, -0.5F, 2, 3, 1, 0.0F);
      this.setRotationAngle(this.earRight, 0.05235988F, 0.0F, (float) (-Math.PI / 3));
      ModelRenderer legLeftpad = new ModelRenderer(this, 48, 39);
      this.legLeftpad = legLeftpad;
      legLeftpad.setRotationPoint(0.0F, 0.5F, 0.0F);
      this.legLeftpad.addBox(-3.0F, 0.0F, -3.0F, 6, 9, 6, 0.0F);
      ModelRenderer earRightpad_1 = new ModelRenderer(this, 40, 39);
      this.earRightpad_1 = earRightpad_1;
      earRightpad_1.setRotationPoint(0.0F, -1.0F, 0.0F);
      this.earRightpad_1.addBox(-2.0F, -5.0F, -1.0F, 4, 4, 2, 0.0F);
      ModelRenderer legLeft = new ModelRenderer(this, 54, 10);
      this.legLeft = legLeft;
      legLeft.setRotationPoint(3.3F, 12.5F, 0.0F);
      this.legLeft.addBox(-1.0F, 0.0F, -1.0F, 2, 10, 2, 0.0F);
      ModelRenderer armRightpad2 = new ModelRenderer(this, 0, 26);
      this.armRightpad2 = armRightpad2;
      armRightpad2.setRotationPoint(0.0F, 0.5F, 0.0F);
      this.armRightpad2.addBox(-2.5F, 0.0F, -2.5F, 5, 7, 5, 0.0F);
      ModelRenderer handLeft = new ModelRenderer(this, 58, 56);
      this.handLeft = handLeft;
      handLeft.setRotationPoint(0.0F, 8.0F, 0.0F);
      this.handLeft.addBox(-2.0F, 0.0F, -2.5F, 4, 4, 5, 0.0F);
      this.setRotationAngle(this.handLeft, 0.0F, 0.0F, 0.05235988F);
      ModelRenderer armLeft = new ModelRenderer(this, 62, 10);
      this.armLeft = armLeft;
      armLeft.setRotationPoint(6.5F, -8.0F, 0.0F);
      this.armLeft.addBox(-1.0F, 0.0F, -1.0F, 2, 10, 2, 0.0F);
      this.setRotationAngle(this.armLeft, 0.0F, 0.0F, (float) (-Math.PI / 12));
      ModelRenderer legRight = new ModelRenderer(this, 90, 8);
      this.legRight = legRight;
      legRight.setRotationPoint(-3.3F, 12.5F, 0.0F);
      this.legRight.addBox(-1.0F, 0.0F, -1.0F, 2, 10, 2, 0.0F);
      ModelRenderer armLeft2 = new ModelRenderer(this, 90, 48);
      this.armLeft2 = armLeft2;
      armLeft2.setRotationPoint(0.0F, 9.6F, 0.0F);
      this.armLeft2.addBox(-1.0F, 0.0F, -1.0F, 2, 8, 2, 0.0F);
      this.setRotationAngle(this.armLeft2, (float) (-Math.PI / 18), 0.0F, 0.0F);
      ModelRenderer legRight2 = new ModelRenderer(this, 20, 35);
      this.legRight2 = legRight2;
      legRight2.setRotationPoint(0.0F, 9.6F, 0.0F);
      this.legRight2.addBox(-1.0F, 0.0F, -1.0F, 2, 8, 2, 0.0F);
      this.setRotationAngle(this.legRight2, 0.034906585F, 0.0F, 0.0F);
      ModelRenderer armLeftpad2 = new ModelRenderer(this, 0, 58);
      this.armLeftpad2 = armLeftpad2;
      armLeftpad2.setRotationPoint(0.0F, 0.5F, 0.0F);
      this.armLeftpad2.addBox(-2.5F, 0.0F, -2.5F, 5, 7, 5, 0.0F);
      ModelRenderer legLeft2 = new ModelRenderer(this, 72, 48);
      this.legLeft2 = legLeft2;
      legLeft2.setRotationPoint(0.0F, 9.6F, 0.0F);
      this.legLeft2.addBox(-1.0F, 0.0F, -1.0F, 2, 8, 2, 0.0F);
      this.setRotationAngle(this.legLeft2, 0.034906585F, 0.0F, 0.0F);
      ModelRenderer hat = new ModelRenderer(this, 70, 24);
      this.hat = hat;
      hat.setRotationPoint(0.0F, -8.4F, 0.0F);
      this.hat.addBox(-3.0F, -0.5F, -3.0F, 6, 1, 6, 0.0F);
      this.setRotationAngle(this.hat, (float) (-Math.PI / 180.0), 0.0F, 0.0F);
      ModelRenderer earRightpad = new ModelRenderer(this, 85, 0);
      this.earRightpad = earRightpad;
      earRightpad.setRotationPoint(0.0F, -1.0F, 0.0F);
      this.earRightpad.addBox(-2.0F, -5.0F, -1.0F, 4, 4, 2, 0.0F);
      ModelRenderer crotch = new ModelRenderer(this, 56, 0);
      this.crotch = crotch;
      crotch.setRotationPoint(0.0F, 9.5F, 0.0F);
      this.crotch.addBox(-5.5F, 0.0F, -3.5F, 11, 3, 7, 0.0F);
      ModelRenderer torso = new ModelRenderer(this, 8, 0);
      this.torso = torso;
      torso.setRotationPoint(0.0F, 0.0F, 0.0F);
      this.torso.addBox(-6.0F, -9.0F, -4.0F, 12, 18, 8, 0.0F);
      this.setRotationAngle(this.torso, (float) (Math.PI / 180.0), 0.0F, 0.0F);
      ModelRenderer armRight2 = new ModelRenderer(this, 90, 20);
      this.armRight2 = armRight2;
      armRight2.setRotationPoint(0.0F, 9.6F, 0.0F);
      this.armRight2.addBox(-1.0F, 0.0F, -1.0F, 2, 8, 2, 0.0F);
      this.setRotationAngle(this.armRight2, (float) (-Math.PI / 18), 0.0F, 0.0F);
      ModelRenderer handRight = new ModelRenderer(this, 20, 26);
      this.handRight = handRight;
      handRight.setRotationPoint(0.0F, 8.0F, 0.0F);
      this.handRight.addBox(-2.0F, 0.0F, -2.5F, 4, 4, 5, 0.0F);
      this.setRotationAngle(this.handRight, 0.0F, 0.0F, -0.05235988F);
      ModelRenderer fredbody = new ModelRenderer(this, 0, 0);
      this.fredbody = fredbody;
      fredbody.setRotationPoint(0.0F, -9.0F, 0.0F);
      this.fredbody.addBox(-1.0F, -14.0F, -1.0F, 2, 24, 2, 0.0F);
      ModelRenderer fredhead = new ModelRenderer(this, 39, 22);
      this.fredhead = fredhead;
      fredhead.setRotationPoint(0.0F, -13.0F, -0.5F);
      this.fredhead.addBox(-5.5F, -8.0F, -4.5F, 11, 8, 9, 0.0F);
      ModelRenderer legRightpad = new ModelRenderer(this, 73, 33);
      this.legRightpad = legRightpad;
      legRightpad.setRotationPoint(0.0F, 0.5F, 0.0F);
      this.legRightpad.addBox(-3.0F, 0.0F, -3.0F, 6, 9, 6, 0.0F);
      ModelRenderer frednose = new ModelRenderer(this, 17, 67);
      this.frednose = frednose;
      frednose.setRotationPoint(0.0F, -2.0F, -4.5F);
      this.frednose.addBox(-4.0F, -2.0F, -3.0F, 8, 4, 3, 0.0F);
      ModelRenderer legLeftpad2 = new ModelRenderer(this, 16, 50);
      this.legLeftpad2 = legLeftpad2;
      legLeftpad2.setRotationPoint(0.0F, 0.5F, 0.0F);
      this.legLeftpad2.addBox(-2.5F, 0.0F, -3.0F, 5, 7, 6, 0.0F);
      ModelRenderer armRightpad3 = new ModelRenderer(this, 70, 10);
      this.armRightpad = armRightpad3;
      armRightpad3.setRotationPoint(0.0F, 0.5F, 0.0F);
      this.armRightpad.addBox(-2.5F, 0.0F, -2.5F, 5, 9, 5, 0.0F);
      ModelRenderer armLeftpad3 = new ModelRenderer(this, 38, 54);
      this.armLeftpad = armLeftpad3;
      armLeftpad3.setRotationPoint(0.0F, 0.5F, 0.0F);
      this.armLeftpad.addBox(-2.5F, 0.0F, -2.5F, 5, 9, 5, 0.0F);
      ModelRenderer hat2 = new ModelRenderer(this, 78, 61);
      this.hat2 = hat2;
      hat2.setRotationPoint(0.0F, 0.1F, 0.0F);
      this.hat2.addBox(-2.0F, -4.0F, -2.0F, 4, 4, 4, 0.0F);
      this.setRotationAngle(this.hat2, (float) (-Math.PI / 180.0), 0.0F, 0.0F);
      ModelRenderer legRightpad2 = new ModelRenderer(this, 0, 39);
      this.legRightpad2 = legRightpad2;
      legRightpad2.setRotationPoint(0.0F, 0.5F, 0.0F);
      this.legRightpad2.addBox(-2.5F, 0.0F, -3.0F, 5, 7, 6, 0.0F);
      ModelRenderer jaw = new ModelRenderer(this, 49, 65);
      this.jaw = jaw;
      jaw.setRotationPoint(0.0F, 0.5F, 0.0F);
      this.jaw.addBox(-5.0F, 0.0F, -4.5F, 10, 3, 9, 0.0F);
      this.setRotationAngle(this.jaw, 0.08726646F, 0.0F, 0.0F);
      ModelRenderer armRight3 = new ModelRenderer(this, 48, 0);
      this.armRight = armRight3;
      armRight3.setRotationPoint(-6.5F, -8.0F, 0.0F);
      this.armRight.addBox(-1.0F, 0.0F, -1.0F, 2, 10, 2, 0.0F);
      this.setRotationAngle(this.armRight, 0.0F, 0.0F, (float) (Math.PI / 12));
      ModelRenderer footLeft = new ModelRenderer(this, 72, 50);
      this.footLeft = footLeft;
      footLeft.setRotationPoint(0.0F, 8.0F, 0.0F);
      this.footLeft.addBox(-2.5F, 0.0F, -6.0F, 5, 3, 8, 0.0F);
      this.setRotationAngle(this.footLeft, -0.034906585F, 0.0F, 0.0F);
      ModelRenderer earLeft = new ModelRenderer(this, 40, 0);
      this.earLeft = earLeft;
      earLeft.setRotationPoint(4.5F, -5.5F, 0.0F);
      this.earLeft.addBox(-1.0F, -3.0F, -0.5F, 2, 3, 1, 0.0F);
      this.setRotationAngle(this.earLeft, 0.05235988F, 0.0F, (float) (Math.PI / 3));
      this.legRight2.addChild(this.footRight);
      this.fredhead.addChild(this.earRight);
      this.legLeft.addChild(this.legLeftpad);
      this.earLeft.addChild(this.earRightpad_1);
      this.fredbody.addChild(this.legLeft);
      this.armRight2.addChild(this.armRightpad2);
      this.armLeft2.addChild(this.handLeft);
      this.fredbody.addChild(this.armLeft);
      this.fredbody.addChild(this.legRight);
      this.armLeft.addChild(this.armLeft2);
      this.legRight.addChild(this.legRight2);
      this.armLeft2.addChild(this.armLeftpad2);
      this.legLeft.addChild(this.legLeft2);
      this.fredhead.addChild(this.hat);
      this.earRight.addChild(this.earRightpad);
      this.fredbody.addChild(this.crotch);
      this.fredbody.addChild(this.torso);
      this.armRight.addChild(this.armRight2);
      this.armRight2.addChild(this.handRight);
      this.fredbody.addChild(this.fredhead);
      this.legRight.addChild(this.legRightpad);
      this.fredhead.addChild(this.frednose);
      this.legLeft2.addChild(this.legLeftpad2);
      this.armRight.addChild(this.armRightpad);
      this.armLeft.addChild(this.armLeftpad);
      this.hat.addChild(this.hat2);
      this.legRight2.addChild(this.legRightpad2);
      this.fredhead.addChild(this.jaw);
      this.fredbody.addChild(this.armRight);
      this.legLeft2.addChild(this.footLeft);
      this.fredhead.addChild(this.earLeft);
   }

   public void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z) {
      modelRenderer.rotateAngleX = x;
      modelRenderer.rotateAngleY = y;
      modelRenderer.rotateAngleZ = z;
   }

   @Inject(
      method = {"render"},
      at = {@At("HEAD")},
      cancellable = true
   )
   public void renderHook(
      Entity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale, CallbackInfo ci
   ) {
      Models customModel = Models.INSTANCE;
      if (customModel.isOn()) {
         ci.cancel();
         this.renderCustom(entityIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
      }
   }

   private void glColor(Color color) {
      float red = (float)color.getRed() / 255.0F;
      float green = (float)color.getGreen() / 255.0F;
      float blue = (float)color.getBlue() / 255.0F;
      float alpha = (float)color.getAlpha() / 255.0F;
      GlStateManager.color(red, green, blue, alpha);
   }

   public void renderCustom(Entity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
      if (this.left_leg == null) {
         this.generatemodel();
      }

      Models customModel = Models.INSTANCE;
      GlStateManager.pushMatrix();
      if ((
            !customModel.onlySelf.getValue()
               || entityIn == Minecraft.getMinecraft().player
               || Managers.FRIENDS.isFriend(entityIn.getName()) && customModel.friends.getValue()
         )
         && customModel.isOn()) {
         if (customModel.Mode.getValue() == Models.mode.AmongUs) {
            boolean flag = entityIn instanceof EntityLivingBase && ((EntityLivingBase)entityIn).getTicksElytraFlying() > 4;
            this.bipedHead.rotateAngleY = netHeadYaw * (float) (Math.PI / 180.0);
            if (flag) {
               this.bipedHead.rotateAngleX = (float) (-Math.PI / 4);
            } else {
               this.bipedHead.rotateAngleX = headPitch * (float) (Math.PI / 180.0);
            }

            this.bipedBody.rotateAngleY = 0.0F;
            this.bipedRightArm.rotationPointZ = 0.0F;
            this.bipedRightArm.rotationPointX = -5.0F;
            this.bipedLeftArm.rotationPointZ = 0.0F;
            this.bipedLeftArm.rotationPointX = 5.0F;
            float f = 1.0F;
            if (flag) {
               f = (float)(
                  entityIn.motionX * entityIn.motionX
                     + entityIn.motionY * entityIn.motionY
                     + entityIn.motionZ * entityIn.motionZ
               );
               f /= 0.2F;
               f = f * f * f;
            }

            if (f < 1.0F) {
               f = 1.0F;
            }

            this.bipedRightArm.rotateAngleX = MathHelper.cos(limbSwing * 0.6662F + (float) Math.PI) * 2.0F * limbSwingAmount * 0.5F / f;
            this.bipedLeftArm.rotateAngleX = MathHelper.cos(limbSwing * 0.6662F) * 2.0F * limbSwingAmount * 0.5F / f;
            this.bipedRightArm.rotateAngleZ = 0.0F;
            this.bipedLeftArm.rotateAngleZ = 0.0F;
            this.right_leg.rotateAngleX = MathHelper.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount / f;
            this.left_leg.rotateAngleX = MathHelper.cos(limbSwing * 0.6662F + (float) Math.PI) * 1.4F * limbSwingAmount / f;
            this.right_leg.rotateAngleY = 0.0F;
            this.left_leg.rotateAngleY = 0.0F;
            this.right_leg.rotateAngleZ = 0.0F;
            this.left_leg.rotateAngleZ = 0.0F;
            Color bodyColor = customModel.bodyColor.getValue();
            Color eyeColor = customModel.eyeColor.getValue();
            Color legsColor = customModel.legsColor.getValue();
            if (customModel.friendHighlight.getValue() && Managers.FRIENDS.isFriend(entityIn.getName())) {
               bodyColor = Color.GREEN;
               eyeColor = Color.WHITE;
               legsColor = Color.GREEN;
            }

            if (this.isChild) {
               GlStateManager.scale(0.5F, 0.5F, 0.5F);
               GlStateManager.translate(0.0F, 24.0F * scale, 0.0F);
               this.body.render(scale);
               this.left_leg.render(scale);
               this.right_leg.render(scale);
            } else {
               GlStateManager.translate(0.0, -0.8, 0.0);
               GlStateManager.scale(1.8, 1.6, 1.6);
               this.glColor(bodyColor);
               GlStateManager.translate(0.0, 0.15, 0.0);
               this.body.render(scale);
               this.glColor(eyeColor);
               this.eye.render(scale);
               this.glColor(legsColor);
               GlStateManager.translate(0.0, -0.15, 0.0);
               this.left_leg.render(scale);
               this.right_leg.render(scale);
               GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            }
         } else if (customModel.isOn() && customModel.Mode.getValue() == Models.mode.Rabbit) {
            GlStateManager.pushMatrix();
            GlStateManager.scale(1.25, 1.25, 1.25);
            GlStateManager.translate(0.0, -0.3, 0.0);
            this.rabbitHead.rotateAngleX = this.bipedHead.rotateAngleX;
            this.rabbitHead.rotateAngleY = this.bipedHead.rotateAngleY;
            this.rabbitHead.rotateAngleZ = this.bipedHead.rotateAngleZ;
            this.rabbitLarm.rotateAngleX = this.bipedLeftArm.rotateAngleX;
            this.rabbitLarm.rotateAngleY = this.bipedLeftArm.rotateAngleY;
            this.rabbitLarm.rotateAngleZ = this.bipedLeftArm.rotateAngleZ;
            this.rabbitRarm.rotateAngleX = this.bipedRightArm.rotateAngleX;
            this.rabbitRarm.rotateAngleY = this.bipedRightArm.rotateAngleY;
            this.rabbitRarm.rotateAngleZ = this.bipedRightArm.rotateAngleZ;
            this.rabbitRleg.rotateAngleX = this.bipedRightLeg.rotateAngleX;
            this.rabbitRleg.rotateAngleY = this.bipedRightLeg.rotateAngleY;
            this.rabbitRleg.rotateAngleZ = this.bipedRightLeg.rotateAngleZ;
            this.rabbitLleg.rotateAngleX = this.bipedLeftLeg.rotateAngleX;
            this.rabbitLleg.rotateAngleY = this.bipedLeftLeg.rotateAngleY;
            this.rabbitLleg.rotateAngleZ = this.bipedLeftLeg.rotateAngleZ;
            this.rabbitBone.render(scale);
            GlStateManager.popMatrix();
         } else if (customModel.isOn() && customModel.Mode.getValue() == Models.mode.Freddy) {
            this.fredhead.rotateAngleX = this.bipedHead.rotateAngleX;
            this.fredhead.rotateAngleY = this.bipedHead.rotateAngleY;
            this.fredhead.rotateAngleZ = this.bipedHead.rotateAngleZ;
            this.armLeft.rotateAngleX = this.bipedLeftArm.rotateAngleX;
            this.armLeft.rotateAngleY = this.bipedLeftArm.rotateAngleY;
            this.armLeft.rotateAngleZ = this.bipedLeftArm.rotateAngleZ;
            this.legRight.rotateAngleX = this.bipedRightLeg.rotateAngleX;
            this.legRight.rotateAngleY = this.bipedRightLeg.rotateAngleY;
            this.legRight.rotateAngleZ = this.bipedRightLeg.rotateAngleZ;
            this.legLeft.rotateAngleX = this.bipedLeftLeg.rotateAngleX;
            this.legLeft.rotateAngleY = this.bipedLeftLeg.rotateAngleY;
            this.legLeft.rotateAngleZ = this.bipedLeftLeg.rotateAngleZ;
            this.armRight.rotateAngleX = this.bipedRightArm.rotateAngleX;
            this.armRight.rotateAngleY = this.bipedRightArm.rotateAngleY;
            this.armRight.rotateAngleZ = this.bipedRightArm.rotateAngleZ;
            GlStateManager.pushMatrix();
            GlStateManager.scale(0.75, 0.65, 0.75);
            GlStateManager.translate(0.0, 0.85, 0.0);
            this.fredbody.render(scale);
            GlStateManager.popMatrix();
         }
      } else {
         super.render(entityIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
         if (this.isChild) {
            GlStateManager.scale(0.5F, 0.5F, 0.5F);
            GlStateManager.translate(0.0F, 24.0F * scale, 0.0F);
         } else if (entityIn.isSneaking()) {
            GlStateManager.translate(0.0F, 0.2F, 0.0F);
         }

         this.bipedLeftLegwear.render(scale);
         this.bipedRightLegwear.render(scale);
         this.bipedLeftArmwear.render(scale);
         this.bipedRightArmwear.render(scale);
         this.bipedBodyWear.render(scale);
      }

      GlStateManager.popMatrix();
   }
}
