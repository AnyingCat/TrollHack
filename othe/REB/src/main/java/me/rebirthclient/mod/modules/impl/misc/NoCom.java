//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "G:\PortableSoft\JBY\MC_Deobf3000\1.12-MCP-Mappings"!

package me.rebirthclient.mod.modules.impl.misc;

import com.mojang.realmsclient.gui.ChatFormatting;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import me.rebirthclient.api.events.impl.PacketEvent;
import me.rebirthclient.asm.accessors.IChunkProviderClient;
import me.rebirthclient.mod.gui.screen.GuiScanner;
import me.rebirthclient.mod.modules.Category;
import me.rebirthclient.mod.modules.Module;
import me.rebirthclient.mod.modules.settings.Bind;
import me.rebirthclient.mod.modules.settings.Setting;
import net.minecraft.network.play.client.CPacketPlayerDigging;
import net.minecraft.network.play.client.CPacketPlayerDigging.Action;
import net.minecraft.network.play.server.SPacketBlockChange;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class NoCom extends Module {
   public static int scannedChunks = 0;
   public static List<NoCom.Dot> dots = new ArrayList<>();
   public static NoCom INSTANCE;
   private static BlockPos playerPos = null;
   private static long time = 0L;
   private static int count = 0;
   private static int masynax = 0;
   private static int masynay = 0;
   private final Setting<Bind> self = this.register(new Setting<>("OpenGui", new Bind(0)));
   public Setting<Integer> delay = this.register(new Setting<>("Delay", 200, 0, 1000));
   public Setting<Integer> loop = this.register(new Setting<>("LoopPerTick", 1, 1, 100));
   public Setting<Integer> startX = this.register(new Setting<>("StartX", 0, 0, 1000000));
   public Setting<Integer> startZ = this.register(new Setting<>("StartZ", 0, 0, 1000000));
   public Setting<Integer> scale = this.register(new Setting<>("PointerScale", 4, 1, 4));
   public Setting<Boolean> you = this.register(new Setting<>("you", true));
   public Setting<Boolean> loadgui = this.register(new Setting<>("LoadGui", true));
   public int couti = 1;
   private int renderDistanceDiameter = 0;
   private int x;
   private int z;

   public NoCom() {
      super("NoCom", "褝泻褋锌谢芯懈褌 写谢�? 锌芯懈褋泻邪-懈谐褉芯泻芯�?", Category.MISC);
      INSTANCE = this;
   }

   public static void getgui() {
      mc.displayGuiScreen(GuiScanner.getGuiScanner());
   }

   public static void rerun(int x, int y) {
      dots.clear();
      playerPos = null;
      count = 0;
      time = 0L;
      masynax = x;
      masynay = y;
   }

   @Override
   public void onUpdate() {
      if (this.self.getValue().isDown()) {
         getgui();
      }

      if (this.loadgui.getValue()) {
         getgui();
         this.loadgui.setValue(false);
      }

      if (GuiScanner.neartrack && scannedChunks > 25) {
         scannedChunks = 0;
      }

      if (GuiScanner.neartrack && scannedChunks == 0) {
         this.donocom((int)mc.player.posX, (int)mc.player.posZ);
      }

      if (!GuiScanner.neartrack) {
         if (!GuiScanner.busy) {
            if (!this.you.getValue()) {
               this.donocom(this.startX.getValue(), this.startZ.getValue());
            } else {
               this.donocom((int)mc.player.posX, (int)mc.player.posZ);
            }
         } else if (masynax != 0 && masynay != 0) {
            this.donocom(masynax, masynay);
         }
      }
   }

   public void donocom(int x3, int y3) {
      playerPos = new BlockPos(mc.player.posX, mc.player.posY - 1.0, mc.player.posZ);
      if (this.renderDistanceDiameter == 0) {
         this.renderDistanceDiameter = 8;
      }

      if (time == 0L) {
         time = System.currentTimeMillis();
      }

      if (System.currentTimeMillis() - time > (long)this.delay.getValue().intValue()) {
         for(int i = 0; i < this.loop.getValue(); ++i) {
            int x1 = this.getSpiralCoords(count)[0] * this.renderDistanceDiameter * 16 + x3;
            int z1 = this.getSpiralCoords(count)[1] * this.renderDistanceDiameter * 16 + y3;
            BlockPos position = new BlockPos(x1, 0, z1);
            this.x = x1;
            this.z = z1;
            mc.player.connection.sendPacket(new CPacketPlayerDigging(Action.ABORT_DESTROY_BLOCK, playerPos, EnumFacing.EAST));
            mc.player.connection.sendPacket(new CPacketPlayerDigging(Action.ABORT_DESTROY_BLOCK, position, EnumFacing.EAST));
            dots.add(new NoCom.Dot(x1 / 16, z1 / 16, NoCom.DotType.Searched));
            playerPos = new BlockPos(mc.player.posX, mc.player.posY - 1.0, mc.player.posZ);
            time = System.currentTimeMillis();
            ++count;
            ++scannedChunks;
         }
      }
   }

   @SubscribeEvent
   public final void onPacketReceive(PacketEvent.Receive event) {
      if (event.getPacket() instanceof SPacketBlockChange) {
         int x = ((SPacketBlockChange)event.getPacket()).getBlockPosition().getX();
         int z = ((SPacketBlockChange)event.getPacket()).getBlockPosition().getZ();
         IChunkProviderClient chunkProviderClient = (IChunkProviderClient)mc.world.getChunkProvider();
         ObjectIterator shittytext = chunkProviderClient.getLoadedChunks().values().iterator();

         while(shittytext.hasNext()) {
            Chunk chunk = (Chunk)shittytext.next();
            if (chunk.x == x / 16 || chunk.z == z / 16) {
               return;
            }
         }

         String shittytextx = "Player spotted at X: " + ChatFormatting.GREEN + x + ChatFormatting.RESET + " Z: " + ChatFormatting.GREEN + z;
         dots.add(new NoCom.Dot(x / 16, z / 16, NoCom.DotType.Spotted));
         this.sendMessage(shittytextx);
         GuiScanner.getInstance().consoleout.add(new NoCom.cout(this.couti, shittytextx));
         ++this.couti;
         if (GuiScanner.track) {
            GuiScanner.getInstance().consoleout.add(new NoCom.cout(this.couti, "tracking x " + x + " z " + z));
            rerun(x, z);
         }
      }
   }

   private int[] getSpiralCoords(int n) {
      int x = 0;
      int z = 0;
      int d = 1;
      int lineNumber = 1;
      int[] coords = new int[]{0, 0};

      for(int i = 0; i < n; ++i) {
         if (2 * x * d < lineNumber) {
            x += d;
            coords = new int[]{x, z};
         } else if (2 * z * d < lineNumber) {
            z += d;
            coords = new int[]{x, z};
         } else {
            d *= -1;
            ++lineNumber;
            ++n;
         }
      }

      return coords;
   }

   @Override
   public void onEnable() {
      playerPos = null;
      count = 0;
      time = 0L;
   }

   @Override
   public void onDisable() {
      dots.clear();
      playerPos = null;
      count = 0;
      time = 0L;
   }

   @Override
   public String getInfo() {
      return this.x + " , " + this.z;
   }

   public static class Dot {
      public NoCom.DotType type;
      public int posX;
      public int posY;
      public Color color;
      public int ticks;

      public Dot(int posX, int posY, NoCom.DotType type) {
         this.posX = posX;
         this.posY = posY;
         this.type = type;
         this.ticks = 0;
      }
   }

   public static enum DotType {
      Spotted,
      Searched;
   }

   public static class cout {
      public String string;
      public int posY;

      public cout(int posY, String out) {
         this.posY = posY;
         this.string = out;
      }
   }
}
