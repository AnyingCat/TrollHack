//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "G:\PortableSoft\JBY\MC_Deobf3000\1.12-MCP-Mappings"!

package me.rebirthclient.api.managers.impl;

import com.google.common.base.Strings;
import com.mojang.realmsclient.gui.ChatFormatting;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import me.rebirthclient.api.events.impl.ConnectionEvent;
import me.rebirthclient.api.events.impl.DamageBlockEvent;
import me.rebirthclient.api.events.impl.DeathEvent;
import me.rebirthclient.api.events.impl.PacketEvent;
import me.rebirthclient.api.events.impl.Render2DEvent;
import me.rebirthclient.api.events.impl.Render3DEvent;
import me.rebirthclient.api.events.impl.RenderFogColorEvent;
import me.rebirthclient.api.events.impl.TotemPopEvent;
import me.rebirthclient.api.events.impl.UpdateWalkingPlayerEvent;
import me.rebirthclient.api.managers.Managers;
import me.rebirthclient.api.util.CombatUtil;
import me.rebirthclient.api.util.EntityUtil;
import me.rebirthclient.api.util.MovementUtil;
import me.rebirthclient.api.util.Timer;
import me.rebirthclient.mod.Mod;
import me.rebirthclient.mod.commands.Command;
import me.rebirthclient.mod.modules.impl.client.ClickGui;
import me.rebirthclient.mod.modules.impl.client.HUD;
import me.rebirthclient.mod.modules.impl.client.Title;
import me.rebirthclient.mod.modules.impl.combat.CombatSetting;
import me.rebirthclient.mod.modules.impl.combat.feetplace.FeetPlace;
import me.rebirthclient.mod.modules.impl.render.RenderSetting;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.play.server.SPacketEntityStatus;
import net.minecraft.network.play.server.SPacketPlayerListItem;
import net.minecraft.network.play.server.SPacketTimeUpdate;
import net.minecraft.network.play.server.SPacketPlayerListItem.Action;
import net.minecraftforge.client.event.ClientChatEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.client.event.EntityViewRenderEvent.FogColors;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.client.event.RenderGameOverlayEvent.Post;
import net.minecraftforge.client.event.RenderGameOverlayEvent.Text;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent.KeyInputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent.ClientConnectedToServerEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent.ClientDisconnectionFromServerEvent;
import org.lwjgl.input.Keyboard;

public class EventManager extends Mod {
   private final Timer logoutTimer = new Timer();
   private final AtomicBoolean tickOngoing = new AtomicBoolean(false);

   public void init() {
      MinecraftForge.EVENT_BUS.register(this);
   }

   public boolean ticksOngoing() {
      return this.tickOngoing.get();
   }

   public void onUnload() {
      MinecraftForge.EVENT_BUS.unregister(this);
   }

   @SubscribeEvent
   public void onUpdate(LivingUpdateEvent event) {
      if (!fullNullCheck() && event.getEntity().getEntityWorld().isRemote && event.getEntityLiving().equals(mc.player)) {
         Managers.MODULES.onUpdate();
         if (HUD.INSTANCE.isOn() && HUD.INSTANCE.arrayList.getValue()) {
            Managers.MODULES.sortModules();
         }
      }
   }

   @SubscribeEvent
   public void onClientConnect(ClientConnectedToServerEvent event) {
      this.logoutTimer.reset();
      Managers.MODULES.onLogin();
   }

   @SubscribeEvent
   public void onClientDisconnect(ClientDisconnectionFromServerEvent event) {
      Managers.CONFIGS.saveConfig(Managers.CONFIGS.config.replaceFirst("Rebirth/", ""));
      Managers.MODULES.onLogout();
   }

