package github.trollhack.core.impl;

import net.minecraft.entity.player.PlayerEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class FriendManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(FriendManager.class);

    private final List<String> friends = new ArrayList<>();

    public boolean isFriend(String name) {
        return friends.contains(name);
    }

    public boolean isFriend(PlayerEntity player) {
        return isFriend(player.getGameProfile().getName());
    }

    public void addFriend(String name) {
        if (friends.contains(name)) return;
        friends.add(name);
        LOGGER.info("Added friend: {}", name);
    }

    public void removeFriend(String name) {
        if (friends.remove(name)) {
            LOGGER.info("Removed friend: {}", name);
        }
    }

    public void toggleFriend(String name) {
        if (friends.contains(name)) {
            removeFriend(name);
        } else {
            addFriend(name);
        }
    }

    public List<String> getFriends() {
        return new ArrayList<>(friends);
    }
}
