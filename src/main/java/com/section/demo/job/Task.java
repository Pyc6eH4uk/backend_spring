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
    public static final int FAILURE = 0;
    public static final int PROCESSING = 1;
    public static final int DONE = 2;

    public static AtomicInteger id = new AtomicInteger(0);
    private int taskId;
    private AtomicInteger status = new AtomicInteger(Task.PROCESSING);

    public Task() {
        this.taskId = id.getAndAdd(1);
    }

    public int getTaskId() {
        return taskId;
    }

    public int getStatus() {
        return status.get();
    }

    public void setStatus(int status) {
        this.status.set(status);
    }
}
