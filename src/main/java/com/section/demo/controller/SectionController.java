package com.section.demo.controller;

import com.section.demo.entity.GeoClass;
import com.section.demo.entity.Section;
import com.section.demo.repository.GeoClassRepository;
import com.section.demo.repository.SectionRepository;
import com.section.demo.service.SectionService;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class SectionController {

    @Autowired
    private SectionRepository sectionRepository;

    @Autowired
    private GeoClassRepository geoClassRepository;

    @Autowired
    private SectionService sectionService;

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
    public String uploadXlsFile(@RequestParam("file") MultipartFile file) throws IOException {
        XSSFWorkbook workbook = new XSSFWorkbook(file.getInputStream());
        XSSFSheet worksheet = workbook.getSheetAt(0);

        for (int i = 1; i < worksheet.getPhysicalNumberOfRows(); i++) {
            XSSFRow row = worksheet.getRow(i);

            Section section = new Section();
            section.setName(row.getCell(0).toString());

            List<GeoClass> geoClasses = new ArrayList<>();

            for (int j = 1; j < row.getLastCellNum() - 1; j++) {
                GeoClass geoClass = new GeoClass();
                String name = row.getCell(j).toString();
                if (name == null) {
                    continue;
                }
                geoClass.setName(name);

                String code = row.getCell(j + 1).toString();
                if (code != null) {
                    geoClass.setCode(code);
                }
                geoClasses.add(geoClass);
            }
            section.setGeoClasses(geoClasses);
            sectionRepository.save(section);
            System.out.println("read");
        }

        return "Ok";
    }
}