   @SubscribeEvent
   public void onTick(ClientTickEvent event) {
      Title.updateTitle();
      Managers.COLORS.rainbowProgress += ClickGui.INSTANCE.rainbowSpeed.getValue();
      if (!fullNullCheck()) {
         Managers.MODULES.onTick();

         for(EntityPlayer player : mc.world.playerEntities) {
            if (player != null && !(player.getHealth() > 0.0F)) {
               MinecraftForge.EVENT_BUS.post(new DeathEvent(player));
               Managers.MODULES.onDeath(player);
            }
         }

         if (CombatUtil.isHole(EntityUtil.getPlayerPos(), false, 4, true)
            && FeetPlace.INSTANCE.isOff()
            && FeetPlace.INSTANCE.enableInHole.getValue()
            && mc.player.onGround
            && !MovementUtil.isJumping()) {
            FeetPlace.INSTANCE.enable();
         }

         if (CombatSetting.INSTANCE.isOff()) {
            CombatSetting.INSTANCE.enable();
         }

         if (RenderSetting.INSTANCE.isOff()) {
            RenderSetting.INSTANCE.enable();
         }
      }
   }

   @SubscribeEvent(
      priority = EventPriority.HIGH
   )
   public void onUpdateWalkingPlayer(UpdateWalkingPlayerEvent event) {
      if (!fullNullCheck()) {
         if (event.getStage() == 0) {
            Managers.SPEED.updateValues();
            Managers.ROTATIONS.updateRotations();
            Managers.POSITION.updatePosition();
         }

         if (event.getStage() == 1) {
            if (CombatSetting.INSTANCE.resetRotation.getValue()) {
               Managers.ROTATIONS.resetRotations();
            }

            if (CombatSetting.INSTANCE.resetPosition.getValue()) {
               Managers.POSITION.restorePosition();
            }
         }
      }
   }

   @SubscribeEvent
   public void onPacketReceive(PacketEvent.Receive event) {
      if (!fullNullCheck()) {
         if (event.getStage() == 0) {
            Managers.SERVER.onPacketReceived();
            if (event.getPacket() instanceof SPacketEntityStatus) {
               SPacketEntityStatus packet = event.getPacket();
               if (packet.getOpCode() == 35 && packet.getEntity(mc.world) instanceof EntityPlayer) {
                  EntityPlayer player = (EntityPlayer)packet.getEntity(mc.world);
                  MinecraftForge.EVENT_BUS.post(new TotemPopEvent(player));
                  Managers.MODULES.onTotemPop(player);
               }
            }

            if (event.getPacket() instanceof SPacketPlayerListItem && !fullNullCheck() && this.logoutTimer.passedS(1.0)) {
               SPacketPlayerListItem packet = event.getPacket();
               if (Action.ADD_PLAYER != packet.getAction() && Action.REMOVE_PLAYER != packet.getAction()) {
                  return;
               }

               packet.getEntries()
                  .stream()
                  .filter(Objects::nonNull)
                  .filter(data -> !Strings.isNullOrEmpty(data.getProfile().getName()) || data.getProfile().getId() != null)
                  .forEach(data -> {
                     UUID id = data.getProfile().getId();
                     switch(packet.getAction()) {
                        case ADD_PLAYER:
                           String name = data.getProfile().getName();
                           MinecraftForge.EVENT_BUS.post(new ConnectionEvent(0, id, name));
                           break;
                        case REMOVE_PLAYER:
                           EntityPlayer entity = mc.world.getPlayerEntityByUUID(id);
                           if (entity != null) {
                              String logoutName = entity.getName();
                              MinecraftForge.EVENT_BUS.post(new ConnectionEvent(1, entity, id, logoutName));
                           } else {
                              MinecraftForge.EVENT_BUS.post(new ConnectionEvent(2, id, null));
                           }
                     }
                  });
            }

            if (event.getPacket() instanceof SPacketTimeUpdate) {
               Managers.SERVER.update();
            }
         }
      }
   }

