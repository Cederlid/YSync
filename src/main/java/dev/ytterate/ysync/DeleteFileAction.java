package dev.ytterate.ysync;

public record DeleteFileAction() implements SyncAction {
    @Override
    public void run() {

    }

    @Override
    public String render() {
        return null;
    }
}
