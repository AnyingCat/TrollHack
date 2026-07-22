//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "G:\PortableSoft\JBY\MC_Deobf3000\1.12-MCP-Mappings"!

package me.rebirthclient.mod.modules.impl.render;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;
import me.rebirthclient.api.events.impl.PacketEvent;
import me.rebirthclient.api.events.impl.Render2DEvent;
import me.rebirthclient.asm.accessors.IGuiBossOverlay;
import me.rebirthclient.mod.modules.Category;
import me.rebirthclient.mod.modules.Module;
import me.rebirthclient.mod.modules.settings.Setting;
import net.minecraft.client.gui.BossInfoClient;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityExpBottle;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.network.play.server.SPacketExplosion;
import net.minecraft.network.play.server.SPacketSoundEffect;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraftforge.client.event.RenderBlockOverlayEvent;
import net.minecraftforge.client.event.EntityViewRenderEvent.FogDensity;
import net.minecraftforge.client.event.RenderBlockOverlayEvent.OverlayType;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.client.event.RenderGameOverlayEvent.Post;
import net.minecraftforge.client.event.RenderGameOverlayEvent.Pre;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;

public class NoRender extends Module {
   public static NoRender INSTANCE = new NoRender();
   public final Setting<Boolean> night = this.add(new Setting<>("Night", true));
   public final Setting<Boolean> armor = this.add(new Setting<>("Armor", true));
   public final Setting<Boolean> fire = this.add(new Setting<>("Fire", true));
   public final Setting<Boolean> blind = this.add(new Setting<>("Blind", true));
   public final Setting<Boolean> nausea = this.add(new Setting<>("Nausea", true));
   public final Setting<Boolean> fog = this.add(new Setting<>("Fog", true));
   public final Setting<Boolean> noWeather = this.add(new Setting<>("Weather", true));
   public final Setting<Boolean> hurtCam = this.add(new Setting<>("HurtCam", true));
   public final Setting<Boolean> totemPops = this.add(new Setting<>("TotemPop", true));
   public final Setting<Boolean> blocks = this.add(new Setting<>("Block", true));
   public final Setting<Boolean> exp = this.add(new Setting<>("Exp", true));
   public final Setting<Boolean> explosion = this.add(new Setting<>("Explosion[!]", false));
   public Setting<Boolean> skyLight = this.add(new Setting<>("SkyLight", false));
   public Setting<Boolean> advancements = this.add(new Setting<>("Advancements", false));
   public Setting<NoRender.Boss> boss = this.add(new Setting<>("BossBars", NoRender.Boss.NONE));
   public Setting<Float> scale = this.add(
      new Setting<>("Scale", 0.5F, 0.5F, 1.0F, v -> this.boss.getValue() == NoRender.Boss.MINIMIZE || this.boss.getValue() == NoRender.Boss.STACK)
   );
   boolean gamma = false;
   float oldBright;
   private static final ResourceLocation GUI_BARS_TEXTURES = new ResourceLocation("textures/gui/bars.png");

   public NoRender() {
      super("NoRender", "Prevent some animation", Category.RENDER);
      INSTANCE = this;
   }

   @SubscribeEvent
   public void onRenderPre(Pre event) {
      if (event.getType() == ElementType.BOSSINFO && this.boss.getValue() != NoRender.Boss.NONE) {
         event.setCanceled(true);
      }
   }

   @Override
   public void onUpdate() {
      if (this.blind.getValue() && mc.player.isPotionActive(MobEffects.BLINDNESS)) {
         mc.player.removePotionEffect(MobEffects.BLINDNESS);
      }

      if (this.noWeather.getValue() && mc.world.isRaining()) {
         mc.world.setRainStrength(0.0F);
      }

      if (mc.player.isPotionActive(MobEffects.NAUSEA) && this.nausea.getValue()) {
         mc.player.removePotionEffect(MobEffects.NAUSEA);
      }

      if (this.night.getValue()) {
         this.gamma = true;
         mc.gameSettings.gammaSetting = 100.0F;
      } else if (this.gamma) {
         mc.gameSettings.gammaSetting = this.oldBright;
         this.gamma = false;
      }
   }

   @SubscribeEvent
   public void fog_density(FogDensity event) {
      if (!this.fog.getValue()) {
         event.setDensity(0.0F);
         event.setCanceled(true);
      }
   }

   @SubscribeEvent
   public void blockOverlayEventListener(RenderBlockOverlayEvent event) {
      if (!fullNullCheck()) {
         if (this.fire.getValue() || this.blocks.getValue()) {
            if (event.getOverlayType() == OverlayType.FIRE || this.blocks.getValue()) {
               event.setCanceled(true);
            }
         }
      }
   }

   @Override
   public void onEnable() {
      this.oldBright = mc.gameSettings.gammaSetting;
   }

   @Override
   public void onDisable() {
      mc.gameSettings.gammaSetting = this.oldBright;
   }

   @Override
   public void onRender2D(Render2DEvent event) {
      if (this.exp.getValue()) {
         for(Entity entity : mc.world.getLoadedEntityList()) {
            if (entity instanceof EntityExpBottle) {
               mc.world.removeEntity(entity);
            }
         }
      }
   }

