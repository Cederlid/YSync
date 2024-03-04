package dev.ytterate.ysync;

import java.io.IOException;
import java.util.List;

public interface ContinueCallback {

    void onGotMisMatches(List<SyncAction> syncActions) throws IOException;

    void copyComplete() throws IOException;

}
