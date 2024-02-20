package dev.ytterate.ysync;

import java.io.File;
import java.nio.file.Files;

public record DeleteDirectoryAction(String directory) implements SyncAction {
    @Override
    public void run() {
        File directoryFile = new File(directory);
        deleteDirectory(directoryFile);
    }

    private void deleteDirectory(File directory) {
        File[] contents = directory.listFiles();
        if (contents != null) {
            for (File f : contents) {
                if (!Files.isSymbolicLink(f.toPath())) {
                    if (f.isDirectory()) {
                        deleteDirectory(f);
                    }
                }
            }
        }
        directory.delete();
    }

    @Override
    public String render() {
        return "Delete directory: " + directory;
    }
}
