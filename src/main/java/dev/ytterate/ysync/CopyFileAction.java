package dev.ytterate.ysync;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public record CopyFileAction(String sourceFile, String destDir) implements SyncAction {
    @Override
    public void run() {
        try {
            Files.copy(new File(sourceFile).toPath(),  new File(destDir, new File(sourceFile).getName()).toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String render() {
        return "Copy sourceFile " + sourceFile + " destDir " + destDir;
    }

}
