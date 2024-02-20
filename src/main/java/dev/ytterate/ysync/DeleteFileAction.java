package dev.ytterate.ysync;

import java.io.File;
import java.nio.file.Files;

public record DeleteFileAction(String file) implements SyncAction {
    @Override
    public void run() {
        deleteFileInSource(new File(file));
    }
    private void deleteFileInSource(File sourceFile) {
        if (sourceFile.isDirectory()) {
            File [] contents = sourceFile.listFiles();
            if (contents != null){
                for (File f : contents){
                    if (! Files.isSymbolicLink(f.toPath())){
                        deleteFileInSource(f);
                    }
                }
            }
            sourceFile.delete();
        }
    }
    @Override
    public String render() {
        return "Delete file: " + file;
    }
}
