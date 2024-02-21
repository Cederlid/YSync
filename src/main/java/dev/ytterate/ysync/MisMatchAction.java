package dev.ytterate.ysync;

import java.io.File;

public record MisMatchAction(File file, File directory) implements SyncAction {
    @Override
    public void run() {
        System.out.printf("%s is a file in %s and a directory in %s!\n", file.getPath(), directory.getPath());
    }


    @Override
    public String render() {
        return null;
    }
}
