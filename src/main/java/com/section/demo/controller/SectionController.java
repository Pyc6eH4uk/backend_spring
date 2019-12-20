package com.section.demo.controller;

import com.section.demo.entity.GeoClass;
import com.section.demo.entity.Section;
import com.section.demo.job.Task;
import com.section.demo.repository.GeoClassRepository;
import com.section.demo.repository.SectionRepository;
import com.section.demo.service.SectionService;
import com.section.demo.service.XlsxFileUpload;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

@RestController
public class SectionController {

    @Autowired
    private SectionRepository sectionRepository;

    @Autowired
    private GeoClassRepository geoClassRepository;

    @Autowired
    private SectionService sectionService;

    @Autowired
    private XlsxFileUpload xlsxFileUpload;

    private Map<Integer, Task> tasks;

    public SectionController() {
        this.tasks = new HashMap<>();
    }

    @GetMapping("/sections/")
    public List<Section> getAllSections() {
        return sectionRepository.findAll();
    }

    @PostMapping("/sections/")
    public ResponseEntity<Map<String, String>> createSection(@Valid @RequestBody Section section) {
        Map<String, String> response = new HashMap<>();
        try {
            section = sectionService.save(section);
        } catch (Exception exception) {
            response.put("message", "Section with such name already exists.");
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

    @DeleteMapping("/sections/{sectionId}")
    public Map<String, Boolean> deleteSection(@PathVariable(value = "sectionId") Long sectionId) {
        Map<String, Boolean> response = new HashMap<>();
        if (sectionRepository.existsById(sectionId)) {
            Section section = sectionRepository.getOne(sectionId);
            sectionRepository.delete(section);
            response.put("deleted", Boolean.TRUE);
            return response;
        }
        response.put("deleted", Boolean.FALSE);
        return response;
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

    @PostMapping("import")
    public Integer uploadXlsFile(@RequestParam("file") MultipartFile file) throws IOException, ExecutionException, InterruptedException {
        Task task = new Task();
        xlsxFileUpload.uploadXlsFile(file, task);
        tasks.put(task.getTaskId(), task);
        return task.getTaskId();
    }

    @GetMapping("import/{taskId}")
    public ResponseEntity<Map<String, String>> statusOfImportFile(@PathVariable(name = "taskId") int taskId) {
        Map<String, String> result = new HashMap<>();
        Task task = tasks.get(taskId);
        if (task == null) {
            result.put("status", "Not existing task");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(result);
        }
        result.put("status", String.valueOf(task.getStatus()));
        return ResponseEntity.ok(result);
    }
}
