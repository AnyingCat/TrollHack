package github.trollhack.events.impl;

import github.trollhack.events.Event;
import net.minecraft.entity.player.PlayerEntity;

public class TotemPopEvent extends Event {
    protected TotemPopEvent(Stage stage) {
        super(stage);
    }

    public static class Detect extends TotemPopEvent {
        private final PlayerEntity player;

        public Detect(PlayerEntity player) {
            super(Stage.Post);
            this.player = player;
        }

        public PlayerEntity getPlayer() {
            return player;
        }
    }

    public static class Pop extends TotemPopEvent {
        private final String name;
        private final int count;

        public Pop(String name, int count) {
            super(Stage.Post);
            this.name = name;
            this.count = count;
        }

        public String getName() {
            return name;
        }

        public int getCount() {
            return count;
        }
    }

    public static class Death extends TotemPopEvent {
        private final String name;
        private final int count;

        public Death(String name, int count) {
            super(Stage.Post);
            this.name = name;
            this.count = count;
        }

        public String getName() {
            return name;
        }

        public int getCount() {
            return count;
        }
    }
}
