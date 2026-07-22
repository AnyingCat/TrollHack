package github.trollhack.gui.mainmenu;

import github.trollhack.utils.interfaces.Mc;
import github.trollhack.utils.render.Render2DUtil;
import github.trollhack.utils.render.font.FontRenderers;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;

import java.awt.*;

public class MainMenu extends Screen implements Mc {
    public MainMenu() {
        super(Text.of("MainMenu"));
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        Render2DUtil.drawMainMenuShader(context.getMatrices(), 0, 0, this.width, this.height);

        FontRenderers.juraLight.drawText(context.getMatrices(), "Troll", this.width / 15.0f, this.width / 12.0f, 3f, new Color(230, 158, 42));

        String[] texts = {"Singleplayer", "Multiplayer", "Options", "Exit"};

        for (int i = 0; i < 4; i++) {
            float posX = this.width / 2.0f + 5.0f + (i - 2) * (10.0f + Math.min(this.width / 6.0f, 300.0f));
            float posY = this.height - Math.min((this.width + this.height * 2) / 25.0f, 150.0f);

            boolean isHovered = mouseX >= posX && mouseX <= posX + Math.min(this.width / 6.0f, 300.0f) &&
                    mouseY >= posY - FontRenderers.juraLight.getStringHeight(0.8f) - 3.0f &&
                    mouseY <= posY + Math.max(Math.min((this.width * 2 + this.height) / 6000.0f + 0.1f, 1.0f), 0.5f) * 2.0f;

            Render2DUtil.drawRect(context.getMatrices(), posX, posY, Math.min(this.width / 6.0f, 120.0f),
                    Math.max(Math.min((this.width * 2 + this.height) / 6000.0f + 0.1f, 1.0f), 0.5f) * 2.0f,
                    isHovered ? new Color(215, 121, 39) : new Color(183, 183, 183));

            String firstLetter = texts[i].substring(0, 1);
            float textY = posY - FontRenderers.juraLight.getStringHeight(0.8f) - 2;
            FontRenderers.juraLight.drawText(context.getMatrices(), firstLetter, posX, textY, 0.8f, new Color(230, 158, 42));
            FontRenderers.juraLight.drawText(context.getMatrices(), texts[i].substring(1),
                    posX + FontRenderers.juraLight.getStringWidth(firstLetter, 0.8f), textY, 0.8f, Color.WHITE);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0) return false;

        for (int i = 0; i < 4; i++) {
            float posX = this.width / 2.0f + 5.0f + (i - 2) * (10.0f + Math.min(this.width / 6.0f, 300.0f));
            float posY = this.height - Math.min((this.width + this.height * 2) / 25.0f, 150.0f);

            if (mouseX >= posX && mouseX <= posX + Math.min(this.width / 6.0f, 300.0f) &&
                    mouseY >= posY - FontRenderers.juraLight.getStringHeight(0.8f) - 2 &&
                    mouseY <= posY + Math.max(Math.min((this.width * 2 + this.height) / 6000.0f + 0.1f, 1.0f), 0.5f) * 2.0f) {
                mc.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0f));
                switch (i) {
                    case 0 -> mc.setScreen(new SelectWorldScreen(this));
                    case 1 -> mc.setScreen(new MultiplayerScreen(this));
                    case 2 -> mc.setScreen(new OptionsScreen(this, mc.options));
                    case 3 -> mc.scheduleStop();
                }
            }
        }
        return true;
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        switch (Character.toLowerCase(chr)) {
            case 's' -> { mc.setScreen(new SelectWorldScreen(this)); return true; }
            case 'm' -> { mc.setScreen(new MultiplayerScreen(this)); return true; }
            case 'o' -> { mc.setScreen(new OptionsScreen(this, mc.options)); return true; }
            case 'e' -> { mc.scheduleStop(); return true; }
        }
        return super.charTyped(chr, modifiers);
    }
}