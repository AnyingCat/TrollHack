/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.client.session.report;

import com.google.common.collect.Lists;
import com.mojang.authlib.minecraft.report.AbuseReportLimits;
import com.mojang.authlib.minecraft.report.ReportChatMessage;
import com.mojang.authlib.minecraft.report.ReportEvidence;
import com.mojang.authlib.minecraft.report.ReportedEntity;
import com.mojang.datafixers.util.Either;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import java.nio.ByteBuffer;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.report.ChatReportScreen;
import net.minecraft.client.session.report.AbuseReport;
import net.minecraft.client.session.report.AbuseReportContext;
import net.minecraft.client.session.report.AbuseReportType;
import net.minecraft.client.session.report.ContextMessageCollector;
import net.minecraft.client.session.report.log.ReceivedMessage;
import net.minecraft.network.message.MessageBody;
import net.minecraft.network.message.MessageLink;
import net.minecraft.network.message.MessageSignatureData;
import net.minecraft.util.Nullables;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class ChatAbuseReport
extends AbuseReport {
    final IntSet selectedMessages = new IntOpenHashSet();

    ChatAbuseReport(UUID uUID, Instant instant, UUID uUID2) {
        super(uUID, instant, uUID2);
    }

    public void toggleMessageSelection(int index, AbuseReportLimits limits) {
        if (this.selectedMessages.contains(index)) {
            this.selectedMessages.remove(index);
        } else if (this.selectedMessages.size() < limits.maxReportedMessageCount()) {
            this.selectedMessages.add(index);
        }
    }

    @Override
    public ChatAbuseReport copy() {
        ChatAbuseReport chatAbuseReport = new ChatAbuseReport(this.reportId, this.currentTime, this.reportedPlayerUuid);
        chatAbuseReport.selectedMessages.addAll(this.selectedMessages);
        chatAbuseReport.opinionComments = this.opinionComments;
        chatAbuseReport.reason = this.reason;
        chatAbuseReport.attested = this.attested;
        return chatAbuseReport;
    }

    @Override
    public Screen createReportScreen(Screen parent, AbuseReportContext context) {
        return new ChatReportScreen(parent, context, this);
    }

    @Override
    public /* synthetic */ AbuseReport copy() {
        return this.copy();
    }

    @Environment(value=EnvType.CLIENT)
    public static class Builder
    extends AbuseReport.Builder<ChatAbuseReport> {
        public Builder(ChatAbuseReport report, AbuseReportLimits limits) {
            super(report, limits);
        }

        public Builder(UUID reportedPlayerUuid, AbuseReportLimits limits) {
            super(new ChatAbuseReport(UUID.randomUUID(), Instant.now(), reportedPlayerUuid), limits);
        }

        public IntSet getSelectedMessages() {
            return ((ChatAbuseReport)this.report).selectedMessages;
        }

        public void toggleMessageSelection(int index) {
            ((ChatAbuseReport)this.report).toggleMessageSelection(index, this.limits);
        }

        public boolean isMessageSelected(int index) {
            return ((ChatAbuseReport)this.report).selectedMessages.contains(index);
        }

        @Override
        public boolean hasEnoughInfo() {
            return StringUtils.isNotEmpty(this.getOpinionComments()) || !this.getSelectedMessages().isEmpty() || this.getReason() != null;
        }

        @Override
        @Nullable
        public AbuseReport.ValidationError validate() {
            if (((ChatAbuseReport)this.report).selectedMessages.isEmpty()) {
                return AbuseReport.ValidationError.NO_REPORTED_MESSAGES;
            }
            if (((ChatAbuseReport)this.report).selectedMessages.size() > this.limits.maxReportedMessageCount()) {
                return AbuseReport.ValidationError.TOO_MANY_MESSAGES;
            }
            if (((ChatAbuseReport)this.report).reason == null) {
                return AbuseReport.ValidationError.NO_REASON;
            }
            if (((ChatAbuseReport)this.report).opinionComments.length() > this.limits.maxOpinionCommentsLength()) {
                return AbuseReport.ValidationError.COMMENTS_TOO_LONG;
            }
            return super.validate();
        }

        @Override
        public Either<AbuseReport.ReportWithId, AbuseReport.ValidationError> build(AbuseReportContext context) {
            AbuseReport.ValidationError validationError = this.validate();
            if (validationError != null) {
                return Either.right(validationError);
            }
            String string = Objects.requireNonNull(((ChatAbuseReport)this.report).reason).getId();
            ReportEvidence reportEvidence = this.collectEvidences(context);
            ReportedEntity reportedEntity = new ReportedEntity(((ChatAbuseReport)this.report).reportedPlayerUuid);
            com.mojang.authlib.minecraft.report.AbuseReport abuseReport = com.mojang.authlib.minecraft.report.AbuseReport.chat(((ChatAbuseReport)this.report).opinionComments, string, reportEvidence, reportedEntity, ((ChatAbuseReport)this.report).currentTime);
            return Either.left(new AbuseReport.ReportWithId(((ChatAbuseReport)this.report).reportId, AbuseReportType.CHAT, abuseReport));
        }

        private ReportEvidence collectEvidences(AbuseReportContext context) {
            ArrayList list = new ArrayList();
            ContextMessageCollector contextMessageCollector = new ContextMessageCollector(this.limits.leadingContextMessageCount());
            contextMessageCollector.add(context.getChatLog(), ((ChatAbuseReport)this.report).selectedMessages, (index, message) -> list.add(this.toReportChatMessage(message, this.isMessageSelected(index))));
            return new ReportEvidence(Lists.reverse(list));
        }

        private ReportChatMessage toReportChatMessage(ReceivedMessage.ChatMessage message, boolean selected) {
            MessageLink messageLink = message.message().link();
            MessageBody messageBody = message.message().signedBody();
            List<ByteBuffer> list = messageBody.lastSeenMessages().entries().stream().map(MessageSignatureData::toByteBuffer).toList();
            ByteBuffer byteBuffer = Nullables.map(message.message().signature(), MessageSignatureData::toByteBuffer);
            return new ReportChatMessage(messageLink.index(), messageLink.sender(), messageLink.sessionId(), messageBody.timestamp(), messageBody.salt(), list, messageBody.content(), byteBuffer, selected);
        }

        public Builder copy() {
            return new Builder(((ChatAbuseReport)this.report).copy(), this.limits);
        }
    }
}

