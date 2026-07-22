package me.catrix.mod;

import me.catrix.api.utils.Wrapper;

public class Mod implements Wrapper {
    private final String name;
    public Mod(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
