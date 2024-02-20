package dev.ytterate.ysync;

import java.io.IOException;

public interface SyncAction {
    void run() throws IOException;

    String render();
}
