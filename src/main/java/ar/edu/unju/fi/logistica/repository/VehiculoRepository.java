package ar.edu.unju.fi.logistica.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import ar.edu.unju.fi.logistica.domain.Vehiculo;

/**
 * Acceso a vehículos. Consultas por capacidad y refrigeración (requisitos
 * Sprint 1/2).
 */
public interface VehiculoRepository extends JpaRepository<Vehiculo, Long> {

	/**
	 * Patente es única (ignore case). Usado para alta de rutas y búsquedas
	 * puntuales.
	 */
	Optional<Vehiculo> findByPatenteIgnoreCase(String patente);
	
	List<Vehiculo> findByPatenteContainingIgnoreCase(String patente);

	/**
	 * Búsqueda unificada con filtros opcionales.
	 *
	 * Si un parámetro es null, no se aplica ese filtro.
	 */
	@Query("""
			select v from Vehiculo v
			 where (:patente     is null or lower(v.patente) = lower(:patente))
			   and (:refrigerado is null or v.refrigerado = :refrigerado)
			   and (:capPesoMin  is null or v.capacidadPesoKg      >= :capPesoMin)
			   and (:capPesoMax  is null or v.capacidadPesoKg      <= :capPesoMax)
			   and (:capVolMin   is null or v.capacidadVolumenDm3 >= :capVolMin)
			   and (:capVolMax   is null or v.capacidadVolumenDm3 <= :capVolMax)
			   and (
			         (:tempMin is null and :tempMax is null)
			      or (
			             v.refrigerado = true
			         and (:tempMin is null or v.rangoTempMin is null or v.rangoTempMin <= :tempMin)
			         and (:tempMax is null or v.rangoTempMax is null or v.rangoTempMax >= :tempMax)
			      )
			   )
			""")
	List<Vehiculo> buscarFiltrado(@Param("patente") String patente, @Param("refrigerado") Boolean refrigerado,
			@Param("capPesoMin") Double capacidadPesoMin, @Param("capPesoMax") Double capacidadPesoMax,
			@Param("capVolMin") Double capacidadVolumenMin, @Param("capVolMax") Double capacidadVolumenMax,
			@Param("tempMin") Double tempMin, @Param("tempMax") Double tempMax);
}
