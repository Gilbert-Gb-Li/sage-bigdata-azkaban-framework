package com.haima.sage.bigdata.azkaban.constants;

/**
 * @author liuyang
 */
public enum JobStatus {

    RUNNING(0), FINISH(1), FAILED(2);

    private int index;

    JobStatus(int index) {
        this.index = index;
    }

    public int getIndex() {
        return index;
    }
}
