/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.client.gui.screen.option;

import com.mojang.datafixers.util.Unit;
import com.mojang.serialization.Codec;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.GameOptionsScreen;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.SimpleOption;
import net.minecraft.text.Text;
import net.minecraft.util.Nullables;
import net.minecraft.world.Difficulty;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class OnlineOptionsScreen
extends GameOptionsScreen {
    private static final Text TITLE_TEXT = Text.translatable("options.online.title");
    @Nullable
    private SimpleOption<Unit> difficulty;

    public OnlineOptionsScreen(Screen parent, GameOptions gameOptions) {
        super(parent, gameOptions, TITLE_TEXT);
    }

    @Override
    protected void init() {
        ClickableWidget clickableWidget;
        super.init();
        if (this.difficulty != null && (clickableWidget = this.body.getWidgetFor(this.difficulty)) != null) {
            clickableWidget.active = false;
        }
    }

    private SimpleOption<?>[] collectOptions(GameOptions gameOptions, MinecraftClient client) {
        ArrayList<SimpleOption> list = new ArrayList<SimpleOption>();
        list.add(gameOptions.getRealmsNotifications());
        list.add(gameOptions.getAllowServerListing());
        SimpleOption simpleOption = Nullables.map(client.world, world -> {
            Difficulty difficulty = world.getDifficulty();
            return new SimpleOption<Unit>("options.difficulty.online", SimpleOption.emptyTooltip(), (text, unit) -> difficulty.getTranslatableName(), new SimpleOption.PotentialValuesBasedCallbacks<Unit>(List.of(Unit.INSTANCE), Codec.EMPTY.codec()), Unit.INSTANCE, unit -> {});
        });
        if (simpleOption != null) {
            this.difficulty = simpleOption;
            list.add(simpleOption);
        }
        return list.toArray(new SimpleOption[0]);
    }

    @Override
    protected void addOptions() {
        this.body.addAll(this.collectOptions(this.gameOptions, this.client));
    }
}

