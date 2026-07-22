//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "G:\PortableSoft\JBY\MC_Deobf3000\1.12-MCP-Mappings"!

package me.rebirthclient;

import java.io.InputStream;
import java.nio.ByteBuffer;
import me.rebirthclient.api.managers.Managers;
import me.rebirthclient.api.util.render.RenderUtil;
import me.rebirthclient.mod.gui.screen.Gui;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Util;
import net.minecraft.util.Util.EnumOS;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.Display;

@Mod(
   modid = "rebirth",
   name = "Rebirth",
   version = "alpha"
)
public class Rebirth {
   public static final String MODID = "rebirth";
   public static final String MODNAME = "Rebirth";
   public static final String MODVERISON = "alpha";
   public static final Logger LOGGER = LogManager.getLogger("Rebirth");
   @Instance
   public static Rebirth INSTANCE;

   public static void load() {
      LOGGER.info("Loading Rebirth alpha...");
      Managers.load();
      if (Gui.INSTANCE == null) {
         Gui.INSTANCE = new Gui();
      }

      LOGGER.info("Rebirth alpha successfully loaded!\n");
   }

   public static void unload(boolean force) {
      LOGGER.info("Unloading Rebirth alpha...");
      Managers.unload(force);
      LOGGER.info("Rebirth alpha successfully unloaded!\n");
   }

   @EventHandler
   public void preInit(FMLPreInitializationEvent event) {
      Display.setTitle("Rebirth alpha: Loading...");
      if (Util.getOSType() != EnumOS.OSX) {
         try (
            InputStream inputStream16x = Minecraft.class.getResourceAsStream("/assets/minecraft/textures/rebirth/constant/icon16x.png");
            InputStream inputStream32x = Minecraft.class.getResourceAsStream("/assets/minecraft/textures/rebirth/constant/icon32x.png");
         ) {
            ByteBuffer[] icons = new ByteBuffer[]{RenderUtil.readImageToBuffer(inputStream16x), RenderUtil.readImageToBuffer(inputStream32x)};
            Display.setIcon(icons);
         } catch (Exception var34) {
            LOGGER.error("Rebirth alpha couldn't set the window icon!", var34);
         }
      }
   }

   @EventHandler
   public void init(FMLInitializationEvent event) {
      load();
   }
}
