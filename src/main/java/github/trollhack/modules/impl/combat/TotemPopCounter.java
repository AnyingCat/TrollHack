package github.trollhack.modules.impl.combat;

import github.trollhack.core.Managers;
import github.trollhack.events.impl.TotemPopEvent;
import github.trollhack.modules.Category;
import github.trollhack.modules.Module;
import github.trollhack.modules.impl.hud.Notification;
import github.trollhack.settings.impl.BooleanSetting;
import github.trollhack.settings.impl.EnumSetting;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class TotemPopCounter extends Module {
    public static final TotemPopCounter INSTANCE = new TotemPopCounter();

    private final BooleanSetting countFriends = booleanSetting("Count Friends", true);
    private final BooleanSetting countSelf = booleanSetting("Count Self", true);
    private final BooleanSetting thanksTo = booleanSetting("Thanks To", false);
    private final EnumSetting<Formatting> colorName = enumSetting("Color Name", Formatting.BLUE);
    private final EnumSetting<Formatting> colorNumber = enumSetting("Color Number", Formatting.GREEN);
    private final BooleanSetting chat = booleanSetting("Chat", true);
    private final EnumSetting<Announce> announce = enumSetting("Announce", Announce.CLIENT, chat::getValue);
    private final BooleanSetting notification = booleanSetting("Notification", true);

    public enum Announce {
        CLIENT, SERVER
    }

    public TotemPopCounter() {
        super("TotemPopCounter", Category.COMBAT);
    }

    @EventHandler(priority = EventPriority.HIGH)
    private void onTotemPop(TotemPopEvent.Pop event) {
        if (nullCheck()) return;
        String name = event.getName();
        if (!friendCheck(name) || !selfCheck(name)) return;
        boolean isSelf = name.equals(mc.player.getName().getString());
        int count = event.getCount();
        String plainText = nameText(name) + " popped " + count + " " + plural(count) + ending(isSelf);
        MutableText message = Text.empty()
                .append(Text.literal(nameText(name)).formatted(colorName.getValue()))
                .append(Text.literal(" popped "))
                .append(Text.literal(String.valueOf(count)).formatted(colorNumber.getValue()))
                .append(Text.literal(" " + plural(count) + ending(isSelf)));
        sendMessage(name, message, plainText, !isSelf && isPublic());
    }

    @EventHandler(priority = EventPriority.HIGH)
    private void onTotemDeath(TotemPopEvent.Death event) {
        if (nullCheck()) return;
        String name = event.getName();
        if (!friendCheck(name) || !selfCheck(name)) return;
        int count = event.getCount();
        String plainText = nameText(name) + " died after popping " + count + " " + plural(count) + ending(false);
        MutableText message = Text.empty()
                .append(Text.literal(nameText(name)).formatted(colorName.getValue()))
                .append(Text.literal(" died after popping "))
                .append(Text.literal(String.valueOf(count)).formatted(colorNumber.getValue()))
                .append(Text.literal(" " + plural(count) + ending(false)));
        sendMessage(name, message, plainText, isPublic());
    }

    private boolean friendCheck(String name) {
        return countFriends.getValue() || !Managers.FRIEND.isFriend(name);
    }

    private boolean selfCheck(String name) {
        return countSelf.getValue() || !name.equals(mc.player.getName().getString());
    }

    private String nameText(String name) {
        if (name.equals(mc.player.getName().getString())) {
            return "I";
        } else if (Managers.FRIEND.isFriend(name)) {
            return isPublic() ? "My friend " + name + ", " : "Your friend " + name + ", ";
        }
        return name;
    }

    private boolean isPublic() {
        return chat.getValue() && announce.getValue() == Announce.SERVER;
    }

    private String plural(int count) {
        return count == 1 ? "totem" : "totems";
    }

    private String ending(boolean self) {
        if (!self && thanksTo.getValue()) {
            return " thanks to Troll !";
        }
        return "!";
    }

    private void sendMessage(String name, Text message, String plainText, boolean publicMessage) {
        if (publicMessage) {
            if (mc.player.networkHandler != null) {
                mc.player.networkHandler.sendChatMessage(plainText);
            }
        } else if (chat.getValue()) {
            if (mc.inGameHud != null) {
                mc.inGameHud.getChatHud().addMessage(Text.literal("[" + getName() + "] ").append(message));
            }
        }
        if (notification.getValue()) {
            Notification.send(this.hashCode() * 31L + name.hashCode(), plainText, 5000);
        }
    }
}
