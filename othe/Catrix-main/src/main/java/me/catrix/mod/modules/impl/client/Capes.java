package me.catrix.mod.modules.impl.client;

import me.catrix.mod.modules.Module;
import net.minecraft.util.Identifier;

public class Capes extends Module {
    public static Capes INSTANCE;

    public Capes() {
        super("Capes", Category.Client);
        INSTANCE = this;
    }

    public final Identifier capeTexture = new Identifier("textures/cape.png");
}