package dev.ytterate.ysync;

import java.io.File;
import java.nio.file.Files;

public record DeleteAction(String target, boolean isDirectory, boolean override) implements SyncAction {
    @Override
    public void run() {
        File targetFile = new File(target);
        if (isDirectory){
            deleteDirectory(targetFile);
        } else {
            deleteFile(targetFile);
        }

    }

    private void deleteFile(File file) {
        if (file.exists()){
            file.delete();
        }
    }

    private void deleteDirectory(File directory) {
        File[] contents = directory.listFiles();
        if (contents != null) {
            for (File f : contents) {
                if (!Files.isSymbolicLink(f.toPath())) {
                    if (f.isDirectory()){
                        deleteDirectory(f);
                    } else {
                        deleteFile(f);
                    }

                }
            }
        }
        directory.delete();
    }

    @Override
    public String render() {
        return isDirectory ? "Delete directory: " + target : "Delete file: " + target;
    }
}
