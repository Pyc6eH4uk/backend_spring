package com.section.demo.job;

import org.springframework.scheduling.annotation.AsyncResult;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

public class Export {
    public static final String FAILURE = "ERROR";
    public static final String PROCESSING = "PROCESSING";
    public static final String DONE = "DONE";

    public static AtomicInteger id = new AtomicInteger(1);
    private String status = Export.PROCESSING;
    private int taskId;
    private Future<byte[]> file;

    public Export() {
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

    public void setFile(Future<byte[]> file) {
        this.file = file;
    }

    public byte[] getResultOfTask() throws ExecutionException, InterruptedException {
        return this.file.get();
    }

}
