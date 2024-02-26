package dev.ytterate.ysync;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public record CopyFileAction(String from, String to) implements SyncAction {
    @Override
    public void run() {
        try {
            Files.copy(new File(from).toPath(),  new File(to, new File(from).getName()).toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String render() {
        return "Copy source " + from + " to " + to;
    }

}
