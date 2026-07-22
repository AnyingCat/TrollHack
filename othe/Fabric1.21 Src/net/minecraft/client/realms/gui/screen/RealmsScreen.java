/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.client.realms.gui.screen;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.stream.Collectors;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.realms.RealmsLabel;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;

@Environment(value=EnvType.CLIENT)
public abstract class RealmsScreen
extends Screen {
    protected static final int field_33055 = 17;
    protected static final int field_33057 = 7;
    protected static final long MAX_FILE_SIZE = 0x140000000L;
    protected static final int field_33061 = 0x4C4C4C;
    protected static final int field_33062 = 0x6C6C6C;
    protected static final int field_33063 = 0x7FFF7F;
    protected static final int field_33040 = 0x3366BB;
    protected static final int field_33041 = 7107012;
    protected static final int field_39676 = 32;
    private final List<RealmsLabel> labels = Lists.newArrayList();

    public RealmsScreen(Text text) {
        super(text);
    }

    /**
     * Moved from RealmsConstants in 20w10a
     */
    protected static int row(int index) {
        return 40 + index * 13;
    }

    protected RealmsLabel addLabel(RealmsLabel label) {
        this.labels.add(label);
        return this.addDrawable(label);
    }

    public Text narrateLabels() {
        return ScreenTexts.joinLines(this.labels.stream().map(RealmsLabel::getText).collect(Collectors.toList()));
    }
}

