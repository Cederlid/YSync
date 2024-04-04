package dev.ytterate.ysync;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import static org.apache.commons.io.FileUtils.copyFile;


/**
 * Copies the complete from directory into the to directory recursively over subdirectories.
 *
 * @param source Path to existing directory
 * @param destination Path to existing parent
 */
public record CopyAction(String source, String destination, boolean override) implements SyncAction {
    @Override
    public void run() {
        File sourceFile = new File(source);
        File destFile = new File(destination, sourceFile.getName());

        try {
            if (sourceFile.isDirectory()) {
                copyDirectory(sourceFile, destFile);
            } else {
                if (destFile.exists() && !override) {
                    return;
                }
                Files.copy(sourceFile.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to copy " + sourceFile + " to " + destFile, e);
        }
    }

    private void copyDirectory(File sourceDir, File destDir) throws IOException {
        if (destDir.exists() && override) {
            if (destDir.isDirectory()) {
                DeleteDirectoryAction deleteDirectoryAction = new DeleteDirectoryAction(destination);
                deleteDirectoryAction.run();
            } else {
                DeleteFileAction deleteFileAction = new DeleteFileAction(destination);
                deleteFileAction.run();
            }

        }
        if (!destDir.exists()) {
            destDir.mkdir();
        }
        for (File f : sourceDir.listFiles()) {
            File destFile = new File(destDir, f.getName());
            if (f.isDirectory()) {
                copyDirectory(f, destFile);
            } else {
                if (destFile.exists() && !override) {
                    continue;
                }
                Files.copy(f.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
        }
    }

    @Override
    public String render() {
        return "Copy source " + source + " destination " + destination + "override: " + override;
    }
}






















