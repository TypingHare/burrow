package burrow.furniture.dictator;

import java.beans.JavaBean;

@JavaBean
public final class ChamberInfo {
    private Long startTimestampMs;
    private Long lastRequestTimestampMs;

    public Long getStartTimestampMs() {
        return startTimestampMs;
    }

    public void setStartTimestampMs(final Long startTimestampMs) {
        this.startTimestampMs = startTimestampMs;
    }

    public Long getLastRequestTimestampMs() {
        return lastRequestTimestampMs;
    }

    public void setLastRequestTimestampMs(final Long lastRequestTimestampMs) {
        this.lastRequestTimestampMs = lastRequestTimestampMs;
    }
}
