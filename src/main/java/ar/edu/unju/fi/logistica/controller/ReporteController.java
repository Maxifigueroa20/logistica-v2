package ar.edu.unju.fi.logistica.controller;

import ar.edu.unju.fi.logistica.enums.EstadoEnvio;
import ar.edu.unju.fi.logistica.service.ReporteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@Slf4j
@RestController
@RequestMapping("/api/reportes")
@Tag(name = "Reportes", description = "Generación de reportes PDF y Excel")
@RequiredArgsConstructor
public class ReporteController {

    private final ReporteService reporteService;

    @Operation(summary = "Generar reporte de envíos por fecha y estado", description = """
            Descarga un archivo PDF o Excel con los envíos creados en un rango de fechas
            y (opcionalmente) filtrados por su estado actual.
            """)
    @GetMapping("/envios")
    public ResponseEntity<byte[]> generarReporteEnvios(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            @Parameter(description = "Fecha inicial del rango (ISO: yyyy-MM-dd)", example = "2025-01-01")
            LocalDate fechaDesde,

            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            @Parameter(description = "Fecha final del rango (ISO: yyyy-MM-dd)", example = "2025-12-31")
            LocalDate fechaHasta,

            @RequestParam(required = false)
            @Parameter(description = "Estado actual del envío (opcional, si no se envía trae todos los estados)", example = "ENTREGADO")
            EstadoEnvio estado,

            @RequestParam(defaultValue = "pdf")
            @Parameter(description = "Formato del reporte ('pdf' o 'excel')", example = "pdf")
            String formato
    ) {
        log.info("GET /api/reportes/envios -> formato={}, estado={}, rango=[{} - {}]",
                formato, estado, fechaDesde, fechaHasta);

        // 1. El servicio se encarga de toda la lógica
        byte[] bytes = reporteService.generarReporteEnvios(fechaDesde, fechaHasta, estado, formato);

        // 2. Preparar respuesta HTTP
        String contentType;
        String filename;

        if ("pdf".equalsIgnoreCase(formato)) {
            contentType = "application/pdf";
            filename = "reporte_envios.pdf";
        } else {
            contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            filename = "reporte_envios.xlsx";
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(bytes);
    }
}
