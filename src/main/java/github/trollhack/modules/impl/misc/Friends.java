package github.trollhack.modules.impl.misc;

import github.trollhack.core.Managers;
import github.trollhack.modules.Category;
import github.trollhack.modules.Module;
import github.trollhack.modules.impl.hud.Notification;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;

public class Friends extends Module {
    public static final Friends INSTANCE = new Friends();

    public Friends() {
        super("Friends", Category.MISC);
    }

    @Override
    public void onEnable() {
        if (nullCheck()) {
            setEnabled(false);
            return;
        }

        HitResult target = mc.crosshairTarget;
        if (target instanceof EntityHitResult entityHitResult && entityHitResult.getEntity() instanceof PlayerEntity player) {
            String name = player.getGameProfile().getName();
            Managers.FRIEND.toggleFriend(name);

            if (Managers.FRIEND.isFriend(name)) {
                Notification.sendReplace(Friends.class.hashCode(), "Added friend: " + name, 3000);
            } else {
                Notification.sendReplace(Friends.class.hashCode(), "Removed friend: " + name, 3000);
            }
        } else {
            Notification.sendReplace(Friends.class.hashCode(), "Please target a player", 3000);
        }

        setEnabled(false);
    }
}