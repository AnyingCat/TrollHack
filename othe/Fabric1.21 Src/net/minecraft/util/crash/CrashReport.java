/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.util.crash;

import com.google.common.collect.Lists;
import com.mojang.logging.LogUtils;
import java.io.BufferedWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletionException;
import net.minecraft.util.PathUtil;
import net.minecraft.util.SystemDetails;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashMemoryReserve;
import net.minecraft.util.crash.CrashReportSection;
import net.minecraft.util.crash.ReportType;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class CrashReport {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss", Locale.ROOT);
    private final String message;
    private final Throwable cause;
    private final List<CrashReportSection> otherSections = Lists.newArrayList();
    @Nullable
    private Path file;
    private boolean hasStackTrace = true;
    private StackTraceElement[] stackTrace = new StackTraceElement[0];
    private final SystemDetails systemDetailsSection = new SystemDetails();

    public CrashReport(String message, Throwable cause) {
        this.message = message;
        this.cause = cause;
    }

    public String getMessage() {
        return this.message;
    }

    public Throwable getCause() {
        return this.cause;
    }

    public String getStackTrace() {
        StringBuilder stringBuilder = new StringBuilder();
        this.addDetails(stringBuilder);
        return stringBuilder.toString();
    }

    public void addDetails(StringBuilder crashReportBuilder) {
        if (!(this.stackTrace != null && this.stackTrace.length > 0 || this.otherSections.isEmpty())) {
            this.stackTrace = ArrayUtils.subarray(this.otherSections.get(0).getStackTrace(), 0, 1);
        }
        if (this.stackTrace != null && this.stackTrace.length > 0) {
            crashReportBuilder.append("-- Head --\n");
            crashReportBuilder.append("Thread: ").append(Thread.currentThread().getName()).append("\n");
            crashReportBuilder.append("Stacktrace:\n");
            for (StackTraceElement stackTraceElement : this.stackTrace) {
                crashReportBuilder.append("\t").append("at ").append(stackTraceElement);
                crashReportBuilder.append("\n");
            }
            crashReportBuilder.append("\n");
        }
        for (CrashReportSection crashReportSection : this.otherSections) {
            crashReportSection.addStackTrace(crashReportBuilder);
            crashReportBuilder.append("\n\n");
        }
        this.systemDetailsSection.writeTo(crashReportBuilder);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public String getCauseAsString() {
        String string;
        StringWriter stringWriter = null;
        PrintWriter printWriter = null;
        Throwable throwable = this.cause;
        if (throwable.getMessage() == null) {
            if (throwable instanceof NullPointerException) {
                throwable = new NullPointerException(this.message);
            } else if (throwable instanceof StackOverflowError) {
                throwable = new StackOverflowError(this.message);
            } else if (throwable instanceof OutOfMemoryError) {
                throwable = new OutOfMemoryError(this.message);
            }
            throwable.setStackTrace(this.cause.getStackTrace());
        }
        try {
            stringWriter = new StringWriter();
            printWriter = new PrintWriter(stringWriter);
            throwable.printStackTrace(printWriter);
            string = stringWriter.toString();
        } catch (Throwable throwable2) {
            IOUtils.closeQuietly(stringWriter);
            IOUtils.closeQuietly(printWriter);
            throw throwable2;
        }
        IOUtils.closeQuietly(stringWriter);
        IOUtils.closeQuietly(printWriter);
        return string;
    }

    public String asString(ReportType type, List<String> extraInfo) {
        StringBuilder stringBuilder = new StringBuilder();
        type.addHeaderAndNugget(stringBuilder, extraInfo);
        stringBuilder.append("Time: ");
        stringBuilder.append(DATE_TIME_FORMATTER.format(ZonedDateTime.now()));
        stringBuilder.append("\n");
        stringBuilder.append("Description: ");
        stringBuilder.append(this.message);
        stringBuilder.append("\n\n");
        stringBuilder.append(this.getCauseAsString());
        stringBuilder.append("\n\nA detailed walkthrough of the error, its code path and all known details is as follows:\n");
        for (int i = 0; i < 87; ++i) {
            stringBuilder.append("-");
        }
        stringBuilder.append("\n\n");
        this.addDetails(stringBuilder);
        return stringBuilder.toString();
    }

    public String asString(ReportType type) {
        return this.asString(type, List.of());
    }

    @Nullable
    public Path getFile() {
        return this.file;
    }

    public boolean writeToFile(Path path, ReportType type, List<String> extraInfo) {
        if (this.file != null) {
            return false;
        }
        try {
            if (path.getParent() != null) {
                PathUtil.createDirectories(path.getParent());
            }
            try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8, new OpenOption[0]);){
                writer.write(this.asString(type, extraInfo));
            }
            this.file = path;
            return true;
        } catch (Throwable throwable) {
            LOGGER.error("Could not save crash report to {}", (Object)path, (Object)throwable);
            return false;
        }
    }

    public boolean writeToFile(Path path, ReportType type) {
        return this.writeToFile(path, type, List.of());
    }

    public SystemDetails getSystemDetailsSection() {
        return this.systemDetailsSection;
    }

    public CrashReportSection addElement(String name) {
        return this.addElement(name, 1);
    }

    public CrashReportSection addElement(String name, int ignoredStackTraceCallCount) {
        CrashReportSection crashReportSection = new CrashReportSection(name);
        if (this.hasStackTrace) {
            int i = crashReportSection.initStackTrace(ignoredStackTraceCallCount);
            StackTraceElement[] stackTraceElements = this.cause.getStackTrace();
            StackTraceElement stackTraceElement = null;
            StackTraceElement stackTraceElement2 = null;
            int j = stackTraceElements.length - i;
            if (j < 0) {
                LOGGER.error("Negative index in crash report handler ({}/{})", (Object)stackTraceElements.length, (Object)i);
            }
            if (stackTraceElements != null && 0 <= j && j < stackTraceElements.length) {
                stackTraceElement = stackTraceElements[j];
                if (stackTraceElements.length + 1 - i < stackTraceElements.length) {
                    stackTraceElement2 = stackTraceElements[stackTraceElements.length + 1 - i];
                }
            }
            this.hasStackTrace = crashReportSection.shouldGenerateStackTrace(stackTraceElement, stackTraceElement2);
            if (stackTraceElements != null && stackTraceElements.length >= i && 0 <= j && j < stackTraceElements.length) {
                this.stackTrace = new StackTraceElement[j];
                System.arraycopy(stackTraceElements, 0, this.stackTrace, 0, this.stackTrace.length);
            } else {
                this.hasStackTrace = false;
            }
        }
        this.otherSections.add(crashReportSection);
        return crashReportSection;
    }

    public static CrashReport create(Throwable cause, String title) {
        CrashReport crashReport;
        while (cause instanceof CompletionException && cause.getCause() != null) {
            cause = cause.getCause();
        }
        if (cause instanceof CrashException) {
            CrashException crashException = (CrashException)cause;
            crashReport = crashException.getReport();
        } else {
            crashReport = new CrashReport(title, cause);
        }
        return crashReport;
    }

    public static void initCrashReport() {
        CrashMemoryReserve.reserveMemory();
        new CrashReport("Don't panic!", new Throwable()).asString(ReportType.MINECRAFT_CRASH_REPORT);
    }
}

