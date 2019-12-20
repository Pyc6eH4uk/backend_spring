package com.section.demo.job;

import com.section.demo.entity.Section;
import com.section.demo.service.XlsxFileUpload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;


public class Task {
    public static final String FAILURE = "ERROR";
    public static final String PROCESSING = "PROCESSING";
    public static final String DONE = "DONE";

    public static AtomicInteger id = new AtomicInteger(1);
    private int taskId;
    private String status = Task.PROCESSING;

    public Task() {
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
