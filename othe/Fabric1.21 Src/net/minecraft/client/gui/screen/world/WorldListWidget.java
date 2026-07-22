/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.client.gui.screen.world;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.NoSuchFileException;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.SharedConstants;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.client.gui.screen.FatalErrorScreen;
import net.minecraft.client.gui.screen.LoadingDisplay;
import net.minecraft.client.gui.screen.MessageScreen;
import net.minecraft.client.gui.screen.NoticeScreen;
import net.minecraft.client.gui.screen.ProgressScreen;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.screen.world.CreateWorldScreen;
import net.minecraft.client.gui.screen.world.EditWorldScreen;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import net.minecraft.client.gui.screen.world.SymlinkWarningScreen;
import net.minecraft.client.gui.screen.world.WorldIcon;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import net.minecraft.client.input.KeyCodes;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.client.world.GeneratorOptionsHolder;
import net.minecraft.nbt.NbtCrashException;
import net.minecraft.nbt.NbtException;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.WorldSavePath;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.path.SymlinkEntry;
import net.minecraft.util.path.SymlinkValidationException;
import net.minecraft.world.level.LevelInfo;
import net.minecraft.world.level.storage.LevelStorage;
import net.minecraft.world.level.storage.LevelStorageException;
import net.minecraft.world.level.storage.LevelSummary;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public class WorldListWidget
extends AlwaysSelectedEntryListWidget<Entry> {
    public static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT).withZone(ZoneId.systemDefault());
    static final Identifier ERROR_HIGHLIGHTED_TEXTURE = Identifier.ofVanilla("world_list/error_highlighted");
    static final Identifier ERROR_TEXTURE = Identifier.ofVanilla("world_list/error");
    static final Identifier MARKED_JOIN_HIGHLIGHTED_TEXTURE = Identifier.ofVanilla("world_list/marked_join_highlighted");
    static final Identifier MARKED_JOIN_TEXTURE = Identifier.ofVanilla("world_list/marked_join");
    static final Identifier WARNING_HIGHLIGHTED_TEXTURE = Identifier.ofVanilla("world_list/warning_highlighted");
    static final Identifier WARNING_TEXTURE = Identifier.ofVanilla("world_list/warning");
    static final Identifier JOIN_HIGHLIGHTED_TEXTURE = Identifier.ofVanilla("world_list/join_highlighted");
    static final Identifier JOIN_TEXTURE = Identifier.ofVanilla("world_list/join");
    static final Logger LOGGER = LogUtils.getLogger();
    static final Text FROM_NEWER_VERSION_FIRST_LINE = Text.translatable("selectWorld.tooltip.fromNewerVersion1").formatted(Formatting.RED);
    static final Text FROM_NEWER_VERSION_SECOND_LINE = Text.translatable("selectWorld.tooltip.fromNewerVersion2").formatted(Formatting.RED);
    static final Text SNAPSHOT_FIRST_LINE = Text.translatable("selectWorld.tooltip.snapshot1").formatted(Formatting.GOLD);
    static final Text SNAPSHOT_SECOND_LINE = Text.translatable("selectWorld.tooltip.snapshot2").formatted(Formatting.GOLD);
    static final Text LOCKED_TEXT = Text.translatable("selectWorld.locked").formatted(Formatting.RED);
    static final Text CONVERSION_TOOLTIP = Text.translatable("selectWorld.conversion.tooltip").formatted(Formatting.RED);
    static final Text INCOMPATIBLE_TOOLTIP = Text.translatable("selectWorld.incompatible.tooltip").formatted(Formatting.RED);
    static final Text EXPERIMENTAL_TEXT = Text.translatable("selectWorld.experimental");
    private final SelectWorldScreen parent;
    private CompletableFuture<List<LevelSummary>> levelsFuture;
    @Nullable
    private List<LevelSummary> levels;
    private String search;
    private final LoadingEntry loadingEntry;

    public WorldListWidget(SelectWorldScreen parent, MinecraftClient client, int width, int height, int y, int itemHeight, String search, @Nullable WorldListWidget oldWidget) {
        super(client, width, height, y, itemHeight);
        this.parent = parent;
        this.loadingEntry = new LoadingEntry(client);
        this.search = search;
        this.levelsFuture = oldWidget != null ? oldWidget.levelsFuture : this.loadLevels();
        this.show(this.tryGet());
    }

    @Override
    protected void clearEntries() {
        this.children().forEach(Entry::close);
        super.clearEntries();
    }

    @Nullable
    private List<LevelSummary> tryGet() {
        try {
            return this.levelsFuture.getNow(null);
        } catch (CancellationException | CompletionException runtimeException) {
            return null;
        }
    }

    void load() {
        this.levelsFuture = this.loadLevels();
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        Optional<WorldEntry> optional;
        if (KeyCodes.isToggle(keyCode) && (optional = this.getSelectedAsOptional()).isPresent()) {
            if (optional.get().isLevelSelectable()) {
                this.client.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0f));
                optional.get().play();
            }
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        List<LevelSummary> list = this.tryGet();
        if (list != this.levels) {
            this.show(list);
        }
        super.renderWidget(context, mouseX, mouseY, delta);
    }

    private void show(@Nullable List<LevelSummary> levels) {
        if (levels == null) {
            this.showLoadingScreen();
        } else {
            this.showSummaries(this.search, levels);
        }
        this.levels = levels;
    }

    public void setSearch(String search) {
        if (this.levels != null && !search.equals(this.search)) {
            this.showSummaries(search, this.levels);
        }
        this.search = search;
    }

    private CompletableFuture<List<LevelSummary>> loadLevels() {
        LevelStorage.LevelList levelList;
        try {
            levelList = this.client.getLevelStorage().getLevelList();
        } catch (LevelStorageException levelStorageException) {
            LOGGER.error("Couldn't load level list", levelStorageException);
            this.showUnableToLoadScreen(levelStorageException.getMessageText());
            return CompletableFuture.completedFuture(List.of());
        }
        if (levelList.isEmpty()) {
            CreateWorldScreen.create(this.client, null);
            return CompletableFuture.completedFuture(List.of());
        }
        return this.client.getLevelStorage().loadSummaries(levelList).exceptionally(throwable -> {
            this.client.setCrashReportSupplierAndAddDetails(CrashReport.create(throwable, "Couldn't load level list"));
            return List.of();
        });
    }

    private void showSummaries(String search, List<LevelSummary> summaries) {
        this.clearEntries();
        search = search.toLowerCase(Locale.ROOT);
        for (LevelSummary levelSummary : summaries) {
            if (!this.shouldShow(search, levelSummary)) continue;
            this.addEntry(new WorldEntry(this, levelSummary));
        }
        this.narrateScreenIfNarrationEnabled();
    }

    private boolean shouldShow(String search, LevelSummary summary) {
        return summary.getDisplayName().toLowerCase(Locale.ROOT).contains(search) || summary.getName().toLowerCase(Locale.ROOT).contains(search);
    }

    private void showLoadingScreen() {
        this.clearEntries();
        this.addEntry(this.loadingEntry);
        this.narrateScreenIfNarrationEnabled();
    }

    private void narrateScreenIfNarrationEnabled() {
        this.refreshScroll();
        this.parent.narrateScreenIfNarrationEnabled(true);
    }

    private void showUnableToLoadScreen(Text message) {
        this.client.setScreen(new FatalErrorScreen(Text.translatable("selectWorld.unable_to_load"), message));
    }

    @Override
    public int getRowWidth() {
        return 270;
    }

    @Override
    public void setSelected(@Nullable Entry entry) {
        LevelSummary levelSummary;
        super.setSelected(entry);
        if (entry instanceof WorldEntry) {
            WorldEntry worldEntry = (WorldEntry)entry;
            levelSummary = worldEntry.level;
        } else {
            levelSummary = null;
        }
        this.parent.worldSelected(levelSummary);
    }

    public Optional<WorldEntry> getSelectedAsOptional() {
        Entry entry = (Entry)this.getSelectedOrNull();
        if (entry instanceof WorldEntry) {
            WorldEntry worldEntry = (WorldEntry)entry;
            return Optional.of(worldEntry);
        }
        return Optional.empty();
    }

    public SelectWorldScreen getParent() {
        return this.parent;
    }

    @Override
    public void appendClickableNarrations(NarrationMessageBuilder builder) {
        if (this.children().contains(this.loadingEntry)) {
            this.loadingEntry.appendNarrations(builder);
            return;
        }
        super.appendClickableNarrations(builder);
    }

    @Environment(value=EnvType.CLIENT)
    public static class LoadingEntry
    extends Entry {
        private static final Text LOADING_LIST_TEXT = Text.translatable("selectWorld.loading_list");
        private final MinecraftClient client;

        public LoadingEntry(MinecraftClient client) {
            this.client = client;
        }

        @Override
        public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            int i = (this.client.currentScreen.width - this.client.textRenderer.getWidth(LOADING_LIST_TEXT)) / 2;
            int j = y + (entryHeight - this.client.textRenderer.fontHeight) / 2;
            context.drawText(this.client.textRenderer, LOADING_LIST_TEXT, i, j, 0xFFFFFF, false);
            String string = LoadingDisplay.get(Util.getMeasuringTimeMs());
            int k = (this.client.currentScreen.width - this.client.textRenderer.getWidth(string)) / 2;
            int l = j + this.client.textRenderer.fontHeight;
            context.drawText(this.client.textRenderer, string, k, l, Colors.GRAY, false);
        }

        @Override
        public Text getNarration() {
            return LOADING_LIST_TEXT;
        }
    }

    @Environment(value=EnvType.CLIENT)
    public final class WorldEntry
    extends Entry
    implements AutoCloseable {
        private static final int field_32435 = 32;
        private static final int field_32436 = 32;
        private final MinecraftClient client;
        private final SelectWorldScreen screen;
        final LevelSummary level;
        private final WorldIcon icon;
        @Nullable
        private Path iconPath;
        private long time;

        public WorldEntry(WorldListWidget levelList, LevelSummary level) {
            this.client = levelList.client;
            this.screen = levelList.getParent();
            this.level = level;
            this.icon = WorldIcon.forWorld(this.client.getTextureManager(), level.getName());
            this.iconPath = level.getIconPath();
            this.validateIconPath();
            this.loadIcon();
        }

        private void validateIconPath() {
            if (this.iconPath == null) {
                return;
            }
            try {
                BasicFileAttributes basicFileAttributes = Files.readAttributes(this.iconPath, BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS);
                if (basicFileAttributes.isSymbolicLink()) {
                    List<SymlinkEntry> list = this.client.getSymlinkFinder().validate(this.iconPath);
                    if (!list.isEmpty()) {
                        LOGGER.warn("{}", (Object)SymlinkValidationException.getMessage(this.iconPath, list));
                        this.iconPath = null;
                    } else {
                        basicFileAttributes = Files.readAttributes(this.iconPath, BasicFileAttributes.class, new LinkOption[0]);
                    }
                }
                if (!basicFileAttributes.isRegularFile()) {
                    this.iconPath = null;
                }
            } catch (NoSuchFileException noSuchFileException) {
                this.iconPath = null;
            } catch (IOException iOException) {
                LOGGER.error("could not validate symlink", iOException);
                this.iconPath = null;
            }
        }

        @Override
        public Text getNarration() {
            MutableText text = Text.translatable("narrator.select.world_info", this.level.getDisplayName(), Text.of(new Date(this.level.getLastPlayed())), this.level.getDetails());
            if (this.level.isLocked()) {
                text = ScreenTexts.joinSentences(text, LOCKED_TEXT);
            }
            if (this.level.isExperimental()) {
                text = ScreenTexts.joinSentences(text, EXPERIMENTAL_TEXT);
            }
            return Text.translatable("narrator.select", text);
        }

        @Override
        public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            Object string = this.level.getDisplayName();
            Object string2 = this.level.getName();
            long l = this.level.getLastPlayed();
            if (l != -1L) {
                string2 = (String)string2 + " (" + DATE_FORMAT.format(Instant.ofEpochMilli(l)) + ")";
            }
            if (StringUtils.isEmpty((CharSequence)string)) {
                string = I18n.translate("selectWorld.world", new Object[0]) + " " + (index + 1);
            }
            Text text = this.level.getDetails();
            context.drawText(this.client.textRenderer, (String)string, x + 32 + 3, y + 1, 0xFFFFFF, false);
            context.drawText(this.client.textRenderer, (String)string2, x + 32 + 3, y + this.client.textRenderer.fontHeight + 3, Colors.GRAY, false);
            context.drawText(this.client.textRenderer, text, x + 32 + 3, y + this.client.textRenderer.fontHeight + this.client.textRenderer.fontHeight + 3, Colors.GRAY, false);
            RenderSystem.enableBlend();
            context.drawTexture(this.icon.getTextureId(), x, y, 0.0f, 0.0f, 32, 32, 32, 32);
            RenderSystem.disableBlend();
            if (this.client.options.getTouchscreen().getValue().booleanValue() || hovered) {
                Identifier identifier4;
                context.fill(x, y, x + 32, y + 32, -1601138544);
                int i = mouseX - x;
                boolean bl = i < 32;
                Identifier identifier = bl ? JOIN_HIGHLIGHTED_TEXTURE : JOIN_TEXTURE;
                Identifier identifier2 = bl ? WARNING_HIGHLIGHTED_TEXTURE : WARNING_TEXTURE;
                Identifier identifier3 = bl ? ERROR_HIGHLIGHTED_TEXTURE : ERROR_TEXTURE;
                Identifier identifier5 = identifier4 = bl ? MARKED_JOIN_HIGHLIGHTED_TEXTURE : MARKED_JOIN_TEXTURE;
                if (this.level instanceof LevelSummary.SymlinkLevelSummary || this.level instanceof LevelSummary.RecoveryWarning) {
                    context.drawGuiTexture(identifier3, x, y, 32, 32);
                    context.drawGuiTexture(identifier4, x, y, 32, 32);
                    return;
                }
                if (this.level.isLocked()) {
                    context.drawGuiTexture(identifier3, x, y, 32, 32);
                    if (bl) {
                        this.screen.setTooltip(this.client.textRenderer.wrapLines(LOCKED_TEXT, 175));
                    }
                } else if (this.level.requiresConversion()) {
                    context.drawGuiTexture(identifier3, x, y, 32, 32);
                    if (bl) {
                        this.screen.setTooltip(this.client.textRenderer.wrapLines(CONVERSION_TOOLTIP, 175));
                    }
                } else if (!this.level.isVersionAvailable()) {
                    context.drawGuiTexture(identifier3, x, y, 32, 32);
                    if (bl) {
                        this.screen.setTooltip(this.client.textRenderer.wrapLines(INCOMPATIBLE_TOOLTIP, 175));
                    }
                } else if (this.level.shouldPromptBackup()) {
                    context.drawGuiTexture(identifier4, x, y, 32, 32);
                    if (this.level.wouldBeDowngraded()) {
                        context.drawGuiTexture(identifier3, x, y, 32, 32);
                        if (bl) {
                            this.screen.setTooltip(ImmutableList.of(FROM_NEWER_VERSION_FIRST_LINE.asOrderedText(), FROM_NEWER_VERSION_SECOND_LINE.asOrderedText()));
                        }
                    } else if (!SharedConstants.getGameVersion().isStable()) {
                        context.drawGuiTexture(identifier2, x, y, 32, 32);
                        if (bl) {
                            this.screen.setTooltip(ImmutableList.of(SNAPSHOT_FIRST_LINE.asOrderedText(), SNAPSHOT_SECOND_LINE.asOrderedText()));
                        }
                    }
                } else {
                    context.drawGuiTexture(identifier, x, y, 32, 32);
                }
            }
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (!this.level.isSelectable()) {
                return true;
            }
            WorldListWidget.this.setSelected(this);
            if (mouseX - (double)WorldListWidget.this.getRowLeft() <= 32.0 || Util.getMeasuringTimeMs() - this.time < 250L) {
                if (this.isLevelSelectable()) {
                    this.client.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0f));
                    this.play();
                }
                return true;
            }
            this.time = Util.getMeasuringTimeMs();
            return super.mouseClicked(mouseX, mouseY, button);
        }

        public boolean isLevelSelectable() {
            return this.level.isSelectable();
        }

        public void play() {
            if (!this.level.isSelectable()) {
                return;
            }
            if (this.level instanceof LevelSummary.SymlinkLevelSummary) {
                this.client.setScreen(SymlinkWarningScreen.world(() -> this.client.setScreen(this.screen)));
                return;
            }
            this.client.createIntegratedServerLoader().start(this.level.getName(), () -> {
                WorldListWidget.this.load();
                this.client.setScreen(this.screen);
            });
        }

        public void deleteIfConfirmed() {
            this.client.setScreen(new ConfirmScreen(confirmed -> {
                if (confirmed) {
                    this.client.setScreen(new ProgressScreen(true));
                    this.delete();
                }
                this.client.setScreen(this.screen);
            }, Text.translatable("selectWorld.deleteQuestion"), Text.translatable("selectWorld.deleteWarning", this.level.getDisplayName()), Text.translatable("selectWorld.deleteButton"), ScreenTexts.CANCEL));
        }

        public void delete() {
            LevelStorage levelStorage = this.client.getLevelStorage();
            String string = this.level.getName();
            try (LevelStorage.Session session = levelStorage.createSessionWithoutSymlinkCheck(string);){
                session.deleteSessionLock();
            } catch (IOException iOException) {
                SystemToast.addWorldDeleteFailureToast(this.client, string);
                LOGGER.error("Failed to delete world {}", (Object)string, (Object)iOException);
            }
            WorldListWidget.this.load();
        }

        public void edit() {
            EditWorldScreen editWorldScreen;
            LevelStorage.Session session;
            this.openReadingWorldScreen();
            String string = this.level.getName();
            try {
                session = this.client.getLevelStorage().createSession(string);
            } catch (IOException iOException) {
                SystemToast.addWorldAccessFailureToast(this.client, string);
                LOGGER.error("Failed to access level {}", (Object)string, (Object)iOException);
                WorldListWidget.this.load();
                return;
            } catch (SymlinkValidationException symlinkValidationException) {
                LOGGER.warn("{}", (Object)symlinkValidationException.getMessage());
                this.client.setScreen(SymlinkWarningScreen.world(() -> this.client.setScreen(this.screen)));
                return;
            }
            try {
                editWorldScreen = EditWorldScreen.create(this.client, session, edited -> {
                    session.tryClose();
                    if (edited) {
                        WorldListWidget.this.load();
                    }
                    this.client.setScreen(this.screen);
                });
            } catch (IOException | NbtCrashException | NbtException exception) {
                session.tryClose();
                SystemToast.addWorldAccessFailureToast(this.client, string);
                LOGGER.error("Failed to load world data {}", (Object)string, (Object)exception);
                WorldListWidget.this.load();
                return;
            }
            this.client.setScreen(editWorldScreen);
        }

        public void recreate() {
            this.openReadingWorldScreen();
            try (LevelStorage.Session session = this.client.getLevelStorage().createSession(this.level.getName());){
                Pair<LevelInfo, GeneratorOptionsHolder> pair = this.client.createIntegratedServerLoader().loadForRecreation(session);
                LevelInfo levelInfo = pair.getFirst();
                GeneratorOptionsHolder generatorOptionsHolder = pair.getSecond();
                Path path = CreateWorldScreen.copyDataPack(session.getDirectory(WorldSavePath.DATAPACKS), this.client);
                generatorOptionsHolder.initializeIndexedFeaturesLists();
                if (generatorOptionsHolder.generatorOptions().isLegacyCustomizedType()) {
                    this.client.setScreen(new ConfirmScreen(confirmed -> this.client.setScreen(confirmed ? CreateWorldScreen.create(this.client, this.screen, levelInfo, generatorOptionsHolder, path) : this.screen), Text.translatable("selectWorld.recreate.customized.title"), Text.translatable("selectWorld.recreate.customized.text"), ScreenTexts.PROCEED, ScreenTexts.CANCEL));
                } else {
                    this.client.setScreen(CreateWorldScreen.create(this.client, this.screen, levelInfo, generatorOptionsHolder, path));
                }
            } catch (SymlinkValidationException symlinkValidationException) {
                LOGGER.warn("{}", (Object)symlinkValidationException.getMessage());
                this.client.setScreen(SymlinkWarningScreen.world(() -> this.client.setScreen(this.screen)));
            } catch (Exception exception) {
                LOGGER.error("Unable to recreate world", exception);
                this.client.setScreen(new NoticeScreen(() -> this.client.setScreen(this.screen), Text.translatable("selectWorld.recreate.error.title"), Text.translatable("selectWorld.recreate.error.text")));
            }
        }

        private void openReadingWorldScreen() {
            this.client.setScreenAndRender(new MessageScreen(Text.translatable("selectWorld.data_read")));
        }

        private void loadIcon() {
            boolean bl;
            boolean bl2 = bl = this.iconPath != null && Files.isRegularFile(this.iconPath, new LinkOption[0]);
            if (bl) {
                try (InputStream inputStream = Files.newInputStream(this.iconPath, new OpenOption[0]);){
                    this.icon.load(NativeImage.read(inputStream));
                } catch (Throwable throwable) {
                    LOGGER.error("Invalid icon for world {}", (Object)this.level.getName(), (Object)throwable);
                    this.iconPath = null;
                }
            } else {
                this.icon.destroy();
            }
        }

        @Override
        public void close() {
            this.icon.close();
        }

        public String getLevelDisplayName() {
            return this.level.getDisplayName();
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static abstract class Entry
    extends AlwaysSelectedEntryListWidget.Entry<Entry>
    implements AutoCloseable {
        @Override
        public void close() {
        }
    }
}

