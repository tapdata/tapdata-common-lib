package io.tapdata.pdk.apis.spec;
/**
 * @author <a href="2749984520@qq.com">Gavin'Xiao</a>
 * @author <a href="https://github.com/11000100111010101100111">Gavin'Xiao</a>
 * @version v1.0 2025/11/18 09:50 Create
 * @description
 */
public class AutoAccumulateBatchInfo {

    private Info batchRead;

    private Info increaseRead;
    

    public static class Info {
        boolean open = false;
        int maxDelayMs = 1000;

        public boolean isOpen() {
            return open;
        }

        public void setOpen(boolean open) {
            this.open = open;
        }

        public int getMaxDelayMs() {
            return maxDelayMs;
        }

        public void setMaxDelayMs(int maxDelayMs) {
            this.maxDelayMs = maxDelayMs;
        }
    }

    public Info getIncreaseRead() {
        return increaseRead;
    }

    public void setIncreaseRead(Info increaseRead) {
        this.increaseRead = increaseRead;
    }
}