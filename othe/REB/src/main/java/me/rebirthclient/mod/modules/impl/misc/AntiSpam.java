//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "G:\PortableSoft\JBY\MC_Deobf3000\1.12-MCP-Mappings"!

package me.rebirthclient.mod.modules.impl.misc;

import java.util.ArrayList;
import java.util.HashMap;
import me.rebirthclient.api.events.impl.PacketEvent;
import me.rebirthclient.mod.commands.Command;
import me.rebirthclient.mod.modules.Category;
import me.rebirthclient.mod.modules.Module;
import me.rebirthclient.mod.modules.settings.Setting;
import net.minecraft.network.play.server.SPacketChat;
import net.minecraft.util.text.ChatType;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class AntiSpam extends Module {
   public static AntiSpam INSTANCE = new AntiSpam();
   private final Setting<Boolean> counts = this.add(new Setting<>("Counts", true));
   private static final HashMap<String, AntiSpam.text> StringMap = new HashMap<>();
   private int count = 10;
   private final ArrayList<AntiSpam.chat> chatMessages = new ArrayList<>();

   public AntiSpam() {
      super("AntiSpam", "Anti Spam", Category.MISC);
      INSTANCE = this;
   }

   @SubscribeEvent
   public void onReceivePacket(PacketEvent.Receive event) {
      if (!fullNullCheck()) {
         if (!event.isCanceled()) {
            if (event.getPacket() instanceof SPacketChat) {
               if (((SPacketChat)event.getPacket()).getType() == ChatType.CHAT) {
                  event.setCanceled(true);
                  ITextComponent component = ((SPacketChat)event.getPacket()).getChatComponent();
                  String message = component.getFormattedText();
                  if (StringMap.containsKey(message)) {
                     StringMap.get(message).addNumber();
                     this.chatMessages
                        .add(
                           new AntiSpam.chat(
                              new Command.ChatMessage(message + (this.counts.getValue() ? " §7x" + StringMap.get(message).number : "")),
                              StringMap.get(message).size
                           )
                        );
                  } else {
                     ++this.count;
                     this.chatMessages.add(new AntiSpam.chat(component, this.count));
                     StringMap.put(message, new AntiSpam.text(this.count));
                  }
               }
            }
         }
      }
   }

   @Override
   public void onTick() {
      for(AntiSpam.chat chat : this.chatMessages) {
         mc.ingameGUI.getChatGUI().printChatMessageWithOptionalDeletion(chat.chatMessage, chat.chatLineId);
      }

      this.chatMessages.clear();
   }

   private static class chat {
      public ITextComponent chatMessage;
      public int chatLineId;

      public chat(ITextComponent chatMessage, int chatLineId) {
         this.chatMessage = chatMessage;
         this.chatLineId = chatLineId;
      }
   }

   private static class text {
      int number = 1;
      int size;

      public text(int size) {
         this.size = size;
      }

      public void addNumber() {
         ++this.number;
      }
   }
}
