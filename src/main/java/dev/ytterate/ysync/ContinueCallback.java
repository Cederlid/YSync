package dev.ytterate.ysync;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface ContinueCallback {

    CompletableFuture<Boolean> onGotMisMatches(List<SyncAction> syncActions) throws IOException;

    void copyComplete() throws IOException;

}
