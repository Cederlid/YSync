package dev.ytterate.ysync;

import java.io.File;

public record DeleteFileAction(String file) implements SyncAction {
    @Override
    public void run() {
        File fileToDelete = new File(file);
        deleteFileInSource(fileToDelete);
    }

    private void deleteFileInSource(File fileToDelete) {
        if (fileToDelete.exists()) {
            fileToDelete.delete();
        }
    }

    @Override
    public String render() {
        return "Delete source: " + file;
    }
}
