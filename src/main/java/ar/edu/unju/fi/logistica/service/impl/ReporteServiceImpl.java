package ar.edu.unju.fi.logistica.service.impl;

import ar.edu.unju.fi.logistica.domain.Envio;
import ar.edu.unju.fi.logistica.domain.HistorialEstadoEnvio;
import ar.edu.unju.fi.logistica.enums.EstadoEnvio;
import ar.edu.unju.fi.logistica.repository.HistorialEstadoEnvioRepository;
import ar.edu.unju.fi.logistica.service.ReporteService;

import com.itextpdf.text.*;
import com.itextpdf.text.Font;
import com.itextpdf.text.pdf.*;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.*;

import org.apache.poi.xssf.usermodel.extensions.XSSFCellBorder;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReporteServiceImpl implements ReporteService {

    private final HistorialEstadoEnvioRepository historialRepo;

    // --- Formateadores ---
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DATE_SHORT_FMT = DateTimeFormatter.ofPattern("dd/MM/yy");
    private static final DateTimeFormatter DATETIME_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    // --- Colores (Diseño Visual) ---
    private static final BaseColor C_HEADER_BLUE = new BaseColor(0, 85, 175);    // #0055AF
    private static final BaseColor C_BG_FILTER   = new BaseColor(230, 240, 245); // #E6F0F5
    private static final BaseColor C_BG_COLHEAD  = new BaseColor(245, 250, 250); // #F5FAFA
    private static final BaseColor C_BG_FOOTER   = new BaseColor(240, 245, 250); // #F0F5FA
    private static final BaseColor C_BORDER_MUTED = new BaseColor(235, 240, 240);// #EBF0F0
    private static final BaseColor C_TXT_SOFT    = new BaseColor(80, 80, 80);    // Texto general
    private static final BaseColor C_TXT_MUTED   = new BaseColor(140, 140, 140); // Texto footer

    // --- Constantes de Borde ---
    private static final float W_FRAME = 2.0f;   // Grosor del Marco Exterior
    private static final float W_FINE  = 0.5f;   // Grosor de líneas internas

    @Override
    public byte[] generarReporteEnvios(LocalDate fechaDesde, LocalDate fechaHasta, EstadoEnvio estado, String formato) {
        LocalDateTime desde = fechaDesde.atStartOfDay();
        LocalDateTime hasta = fechaHasta.atTime(LocalTime.MAX);

        List<HistorialEstadoEnvio> historiales = historialRepo.findByEstadoNuevoAndFechaHoraBetween(
                EstadoEnvio.GENERADO, desde, hasta
        );

        List<Envio> envios = historiales.stream()
                .map(HistorialEstadoEnvio::getEnvio)
                .filter(e -> (estado == null || e.getEstadoActual() == estado))
                .collect(Collectors.toList());

        log.info("Generando reporte {} para {} envíos", formato, envios.size());

        try {
            if ("pdf".equalsIgnoreCase(formato)) return generarPdf(envios, fechaDesde, fechaHasta, estado);
            else return generarExcel(envios, fechaDesde, fechaHasta, estado);
        } catch (Exception e) {
            throw new RuntimeException("Error generando reporte", e);
        }
    }

    // ========================================================================
    //                                  PDF (iText 5)
    // ========================================================================
    private byte[] generarPdf(List<Envio> envios, LocalDate fDesde, LocalDate fHasta, EstadoEnvio estado) throws DocumentException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4, 30, 30, 30, 30);
        PdfWriter.getInstance(document, baos);
        document.open();

        // Tabla Maestra
        PdfPTable mainTable = new PdfPTable(6);
        mainTable.setWidthPercentage(100);
        mainTable.setWidths(new float[]{1.5f, 1.5f, 3.0f, 2.5f, 2.0f, 2.2f}); // Distribución solicitada

        // --- Fuentes ---
        Font fTitle = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, BaseColor.WHITE);
        Font fLabelBold = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, C_TXT_SOFT);
        Font fTextSoft = FontFactory.getFont(FontFactory.HELVETICA, 8, C_TXT_SOFT);
        // Cabecera: Usamos BOLD con color gris medio para dar "grosor" sin ser negro puro
        Font fColHead = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, new BaseColor(100, 100, 100));
        Font fFooter = FontFactory.getFont(FontFactory.HELVETICA, 7, C_TXT_MUTED);

        // --- A. Header Principal (Azul) ---
        PdfPCell cHeader = new PdfPCell(new Phrase("Reporte de Logística y Envíos", fTitle));
        cHeader.setColspan(6);
        cHeader.setBackgroundColor(C_HEADER_BLUE);
        cHeader.setHorizontalAlignment(Element.ALIGN_CENTER);
        cHeader.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cHeader.setPaddingTop(25);    // Espaciado vertical
        cHeader.setPaddingBottom(25);

        // Bordes del Header: Arriba y Lados (Marco), Abajo (Nada)
        cHeader.setBorderColorTop(C_BORDER_MUTED); cHeader.setBorderWidthTop(W_FRAME);
        cHeader.setBorderColorLeft(C_BORDER_MUTED); cHeader.setBorderWidthLeft(W_FRAME);
        cHeader.setBorderColorRight(C_BORDER_MUTED); cHeader.setBorderWidthRight(W_FRAME);
        cHeader.setBorderWidthBottom(0);

        mainTable.addCell(cHeader);

        // --- B. Filtros (Nested Table) ---
        PdfPTable nestedFilter = new PdfPTable(2);
        nestedFilter.setWidthPercentage(100);

        Phrase pRango = new Phrase();
        pRango.add(new Chunk("Rango de Fechas: ", fLabelBold));
        pRango.add(new Chunk(fDesde.format(DATE_FMT) + " a " + fHasta.format(DATE_FMT), fTextSoft));
        PdfPCell cLeft = new PdfPCell(pRango);
        cLeft.setBorder(Rectangle.NO_BORDER);
        cLeft.setBackgroundColor(C_BG_FILTER);

        Phrase pEstado = new Phrase();
        pEstado.add(new Chunk("Estado Actual: ", fLabelBold));
        pEstado.add(new Chunk((estado != null ? estado.toString() : "TODOS"), fTextSoft));
        PdfPCell cRight = new PdfPCell(pEstado);
        cRight.setBorder(Rectangle.NO_BORDER);
        cRight.setBackgroundColor(C_BG_FILTER);
        cRight.setHorizontalAlignment(Element.ALIGN_RIGHT);

        nestedFilter.addCell(cLeft);
        nestedFilter.addCell(cRight);

        PdfPCell cFilterRow = new PdfPCell(nestedFilter);
        cFilterRow.setColspan(6);
        cFilterRow.setBackgroundColor(C_BG_FILTER);
        cFilterRow.setPaddingTop(15);    // Espaciado vertical
        cFilterRow.setPaddingBottom(15);
        cFilterRow.setPaddingLeft(10);
        cFilterRow.setPaddingRight(10);

        // Bordes Filtro: Lados (Marco), Arriba (Nada), Abajo (Fino)
        cFilterRow.setBorderColorLeft(C_BORDER_MUTED); cFilterRow.setBorderWidthLeft(W_FRAME);
        cFilterRow.setBorderColorRight(C_BORDER_MUTED); cFilterRow.setBorderWidthRight(W_FRAME);
        cFilterRow.setBorderWidthTop(0);
        cFilterRow.setBorderColorBottom(C_BORDER_MUTED); cFilterRow.setBorderWidthBottom(W_FINE);

        mainTable.addCell(cFilterRow);

        // --- C. Cabeceras de Columna ---
        String[] headers = {"N° ENVÍO", "F. EMISIÓN", "CLIENTE (RAZÓN SOCIAL)", "DESTINO", "ARTÍCULOS (CANT.)", "ESTADO ENVÍO"};
        for (int i = 0; i < headers.length; i++) {
            PdfPCell cell = new PdfPCell(new Phrase(headers[i], fColHead));
            cell.setBackgroundColor(C_BG_COLHEAD);
            cell.setPadding(8);
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            applyCellBorders(cell, i, headers.length);
            mainTable.addCell(cell);
        }

        // --- D. Datos ---
        for (Envio e : envios) {
            addBodyCell(mainTable, e.getId().toString(), fTextSoft, 0, 6);
            addBodyCell(mainTable, getFechaCreacion(e, DATE_SHORT_FMT), fTextSoft, 1, 6);
            addBodyCell(mainTable, e.getDestinatario().getNombreRazonSocial(), fTextSoft, 2, 6);
            addBodyCell(mainTable, e.getDireccionEntrega(), fTextSoft, 3, 6);
            addBodyCell(mainTable, e.getPaquetes().size() + " unid.", fTextSoft, 4, 6);
            addBodyCell(mainTable, e.getEstadoActual().toString(), fTextSoft, 5, 6);
        }

        // --- E. Total (Penúltima) ---
        Phrase pTotal = new Phrase();
        pTotal.add(new Chunk("Total de Envíos en el Reporte: ", fLabelBold)); // Negrita
        pTotal.add(new Chunk(String.valueOf(envios.size()), fLabelBold));     // Negrita

        PdfPCell cTotal = new PdfPCell(pTotal);
        cTotal.setColspan(6);
        cTotal.setBackgroundColor(C_BG_FILTER);
        cTotal.setHorizontalAlignment(Element.ALIGN_CENTER);
        cTotal.setPaddingTop(10);
        cTotal.setPaddingBottom(10);

        // Bordes Total: Lados (Marco), Arriba/Abajo (Fino)
        cTotal.setBorderColorLeft(C_BORDER_MUTED); cTotal.setBorderWidthLeft(W_FRAME);
        cTotal.setBorderColorRight(C_BORDER_MUTED); cTotal.setBorderWidthRight(W_FRAME);
        cTotal.setBorderColorTop(C_BORDER_MUTED); cTotal.setBorderWidthTop(W_FINE);
        cTotal.setBorderColorBottom(C_BORDER_MUTED); cTotal.setBorderWidthBottom(W_FINE);

        mainTable.addCell(cTotal);

        // --- F. Footer (Última) ---
        String fechaGen = LocalDateTime.now().format(DATETIME_FMT);
        PdfPCell cFooter = new PdfPCell(new Phrase("Reporte generado automáticamente el " + fechaGen + " horas.", fFooter));
        cFooter.setColspan(6);
        cFooter.setBackgroundColor(C_BG_FOOTER);
        cFooter.setHorizontalAlignment(Element.ALIGN_RIGHT);
        cFooter.setVerticalAlignment(Element.ALIGN_BOTTOM);
        cFooter.setPaddingTop(12);
        cFooter.setPaddingBottom(12);
        cFooter.setPaddingRight(10);

        // Bordes Footer: Lados y Abajo (Marco)
        cFooter.setBorderColorLeft(C_BORDER_MUTED); cFooter.setBorderWidthLeft(W_FRAME);
        cFooter.setBorderColorRight(C_BORDER_MUTED); cFooter.setBorderWidthRight(W_FRAME);
        cFooter.setBorderColorBottom(C_BORDER_MUTED); cFooter.setBorderWidthBottom(W_FRAME);
        cFooter.setBorderWidthTop(0); // Se conecta con el fino del Total

        mainTable.addCell(cFooter);

        document.add(mainTable);
        document.close();
        return baos.toByteArray();
    }

    // Helper para bordes de celdas de contenido (Cabecera y Cuerpo)
    private void applyCellBorders(PdfPCell cell, int colIndex, int totalCols) {
        cell.setBorderColor(C_BORDER_MUTED);
        // Horizontales: Finos
        cell.setBorderWidthTop(W_FINE);
        cell.setBorderWidthBottom(W_FINE);

        // Verticales: Ninguno (interior)
        cell.setBorderWidthLeft(0);
        cell.setBorderWidthRight(0);

        // Marco Izquierdo (Primera columna)
        if (colIndex == 0) {
            cell.setBorderWidthLeft(W_FRAME);
        }
        // Marco Derecho (Última columna)
        if (colIndex == totalCols - 1) {
            cell.setBorderWidthRight(W_FRAME);
        }
    }

    private void addBodyCell(PdfPTable table, String text, Font font, int colIndex, int totalCols) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setPadding(8);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        applyCellBorders(cell, colIndex, totalCols);
        table.addCell(cell);
    }

    // ========================================================================
    //                                  EXCEL
    // ========================================================================
    private byte[] generarExcel(List<Envio> envios, LocalDate fDesde, LocalDate fHasta, EstadoEnvio estado) throws IOException {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Reporte Envíos");

            // Estilos
            XSSFCellStyle styleHeaderBlue = createStyle(workbook, new java.awt.Color(0, 85, 175), true, 14, IndexedColors.WHITE);
            styleHeaderBlue.setAlignment(HorizontalAlignment.CENTER);

            XSSFCellStyle styleFilter = createStyle(workbook, new java.awt.Color(230, 240, 245), true, 9, IndexedColors.GREY_80_PERCENT);

            XSSFCellStyle styleColHead = createStyle(workbook, new java.awt.Color(245, 250, 250), true, 9, IndexedColors.GREY_80_PERCENT);
            styleColHead.setBorderBottom(BorderStyle.THIN);
            styleColHead.setBorderColor(XSSFCellBorder.BorderSide.BOTTOM, new XSSFColor(new java.awt.Color(235, 240, 240), null));

            XSSFCellStyle styleData = createStyle(workbook, null, false, 9, IndexedColors.GREY_80_PERCENT);
            styleData.setBorderBottom(BorderStyle.THIN);
            styleData.setBorderColor(XSSFCellBorder.BorderSide.BOTTOM, new XSSFColor(new java.awt.Color(235, 240, 240), null));

            int rowNum = 0;

            // 1. Header
            Row r1 = sheet.createRow(rowNum++); r1.setHeightInPoints(35);
            createCell(r1, 0, "Reporte de Logística y Envíos", styleHeaderBlue);
            for(int i=1; i<6; i++) createCell(r1, i, "", styleHeaderBlue);
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 5));

            // 2. Filtros
            Row r2 = sheet.createRow(rowNum++); r2.setHeightInPoints(25);
            createCell(r2, 0, "Rango: " + fDesde + " a " + fHasta, styleFilter);
            for(int i=1; i<5; i++) createCell(r2, i, "", styleFilter);
            createCell(r2, 5, "Estado: " + (estado != null ? estado : "TODOS"), styleFilter);
            r2.getCell(5).getCellStyle().setAlignment(HorizontalAlignment.RIGHT);

            sheet.addMergedRegion(new CellRangeAddress(r2.getRowNum(), r2.getRowNum(), 0, 1));

            // 3. Cabeceras
            Row r3 = sheet.createRow(rowNum++);
            String[] cols = {"N° ENVIO", "F. EMISION", "CLIENTE", "DESTINO", "CANT.", "ESTADO"};
            for(int i=0; i<6; i++) createCell(r3, i, cols[i], styleColHead);

            // 4. Datos
            for(Envio e : envios) {
                Row r = sheet.createRow(rowNum++);
                createCell(r, 0, e.getId().toString(), styleData);
                createCell(r, 1, getFechaCreacion(e, DATE_SHORT_FMT), styleData);
                createCell(r, 2, e.getDestinatario().getNombreRazonSocial(), styleData);
                createCell(r, 3, e.getDireccionEntrega(), styleData);
                createCell(r, 4, e.getPaquetes().size() + " unid.", styleData);
                createCell(r, 5, e.getEstadoActual().toString(), styleData);
            }

            // 5. Total
            Row rTot = sheet.createRow(rowNum++); rTot.setHeightInPoints(25);
            createCell(rTot, 0, "Total de Envíos: " + envios.size(), styleFilter);
            styleFilter.setAlignment(HorizontalAlignment.CENTER);
            for(int i=1; i<6; i++) createCell(rTot, i, "", styleFilter);
            sheet.addMergedRegion(new CellRangeAddress(rowNum-1, rowNum-1, 0, 5));

            // 6. Footer
            XSSFCellStyle styleFooter = createStyle(workbook, new java.awt.Color(240, 245, 250), false, 8, IndexedColors.GREY_50_PERCENT);
            styleFooter.setAlignment(HorizontalAlignment.RIGHT);
            Row rFoot = sheet.createRow(rowNum++); rFoot.setHeightInPoints(20);
            createCell(rFoot, 0, "Generado el " + LocalDateTime.now().format(DATETIME_FMT), styleFooter);
            for(int i=1; i<6; i++) createCell(rFoot, i, "", styleFooter);
            sheet.addMergedRegion(new CellRangeAddress(rowNum-1, rowNum-1, 0, 5));

            // Anchos
            for(int i=0; i<6; i++) sheet.setColumnWidth(i, 4000);
            sheet.setColumnWidth(2, 7000);
            sheet.setColumnWidth(3, 7000);

            workbook.write(baos);
            return baos.toByteArray();
        }
    }

    private XSSFCellStyle createStyle(Workbook wb, java.awt.Color bg, boolean bold, int size, IndexedColors color) {
        XSSFCellStyle style = (XSSFCellStyle) wb.createCellStyle();
        if(bg != null) {
            style.setFillForegroundColor(new XSSFColor(bg, null));
            style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        }
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        org.apache.poi.ss.usermodel.Font f = wb.createFont();
        f.setBold(bold);
        f.setFontHeightInPoints((short) size);
        f.setColor(color.getIndex());
        style.setFont(f);
        return style;
    }

    private void createCell(Row r, int i, String v, CellStyle s) {
        Cell c = r.createCell(i); c.setCellValue(v); c.setCellStyle(s);
    }

    private String getFechaCreacion(Envio e, DateTimeFormatter fmt) {
        return e.getHistoriales().stream()
                .filter(h -> h.getEstadoNuevo() == EstadoEnvio.GENERADO)
                .findFirst()
                .map(h -> h.getFechaHora().format(fmt))
                .orElse("-");
    }
}
