package com.section.demo.controller;

import com.section.demo.entity.GeoClass;
import com.section.demo.entity.Section;
import com.section.demo.job.Export;
import com.section.demo.job.Import;
import com.section.demo.repository.GeoClassRepository;
import com.section.demo.repository.SectionRepository;
import com.section.demo.service.XlsxFileExport;
import com.section.demo.service.XlsxFileUpload;
import org.apache.poi.util.ArrayUtil;
import org.springframework.beans.factory.annotation.Autowired;
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
    private XlsxFileUpload xlsxFileUpload;

    @Autowired
    private XlsxFileExport xlsxFileExport;

    private Map<Integer, Import> importTasks;
    private Map<Integer, Future<byte[]>> exportTasks;


    public SectionController() {
        this.importTasks = new HashMap<>();
        this.exportTasks = new HashMap<>();
    }

    @GetMapping("/sections/")
    public List<Section> getAllSections() {
        return sectionRepository.findAll();
    }

    @PostMapping("/sections/")
    public ResponseEntity<Map<String, String>> createSection(@Valid @RequestBody Section section) {
        Map<String, String> response = new HashMap<>();
        try {
            section = sectionRepository.save(section);
        } catch (Exception exception) {
            response.put("message", "section with such name already exists.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
        @Valid Section finalSection = section;
        List<GeoClass> geoClasses = section.getGeoClasses();
        if (geoClasses != null) {
            geoClasses.forEach(geoClass -> geoClass.setSections(finalSection));
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
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        section.setId(sectionId);
//        List<GeoClass> geoClassesOptional = sectionOptional.get().getGeoClasses();
//        List<GeoClass> geoClasses = section.getGeoClasses();
//        if (geoClasses != null) {
//            for (GeoClass geoClass: geoClassesOptional) {
//                geoClass.setId();
//            }
//            geoClassesOptional.addAll(geoClasses);
//        }
//        section.setGeoClasses(geoClassesOptional);
        sectionRepository.save(section);
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
        List<GeoClass> geoClasses = geoClassRepository.findAllByCode(code);

        List<Map<String, Object>> sections = new ArrayList<>();
        for (GeoClass geoClass : geoClasses) {
            Map<String, Object> response = new HashMap<>();
            Section section = sectionRepository.findByGeoClasses(geoClass);
            response.put("id", section.getId());
            response.put("name", section.getName());
            sections.add(response);
        }
        return sections;
    }

    @PostMapping("/import/")
    public Integer uploadXlsFile(@RequestParam("file") MultipartFile file) throws InterruptedException {
        Import task = new Import();
        xlsxFileUpload.uploadXlsFile(file, task);
        importTasks.put(task.getTaskId(), task);
        return task.getTaskId();
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
        Future<byte[]> future = xlsxFileExport.exportDBDataToXlsxFile();
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

        byte[] out = new byte[0];

        if (future.isDone()) {
            try {
                out = future.get();
            } catch (InterruptedException | ExecutionException e) {
                result.put("status", Export.PROCESSING);
                return ResponseEntity.ok(result);
            }
        }
        return ResponseEntity
                .ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + SectionController.FILENAME + taskId + SectionController.EXTENSION)
                .body(out);
    }

}
