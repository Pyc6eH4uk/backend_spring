package com.section.demo.service;

import com.section.demo.entity.GeoClass;
import com.section.demo.entity.Section;
import com.section.demo.job.Task;
import com.section.demo.repository.SectionRepository;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

@Service
public class XlsxFileUpload {

    private SectionRepository sectionRepository;

    private static final Logger LOGGER = LoggerFactory.getLogger(XlsxFileUpload.class);

    public XlsxFileUpload(SectionRepository sectionRepository) {
        this.sectionRepository = sectionRepository;
    }

    @Async
    public void uploadXlsFile(MultipartFile file, Task task) throws IOException, InterruptedException {
        try {
            XSSFWorkbook workbook = new XSSFWorkbook(file.getInputStream());
            XSSFSheet worksheet = workbook.getSheetAt(0);
            List<Section> sections = new ArrayList<>();
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
                sections.add(sectionRepository.save(section));
            }
        } catch (Exception exception) {
            System.out.println("EXCEPTION");
            task.setStatus(Task.FAILURE);
            return;
        }
        Thread.sleep(5000L);
        task.setStatus(Task.DONE);
    }
}