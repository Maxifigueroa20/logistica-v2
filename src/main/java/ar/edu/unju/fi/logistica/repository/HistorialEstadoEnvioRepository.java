package ar.edu.unju.fi.logistica.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import ar.edu.unju.fi.logistica.domain.HistorialEstadoEnvio;

/**
 * Acceso al historial de cambios de estado de un envío.
 * Usado para auditar el flujo (Sprint 2).
 */
public interface HistorialEstadoEnvioRepository extends JpaRepository<HistorialEstadoEnvio, Long> {

    /** Devuelve el historial ordenado cronológicamente. */
    List<HistorialEstadoEnvio> findByEnvio_IdOrderByFechaHoraAsc(Long envioId);
}
