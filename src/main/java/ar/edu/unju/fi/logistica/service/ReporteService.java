package ar.edu.unju.fi.logistica.service;

import ar.edu.unju.fi.logistica.enums.EstadoEnvio;

import java.time.LocalDate;

public interface ReporteService {
    byte[] generarReporteEnvios(
            LocalDate fechaDesde,
            LocalDate fechaHasta,
            EstadoEnvio estado,
            String formato
    );
}
