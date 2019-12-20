package com.section.demo.job;

import com.section.demo.entity.Section;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

public class Task {
    public static int taskId;
    private CompletableFuture<List<Section>> processingSections;

    public Task(CompletableFuture<List<Section>> processingSections) {
        this.processingSections = processingSections;
        ++taskId;
    }

    public Integer getTaskId() {
        return taskId;
    }

    public Map<String, String> statusOfProcessingSections() {
        Map<String, String> result = new HashMap<>();
        if (processingSections.isDone()) {
            result.put("status", "DONE");
        } else {
            result.put("status", "PROGRESS");
        }
        return result;
    }
}
