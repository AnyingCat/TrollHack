//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "G:\PortableSoft\JBY\MC_Deobf3000\1.12-MCP-Mappings"!

package me.rebirthclient.mod.modules.impl.combat;

import me.rebirthclient.api.events.impl.PacketEvent;
import me.rebirthclient.api.managers.Managers;
import me.rebirthclient.api.util.Timer;
import me.rebirthclient.mod.modules.Category;
import me.rebirthclient.mod.modules.Module;
import me.rebirthclient.mod.modules.settings.Setting;
import net.minecraft.block.BlockWeb;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.item.ItemSword;
import net.minecraft.network.play.client.CPacketUseEntity;
import net.minecraft.network.play.client.CPacketPlayer.Position;
import net.minecraft.network.play.client.CPacketUseEntity.Action;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class Criticals extends Module {
   public static Criticals INSTANCE;
   private final Setting<Criticals.Mode> mode = this.add(new Setting<>("Mode", Criticals.Mode.PACKET));
   private final Setting<Boolean> webs = this.add(new Setting<>("Webs", false, v -> this.mode.getValue() == Criticals.Mode.NCP));
   private final Setting<Boolean> onlyAura = this.add(new Setting<>("OnlyAura", false));
   private final Setting<Boolean> onlySword = this.add(new Setting<>("OnlySword", true));
   private final Setting<Integer> delay = this.add(new Setting<>("Delay", 100, 0, 1000));
   private final Timer delayTimer = new Timer();

   public Criticals() {
      super("Criticals", "Always do as much damage as you can!", Category.COMBAT);
      INSTANCE = this;
   }

   @Override
   public String getInfo() {
      return this.mode.getValue() == Criticals.Mode.NCP ? String.valueOf(this.mode.getValue()) : Managers.TEXT.normalizeCases(this.mode.getValue());
   }

   @SubscribeEvent
   public void onPacketSend(PacketEvent.Send event) {
      if (!fullNullCheck()) {
         if (!event.isCanceled()) {
            if (Aura.target != null || !this.onlyAura.getValue()) {
               if (!this.onlySword.getValue() || mc.player.getHeldItemMainhand().item instanceof ItemSword) {
                  if (this.delayTimer.passedMs((long)this.delay.getValue().intValue())) {
                     if (event.getPacket() instanceof CPacketUseEntity
                        && ((CPacketUseEntity)event.getPacket()).getAction() == Action.ATTACK
                        && mc.player.onGround
                        && mc.player.collidedVertically
                        && !mc.player.isInLava()
                        && !mc.player.isInWater()) {
                        Entity attackedEntity = ((CPacketUseEntity)event.getPacket()).getEntityFromWorld(mc.world);
                        if (attackedEntity instanceof EntityEnderCrystal || attackedEntity == null) {
                           return;
                        }

                        this.delayTimer.reset();
                        switch((Criticals.Mode)this.mode.getValue()) {
                           case PACKET:
                              mc.player
                                 .connection
                                 .sendPacket(
                                    new Position(
                                       mc.player.posX, mc.player.posY + 0.0625101, mc.player.posZ, false
                                    )
                                 );
                              mc.player
                                 .connection
                                 .sendPacket(
                                    new Position(mc.player.posX, mc.player.posY, mc.player.posZ, false)
                                 );
                              mc.player
                                 .connection
                                 .sendPacket(
                                    new Position(
                                       mc.player.posX, mc.player.posY + 0.0125, mc.player.posZ, false
                                    )
                                 );
                              mc.player
                                 .connection
                                 .sendPacket(
                                    new Position(mc.player.posX, mc.player.posY, mc.player.posZ, false)
                                 );
                              break;
                           case NCP:
                              if (this.webs.getValue() && mc.world.getBlockState(new BlockPos(mc.player)).getBlock() instanceof BlockWeb) {
                                 mc.player
                                    .connection
                                    .sendPacket(
                                       new Position(
                                          mc.player.posX, mc.player.posY + 0.0625101, mc.player.posZ, false
                                       )
                                    );
                                 mc.player
                                    .connection
                                    .sendPacket(
                                       new Position(mc.player.posX, mc.player.posY, mc.player.posZ, false)
                                    );
                                 mc.player
                                    .connection
                                    .sendPacket(
                                       new Position(
                                          mc.player.posX, mc.player.posY + 0.0125, mc.player.posZ, false
                                       )
                                    );
                                 mc.player
                                    .connection
                                    .sendPacket(
                                       new Position(mc.player.posX, mc.player.posY, mc.player.posZ, false)
                                    );
                              } else {
                                 mc.player
                                    .connection
                                    .sendPacket(
                                       new Position(
                                          mc.player.posX, mc.player.posY + 0.11, mc.player.posZ, false
                                       )
                                    );
                                 mc.player
                                    .connection
                                    .sendPacket(
                                       new Position(
                                          mc.player.posX, mc.player.posY + 0.1100013579, mc.player.posZ, false
                                       )
                                    );
                                 mc.player
                                    .connection
                                    .sendPacket(
                                       new Position(
                                          mc.player.posX, mc.player.posY + 1.3579E-6, mc.player.posZ, false
                                       )
                                    );
                              }
                        }

                        mc.player.onCriticalHit(attackedEntity);
                     }
                  }
               }
            }
         }
      }
   }

   private static enum Mode {
      PACKET,
      NCP;
   }
}
