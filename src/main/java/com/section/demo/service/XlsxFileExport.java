package com.section.demo.service;

import com.section.demo.entity.GeoClass;
import com.section.demo.entity.Section;
import com.section.demo.job.Export;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.tomcat.util.http.fileupload.ByteArrayOutputStream;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.Future;

@Service
public class XlsxFileExport {

    public XlsxFileExport() {}

    private static final String SECTION_COLUMN = "Section names";
    private static final String CLASS_COLUMN = "Class name";
    private static final String CODE_COLUMN = "Code name";

    @Async
    public Future<byte[]> exportDBDataToXlsxFile(List<Section> sections, int columnsCount, Export export) throws IOException, InterruptedException {
        try {
            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("Result");

            Row headerRow = sheet.createRow(0);
            Cell section_cell = headerRow.createCell(0);
            section_cell.setCellValue(SECTION_COLUMN);

            for (int i = 1; i < columnsCount - 1; i += 2) {
                Cell class_cell = headerRow.createCell(i);
                class_cell.setCellValue(CLASS_COLUMN + i);
                Cell code_cell = headerRow.createCell((i + 1));
                code_cell.setCellValue(CODE_COLUMN + " " + (i + 1));
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

                    row.createCell(cell).setCellValue(geoClass.getName());
                    cell++;
                }
            }
            Thread.sleep(5000L);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            workbook.close();
            return new AsyncResult<>(out.toByteArray());
        } catch (Exception exception) {
            export.setStatus(Export.FAILURE);
            return null;
        }
    }
}
