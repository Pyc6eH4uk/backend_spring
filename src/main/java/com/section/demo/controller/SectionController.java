package com.section.demo.controller;

import com.section.demo.entity.GeoClass;
import com.section.demo.entity.Section;
import com.section.demo.job.Export;
import com.section.demo.job.Import;
import com.section.demo.repository.GeoClassRepository;
import com.section.demo.repository.SectionRepository;
import com.section.demo.service.XlsFileExport;
import com.section.demo.service.XlsFileUpload;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Sort;
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

    @Autowired
    private SectionRepository sectionRepository;

    @Autowired
    private GeoClassRepository geoClassRepository;

    @Autowired
    private XlsFileUpload xlsFileUpload;

    @Autowired
    private XlsFileExport xlsFileExport;

    private Map<Integer, Import> importTasks;
    private Map<Integer, Future<byte[]>> exportTasks;


    public SectionController() {
        this.importTasks = new HashMap<>();
        this.exportTasks = new HashMap<>();
    }

    @GetMapping("/sections/")
    public List<Section> getAllSections() {
        return sectionRepository.findAll(Sort.by(Sort.Direction.ASC, "name"));
    }

    @PostMapping("/sections/")
    public ResponseEntity<Map<String, String>> createSection(@Valid @RequestBody Section section) {
        Map<String, String> response = new HashMap<>();
        try {
            sectionRepository.save(section);
        } catch (DataIntegrityViolationException exception) {
            response.put("message", "section with such name already exists or values for geologicalClasses are empty");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
        response.put("message", "Successfully created section.");
        return ResponseEntity.ok(response);
    }

    @PutMapping("/sections/{sectionId}")
    public ResponseEntity<Map<String, String>> updateSection(@Valid @RequestBody Section section, @PathVariable(value = "sectionId") Long sectionId) {
        Optional<Section> sectionOptional = sectionRepository.findById(sectionId);
        Map<String, String> response = new HashMap<>();
        if (!sectionOptional.isPresent()) {
            response.put("message", "not existing section");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        section.setId(sectionId);
        List<GeoClass> geologicalClassesOptional = sectionOptional.get().getgeologicalClasses();
        List<GeoClass> geologicalClasses = section.getgeologicalClasses();

        if (geologicalClasses != null) {
            geologicalClassesOptional.addAll(geologicalClasses);
        }
        section.setgeologicalClasses(geologicalClassesOptional);
        try {
            sectionRepository.save(section);
        } catch (DataIntegrityViolationException exception) {
            response.put("message", "provided code already exists for this section or values for geologicalClasses are empty");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
        response.put("message", "successfully update");
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/sections/{sectionId}")
    public ResponseEntity deleteSection(@PathVariable(value = "sectionId") long sectionId) {
        Map<String, String> response = new HashMap<>();
        if (sectionRepository.existsById(sectionId)) {
            Section section = sectionRepository.getOne(sectionId);
            sectionRepository.delete(section);
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body(null);
        }
        response.put("message", "not found section");
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @GetMapping("/sections/by-code")
    public List<Map<String, Object>> getAllListsByGeoClassCode(@RequestParam String code) {
        List<GeoClass> geologicalClasses = geoClassRepository.findAllByCode(code);

        List<Map<String, Object>> sections = new ArrayList<>();
        for (GeoClass geoClass : geologicalClasses) {
            Map<String, Object> response = new HashMap<>();
            Section section = sectionRepository.findByGeologicalClasses(geoClass);
            response.put("id", section.getId());
            response.put("name", section.getName());
            sections.add(response);
        }
        return sections;
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
