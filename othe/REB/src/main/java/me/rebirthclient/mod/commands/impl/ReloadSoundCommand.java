//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "G:\PortableSoft\JBY\MC_Deobf3000\1.12-MCP-Mappings"!

package me.rebirthclient.mod.commands.impl;

import com.mojang.realmsclient.gui.ChatFormatting;
import me.rebirthclient.mod.commands.Command;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.audio.SoundManager;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

public class ReloadSoundCommand extends Command {
   public ReloadSoundCommand() {
      super("sound", new String[0]);
   }

   @Override
   public void execute(String[] commands) {
      try {
         SoundManager sndManager = (SoundManager)ObfuscationReflectionHelper.getPrivateValue(
            SoundHandler.class, mc.getSoundHandler(), new String[]{"sndManager", "sndManager"}
         );
         sndManager.reloadSoundSystem();
         Command.sendMessage(ChatFormatting.GREEN + "Reloaded Sound System.");
      } catch (Exception var3) {
         System.out.println(ChatFormatting.RED + "Could not restart sound manager: " + var3);
         var3.printStackTrace();
         Command.sendMessage(ChatFormatting.RED + "Couldnt Reload Sound System!");
      }
   }
}
