package dev.ytterate.ysync;

import java.io.File;
import java.io.IOException;

public record MisMatchAction(String source, String dest) implements SyncAction {
    @Override
    public void run() throws IOException {
        File sourceTarget = new File(source);
        File destTarget = new File(dest);

        if (sourceTarget.isFile()){
            // TODO: The source is a file, so delete a directory in the target directory with the same name as the source file.
            //  You will need to create the name of the diretory to delete.
            File directoryToDelete = new File(destTarget.getParent(), sourceTarget.getName());
            if (directoryToDelete.exists() && directoryToDelete.isDirectory()){
                DeleteFileAction deleteFileAction = new DeleteFileAction(directoryToDelete.getPath());
                deleteFileAction.run();
            }
            CopyFileAction copyFileAction = new CopyFileAction(source, dest);
            copyFileAction.run();
        } else {
            // TODO: The source is a directory, so delete a file in the target directory with the same name as the source file.
            //  You will need to create the name of the file to delete.
            File fileToDelete = new File(destTarget.getParent(), new File(source).getName());
            if (fileToDelete.exists() && fileToDelete.isFile()){
                DeleteFileAction deleteFileAction = new DeleteFileAction(fileToDelete.getPath());
                deleteFileAction.run();
            }
            CopyDirectoryAction copyDirectoryAction = new CopyDirectoryAction(source, dest);
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
