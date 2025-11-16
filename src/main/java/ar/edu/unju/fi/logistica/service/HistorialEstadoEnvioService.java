package ar.edu.unju.fi.logistica.service;

import java.util.List;

import ar.edu.unju.fi.logistica.dto.historial.HistorialEstadoEnvioDTO;

/**
 * Servicio auxiliar para consultar el historial de cambios de estado de los envíos.
 */
public interface HistorialEstadoEnvioService {

    /** Devuelve el historial completo de un envío (ordenado cronológicamente). */
    List<HistorialEstadoEnvioDTO> listarPorEnvio(Long envioId);
}
