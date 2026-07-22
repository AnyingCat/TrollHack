/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.client.util;

import com.google.common.base.Charsets;
import com.mojang.logging.LogUtils;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.util.Collection;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.collection.ArrayListDeque;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public class CommandHistoryManager {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int MAX_SIZE = 50;
    private static final String FILENAME = "command_history.txt";
    private final Path path;
    private final ArrayListDeque<String> history = new ArrayListDeque(50);

    public CommandHistoryManager(Path directoryPath) {
        this.path = directoryPath.resolve(FILENAME);
        if (Files.exists(this.path, new LinkOption[0])) {
            try (BufferedReader bufferedReader = Files.newBufferedReader(this.path, Charsets.UTF_8);){
                this.history.addAll(bufferedReader.lines().toList());
            } catch (Exception exception) {
                LOGGER.error("Failed to read {}, command history will be missing", (Object)FILENAME, (Object)exception);
            }
        }
    }

    public void add(String command) {
        if (!command.equals(this.history.peekLast())) {
            if (this.history.size() >= 50) {
                this.history.removeFirst();
            }
            this.history.addLast(command);
            this.write();
        }
    }

    private void write() {
        try (BufferedWriter bufferedWriter = Files.newBufferedWriter(this.path, Charsets.UTF_8, new OpenOption[0]);){
            for (String string : this.history) {
                bufferedWriter.write(string);
                bufferedWriter.newLine();
            }
        } catch (IOException iOException) {
            LOGGER.error("Failed to write {}, command history will be missing", (Object)FILENAME, (Object)iOException);
        }
    }

    public Collection<String> getHistory() {
        return this.history;
    }
}

