package com.section.demo.service;

import com.section.demo.entity.GeoClass;
import com.section.demo.entity.Section;
import com.section.demo.job.Import;
import com.section.demo.repository.SectionRepository;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Service
public class XlsFileUpload {

    private SectionRepository sectionRepository;

    public XlsFileUpload(SectionRepository sectionRepository) {
        this.sectionRepository = sectionRepository;
    }

    @Async
    public void uploadXlsFile(MultipartFile file, Import task) {
        try {
            XSSFWorkbook workbook = new XSSFWorkbook(file.getInputStream());
            XSSFSheet worksheet = workbook.getSheetAt(0);
            List<Section> sections = new ArrayList<>();
            for (int i = 1; i < worksheet.getPhysicalNumberOfRows(); i++) {
                XSSFRow row = worksheet.getRow(i);

                Section section = new Section();
                section.setName(row.getCell(0).toString());

                List<GeoClass> geologicalClasses = new ArrayList<>();

                for (int j = 1; j < row.getLastCellNum() - 1; j++) {
                    GeoClass geoClass = new GeoClass();
                    String name = row.getCell(j).toString();
                    if (name == null) {
                        continue;
                    }
                    geoClass.setName(name);

                    String code = row.getCell(++j).toString();
                    if (code != null) {
                        geoClass.setCode(code);
                    }
                    geologicalClasses.add(geoClass);
                }
                section.setgeologicalClasses(geologicalClasses);
                sections.add(sectionRepository.save(section));
            }
        } catch (Exception exception) {
            task.setStatus(Import.FAILURE);
            return;
        }
        task.setStatus(Import.DONE);
    }
}