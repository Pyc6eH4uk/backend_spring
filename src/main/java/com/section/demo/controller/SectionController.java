package com.section.demo.controller;

import com.section.demo.entity.Section;
import com.section.demo.job.Export;
import com.section.demo.job.Import;
import com.section.demo.service.SectionService;
import com.section.demo.service.XlsFileExport;
import com.section.demo.service.XlsFileUpload;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

@RestController
public class SectionController {

    public static final String FILENAME = "result";
    public static final String EXTENSION = ".xlsx";

    private final SectionService sectionService;
    private final XlsFileUpload xlsFileUpload;
    private final XlsFileExport xlsFileExport;


    private Map<Integer, Import> importTasks;
    private Map<Integer, Future<byte[]>> exportTasks;


    public SectionController(SectionService sectionService, XlsFileUpload xlsFileUpload, XlsFileExport xlsFileExport) {
        this.sectionService = sectionService;
        this.xlsFileUpload = xlsFileUpload;
        this.xlsFileExport = xlsFileExport;
        this.importTasks = new HashMap<>();
        this.exportTasks = new HashMap<>();
    }

    @GetMapping("/sections/")
    public List<Section> getAllSections() {
        return sectionService.findAll();
    }

    @PostMapping("/sections/")
    public Section createSection(@Valid @RequestBody final Section section) {
        return sectionService.create(section);
    }

    @PutMapping("/sections/{sectionId}")
    public Section updateSection(@Valid @RequestBody final Section section,
                                 @PathVariable(value = "sectionId") final Long sectionId) {
        section.setId(sectionId);
        return sectionService.update(section);
    }

    @DeleteMapping("/sections/{sectionId}")
    public void deleteSection(@PathVariable(value = "sectionId") final Long sectionId) {
        sectionService.delete(sectionId);
    }

    @GetMapping("/sections/by-code")
    public List<Section> findAllListsByGeoClassCode(@RequestParam String code) {
        return sectionService.findAllByGeoClassCode(code);
    }

    @PostMapping("/import/")
    public ResponseEntity<Map<String, String>> uploadXlsFile(@RequestParam("file") MultipartFile file) {
        Map<String, String> result = new HashMap<>();
        if (file == null) {
            result.put("message", "provide file");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
        }
        Import task = new Import();
        xlsFileUpload.uploadXlsFile(file, task);
        int taskId = task.getTaskId();
        importTasks.put(taskId, task);
        result.put("taskId", String.valueOf(taskId));
        return ResponseEntity.ok(result);
    }

    @GetMapping("/import/{taskId}")
    public ResponseEntity<Map<String, String>> statusOfImportFile(@PathVariable(name = "taskId") int taskId) {
        Map<String, String> result = new HashMap<>();
        Import task = importTasks.get(taskId);
        if (task == null) {
            result.put("status", "Not existing task");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(result);
        }
        result.put("status", String.valueOf(task.getStatus()));
        return ResponseEntity.ok(result);
    }

    @GetMapping("/export/")
    public ResponseEntity<Map<String, String>> exportFile() throws IOException, InterruptedException {
        Map<String, String> result = new HashMap<>();
        Export exportTask = new Export();
        Future<byte[]> future = xlsFileExport.exportDBDataToXlsxFile();
        int taskId = exportTask.getTaskId();
        exportTasks.put(taskId, future);
        result.put("taskId", String.valueOf(taskId));
        return ResponseEntity.ok(result);
    }

    @GetMapping("/export/{exportId}")
    public ResponseEntity<Map<String, String>> statusOfExportFile(@PathVariable(name = "exportId") int taskId) {
        Map<String, String> result = new HashMap<>();
        Future<byte[]> future = exportTasks.get(taskId);

        if (future == null) {
            result.put("status", "Not existing task");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(result);
        }

        if (future.isDone()) {
            try {
                future.get();
                result.put("status", Export.DONE);
            } catch (InterruptedException | ExecutionException exception) {
                result.put("status", Export.FAILURE);
            }
        } else {
            result.put("status", Export.PROCESSING);
        }
        return ResponseEntity.ok(result);
    }

    @GetMapping("/export/{exportId}/file")
    public ResponseEntity exportFile(@PathVariable(name = "exportId") int taskId) {
        Future<byte[]> future = exportTasks.get(taskId);
        Map<String, String> result = new HashMap<>();

        if (future == null) {
            result.put("status", "Not existing task");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(result);
        }

        if (!future.isDone()) {
            result.put("status", Export.PROCESSING);
            return ResponseEntity.ok(result);
        }

        byte[] out = new byte[0];

        try {
            out = future.get();
        } catch (InterruptedException | ExecutionException exception) {
            result.put("status", Export.FAILURE);
            return ResponseEntity.ok(result);
        }

        return ResponseEntity
                .ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + SectionController.FILENAME + taskId + SectionController.EXTENSION)
                .body(out);
    }

}
