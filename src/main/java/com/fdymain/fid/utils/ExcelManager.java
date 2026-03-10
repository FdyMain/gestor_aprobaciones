package com.fdymain.fid.utils;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

public class ExcelManager {

    private static final String EXCEL_FILE =
            "src/main/resources/resultados_aprobaciones.xlsx";

    private static final DateTimeFormatter SHEET_FMT =
            DateTimeFormatter.ofPattern("'resultados_'dd_MM_yy_HH_mm");

    private Workbook workbook;
    private Sheet sheet;
    private int rowIndex = 1;
    private Path excelPath;

    // ================= INICIALIZAR =================

    public void inicializar() throws Exception {

        excelPath = Paths.get(EXCEL_FILE);

        if (Files.exists(excelPath)) {

            try (FileInputStream fis = new FileInputStream(excelPath.toFile())) {
                workbook = new XSSFWorkbook(fis);
            }

        } else {

            workbook = new XSSFWorkbook();
        }

        String sheetName = LocalDateTime.now().format(SHEET_FMT);

        sheet = workbook.createSheet(sheetName);

        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("Referencia");
        header.createCell(1).setCellValue("Tarea");
        header.createCell(2).setCellValue("Proceso");
        header.createCell(3).setCellValue("Id proceso");
        header.createCell(4).setCellValue("Recibido");
        header.createCell(5).setCellValue("Fecha límite");
        header.createCell(6).setCellValue("Estado");

        persistirWorkbook();
    }

    // ================= GUARDAR REGISTRO =================

    public void guardarRegistro(Map<String, String> r) throws Exception {

        Row row = sheet.createRow(rowIndex++);

        row.createCell(0).setCellValue(r.get("referencia"));
        row.createCell(1).setCellValue(r.get("tarea"));
        row.createCell(2).setCellValue(r.get("proceso"));
        row.createCell(3).setCellValue(r.get("idProceso"));
        row.createCell(4).setCellValue(r.get("recibido"));
        row.createCell(5).setCellValue(r.get("fechaLimite"));
        row.createCell(6).setCellValue(r.get("estado"));

        persistirWorkbook();
    }

    // ================= PERSISTIR =================

    private void persistirWorkbook() throws Exception {

        try (FileOutputStream fos = new FileOutputStream(excelPath.toFile())) {
            workbook.write(fos);
        }
    }

    // ================= CERRAR =================

    public void cerrar() throws Exception {
        if (workbook != null) {
            workbook.close();
        }
    }

    // ================= GETTER =================

    public Path getExcelPath() {
        return excelPath;
    }
}
