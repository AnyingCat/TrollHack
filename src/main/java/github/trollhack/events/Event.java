package github.trollhack.events;

import meteordevelopment.orbit.ICancellable;

public class Event implements ICancellable {
    private boolean cancelled = false;
    private final Stage stage;

    public Event() {
        this.stage = Stage.Pre;
    }

    public Event(Stage stage) {
        this.stage = stage;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    public Stage getStage() {
        return stage;
    }

    public boolean isPre() {
        return stage == Stage.Pre;
    }

    public boolean isPost() {
        return stage == Stage.Post;
    }

    public enum Stage {
        Pre, Post
    }
}
