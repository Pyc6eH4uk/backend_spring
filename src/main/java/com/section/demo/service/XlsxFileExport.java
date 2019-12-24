package com.section.demo.service;

import com.section.demo.entity.GeoClass;
import com.section.demo.entity.Section;
import com.section.demo.job.Export;
import com.section.demo.repository.GeoClassRepository;
import com.section.demo.repository.SectionRepository;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.tomcat.util.http.fileupload.ByteArrayOutputStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;

import javax.print.attribute.standard.MediaSize;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.Future;

@Service
public class XlsxFileExport {

    @Autowired
    private SectionRepository sectionRepository;

    @Autowired
    private GeoClassRepository geoClassRepository;

    public XlsxFileExport() {}

    private static final String SECTION_COLUMN = "Section names";
    private static final String CLASS_COLUMN = "Class";
    private static final String CODE_COLUMN = "Code";
    private static final String NAME = "name";

    @Async
    public Future<byte[]> exportDBDataToXlsxFile() throws IOException, InterruptedException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Result");

        Row headerRow = sheet.createRow(0);
        Cell section_cell = headerRow.createCell(0);
        section_cell.setCellValue(SECTION_COLUMN);

        List<Section> sections = sectionRepository.findAll();
        int columnsCount = 0;
        for (Section section : sections) {
            int geoClassLength = section.geoClasses.size();
            if (columnsCount < geoClassLength) {
                columnsCount = geoClassLength;
            }
        }

        for (int i = 1; i <= columnsCount; i += 1) {
            Cell class_cell = headerRow.createCell(i);
            class_cell.setCellValue(CLASS_COLUMN + " " + i + " " + NAME);
            Cell code_cell = headerRow.createCell(i + 1);
            code_cell.setCellValue(CODE_COLUMN + " " + i + " " + NAME);
            i++;
        }

        int rowNumber = 1;

        for (Section section : sections) {
            int cell = 0;
            Row row = sheet.createRow(rowNumber++);

            row.createCell(cell).setCellValue(section.getName());

            List<GeoClass> geoClasses = section.getGeoClasses();
            if (geoClasses == null) {
                continue;
            }
            cell++;
            for (GeoClass geoClass : geoClasses) {
                row.createCell(cell).setCellValue(geoClass.getName());
                cell++;

                row.createCell(cell).setCellValue(geoClass.getCode());
//                cell++;
            }
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        workbook.write(out);
        workbook.close();
        System.out.println("SLEEP");
        Thread.sleep(5000L);
        System.out.println("AFTER SLEEP");
        return new AsyncResult<>(out.toByteArray());
    }
}
