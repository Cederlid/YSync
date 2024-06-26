package dev.ytterate.ysync;

import java.io.File;

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
            File fileToDelete = new File(destTarget.getParent(), new File(source).getName());
            DeleteAction deleteAction = new DeleteAction(fileToDelete.getPath(), true);
            deleteAction.run();
            CopyAction copyFileAction = new CopyAction(sourceTarget.getPath(), destTarget.getParent(), false);
            copyFileAction.run();
        }
    }

    public void confirm() {
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
    public String toString() {
        return "Source: " + source + " dest: " + dest;
    }
}
