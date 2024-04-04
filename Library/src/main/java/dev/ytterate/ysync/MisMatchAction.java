package dev.ytterate.ysync;

import java.io.File;
import java.io.IOException;

public class MisMatchAction implements SyncAction {
    boolean copyConfirmed = false;
    String source;
    String dest;
    public MisMatchAction(String source, String dest) {
        this.source = source;
        this.dest = dest;
    }

    @Override
    public void run() {
        File sourceTarget = new File(source);
        File destTarget = new File(dest);

        if (copyConfirmed) {
            if (sourceTarget.isFile()){
                File directoryToDelete = new File(destTarget.getParent(), sourceTarget.getName());
                if (directoryToDelete.exists() && directoryToDelete.isDirectory()){
                    DeleteDirectoryAction deleteDirectoryAction = new DeleteDirectoryAction(directoryToDelete.getPath());
                    deleteDirectoryAction.run();
                }
                CopyAction copyFileAction = new CopyAction(sourceTarget.getPath(), destTarget.getParent(), false);
                copyFileAction.run();
            } else {
                File fileToDelete = new File(destTarget.getParent(), new File(source).getName());
                if (fileToDelete.exists() && fileToDelete.isFile()){
                    DeleteFileAction deleteFileAction = new DeleteFileAction(fileToDelete.getPath());
                    deleteFileAction.run();
                }
                CopyAction copyDirectoryAction = new CopyAction(sourceTarget.getPath(), destTarget.getParent(),false);
                copyDirectoryAction.run();
            }
        }
    }

    public void confirm(){
        copyConfirmed = true;
    }

    @Override
    public boolean isMisMatch() {
        return true;
    }

    @Override
    public String render() {
        return null;
    }

    @Override
    public String toString(){
        return "Source: " + source + " dest: " + dest;
    }
}
