package dev.ytterate.ysync;

import java.io.File;
import java.io.IOException;

import static org.apache.commons.io.FileUtils.copyFile;

public record CopyDirectoryAction(String from, String to) implements SyncAction {
    @Override
    public void run() throws IOException {
        copyDirectory(new File(from), new File(to, new File(from).getName()));
    }

    private void copyDirectory(File sourceDir, File destDir) throws IOException {

        if (!destDir.exists()) {
            destDir.mkdir();
        }
        for (File f : sourceDir.listFiles()) {
            if (f.isDirectory()) {
                copyDirectory(f, new File(destDir, f.getName()));
            } else {
                copyFile(f, new File(destDir, f.getName()));
            }
        }
    }

    @Override
    public String render() {
        return "Copy dest " + from + " destDir " + to;
    }
}
