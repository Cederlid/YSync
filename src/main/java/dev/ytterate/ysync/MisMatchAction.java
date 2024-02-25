package dev.ytterate.ysync;

import java.io.File;
import java.io.IOException;

public record MisMatchAction(File file, File directory) implements SyncAction {
    @Override
    public void run() throws IOException {
        if (file.isFile()){
            CopyFileAction copyFileAction = new CopyFileAction(file.getPath(), directory.getPath());
            copyFileAction.run();
        } else {
            CopyDirectoryAction copyDirectoryAction = new CopyDirectoryAction(file.getPath(), directory.getPath());
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
