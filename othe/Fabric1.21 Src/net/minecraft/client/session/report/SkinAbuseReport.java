/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.client.session.report;

import com.mojang.authlib.minecraft.report.AbuseReportLimits;
import com.mojang.authlib.minecraft.report.ReportedEntity;
import com.mojang.datafixers.util.Either;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Supplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.report.SkinReportScreen;
import net.minecraft.client.session.report.AbuseReport;
import net.minecraft.client.session.report.AbuseReportContext;
import net.minecraft.client.session.report.AbuseReportType;
import net.minecraft.client.util.SkinTextures;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class SkinAbuseReport
extends AbuseReport {
    final Supplier<SkinTextures> skinSupplier;

    SkinAbuseReport(UUID reportId, Instant currentTime, UUID reportedPlayerUuid, Supplier<SkinTextures> skinSupplier) {
        super(reportId, currentTime, reportedPlayerUuid);
        this.skinSupplier = skinSupplier;
    }

    public Supplier<SkinTextures> getSkinSupplier() {
        return this.skinSupplier;
    }

    @Override
    public SkinAbuseReport copy() {
        SkinAbuseReport skinAbuseReport = new SkinAbuseReport(this.reportId, this.currentTime, this.reportedPlayerUuid, this.skinSupplier);
        skinAbuseReport.opinionComments = this.opinionComments;
        skinAbuseReport.reason = this.reason;
        skinAbuseReport.attested = this.attested;
        return skinAbuseReport;
    }

    @Override
    public Screen createReportScreen(Screen parent, AbuseReportContext context) {
        return new SkinReportScreen(parent, context, this);
    }

    @Override
    public /* synthetic */ AbuseReport copy() {
        return this.copy();
    }

    @Environment(value=EnvType.CLIENT)
    public static class Builder
    extends AbuseReport.Builder<SkinAbuseReport> {
        public Builder(SkinAbuseReport report, AbuseReportLimits limits) {
            super(report, limits);
        }

        public Builder(UUID reportedPlayerUuid, Supplier<SkinTextures> skinSupplier, AbuseReportLimits limits) {
            super(new SkinAbuseReport(UUID.randomUUID(), Instant.now(), reportedPlayerUuid, skinSupplier), limits);
        }

        @Override
        public boolean hasEnoughInfo() {
            return StringUtils.isNotEmpty(this.getOpinionComments()) || this.getReason() != null;
        }

        @Override
        @Nullable
        public AbuseReport.ValidationError validate() {
            if (((SkinAbuseReport)this.report).reason == null) {
                return AbuseReport.ValidationError.NO_REASON;
            }
            if (((SkinAbuseReport)this.report).opinionComments.length() > this.limits.maxOpinionCommentsLength()) {
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
            String string = Objects.requireNonNull(((SkinAbuseReport)this.report).reason).getId();
            ReportedEntity reportedEntity = new ReportedEntity(((SkinAbuseReport)this.report).reportedPlayerUuid);
            SkinTextures skinTextures = ((SkinAbuseReport)this.report).skinSupplier.get();
            String string2 = skinTextures.textureUrl();
            com.mojang.authlib.minecraft.report.AbuseReport abuseReport = com.mojang.authlib.minecraft.report.AbuseReport.skin(((SkinAbuseReport)this.report).opinionComments, string, string2, reportedEntity, ((SkinAbuseReport)this.report).currentTime);
            return Either.left(new AbuseReport.ReportWithId(((SkinAbuseReport)this.report).reportId, AbuseReportType.SKIN, abuseReport));
        }
    }
}

