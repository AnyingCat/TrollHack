package github.trollhack.core.impl;

import github.trollhack.events.EventBusHolder;
import github.trollhack.events.impl.Render2DEvent;
import github.trollhack.modules.HudModule;
import github.trollhack.modules.impl.hud.*;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.client.MinecraftClient;

import java.util.List;

public class HudManager {
    public final List<HudModule> hudModules = List.of(
            Watermark.INSTANCE,
            ModuleList.INSTANCE,
            Notification.INSTANCE,
            Username.INSTANCE,
            CPS.INSTANCE,
            Fps.INSTANCE,
            MemoryUsage.INSTANCE,
            Ping.INSTANCE,
            ServerBrand.INSTANCE,
            TPS.INSTANCE,
            Time.INSTANCE,
            Armor.INSTANCE,
            Coordinate.INSTANCE
    );

    public HudManager() {
        EventBusHolder.INSTANCE.subscribe(this);
    }

    @EventHandler(priority = EventPriority.LOW)
    private void onRender2D(Render2DEvent event) {
        if (MinecraftClient.getInstance().currentScreen != null) return;
        for (HudModule hud : hudModules) {
            if (hud.isEnabled()) {
                hud.onHudRender(event.getContext());
            }
        }
    }
}
