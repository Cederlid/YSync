package dev.ytterate.ysync;

import java.io.File;

public record DeleteFileAction(String file) implements SyncAction {
    @Override
    public void run() {
        deleteFileInSource(file);
    }
    private void deleteFileInSource(String file) {
        File fileToDelete = new File(file);
        if (fileToDelete.exists()){
            fileToDelete.delete();
        }
    }
    @Override
    public String render() {
        return "Delete file: " + file;
    }
}
