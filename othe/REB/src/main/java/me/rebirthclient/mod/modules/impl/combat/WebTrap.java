//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "G:\PortableSoft\JBY\MC_Deobf3000\1.12-MCP-Mappings"!

package me.rebirthclient.mod.modules.impl.combat;

import com.mojang.realmsclient.gui.ChatFormatting;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import me.rebirthclient.api.events.impl.PacketEvent;
import me.rebirthclient.api.events.impl.Render3DEvent;
import me.rebirthclient.api.managers.Managers;
import me.rebirthclient.api.util.BlockUtil;
import me.rebirthclient.api.util.EntityUtil;
import me.rebirthclient.api.util.InventoryUtil;
import me.rebirthclient.api.util.Timer;
import me.rebirthclient.api.util.math.MathUtil;
import me.rebirthclient.api.util.render.RenderUtil;
import me.rebirthclient.mod.modules.Category;
import me.rebirthclient.mod.modules.Module;
import me.rebirthclient.mod.modules.settings.Setting;
import net.minecraft.block.BlockWeb;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.network.play.server.SPacketBlockChange;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class WebTrap extends Module {
   public static boolean isPlacing;
   private final Setting<Double> range = this.add(new Setting<>("Range", 5.0, 0.0, 7.0));
   private final Setting<Integer> delay = this.add(new Setting<>("TickDelay", 50, 0, 250));
   private final Setting<Integer> blocksPerPlace = this.add(new Setting<>("BPT", 1, 1, 30));
   private final Setting<Boolean> packet = this.add(new Setting<>("Packet", false));
   private final Setting<Boolean> disable = this.add(new Setting<>("AutoDisable", false));
   private final Setting<Boolean> rotate = this.add(new Setting<>("Rotate", true));
   private final Setting<Boolean> raytrace = this.add(new Setting<>("Raytrace", false));
   private final Setting<Boolean> feet = this.add(new Setting<>("Feet", true));
   private final Setting<Boolean> face = this.add(new Setting<>("Face", false));
   private final Setting<Boolean> render = this.add(new Setting<>("Render", true).setParent());
   private final Setting<Boolean> box = this.add(new Setting<>("Box", true, v -> this.render.isOpen()));
   private final Setting<Boolean> line = this.add(new Setting<>("Line", true, v -> this.render.isOpen()));
   private final Timer timer = new Timer();
   private final Map<BlockPos, Long> renderBlocks = new ConcurrentHashMap<>();
   private final Timer renderTimer = new Timer();
   private EntityPlayer target;
   private boolean didPlace;
   private boolean isSneaking;
   private int lastHotbarSlot;
   private int placements;
   private BlockPos startPos;

   public WebTrap() {
      super("WebTrap", "Traps other players in webs", Category.COMBAT);
   }

   @Override
   public void onEnable() {
      if (!fullNullCheck()) {
         this.startPos = EntityUtil.getRoundedPos(mc.player);
         this.lastHotbarSlot = mc.player.inventory.currentItem;
      }
   }

   @Override
   public void onDisable() {
      isPlacing = false;
      this.isSneaking = EntityUtil.stopSneaking(this.isSneaking);
      InventoryUtil.doSwap(this.lastHotbarSlot);
   }

   @Override
   public void onTick() {
      this.doTrap();
   }

   @Override
   public String getInfo() {
      return this.target != null ? this.target.getName() : null;
   }

   @Override
   public void onRender3D(Render3DEvent event) {
      if (this.render.getValue()) {
         this.renderTimer.reset();
         this.renderBlocks
            .forEach(
               (pos, time) -> {
                  int lineA = 255;
                  int fillA = 80;
                  if (System.currentTimeMillis() - time > 400L) {
                     this.renderTimer.reset();
                     this.renderBlocks.remove(pos);
                  } else {
                     long endTime = System.currentTimeMillis() - time - 100L;
                     double normal = MathUtil.normalize((double)endTime, 0.0, 500.0);
                     normal = MathHelper.clamp(normal, 0.0, 1.0);
                     normal = -normal + 1.0;
                     int firstAl = (int)(normal * (double)lineA);
                     int secondAl = (int)(normal * (double)fillA);
                     RenderUtil.drawBoxESP(
                        new BlockPos(pos),
                        Managers.COLORS.getCurrent(),
                        true,
                        new Color(255, 255, 255, firstAl),
                        0.7F,
                        this.line.getValue(),
                        this.box.getValue(),
                        secondAl,
                        true,
                        0.0
                     );
                  }
               }
            );
      }
   }

   @SubscribeEvent
   public void onReceivePacket(PacketEvent.Receive event) {
      if (!event.isCanceled()) {
         if (event.getPacket() instanceof SPacketBlockChange && this.renderBlocks.containsKey(((SPacketBlockChange)event.getPacket()).getBlockPosition())) {
            this.renderTimer.reset();
            if (((SPacketBlockChange)event.getPacket()).getBlockState().getBlock() != Blocks.AIR && this.renderTimer.passedMs(400L)) {
               this.renderBlocks.remove(((SPacketBlockChange)event.getPacket()).getBlockPosition());
            }
         }
      }
   }

   private void doTrap() {
      if (!this.check()) {
         this.doWebTrap();
         if (this.didPlace) {
            this.timer.reset();
         }
      }
   }

   private void doWebTrap() {
      List<Vec3d> placeTargets = this.getPlacements();
      this.placeList(placeTargets);
   }

   private List<Vec3d> getPlacements() {
      ArrayList<Vec3d> list = new ArrayList();
      Vec3d baseVec = this.target.getPositionVector();
      if (this.feet.getValue()) {
         list.add(baseVec);
      }

      if (this.face.getValue()) {
         list.add(baseVec.add(0.0, 1.0, 0.0));
      }

      return list;
   }

   private void placeList(List<Vec3d> list) {
      list.sort(
         (vec3d, vec3d2) -> Double.compare(
               mc.player.getDistanceSq(vec3d2.x, vec3d2.y, vec3d2.z),
               mc.player.getDistanceSq(vec3d.x, vec3d.y, vec3d.z)
            )
      );
      list.sort(Comparator.comparingDouble(vec3d -> vec3d.y));

      for(Vec3d vec3d3 : list) {
         BlockPos position = new BlockPos(vec3d3);
         int placeability = BlockUtil.getPlaceAbility(position, this.raytrace.getValue());
         if (placeability == 3 || placeability == 1) {
            int webSlot = InventoryUtil.findHotbarClass(BlockWeb.class);
            InventoryUtil.doSwap(webSlot);
            this.renderBlocks.put(position, System.currentTimeMillis());
            this.placeBlock(position);
            InventoryUtil.doSwap(this.lastHotbarSlot);
         }
      }
   }

   private boolean check() {
      isPlacing = false;
      this.didPlace = false;
      this.placements = 0;
      int webSlot = InventoryUtil.findHotbarClass(BlockWeb.class);
      if (this.isOff()) {
         return true;
      } else if (this.disable.getValue() && !this.startPos.equals(EntityUtil.getRoundedPos(mc.player))) {
         this.disable();
         return true;
      } else if (webSlot == -1) {
         this.sendMessage("[" + this.getName() + "] " + ChatFormatting.RED + "No webs in hotbar. disabling...");
         this.disable();
         return true;
      } else {
         if (mc.player.inventory.currentItem != this.lastHotbarSlot && mc.player.inventory.currentItem != webSlot) {
            this.lastHotbarSlot = mc.player.inventory.currentItem;
         }

         this.isSneaking = EntityUtil.stopSneaking(this.isSneaking);
         this.target = this.getTarget(this.range.getValue());
         return this.target == null || !this.timer.passedMs((long)this.delay.getValue().intValue());
      }
   }

   private EntityPlayer getTarget(double range) {
      EntityPlayer target = null;
      double distance = Math.pow(range, 2.0) + 1.0;

      for(EntityPlayer player : mc.world.playerEntities) {
         if (EntityUtil.isValid(player, range) && !player.isInWeb && !(Managers.SPEED.getPlayerSpeed(player) > 30.0)) {
            if (target == null) {
               target = player;
               distance = mc.player.getDistanceSq(player);
            } else if (mc.player.getDistanceSq(player) < distance) {
               target = player;
               distance = mc.player.getDistanceSq(player);
            }
         }
      }

      return target;
   }

   private void placeBlock(BlockPos pos) {
      if (this.placements < this.blocksPerPlace.getValue() && mc.player.getDistanceSq(pos) <= MathUtil.square(6.0)) {
         isPlacing = true;
         Managers.INTERACTIONS.placeBlock(pos, this.rotate.getValue(), this.packet.getValue(), false, true);
         this.didPlace = true;
         ++this.placements;
      }
   }
}
