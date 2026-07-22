/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.client.gui.screen.report;

import it.unimi.dsi.fastutil.ints.IntSet;
import java.util.UUID;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.report.AbuseReportReasonScreen;
import net.minecraft.client.gui.screen.report.ChatSelectionScreen;
import net.minecraft.client.gui.screen.report.ReportScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.EditBoxWidget;
import net.minecraft.client.gui.widget.LayoutWidgets;
import net.minecraft.client.session.report.AbuseReportContext;
import net.minecraft.client.session.report.AbuseReportReason;
import net.minecraft.client.session.report.ChatAbuseReport;
import net.minecraft.text.Text;

@Environment(value=EnvType.CLIENT)
public class ChatReportScreen
extends ReportScreen<ChatAbuseReport.Builder> {
    private static final Text TITLE_TEXT = Text.translatable("gui.chatReport.title");
    private static final Text SELECT_CHAT_TEXT = Text.translatable("gui.chatReport.select_chat");
    private EditBoxWidget commentsBox;
    private ButtonWidget selectChatButton;
    private ButtonWidget selectReasonButton;

    private ChatReportScreen(Screen parent, AbuseReportContext context, ChatAbuseReport.Builder reportBuilder) {
        super(TITLE_TEXT, parent, context, reportBuilder);
    }

    public ChatReportScreen(Screen parent, AbuseReportContext reporter, UUID reportedPlayerUuid) {
        this(parent, reporter, new ChatAbuseReport.Builder(reportedPlayerUuid, reporter.getSender().getLimits()));
    }

    public ChatReportScreen(Screen parent, AbuseReportContext context, ChatAbuseReport report) {
        this(parent, context, new ChatAbuseReport.Builder(report, context.getSender().getLimits()));
    }

    @Override
    protected void addContent() {
        this.selectChatButton = this.layout.add(ButtonWidget.builder(SELECT_CHAT_TEXT, button -> this.client.setScreen(new ChatSelectionScreen(this, this.context, (ChatAbuseReport.Builder)this.reportBuilder, updatedReportBuilder -> {
            this.reportBuilder = updatedReportBuilder;
            this.onChange();
        }))).width(280).build());
        this.selectReasonButton = ButtonWidget.builder(SELECT_REASON_TEXT, button -> this.client.setScreen(new AbuseReportReasonScreen(this, ((ChatAbuseReport.Builder)this.reportBuilder).getReason(), reason -> {
            ((ChatAbuseReport.Builder)this.reportBuilder).setReason((AbuseReportReason)((Object)((Object)reason)));
            this.onChange();
        }))).width(280).build();
        this.layout.add(LayoutWidgets.createLabeledWidget(this.textRenderer, this.selectReasonButton, OBSERVED_WHAT_TEXT));
        this.commentsBox = this.createCommentsBox(280, this.textRenderer.fontHeight * 8, comments -> {
            ((ChatAbuseReport.Builder)this.reportBuilder).setOpinionComments((String)comments);
            this.onChange();
        });
        this.layout.add(LayoutWidgets.createLabeledWidget(this.textRenderer, this.commentsBox, MORE_COMMENTS_TEXT, positioner -> positioner.marginBottom(12)));
    }

    @Override
    protected void onChange() {
        IntSet intSet = ((ChatAbuseReport.Builder)this.reportBuilder).getSelectedMessages();
        if (intSet.isEmpty()) {
            this.selectChatButton.setMessage(SELECT_CHAT_TEXT);
        } else {
            this.selectChatButton.setMessage(Text.translatable("gui.chatReport.selected_chat", intSet.size()));
        }
        AbuseReportReason abuseReportReason = ((ChatAbuseReport.Builder)this.reportBuilder).getReason();
        if (abuseReportReason != null) {
            this.selectReasonButton.setMessage(abuseReportReason.getText());
        } else {
            this.selectReasonButton.setMessage(SELECT_REASON_TEXT);
        }
        super.onChange();
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (super.mouseReleased(mouseX, mouseY, button)) {
            return true;
        }
        return this.commentsBox.mouseReleased(mouseX, mouseY, button);
    }
}

