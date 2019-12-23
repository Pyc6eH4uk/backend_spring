package com.section.demo.controller;

import com.section.demo.entity.GeoClass;
import com.section.demo.entity.Section;
import com.section.demo.job.Export;
import com.section.demo.job.Import;
import com.section.demo.repository.GeoClassRepository;
import com.section.demo.repository.SectionRepository;
import com.section.demo.service.SectionService;
import com.section.demo.service.XlsxFileExport;
import com.section.demo.service.XlsxFileUpload;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    @PostMapping("/import/")
    public Integer uploadXlsFile(@RequestParam("file") MultipartFile file) throws IOException, ExecutionException, InterruptedException {
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
        int columnsCount = geoClassRepository.countAllRows();
        List<Section> sections = sectionRepository.findAll();
        Future<byte[]> future = xlsxFileExport.exportDBDataToXlsxFile(sections, columnsCount, exportTask);
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
            result.put("status", Export.DONE);
        }
//        result.put("status", /);
        return ResponseEntity.ok(result);
    }

}
