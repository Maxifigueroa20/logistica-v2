package ar.edu.unju.fi.logistica.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import ar.edu.unju.fi.logistica.domain.Ruta;

/**
 * Acceso a rutas.
 * - Única por (fecha, vehículo).
 * - Consultas de envíos por ruta/fecha (requisito Sprint 1).
 */
public interface RutaRepository extends JpaRepository<Ruta, Long> {

    /** Para hacer cumplir la unicidad (fecha, vehículo). */
    Optional<Ruta> findByFechaAndVehiculo_Id(LocalDate fecha, Long vehiculoId);

    /** Rutas por fecha (útil para vistas/listados). */
    List<Ruta> findByFecha(LocalDate fecha);

    /**
     * IDs de envíos asignados a una ruta en una fecha.
     * Nota: se usa la relación Envio.ruta y Ruta.fecha.
     */
    @Query("""
           select e.id
             from Envio e
            where e.ruta.id = :rutaId
              and e.ruta.fecha = :fecha
           """)
    List<Long> findEnviosIdsByRutaAndFecha(Long rutaId, LocalDate fecha);
}
