/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.client.session.report;

import com.mojang.authlib.minecraft.report.AbuseReportLimits;
import com.mojang.datafixers.util.Either;
import java.time.Instant;
import java.util.UUID;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.session.report.AbuseReportContext;
import net.minecraft.client.session.report.AbuseReportReason;
import net.minecraft.client.session.report.AbuseReportType;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public abstract class AbuseReport {
    protected final UUID reportId;
    protected final Instant currentTime;
    protected final UUID reportedPlayerUuid;
    protected String opinionComments = "";
    @Nullable
    protected AbuseReportReason reason;
    protected boolean attested;

    public AbuseReport(UUID reportId, Instant currentTime, UUID reportedPlayerUuid) {
        this.reportId = reportId;
        this.currentTime = currentTime;
        this.reportedPlayerUuid = reportedPlayerUuid;
    }

    public boolean playerUuidEquals(UUID uuid) {
        return uuid.equals(this.reportedPlayerUuid);
    }

    public abstract AbuseReport copy();

    public abstract Screen createReportScreen(Screen var1, AbuseReportContext var2);

    @Environment(value=EnvType.CLIENT)
    public record ValidationError(Text message) {
        public static final ValidationError NO_REASON = new ValidationError(Text.translatable("gui.abuseReport.send.no_reason"));
        public static final ValidationError NO_REPORTED_MESSAGES = new ValidationError(Text.translatable("gui.chatReport.send.no_reported_messages"));
        public static final ValidationError TOO_MANY_MESSAGES = new ValidationError(Text.translatable("gui.chatReport.send.too_many_messages"));
        public static final ValidationError COMMENTS_TOO_LONG = new ValidationError(Text.translatable("gui.abuseReport.send.comment_too_long"));
        public static final ValidationError NOT_ATTESTED = new ValidationError(Text.translatable("gui.abuseReport.send.not_attested"));

        public Tooltip createTooltip() {
            return Tooltip.of(this.message);
        }
    }

    @Environment(value=EnvType.CLIENT)
    public record ReportWithId(UUID id, AbuseReportType reportType, com.mojang.authlib.minecraft.report.AbuseReport report) {
    }

    @Environment(value=EnvType.CLIENT)
    public static abstract class Builder<R extends AbuseReport> {
        protected final R report;
        protected final AbuseReportLimits limits;

        protected Builder(R report, AbuseReportLimits limits) {
            this.report = report;
            this.limits = limits;
        }

        public R getReport() {
            return this.report;
        }

        public UUID getReportedPlayerUuid() {
            return ((AbuseReport)this.report).reportedPlayerUuid;
        }

        public String getOpinionComments() {
            return ((AbuseReport)this.report).opinionComments;
        }

        public boolean isAttested() {
            return ((AbuseReport)this.getReport()).attested;
        }

        public void setOpinionComments(String opinionComments) {
            ((AbuseReport)this.report).opinionComments = opinionComments;
        }

        @Nullable
        public AbuseReportReason getReason() {
            return ((AbuseReport)this.report).reason;
        }

        public void setReason(AbuseReportReason reason) {
            ((AbuseReport)this.report).reason = reason;
        }

        public void setAttested(boolean attested) {
            ((AbuseReport)this.report).attested = attested;
        }

        public abstract boolean hasEnoughInfo();

        @Nullable
        public ValidationError validate() {
            if (!((AbuseReport)this.getReport()).attested) {
                return ValidationError.NOT_ATTESTED;
            }
            return null;
        }

        public abstract Either<ReportWithId, ValidationError> build(AbuseReportContext var1);
    }
}

