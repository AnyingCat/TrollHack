/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.client.realms.gui.screen;

import com.mojang.logging.LogUtils;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.DirectionalLayoutWidget;
import net.minecraft.client.gui.widget.ElementListWidget;
import net.minecraft.client.gui.widget.ThreePartsLayoutWidget;
import net.minecraft.client.realms.RealmsClient;
import net.minecraft.client.realms.dto.Backup;
import net.minecraft.client.realms.dto.RealmsServer;
import net.minecraft.client.realms.exception.RealmsServiceException;
import net.minecraft.client.realms.gui.RealmsPopups;
import net.minecraft.client.realms.gui.screen.RealmsBackupInfoScreen;
import net.minecraft.client.realms.gui.screen.RealmsConfigureWorldScreen;
import net.minecraft.client.realms.gui.screen.RealmsLongRunningMcoTaskScreen;
import net.minecraft.client.realms.gui.screen.RealmsScreen;
import net.minecraft.client.realms.task.DownloadTask;
import net.minecraft.client.realms.task.RestoreTask;
import net.minecraft.client.realms.util.RealmsUtil;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public class RealmsBackupScreen
extends RealmsScreen {
    static final Logger LOGGER = LogUtils.getLogger();
    private static final Text BACKUPS_TEXT = Text.translatable("mco.configure.world.backup");
    static final Text RESTORE_TEXT = Text.translatable("mco.backup.button.restore");
    static final Text CHANGES_TOOLTIP = Text.translatable("mco.backup.changes.tooltip");
    private static final Text NO_BACKUPS_TEXT = Text.translatable("mco.backup.nobackups");
    private static final Text DOWNLOAD_TEXT = Text.translatable("mco.backup.button.download");
    private static final String UPLOADED = "uploaded";
    private static final int field_49447 = 8;
    final RealmsConfigureWorldScreen parent;
    List<Backup> backups = Collections.emptyList();
    @Nullable
    BackupObjectSelectionList selectionList;
    final ThreePartsLayoutWidget layout = new ThreePartsLayoutWidget(this);
    private final int slotId;
    @Nullable
    ButtonWidget downloadButton;
    final RealmsServer serverData;
    boolean noBackups = false;

    public RealmsBackupScreen(RealmsConfigureWorldScreen parent, RealmsServer serverData, int slotId) {
        super(BACKUPS_TEXT);
        this.parent = parent;
        this.serverData = serverData;
        this.slotId = slotId;
    }

    @Override
    public void init() {
        this.layout.addHeader(BACKUPS_TEXT, this.textRenderer);
        this.selectionList = this.layout.addBody(new BackupObjectSelectionList());
        DirectionalLayoutWidget directionalLayoutWidget = this.layout.addFooter(DirectionalLayoutWidget.horizontal().spacing(8));
        this.downloadButton = directionalLayoutWidget.add(ButtonWidget.builder(DOWNLOAD_TEXT, button -> this.downloadClicked()).build());
        this.downloadButton.active = false;
        directionalLayoutWidget.add(ButtonWidget.builder(ScreenTexts.BACK, button -> this.close()).build());
        this.layout.forEachChild(element -> {
            ClickableWidget cfr_ignored_0 = (ClickableWidget)this.addDrawableChild(element);
        });
        this.initTabNavigation();
        this.startBackupFetcher();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        if (this.noBackups && this.selectionList != null) {
            context.drawText(this.textRenderer, NO_BACKUPS_TEXT, this.width / 2 - this.textRenderer.getWidth(NO_BACKUPS_TEXT) / 2, this.selectionList.getY() + this.selectionList.getHeight() / 2 - this.textRenderer.fontHeight / 2, Colors.WHITE, false);
        }
    }

    @Override
    protected void initTabNavigation() {
        this.layout.refreshPositions();
        if (this.selectionList != null) {
            this.selectionList.position(this.width, this.layout);
        }
    }

    private void startBackupFetcher() {
        new Thread("Realms-fetch-backups"){

            @Override
            public void run() {
                RealmsClient realmsClient = RealmsClient.create();
                try {
                    List<Backup> list = realmsClient.backupsFor((long)RealmsBackupScreen.this.serverData.id).backups;
                    RealmsBackupScreen.this.client.execute(() -> {
                        RealmsBackupScreen.this.backups = list;
                        RealmsBackupScreen.this.noBackups = RealmsBackupScreen.this.backups.isEmpty();
                        if (!RealmsBackupScreen.this.noBackups && RealmsBackupScreen.this.downloadButton != null) {
                            RealmsBackupScreen.this.downloadButton.active = true;
                        }
                        if (RealmsBackupScreen.this.selectionList != null) {
                            RealmsBackupScreen.this.selectionList.children().clear();
                            for (Backup backup : RealmsBackupScreen.this.backups) {
                                RealmsBackupScreen.this.selectionList.addEntry(backup);
                            }
                        }
                    });
                } catch (RealmsServiceException realmsServiceException) {
                    LOGGER.error("Couldn't request backups", realmsServiceException);
                }
            }
        }.start();
    }

    @Override
    public void close() {
        this.client.setScreen(this.parent);
    }

    private void downloadClicked() {
        this.client.setScreen(RealmsPopups.createInfoPopup(this, Text.translatable("mco.configure.world.restore.download.question.line1"), popupScreen -> this.client.setScreen(new RealmsLongRunningMcoTaskScreen(this.parent.getNewScreen(), new DownloadTask(this.serverData.id, this.slotId, this.serverData.name + " (" + this.serverData.slots.get(this.serverData.activeSlot).getSlotName(this.serverData.activeSlot) + ")", this)))));
    }

    @Environment(value=EnvType.CLIENT)
    class BackupObjectSelectionList
    extends ElementListWidget<BackupObjectSelectionListEntry> {
        private static final int field_49450 = 36;

        public BackupObjectSelectionList() {
            super(MinecraftClient.getInstance(), RealmsBackupScreen.this.width, RealmsBackupScreen.this.layout.getContentHeight(), RealmsBackupScreen.this.layout.getHeaderHeight(), 36);
        }

        public void addEntry(Backup backup) {
            this.addEntry(new BackupObjectSelectionListEntry(backup));
        }

        @Override
        public int getMaxPosition() {
            return this.getEntryCount() * 36 + this.headerHeight;
        }

        @Override
        public int getRowWidth() {
            return 300;
        }
    }

    @Environment(value=EnvType.CLIENT)
    class BackupObjectSelectionListEntry
    extends ElementListWidget.Entry<BackupObjectSelectionListEntry> {
        private static final int field_44525 = 2;
        private final Backup mBackup;
        @Nullable
        private ButtonWidget restoreButton;
        @Nullable
        private ButtonWidget changesButton;
        private final List<ClickableWidget> buttons = new ArrayList<ClickableWidget>();

        public BackupObjectSelectionListEntry(Backup backup) {
            this.mBackup = backup;
            this.updateChangeList(backup);
            if (!backup.changeList.isEmpty()) {
                this.changesButton = ButtonWidget.builder(CHANGES_TOOLTIP, button -> RealmsBackupScreen.this.client.setScreen(new RealmsBackupInfoScreen(RealmsBackupScreen.this, this.mBackup))).width(8 + RealmsBackupScreen.this.textRenderer.getWidth(CHANGES_TOOLTIP)).narrationSupplier(textSupplier -> ScreenTexts.joinSentences(Text.translatable("mco.backup.narration", this.getLastModifiedDate()), (Text)textSupplier.get())).build();
                this.buttons.add(this.changesButton);
            }
            if (!RealmsBackupScreen.this.serverData.expired) {
                this.restoreButton = ButtonWidget.builder(RESTORE_TEXT, button -> this.restore()).width(8 + RealmsBackupScreen.this.textRenderer.getWidth(CHANGES_TOOLTIP)).narrationSupplier(textSupplier -> ScreenTexts.joinSentences(Text.translatable("mco.backup.narration", this.getLastModifiedDate()), (Text)textSupplier.get())).build();
                this.buttons.add(this.restoreButton);
            }
        }

        private void updateChangeList(Backup backup) {
            int i = RealmsBackupScreen.this.backups.indexOf(backup);
            if (i == RealmsBackupScreen.this.backups.size() - 1) {
                return;
            }
            Backup backup2 = RealmsBackupScreen.this.backups.get(i + 1);
            for (String string : backup.metadata.keySet()) {
                if (!string.contains(RealmsBackupScreen.UPLOADED) && backup2.metadata.containsKey(string)) {
                    if (backup.metadata.get(string).equals(backup2.metadata.get(string))) continue;
                    this.addChange(string);
                    continue;
                }
                this.addChange(string);
            }
        }

        private void addChange(String metadataKey) {
            if (metadataKey.contains(RealmsBackupScreen.UPLOADED)) {
                String string = DateFormat.getDateTimeInstance(3, 3).format(this.mBackup.lastModifiedDate);
                this.mBackup.changeList.put(metadataKey, string);
                this.mBackup.setUploadedVersion(true);
            } else {
                this.mBackup.changeList.put(metadataKey, this.mBackup.metadata.get(metadataKey));
            }
        }

        private String getLastModifiedDate() {
            return DateFormat.getDateTimeInstance(3, 3).format(this.mBackup.lastModifiedDate);
        }

        private void restore() {
            Text text = RealmsUtil.convertToAgePresentation(this.mBackup.lastModifiedDate);
            MutableText text2 = Text.translatable("mco.configure.world.restore.question.line1", this.getLastModifiedDate(), text);
            RealmsBackupScreen.this.client.setScreen(RealmsPopups.createContinuableWarningPopup(RealmsBackupScreen.this, text2, popup -> RealmsBackupScreen.this.client.setScreen(new RealmsLongRunningMcoTaskScreen(RealmsBackupScreen.this.parent.getNewScreen(), new RestoreTask(this.mBackup, RealmsBackupScreen.this.serverData.id, RealmsBackupScreen.this.parent)))));
        }

        @Override
        public List<? extends Element> children() {
            return this.buttons;
        }

        @Override
        public List<? extends Selectable> selectableChildren() {
            return this.buttons;
        }

        @Override
        public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            int i = y + entryHeight / 2;
            int j = i - ((RealmsBackupScreen)RealmsBackupScreen.this).textRenderer.fontHeight - 2;
            int k = i + 2;
            int l = this.mBackup.isUploadedVersion() ? -8388737 : Colors.WHITE;
            context.drawText(RealmsBackupScreen.this.textRenderer, Text.translatable("mco.backup.entry", RealmsUtil.convertToAgePresentation(this.mBackup.lastModifiedDate)), x, j, l, false);
            context.drawText(RealmsBackupScreen.this.textRenderer, this.getMediumDatePresentation(this.mBackup.lastModifiedDate), x, k, 0x4C4C4C, false);
            int m = 0;
            int n = y + entryHeight / 2 - 10;
            if (this.restoreButton != null) {
                this.restoreButton.setX(x + entryWidth - (m += this.restoreButton.getWidth() + 8));
                this.restoreButton.setY(n);
                this.restoreButton.render(context, mouseX, mouseY, tickDelta);
            }
            if (this.changesButton != null) {
                this.changesButton.setX(x + entryWidth - (m += this.changesButton.getWidth() + 8));
                this.changesButton.setY(n);
                this.changesButton.render(context, mouseX, mouseY, tickDelta);
            }
        }

        private String getMediumDatePresentation(Date lastModifiedDate) {
            return DateFormat.getDateTimeInstance(3, 3).format(lastModifiedDate);
        }
    }
}

