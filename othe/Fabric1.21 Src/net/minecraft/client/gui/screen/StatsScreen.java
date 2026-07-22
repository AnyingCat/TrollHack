/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.client.gui.screen;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.DirectionalLayoutWidget;
import net.minecraft.client.gui.widget.ThreePartsLayoutWidget;
import net.minecraft.client.realms.gui.RealmsLoadingWidget;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.entity.EntityType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.ClientStatusC2SPacket;
import net.minecraft.registry.Registries;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stat;
import net.minecraft.stat.StatHandler;
import net.minecraft.stat.StatType;
import net.minecraft.stat.Stats;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class StatsScreen
extends Screen {
    private static final Text TITLE_TEXT = Text.translatable("gui.stats");
    static final Identifier SLOT_TEXTURE = Identifier.ofVanilla("container/slot");
    static final Identifier HEADER_TEXTURE = Identifier.ofVanilla("statistics/header");
    static final Identifier SORT_UP_TEXTURE = Identifier.ofVanilla("statistics/sort_up");
    static final Identifier SORT_DOWN_TEXTURE = Identifier.ofVanilla("statistics/sort_down");
    private static final Text DOWNLOADING_STATS_TEXT = Text.translatable("multiplayer.downloadingStats");
    static final Text NONE_TEXT = Text.translatable("stats.none");
    private static final Text GENERAL_BUTTON_TEXT = Text.translatable("stat.generalButton");
    private static final Text ITEM_BUTTON_TEXT = Text.translatable("stat.itemsButton");
    private static final Text MOBS_BUTTON_TEXT = Text.translatable("stat.mobsButton");
    protected final Screen parent;
    private static final int field_49520 = 280;
    private static final int field_49521 = 5;
    private static final int field_49522 = 58;
    private ThreePartsLayoutWidget layout = new ThreePartsLayoutWidget(this, 33, 58);
    @Nullable
    private GeneralStatsListWidget generalStats;
    @Nullable
    ItemStatsListWidget itemStats;
    @Nullable
    private EntityStatsListWidget mobStats;
    final StatHandler statHandler;
    @Nullable
    private AlwaysSelectedEntryListWidget<?> selectedList;
    private boolean downloadingStats = true;

    public StatsScreen(Screen parent, StatHandler statHandler) {
        super(TITLE_TEXT);
        this.parent = parent;
        this.statHandler = statHandler;
    }

    @Override
    protected void init() {
        this.layout.addBody(new RealmsLoadingWidget(this.textRenderer, DOWNLOADING_STATS_TEXT));
        this.client.getNetworkHandler().sendPacket(new ClientStatusC2SPacket(ClientStatusC2SPacket.Mode.REQUEST_STATS));
    }

    public void createLists() {
        this.generalStats = new GeneralStatsListWidget(this.client);
        this.itemStats = new ItemStatsListWidget(this.client);
        this.mobStats = new EntityStatsListWidget(this.client);
    }

    public void createButtons() {
        ThreePartsLayoutWidget threePartsLayoutWidget = new ThreePartsLayoutWidget(this, 33, 58);
        threePartsLayoutWidget.addHeader(TITLE_TEXT, this.textRenderer);
        DirectionalLayoutWidget directionalLayoutWidget = threePartsLayoutWidget.addFooter(DirectionalLayoutWidget.vertical()).spacing(5);
        directionalLayoutWidget.getMainPositioner().alignHorizontalCenter();
        DirectionalLayoutWidget directionalLayoutWidget2 = directionalLayoutWidget.add(DirectionalLayoutWidget.horizontal()).spacing(5);
        directionalLayoutWidget2.add(ButtonWidget.builder(GENERAL_BUTTON_TEXT, button -> this.selectStatList(this.generalStats)).width(120).build());
        ButtonWidget buttonWidget = directionalLayoutWidget2.add(ButtonWidget.builder(ITEM_BUTTON_TEXT, button -> this.selectStatList(this.itemStats)).width(120).build());
        ButtonWidget buttonWidget2 = directionalLayoutWidget2.add(ButtonWidget.builder(MOBS_BUTTON_TEXT, button -> this.selectStatList(this.mobStats)).width(120).build());
        directionalLayoutWidget.add(ButtonWidget.builder(ScreenTexts.DONE, button -> this.close()).width(200).build());
        if (this.itemStats != null && this.itemStats.children().isEmpty()) {
            buttonWidget.active = false;
        }
        if (this.mobStats != null && this.mobStats.children().isEmpty()) {
            buttonWidget2.active = false;
        }
        this.layout = threePartsLayoutWidget;
        this.layout.forEachChild(child -> {
            ClickableWidget cfr_ignored_0 = (ClickableWidget)this.addDrawableChild(child);
        });
        this.initTabNavigation();
    }

    @Override
    protected void initTabNavigation() {
        this.layout.refreshPositions();
        if (this.selectedList != null) {
            this.selectedList.position(this.width, this.layout);
        }
    }

    @Override
    public void close() {
        this.client.setScreen(this.parent);
    }

    public void onStatsReady() {
        if (this.downloadingStats) {
            this.createLists();
            this.selectStatList(this.generalStats);
            this.createButtons();
            this.setInitialFocus();
            this.downloadingStats = false;
        }
    }

    @Override
    public boolean shouldPause() {
        return !this.downloadingStats;
    }

    public void selectStatList(@Nullable AlwaysSelectedEntryListWidget<?> list) {
        if (this.selectedList != null) {
            this.remove(this.selectedList);
        }
        if (list != null) {
            this.addDrawableChild(list);
            this.selectedList = list;
            this.initTabNavigation();
        }
    }

    static String getStatTranslationKey(Stat<Identifier> stat) {
        return "stat." + stat.getValue().toString().replace(':', '.');
    }

    @Environment(value=EnvType.CLIENT)
    class GeneralStatsListWidget
    extends AlwaysSelectedEntryListWidget<Entry> {
        public GeneralStatsListWidget(MinecraftClient client) {
            super(client, StatsScreen.this.width, StatsScreen.this.height - 33 - 58, 33, 14);
            ObjectArrayList<Stat<Identifier>> objectArrayList = new ObjectArrayList<Stat<Identifier>>(Stats.CUSTOM.iterator());
            objectArrayList.sort((Comparator<Stat<Identifier>>)Comparator.comparing(stat -> I18n.translate(StatsScreen.getStatTranslationKey(stat), new Object[0])));
            for (Stat stat2 : objectArrayList) {
                this.addEntry(new Entry(stat2));
            }
        }

        @Override
        public int getRowWidth() {
            return 280;
        }

        @Environment(value=EnvType.CLIENT)
        class Entry
        extends AlwaysSelectedEntryListWidget.Entry<Entry> {
            private final Stat<Identifier> stat;
            private final Text displayName;

            Entry(Stat<Identifier> stat) {
                this.stat = stat;
                this.displayName = Text.translatable(StatsScreen.getStatTranslationKey(stat));
            }

            private String getFormatted() {
                return this.stat.format(StatsScreen.this.statHandler.getStat(this.stat));
            }

            @Override
            public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
                int i = y + entryHeight / 2 - ((StatsScreen)StatsScreen.this).textRenderer.fontHeight / 2;
                int j = index % 2 == 0 ? Colors.WHITE : Colors.ALTERNATE_WHITE;
                context.drawTextWithShadow(StatsScreen.this.textRenderer, this.displayName, x + 2, i, j);
                String string = this.getFormatted();
                context.drawTextWithShadow(StatsScreen.this.textRenderer, string, x + entryWidth - StatsScreen.this.textRenderer.getWidth(string) - 4, i, j);
            }

            @Override
            public Text getNarration() {
                return Text.translatable("narrator.select", Text.empty().append(this.displayName).append(ScreenTexts.SPACE).append(this.getFormatted()));
            }
        }
    }

    @Environment(value=EnvType.CLIENT)
    class ItemStatsListWidget
    extends AlwaysSelectedEntryListWidget<Entry> {
        private static final int field_49524 = 18;
        private static final int field_49525 = 22;
        private static final int field_49526 = 1;
        private static final int field_49527 = 0;
        private static final int field_49528 = -1;
        private static final int field_49529 = 1;
        private final Identifier[] headerIconTextures;
        protected final List<StatType<Block>> blockStatTypes;
        protected final List<StatType<Item>> itemStatTypes;
        protected final Comparator<Entry> comparator;
        @Nullable
        protected StatType<?> selectedStatType;
        protected int selectedHeaderColumn;
        protected int listOrder;

        public ItemStatsListWidget(MinecraftClient client) {
            boolean bl;
            super(client, StatsScreen.this.width, StatsScreen.this.height - 33 - 58, 33, 22);
            this.headerIconTextures = new Identifier[]{Identifier.ofVanilla("statistics/block_mined"), Identifier.ofVanilla("statistics/item_broken"), Identifier.ofVanilla("statistics/item_crafted"), Identifier.ofVanilla("statistics/item_used"), Identifier.ofVanilla("statistics/item_picked_up"), Identifier.ofVanilla("statistics/item_dropped")};
            this.comparator = new ItemComparator();
            this.selectedHeaderColumn = -1;
            this.blockStatTypes = Lists.newArrayList();
            this.blockStatTypes.add(Stats.MINED);
            this.itemStatTypes = Lists.newArrayList(Stats.BROKEN, Stats.CRAFTED, Stats.USED, Stats.PICKED_UP, Stats.DROPPED);
            this.setRenderHeader(true, 22);
            Set<Item> set = Sets.newIdentityHashSet();
            for (Item item : Registries.ITEM) {
                bl = false;
                for (StatType<Item> statType : this.itemStatTypes) {
                    if (!statType.hasStat(item) || StatsScreen.this.statHandler.getStat(statType.getOrCreateStat(item)) <= 0) continue;
                    bl = true;
                }
                if (!bl) continue;
                set.add(item);
            }
            for (Block block : Registries.BLOCK) {
                bl = false;
                for (StatType<ItemConvertible> statType : this.blockStatTypes) {
                    if (!statType.hasStat(block) || StatsScreen.this.statHandler.getStat(statType.getOrCreateStat(block)) <= 0) continue;
                    bl = true;
                }
                if (!bl) continue;
                set.add(block.asItem());
            }
            set.remove(Items.AIR);
            for (Item item : set) {
                this.addEntry(new Entry(item));
            }
        }

        int method_57742(int i) {
            return 75 + 40 * i;
        }

        @Override
        protected void renderHeader(DrawContext context, int x, int y) {
            Identifier identifier;
            int i;
            if (!this.client.mouse.wasLeftButtonClicked()) {
                this.selectedHeaderColumn = -1;
            }
            for (i = 0; i < this.headerIconTextures.length; ++i) {
                identifier = this.selectedHeaderColumn == i ? SLOT_TEXTURE : HEADER_TEXTURE;
                context.drawGuiTexture(identifier, x + this.method_57742(i) - 18, y + 1, 0, 18, 18);
            }
            if (this.selectedStatType != null) {
                i = this.method_57742(this.getHeaderIndex(this.selectedStatType)) - 36;
                identifier = this.listOrder == 1 ? SORT_UP_TEXTURE : SORT_DOWN_TEXTURE;
                context.drawGuiTexture(identifier, x + i, y + 1, 0, 18, 18);
            }
            for (i = 0; i < this.headerIconTextures.length; ++i) {
                int j = this.selectedHeaderColumn == i ? 1 : 0;
                context.drawGuiTexture(this.headerIconTextures[i], x + this.method_57742(i) - 18 + j, y + 1 + j, 0, 18, 18);
            }
        }

        @Override
        public int getRowWidth() {
            return 280;
        }

        @Override
        protected boolean clickedHeader(int x, int y) {
            this.selectedHeaderColumn = -1;
            for (int i = 0; i < this.headerIconTextures.length; ++i) {
                int j = x - this.method_57742(i);
                if (j < -36 || j > 0) continue;
                this.selectedHeaderColumn = i;
                break;
            }
            if (this.selectedHeaderColumn >= 0) {
                this.selectStatType(this.getStatType(this.selectedHeaderColumn));
                this.client.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0f));
                return true;
            }
            return super.clickedHeader(x, y);
        }

        private StatType<?> getStatType(int headerColumn) {
            return headerColumn < this.blockStatTypes.size() ? this.blockStatTypes.get(headerColumn) : this.itemStatTypes.get(headerColumn - this.blockStatTypes.size());
        }

        private int getHeaderIndex(StatType<?> statType) {
            int i = this.blockStatTypes.indexOf(statType);
            if (i >= 0) {
                return i;
            }
            int j = this.itemStatTypes.indexOf(statType);
            if (j >= 0) {
                return j + this.blockStatTypes.size();
            }
            return -1;
        }

        @Override
        protected void renderDecorations(DrawContext context, int mouseX, int mouseY) {
            if (mouseY < this.getY() || mouseY > this.getBottom()) {
                return;
            }
            Entry entry = (Entry)this.getHoveredEntry();
            int i = this.getRowLeft();
            if (entry != null) {
                if (mouseX < i || mouseX > i + 18) {
                    return;
                }
                Item item = entry.getItem();
                context.drawTooltip(StatsScreen.this.textRenderer, item.getName(), mouseX, mouseY);
            } else {
                Text text = null;
                int j = mouseX - i;
                for (int k = 0; k < this.headerIconTextures.length; ++k) {
                    int l = this.method_57742(k);
                    if (j < l - 18 || j > l) continue;
                    text = this.getStatType(k).getName();
                    break;
                }
                if (text != null) {
                    context.drawTooltip(StatsScreen.this.textRenderer, text, mouseX, mouseY);
                }
            }
        }

        protected void selectStatType(StatType<?> statType) {
            if (statType != this.selectedStatType) {
                this.selectedStatType = statType;
                this.listOrder = -1;
            } else if (this.listOrder == -1) {
                this.listOrder = 1;
            } else {
                this.selectedStatType = null;
                this.listOrder = 0;
            }
            this.children().sort(this.comparator);
        }

        @Environment(value=EnvType.CLIENT)
        class ItemComparator
        implements Comparator<Entry> {
            ItemComparator() {
            }

            @Override
            public int compare(Entry entry, Entry entry2) {
                int j;
                int i;
                Item item = entry.getItem();
                Item item2 = entry2.getItem();
                if (ItemStatsListWidget.this.selectedStatType == null) {
                    i = 0;
                    j = 0;
                } else if (ItemStatsListWidget.this.blockStatTypes.contains(ItemStatsListWidget.this.selectedStatType)) {
                    StatType<?> statType = ItemStatsListWidget.this.selectedStatType;
                    i = item instanceof BlockItem ? StatsScreen.this.statHandler.getStat(statType, ((BlockItem)item).getBlock()) : -1;
                    j = item2 instanceof BlockItem ? StatsScreen.this.statHandler.getStat(statType, ((BlockItem)item2).getBlock()) : -1;
                } else {
                    StatType<?> statType = ItemStatsListWidget.this.selectedStatType;
                    i = StatsScreen.this.statHandler.getStat(statType, item);
                    j = StatsScreen.this.statHandler.getStat(statType, item2);
                }
                if (i == j) {
                    return ItemStatsListWidget.this.listOrder * Integer.compare(Item.getRawId(item), Item.getRawId(item2));
                }
                return ItemStatsListWidget.this.listOrder * Integer.compare(i, j);
            }

            @Override
            public /* synthetic */ int compare(Object a, Object b) {
                return this.compare((Entry)a, (Entry)b);
            }
        }

        @Environment(value=EnvType.CLIENT)
        class Entry
        extends AlwaysSelectedEntryListWidget.Entry<Entry> {
            private final Item item;

            Entry(Item item) {
                this.item = item;
            }

            public Item getItem() {
                return this.item;
            }

            @Override
            public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
                context.drawGuiTexture(SLOT_TEXTURE, x, y, 0, 18, 18);
                context.drawItemWithoutEntity(this.item.getDefaultStack(), x + 1, y + 1);
                if (StatsScreen.this.itemStats != null) {
                    int i;
                    for (i = 0; i < StatsScreen.this.itemStats.blockStatTypes.size(); ++i) {
                        Stat<Block> stat;
                        Item item = this.item;
                        if (item instanceof BlockItem) {
                            BlockItem blockItem = (BlockItem)item;
                            stat = StatsScreen.this.itemStats.blockStatTypes.get(i).getOrCreateStat(blockItem.getBlock());
                        } else {
                            stat = null;
                        }
                        this.render(context, stat, x + ItemStatsListWidget.this.method_57742(i), y + entryHeight / 2 - ((StatsScreen)StatsScreen.this).textRenderer.fontHeight / 2, index % 2 == 0);
                    }
                    for (i = 0; i < StatsScreen.this.itemStats.itemStatTypes.size(); ++i) {
                        this.render(context, StatsScreen.this.itemStats.itemStatTypes.get(i).getOrCreateStat(this.item), x + ItemStatsListWidget.this.method_57742(i + StatsScreen.this.itemStats.blockStatTypes.size()), y + entryHeight / 2 - ((StatsScreen)StatsScreen.this).textRenderer.fontHeight / 2, index % 2 == 0);
                    }
                }
            }

            protected void render(DrawContext context, @Nullable Stat<?> stat, int x, int y, boolean white) {
                Text text = stat == null ? NONE_TEXT : Text.literal(stat.format(StatsScreen.this.statHandler.getStat(stat)));
                context.drawTextWithShadow(StatsScreen.this.textRenderer, text, x - StatsScreen.this.textRenderer.getWidth(text), y, white ? Colors.WHITE : Colors.ALTERNATE_WHITE);
            }

            @Override
            public Text getNarration() {
                return Text.translatable("narrator.select", this.item.getName());
            }
        }
    }

    @Environment(value=EnvType.CLIENT)
    class EntityStatsListWidget
    extends AlwaysSelectedEntryListWidget<Entry> {
        public EntityStatsListWidget(MinecraftClient client) {
            super(client, StatsScreen.this.width, StatsScreen.this.height - 33 - 58, 33, ((StatsScreen)StatsScreen.this).textRenderer.fontHeight * 4);
            for (EntityType entityType : Registries.ENTITY_TYPE) {
                if (StatsScreen.this.statHandler.getStat(Stats.KILLED.getOrCreateStat(entityType)) <= 0 && StatsScreen.this.statHandler.getStat(Stats.KILLED_BY.getOrCreateStat(entityType)) <= 0) continue;
                this.addEntry(new Entry(entityType));
            }
        }

        @Override
        public int getRowWidth() {
            return 280;
        }

        @Environment(value=EnvType.CLIENT)
        class Entry
        extends AlwaysSelectedEntryListWidget.Entry<Entry> {
            private final Text entityTypeName;
            private final Text killedText;
            private final Text killedByText;
            private final boolean killedAny;
            private final boolean killedByAny;

            public Entry(EntityType<?> entityType) {
                this.entityTypeName = entityType.getName();
                int i = StatsScreen.this.statHandler.getStat(Stats.KILLED.getOrCreateStat(entityType));
                if (i == 0) {
                    this.killedText = Text.translatable("stat_type.minecraft.killed.none", this.entityTypeName);
                    this.killedAny = false;
                } else {
                    this.killedText = Text.translatable("stat_type.minecraft.killed", i, this.entityTypeName);
                    this.killedAny = true;
                }
                int j = StatsScreen.this.statHandler.getStat(Stats.KILLED_BY.getOrCreateStat(entityType));
                if (j == 0) {
                    this.killedByText = Text.translatable("stat_type.minecraft.killed_by.none", this.entityTypeName);
                    this.killedByAny = false;
                } else {
                    this.killedByText = Text.translatable("stat_type.minecraft.killed_by", this.entityTypeName, j);
                    this.killedByAny = true;
                }
            }

            @Override
            public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
                context.drawTextWithShadow(StatsScreen.this.textRenderer, this.entityTypeName, x + 2, y + 1, Colors.WHITE);
                context.drawTextWithShadow(StatsScreen.this.textRenderer, this.killedText, x + 2 + 10, y + 1 + ((StatsScreen)StatsScreen.this).textRenderer.fontHeight, this.killedAny ? Colors.ALTERNATE_WHITE : Colors.GRAY);
                context.drawTextWithShadow(StatsScreen.this.textRenderer, this.killedByText, x + 2 + 10, y + 1 + ((StatsScreen)StatsScreen.this).textRenderer.fontHeight * 2, this.killedByAny ? Colors.ALTERNATE_WHITE : Colors.GRAY);
            }

            @Override
            public Text getNarration() {
                return Text.translatable("narrator.select", ScreenTexts.joinSentences(this.killedText, this.killedByText));
            }
        }
    }
}

