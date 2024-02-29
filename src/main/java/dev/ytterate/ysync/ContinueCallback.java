package dev.ytterate.ysync;

import java.io.File;
import java.io.IOException;
import java.util.List;

public interface ContinueCallback {

    void onContinueClicked(List<SyncAction> selectedActions, int copyDirectionCount, File file1, File file2) throws IOException;
    void onContinueCompleted();
}
