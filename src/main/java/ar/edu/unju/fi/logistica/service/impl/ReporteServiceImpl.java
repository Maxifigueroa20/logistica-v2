package ar.edu.unju.fi.logistica.service.impl;

import ar.edu.unju.fi.logistica.domain.Envio;
import ar.edu.unju.fi.logistica.domain.HistorialEstadoEnvio;
import ar.edu.unju.fi.logistica.enums.EstadoEnvio;
import ar.edu.unju.fi.logistica.repository.HistorialEstadoEnvioRepository;
import ar.edu.unju.fi.logistica.service.ReporteService;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

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
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @Override
    public byte[] generarReporteEnvios(LocalDate fechaDesde, LocalDate fechaHasta, EstadoEnvio estado, String formato) {

        // 1. Convertir LocalDate a LocalDateTime para la consulta
        LocalDateTime desde = fechaDesde.atStartOfDay();
        LocalDateTime hasta = fechaHasta.atTime(LocalTime.MAX);

        // 2. Obtener los envíos CREADOS en ese rango
        List<HistorialEstadoEnvio> historiales = historialRepo.findByEstadoNuevoAndFechaHoraBetween(
                EstadoEnvio.GENERADO, desde, hasta
        );

        // 3. Obtener la lista de Envíos y filtrar por estado actual (si es necesario)
        List<Envio> envios = historiales.stream()
                .map(HistorialEstadoEnvio::getEnvio)
                .filter(envio -> (estado == null || envio.getEstadoActual() == estado))
                .collect(Collectors.toList());

        log.info("Generando reporte [Formato: {}, Rango: {} a {}, Estado: {}, Envíos encontrados: {}]",
                formato, fechaDesde, fechaHasta, (estado != null ? estado : "TODOS"), envios.size());

        try {
            if ("pdf".equalsIgnoreCase(formato)) {
                return generarPdf(envios, fechaDesde, fechaHasta, estado);
            } else if ("excel".equalsIgnoreCase(formato)) {
                return generarExcel(envios, fechaDesde, fechaHasta, estado);
            }
            throw new IllegalArgumentException("Formato no soportado: " + formato);
        } catch (Exception e) {
            log.error("Error al generar reporte: " + e.getMessage(), e);
            throw new RuntimeException("Error al generar reporte", e);
        }
    }

    // --- Generador PDF (iText 5) ---
    private byte[] generarPdf(List<Envio> envios, LocalDate fechaDesde, LocalDate fechaHasta, EstadoEnvio estado) throws DocumentException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4);
        PdfWriter.getInstance(document, baos);
        document.open();

        Font fontTitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
        Font fontHeader = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
        Font fontBody = FontFactory.getFont(FontFactory.HELVETICA, 10);

        document.add(new Paragraph("Reporte de Logística y Envíos", fontTitulo));
        document.add(new Paragraph(" "));
        document.add(new Paragraph("Rango de Fechas: " + fechaDesde.format(DATE_FORMATTER) + " a " + fechaHasta.format(DATE_FORMATTER), fontBody));
        document.add(new Paragraph("Estado Actual: " + (estado != null ? estado.toString() : "TODOS"), fontBody));
        document.add(new Paragraph(" "));

        PdfPTable table = new PdfPTable(5); // 5 columnas
        table.setWidthPercentage(100);

        // Cabeceras
        table.addCell(new PdfPCell(new Phrase("N° Envío", fontHeader)));
        table.addCell(new PdfPCell(new Phrase("F. Emisión", fontHeader)));
        table.addCell(new PdfPCell(new Phrase("Destinatario", fontHeader)));
        table.addCell(new PdfPCell(new Phrase("Paquetes", fontHeader)));
        table.addCell(new PdfPCell(new Phrase("Estado Envío", fontHeader)));

        // Datos
        for (Envio envio : envios) {
            table.addCell(new PdfPCell(new Phrase(envio.getId().toString(), fontBody)));
            // Buscamos la fecha de creación en su historial
            String fechaCreacion = envio.getHistoriales().stream()
                    .filter(h -> h.getEstadoNuevo() == EstadoEnvio.GENERADO)
                    .findFirst()
                    .map(h -> h.getFechaHora().format(DATETIME_FORMATTER))
                    .orElse("N/A");

            table.addCell(new PdfPCell(new Phrase(fechaCreacion, fontBody)));
            table.addCell(new PdfPCell(new Phrase(envio.getDestinatario().getNombreRazonSocial(), fontBody)));
            table.addCell(new PdfPCell(new Phrase(String.valueOf(envio.getPaquetes().size()) + " unid.", fontBody)));
            table.addCell(new PdfPCell(new Phrase(envio.getEstadoActual().toString(), fontBody)));
        }

        document.add(table);
        document.add(new Paragraph(" "));
        document.add(new Paragraph("Total de Envíos en el Reporte: " + envios.size(), fontHeader));

        document.close();
        return baos.toByteArray();
    }

    // --- Generador Excel (Apache POI) ---
    private byte[] generarExcel(List<Envio> envios, LocalDate fechaDesde, LocalDate fechaHasta, EstadoEnvio estado) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Reporte Envíos");

        int rowNum = 0;
        sheet.createRow(rowNum++).createCell(0).setCellValue("Reporte de Logística y Envíos");
        sheet.createRow(rowNum++).createCell(0).setCellValue("Rango de Fechas: " + fechaDesde.format(DATE_FORMATTER) + " a " + fechaHasta.format(DATE_FORMATTER));
        sheet.createRow(rowNum++).createCell(0).setCellValue("Estado Actual: " + (estado != null ? estado.toString() : "TODOS"));
        rowNum++; // Espacio

        // Cabeceras
        Row headerRow = sheet.createRow(rowNum++);
        headerRow.createCell(0).setCellValue("N° Envío");
        headerRow.createCell(1).setCellValue("F. Emisión");
        headerRow.createCell(2).setCellValue("Destinatario");
        headerRow.createCell(3).setCellValue("Paquetes");
        headerRow.createCell(4).setCellValue("Estado Envío");

        // Datos
        for (Envio envio : envios) {
            Row dataRow = sheet.createRow(rowNum++);
            dataRow.createCell(0).setCellValue(envio.getId());

            String fechaCreacion = envio.getHistoriales().stream()
                    .filter(h -> h.getEstadoNuevo() == EstadoEnvio.GENERADO)
                    .findFirst()
                    .map(h -> h.getFechaHora().format(DATETIME_FORMATTER))
                    .orElse("N/A");

            dataRow.createCell(1).setCellValue(fechaCreacion);
            dataRow.createCell(2).setCellValue(envio.getDestinatario().getNombreRazonSocial());
            dataRow.createCell(3).setCellValue(envio.getPaquetes().size());
            dataRow.createCell(4).setCellValue(envio.getEstadoActual().toString());
        }

        rowNum++;
        sheet.createRow(rowNum++).createCell(0).setCellValue("Total de Envíos: " + envios.size());

        // Auto-ajuste de columnas
        for(int i = 0; i < 5; i++) {
            sheet.autoSizeColumn(i);
        }

        workbook.write(baos);
        workbook.close();
        return baos.toByteArray();
    }
}
