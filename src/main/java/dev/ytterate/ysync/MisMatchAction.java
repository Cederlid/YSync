package dev.ytterate.ysync;

import java.io.File;
import java.io.IOException;

public record MisMatchAction(String source, String dest) implements SyncAction {
    @Override
    public void run() throws IOException {
        File sourceTarget = new File(source);
        File destTarget = new File(dest);

        if (sourceTarget.isFile()){
            File directoryToDelete = new File(destTarget.getParent(), sourceTarget.getName());
            if (directoryToDelete.exists() && directoryToDelete.isDirectory()){
                DeleteDirectoryAction deleteDirectoryAction = new DeleteDirectoryAction(directoryToDelete.getPath());
                deleteDirectoryAction.run();
            }
            CopyFileAction copyFileAction = new CopyFileAction(sourceTarget.getPath(), destTarget.getParent());
            copyFileAction.run();
        } else {
            File fileToDelete = new File(destTarget.getParent(), new File(source).getName());
            if (fileToDelete.exists() && fileToDelete.isFile()){
                DeleteFileAction deleteFileAction = new DeleteFileAction(fileToDelete.getPath());
                deleteFileAction.run();
            }
            CopyDirectoryAction copyDirectoryAction = new CopyDirectoryAction(sourceTarget.getPath(), destTarget.getPath());
            copyDirectoryAction.run();
        }
    }

    @Override
    public boolean isMisMatch() {
        return true;
    }

    @Override
    public String render() {
        return null;
    }
}
