package me.catrix.core.impl;

import me.catrix.api.utils.world.BlockUtil;
import me.catrix.Catrix;
import me.catrix.api.events.eventbus.EventHandler;
import me.catrix.api.events.eventbus.EventPriority;
import me.catrix.api.events.impl.TickEvent;
import me.catrix.mod.modules.impl.render.PlaceRender;

public class ThreadManager {
    public static ClientService clientService;

    public ThreadManager() {
        Catrix.EVENT_BUS.subscribe(this);
        clientService = new ClientService();
        clientService.setName("CatrixClientService");
        clientService.setDaemon(true);
        clientService.start();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEvent(TickEvent event) {
        if (event.isPre()) {
            if (!clientService.isAlive()) {
                clientService = new ClientService();
                clientService.setName("CatrixClientService");
                clientService.setDaemon(true);
                clientService.start();
            }
            BlockUtil.placedPos.forEach(pos -> PlaceRender.renderMap.put(pos, PlaceRender.INSTANCE.create(pos)));
            BlockUtil.placedPos.clear();
            Catrix.SERVER.onUpdate();
            Catrix.PLAYER.onUpdate();
            Catrix.MODULE.onUpdate();
            Catrix.GUI.onUpdate();
            Catrix.POP.onUpdate();
        }
    }

    public static class ClientService extends Thread {
        @Override
        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    if (Catrix.MODULE != null) {
                        Catrix.MODULE.onThread();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