   @SubscribeEvent(
      priority = EventPriority.LOWEST
   )
   public void onReceivePacket(PacketEvent.Receive event) {
      if (!event.isCanceled()) {
         if (this.explosion.getValue() && event.getPacket() instanceof SPacketExplosion) {
            event.setCanceled(true);
         } else {
            if (this.exp.getValue() && event.getPacket() instanceof SPacketSoundEffect) {
               SPacketSoundEffect packet = event.getPacket();
               if (packet.getCategory() == SoundCategory.NEUTRAL && packet.getSound() == SoundEvents.ENTITY_EXPERIENCE_BOTTLE_THROW) {
                  event.setCanceled(true);
                  return;
               }
            }
         }
      }
   }

   @SubscribeEvent
   public void onRenderPost(Post event) {
      if (event.getType() == ElementType.BOSSINFO && this.boss.getValue() != NoRender.Boss.NONE) {
         if (this.boss.getValue() == NoRender.Boss.MINIMIZE) {
            Map<UUID, BossInfoClient> map = ((IGuiBossOverlay)mc.ingameGUI.getBossOverlay()).getMapBossInfos();
            if (map == null) {
               return;
            }

            ScaledResolution scaledresolution = new ScaledResolution(mc);
            int i = scaledresolution.getScaledWidth();
            int j = 12;

            for(Entry<UUID, BossInfoClient> entry : map.entrySet()) {
               BossInfoClient info = (BossInfoClient)entry.getValue();
               String text = info.getName().getFormattedText();
               int k = (int)((float)i / this.scale.getValue() / 2.0F - 91.0F);
               GL11.glScaled((double)this.scale.getValue().floatValue(), (double)this.scale.getValue().floatValue(), 1.0);
               if (!event.isCanceled()) {
                  GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                  mc.getTextureManager().bindTexture(GUI_BARS_TEXTURES);
                  ((IGuiBossOverlay)mc.ingameGUI.getBossOverlay()).invokeRender(k, j, info);
                  mc.fontRenderer
                     .drawStringWithShadow(
                        text, (float)i / this.scale.getValue() / 2.0F - (float)mc.fontRenderer.getStringWidth(text) / 2.0F, (float)(j - 9), 16777215
                     );
               }

               GL11.glScaled(1.0 / (double)this.scale.getValue().floatValue(), 1.0 / (double)this.scale.getValue().floatValue(), 1.0);
               j += 10 + mc.fontRenderer.FONT_HEIGHT;
            }
         } else if (this.boss.getValue() == NoRender.Boss.STACK) {
            Map<UUID, BossInfoClient> map = ((IGuiBossOverlay)mc.ingameGUI.getBossOverlay()).getMapBossInfos();
            HashMap<String, NoRender.Pair<BossInfoClient, Integer>> to = new HashMap<>();

            for(Entry<UUID, BossInfoClient> entry2 : map.entrySet()) {
               String s = ((BossInfoClient)entry2.getValue()).getName().getFormattedText();
               if (to.containsKey(s)) {
                  NoRender.Pair<BossInfoClient, Integer> p = to.get(s);
                  p = new NoRender.Pair<>(p.getKey(), p.getValue() + 1);
                  to.put(s, p);
               } else {
                  NoRender.Pair<BossInfoClient, Integer> p = new NoRender.Pair<>(entry2.getValue(), 1);
                  to.put(s, p);
               }
            }

            ScaledResolution scaledresolution2 = new ScaledResolution(mc);
            int l = scaledresolution2.getScaledWidth();
            int m = 12;

            for(Entry<String, NoRender.Pair<BossInfoClient, Integer>> entry3 : to.entrySet()) {
               String text = entry3.getKey();
               BossInfoClient info2 = (BossInfoClient)entry3.getValue().getKey();
               int a = entry3.getValue().getValue();
               text = text + " x" + a;
               int k2 = (int)((float)l / this.scale.getValue() / 2.0F - 91.0F);
               GL11.glScaled((double)this.scale.getValue().floatValue(), (double)this.scale.getValue().floatValue(), 1.0);
               if (!event.isCanceled()) {
                  GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                  mc.getTextureManager().bindTexture(GUI_BARS_TEXTURES);
                  ((IGuiBossOverlay)mc.ingameGUI.getBossOverlay()).invokeRender(k2, m, info2);
                  mc.fontRenderer
                     .drawStringWithShadow(
                        text, (float)l / this.scale.getValue() / 2.0F - (float)mc.fontRenderer.getStringWidth(text) / 2.0F, (float)(m - 9), 16777215
                     );
               }

               GL11.glScaled(1.0 / (double)this.scale.getValue().floatValue(), 1.0 / (double)this.scale.getValue().floatValue(), 1.0);
               m += 10 + mc.fontRenderer.FONT_HEIGHT;
            }
         }
      }
   }

   public static enum Boss {
      NONE,
      REMOVE,
      STACK,
      MINIMIZE;
   }

   public static class Pair<T, S> {
      T key;
      S value;

      public Pair(T key, S value) {
         this.key = key;
         this.value = value;
      }

      public T getKey() {
         return this.key;
      }

      public void setKey(T key) {
         this.key = key;
      }

      public S getValue() {
         return this.value;
      }

      public void setValue(S value) {
         this.value = value;
      }
   }
}
