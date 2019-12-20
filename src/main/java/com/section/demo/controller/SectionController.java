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

    @GetMapping("/sections/")
    public List<Section> getAllSections() {
        return sectionRepository.findAll();
    }

    @PostMapping("/sections/")
    public ResponseEntity<Section> createSection(@Valid @RequestBody Section section) {
        section = sectionService.save(section);
        @Valid Section finalSection = section;
        section.getGeoClasses().forEach(geoClass -> geoClass.setSections(finalSection));
        return ResponseEntity.ok(section);
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
        for (GeoClass geoClass: geoClasses) {
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
        xlsxFileUpload.uploadXlsFile(file);
        return 1;
    }

    @GetMapping("import/{taskId}")
    public Map<String, String> statusOfImportFile(@PathVariable(name = "taskId") String taskId) {
        Map<String, String> result = new HashMap<>();
        Task task;
        result.put("status", "DONE");
        return result;
    }
}
