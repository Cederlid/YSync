package dev.ytterate.ysync;

import java.io.IOException;

public interface Resolved {
    void onResolvedMisMatches() throws IOException;
}