   @SubscribeEvent
   public void onWorldRender(RenderWorldLastEvent event) {
      if (!fullNullCheck()) {
         if (!event.isCanceled()) {
            Managers.FPS.update();
            mc.profiler.startSection("Rebirth");
            GlStateManager.disableTexture2D();
            GlStateManager.enableBlend();
            GlStateManager.disableAlpha();
            GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
            GlStateManager.shadeModel(7425);
            GlStateManager.disableDepth();
            GlStateManager.glLineWidth(1.0F);
            Render3DEvent render3dEvent = new Render3DEvent(event.getPartialTicks());
            Managers.MODULES.onRender3D(render3dEvent);
            GlStateManager.glLineWidth(1.0F);
            GlStateManager.shadeModel(7424);
            GlStateManager.disableBlend();
            GlStateManager.enableAlpha();
            GlStateManager.enableTexture2D();
            GlStateManager.enableDepth();
            GlStateManager.enableCull();
            GlStateManager.enableCull();
            GlStateManager.depthMask(true);
            GlStateManager.enableTexture2D();
            GlStateManager.enableBlend();
            GlStateManager.enableDepth();
            mc.profiler.endSection();
         }
      }
   }

   @SubscribeEvent
   public void renderHUD(Post event) {
      if (event.getType() == ElementType.HOTBAR) {
         Managers.TEXT.updateResolution();
      }
   }

   @SubscribeEvent(
      priority = EventPriority.LOW
   )
   public void onRenderGameOverlayEvent(Text event) {
      if (event.getType() == ElementType.TEXT) {
         ScaledResolution resolution = new ScaledResolution(mc);
         Render2DEvent render2DEvent = new Render2DEvent(event.getPartialTicks(), resolution);
         Managers.MODULES.onRender2D(render2DEvent);
         GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
      }
   }

   @SubscribeEvent(
      priority = EventPriority.NORMAL,
      receiveCanceled = true
   )
   public void onKeyInput(KeyInputEvent event) {
      if (Keyboard.getEventKeyState()) {
         Managers.MODULES.onKeyInput(Keyboard.getEventKey());
      }
   }

   @SubscribeEvent(
      priority = EventPriority.HIGHEST
   )
   public void onChatSent(ClientChatEvent event) {
      if (event.getMessage().startsWith(Command.getCommandPrefix())) {
         event.setCanceled(true);

         try {
            mc.ingameGUI.getChatGUI().addToSentMessages(event.getMessage());
            if (event.getMessage().length() > 1) {
               Managers.COMMANDS.executeCommand(event.getMessage().substring(Command.getCommandPrefix().length() - 1));
            } else {
               Command.sendMessage("Please enter a command.");
            }
         } catch (Exception var3) {
            var3.printStackTrace();
            Command.sendMessage(ChatFormatting.RED + "An error occurred while running this command. Check the log!");
         }
      }
   }

   @SubscribeEvent
   public void onFogColor(FogColors event) {
      RenderFogColorEvent fogColorEvent = new RenderFogColorEvent();
      MinecraftForge.EVENT_BUS.post(fogColorEvent);
      if (fogColorEvent.isCanceled()) {
         event.setRed((float)fogColorEvent.getColor().getRed() / 255.0F);
         event.setGreen((float)fogColorEvent.getColor().getGreen() / 255.0F);
         event.setBlue((float)fogColorEvent.getColor().getBlue() / 255.0F);
      }
   }

   @SubscribeEvent
   public void BlockBreak(DamageBlockEvent event) {
      if (event.getPosition().getY() != -1) {
         EntityPlayer breaker = (EntityPlayer)mc.world.getEntityByID(event.getBreakerId());
         if (breaker != null
            && !(
               breaker.getDistance(
                     (double)event.getPosition().getX() + 0.5,
                     (double)event.getPosition().getY(),
                     (double)event.getPosition().getZ() + 0.5
                  )
                  > 7.0
            )) {
            BreakManager.MineMap.put(breaker, event.getPosition());
         }
      }
   }
}
