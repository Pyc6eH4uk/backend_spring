package com.section.demo.job;

import java.util.concurrent.atomic.AtomicInteger;


public class Import {
    public static final String FAILURE = "ERROR";
    public static final String PROCESSING = "PROCESSING";
    public static final String DONE = "DONE";

    public static AtomicInteger id = new AtomicInteger(1);
    private int taskId;
    private String status = Import.PROCESSING;

    public Import() {
        this.taskId = id.getAndAdd(1);
    }

    public int getTaskId() {
        return taskId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
