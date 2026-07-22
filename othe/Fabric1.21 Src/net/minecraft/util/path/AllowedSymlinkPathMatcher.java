/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.util.path;

import com.mojang.logging.LogUtils;
import java.io.BufferedReader;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;

public class AllowedSymlinkPathMatcher
implements PathMatcher {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String COMMENT_LINE_PREFIX = "#";
    private final List<Entry> allowedEntries;
    private final Map<String, PathMatcher> matcherCache = new ConcurrentHashMap<String, PathMatcher>();

    public AllowedSymlinkPathMatcher(List<Entry> allowedEntries) {
        this.allowedEntries = allowedEntries;
    }

    public PathMatcher get(FileSystem fileSystem) {
        return this.matcherCache.computeIfAbsent(fileSystem.provider().getScheme(), scheme -> {
            List<PathMatcher> list;
            try {
                list = this.allowedEntries.stream().map(entry -> entry.compile(fileSystem)).toList();
            } catch (Exception exception) {
                LOGGER.error("Failed to compile file pattern list", exception);
                return path -> false;
            }
            return switch (list.size()) {
                case 0 -> path -> false;
                case 1 -> list.get(0);
                default -> path -> {
                    for (PathMatcher pathMatcher : list) {
                        if (!pathMatcher.matches(path)) continue;
                        return true;
                    }
                    return false;
                };
            };
        });
    }

    @Override
    public boolean matches(Path path) {
        return this.get(path.getFileSystem()).matches(path);
    }

    public static AllowedSymlinkPathMatcher fromReader(BufferedReader reader) {
        return new AllowedSymlinkPathMatcher(reader.lines().flatMap(line -> Entry.readLine(line).stream()).toList());
    }

    public record Entry(EntryType type, String pattern) {
        public PathMatcher compile(FileSystem fileSystem) {
            return this.type().compile(fileSystem, this.pattern);
        }

        static Optional<Entry> readLine(String line) {
            if (line.isBlank() || line.startsWith(AllowedSymlinkPathMatcher.COMMENT_LINE_PREFIX)) {
                return Optional.empty();
            }
            if (!line.startsWith("[")) {
                return Optional.of(new Entry(EntryType.PREFIX, line));
            }
            int i = line.indexOf(93, 1);
            if (i == -1) {
                throw new IllegalArgumentException("Unterminated type in line '" + line + "'");
            }
            String string = line.substring(1, i);
            String string2 = line.substring(i + 1);
            return switch (string) {
                case "glob", "regex" -> Optional.of(new Entry(EntryType.DEFAULT, string + ":" + string2));
                case "prefix" -> Optional.of(new Entry(EntryType.PREFIX, string2));
                default -> throw new IllegalArgumentException("Unsupported definition type in line '" + line + "'");
            };
        }

        static Entry glob(String pattern) {
            return new Entry(EntryType.DEFAULT, "glob:" + pattern);
        }

        static Entry regex(String pattern) {
            return new Entry(EntryType.DEFAULT, "regex:" + pattern);
        }

        static Entry prefix(String prefix) {
            return new Entry(EntryType.PREFIX, prefix);
        }
    }

    @FunctionalInterface
    public static interface EntryType {
        public static final EntryType DEFAULT = FileSystem::getPathMatcher;
        public static final EntryType PREFIX = (fileSystem, prefix) -> path -> path.toString().startsWith(prefix);

        public PathMatcher compile(FileSystem var1, String var2);
    }
}

