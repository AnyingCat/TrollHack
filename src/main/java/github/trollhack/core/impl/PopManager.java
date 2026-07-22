package github.trollhack.core.impl;

import github.trollhack.events.EventBusHolder;
import github.trollhack.events.impl.TickEvent;
import github.trollhack.events.impl.TotemPopEvent;
import github.trollhack.modules.Module;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.entity.player.PlayerEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static github.trollhack.core.Managers.mc;

public class PopManager {
    public final Map<String, Integer> popContainer = new HashMap<>();
    private final List<PlayerEntity> deadPlayers = new ArrayList<>();

    public PopManager() {
        EventBusHolder.INSTANCE.subscribe(this);
    }

    public int getPop(String name) {
        return popContainer.getOrDefault(name, 0);
    }

    @EventHandler(priority = EventPriority.HIGH)
    private void onTotemPopDetect(TotemPopEvent.Detect event) {
        if (Module.nullCheck()) return;
        onTotemPop(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.HIGH)
    private void onTick(TickEvent event) {
        if (Module.nullCheck()) return;
        for (PlayerEntity player : mc.world.getPlayers()) {
            if (!player.isDead()) {
                deadPlayers.remove(player);
                continue;
            }
            if (deadPlayers.contains(player)) continue;
            String name = player.getName().getString();
            int count = popContainer.getOrDefault(name, 0);
            EventBusHolder.INSTANCE.post(new TotemPopEvent.Death(name, count));
            popContainer.remove(name);
            deadPlayers.add(player);
        }
    }

    public void onTotemPop(PlayerEntity player) {
        String name = player.getName().getString();
        int count = popContainer.getOrDefault(name, 0) + 1;
        popContainer.put(name, count);
        EventBusHolder.INSTANCE.post(new TotemPopEvent.Pop(name, count));
    }
}
