package com.section.demo.service;

import com.section.demo.entity.GeoClass;
import com.section.demo.entity.Section;
import com.section.demo.repository.SectionRepository;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.tomcat.util.http.fileupload.ByteArrayOutputStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.Future;

@Service
public class XlsFileExport {

    @Autowired
    private SectionRepository sectionRepository;

    public XlsFileExport() {}

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

        List<Section> sections = sectionRepository.findAll(Sort.by(Sort.Direction.ASC, "name"));
        int columnsCount = 0;
        for (Section section : sections) {
            int geoClassLength = section.geologicalClasses.size();
            if (columnsCount < geoClassLength) {
                columnsCount = geoClassLength;
            }
        }

        columnsCount *= 2;

        int j = 1;
        for (int i = 1; i <= columnsCount; i += 1) {
            Cell class_cell = headerRow.createCell(i);
            if (i % 2 == 0) {
                class_cell.setCellValue(CODE_COLUMN + " " + j + " " + NAME);
                j++;
            } else {
                class_cell.setCellValue(CLASS_COLUMN + " " + j + " " + NAME);
            }
        }

        int rowNumber = 1;

        for (Section section : sections) {
            int cell = 0;
            Row row = sheet.createRow(rowNumber++);

            row.createCell(cell).setCellValue(section.getName());

            List<GeoClass> geologicalClasses = section.getGeologicalClasses();
            if (geologicalClasses == null) {
                continue;
            }
            cell++;
            for (GeoClass geoClass : geologicalClasses) {
                row.createCell(cell).setCellValue(geoClass.getName());
                cell++;

                row.createCell(cell).setCellValue(geoClass.getCode());
                cell++;
            }
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        workbook.write(out);
        workbook.close();
        return new AsyncResult<>(out.toByteArray());
    }
}
