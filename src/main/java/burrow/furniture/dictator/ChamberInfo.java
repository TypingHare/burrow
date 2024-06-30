package burrow.furniture.dictator;

import java.beans.JavaBean;

@JavaBean
public final class ChamberInfo {
    private Long initiateTimestampMs;
    private Long lastRequestTimestampMs;

    public Long getInitiateTimestampMs() {
        return initiateTimestampMs;
    }

    public void setInitiateTimestampMs(final Long initiateTimestampMs) {
        this.initiateTimestampMs = initiateTimestampMs;
    }

    public Long getLastRequestTimestampMs() {
        return lastRequestTimestampMs;
    }

    public void setLastRequestTimestampMs(final Long lastRequestTimestampMs) {
        this.lastRequestTimestampMs = lastRequestTimestampMs;
    }
}
