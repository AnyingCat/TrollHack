/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.advancement;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.advancement.AdvancementFrame;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.text.Text;
import net.minecraft.text.TextCodecs;
import net.minecraft.util.Identifier;

public class AdvancementDisplay {
    public static final Codec<AdvancementDisplay> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)ItemStack.VALIDATED_CODEC.fieldOf("icon")).forGetter(AdvancementDisplay::getIcon), ((MapCodec)TextCodecs.CODEC.fieldOf("title")).forGetter(AdvancementDisplay::getTitle), ((MapCodec)TextCodecs.CODEC.fieldOf("description")).forGetter(AdvancementDisplay::getDescription), Identifier.CODEC.optionalFieldOf("background").forGetter(AdvancementDisplay::getBackground), AdvancementFrame.CODEC.optionalFieldOf("frame", AdvancementFrame.TASK).forGetter(AdvancementDisplay::getFrame), Codec.BOOL.optionalFieldOf("show_toast", true).forGetter(AdvancementDisplay::shouldShowToast), Codec.BOOL.optionalFieldOf("announce_to_chat", true).forGetter(AdvancementDisplay::shouldAnnounceToChat), Codec.BOOL.optionalFieldOf("hidden", false).forGetter(AdvancementDisplay::isHidden)).apply((Applicative<AdvancementDisplay, ?>)instance, AdvancementDisplay::new));
    public static final PacketCodec<RegistryByteBuf, AdvancementDisplay> PACKET_CODEC = PacketCodec.of(AdvancementDisplay::toPacket, AdvancementDisplay::fromPacket);
    private final Text title;
    private final Text description;
    private final ItemStack icon;
    private final Optional<Identifier> background;
    private final AdvancementFrame frame;
    private final boolean showToast;
    private final boolean announceToChat;
    private final boolean hidden;
    private float x;
    private float y;

    public AdvancementDisplay(ItemStack icon, Text title, Text description, Optional<Identifier> background, AdvancementFrame frame, boolean showToast, boolean announceToChat, boolean hidden) {
        this.title = title;
        this.description = description;
        this.icon = icon;
        this.background = background;
        this.frame = frame;
        this.showToast = showToast;
        this.announceToChat = announceToChat;
        this.hidden = hidden;
    }

    public void setPos(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public Text getTitle() {
        return this.title;
    }

    public Text getDescription() {
        return this.description;
    }

    public ItemStack getIcon() {
        return this.icon;
    }

    public Optional<Identifier> getBackground() {
        return this.background;
    }

    public AdvancementFrame getFrame() {
        return this.frame;
    }

    public float getX() {
        return this.x;
    }

    public float getY() {
        return this.y;
    }

    public boolean shouldShowToast() {
        return this.showToast;
    }

    public boolean shouldAnnounceToChat() {
        return this.announceToChat;
    }

    public boolean isHidden() {
        return this.hidden;
    }

    private void toPacket(RegistryByteBuf buf) {
        TextCodecs.UNLIMITED_REGISTRY_PACKET_CODEC.encode(buf, this.title);
        TextCodecs.UNLIMITED_REGISTRY_PACKET_CODEC.encode(buf, this.description);
        ItemStack.PACKET_CODEC.encode(buf, this.icon);
        buf.writeEnumConstant(this.frame);
        int i = 0;
        if (this.background.isPresent()) {
            i |= 1;
        }
        if (this.showToast) {
            i |= 2;
        }
        if (this.hidden) {
            i |= 4;
        }
        buf.writeInt(i);
        this.background.ifPresent(buf::writeIdentifier);
        buf.writeFloat(this.x);
        buf.writeFloat(this.y);
    }

    private static AdvancementDisplay fromPacket(RegistryByteBuf buf) {
        Text text = (Text)TextCodecs.UNLIMITED_REGISTRY_PACKET_CODEC.decode(buf);
        Text text2 = (Text)TextCodecs.UNLIMITED_REGISTRY_PACKET_CODEC.decode(buf);
        ItemStack itemStack = (ItemStack)ItemStack.PACKET_CODEC.decode(buf);
        AdvancementFrame advancementFrame = buf.readEnumConstant(AdvancementFrame.class);
        int i = buf.readInt();
        Optional<Identifier> optional = (i & 1) != 0 ? Optional.of(buf.readIdentifier()) : Optional.empty();
        boolean bl = (i & 2) != 0;
        boolean bl2 = (i & 4) != 0;
        AdvancementDisplay advancementDisplay = new AdvancementDisplay(itemStack, text, text2, optional, advancementFrame, bl, false, bl2);
        advancementDisplay.setPos(buf.readFloat(), buf.readFloat());
        return advancementDisplay;
    }
}

