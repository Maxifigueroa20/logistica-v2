package ar.edu.unju.fi.logistica.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import ar.edu.unju.fi.logistica.domain.Paquete;

/**
 * Acceso a paquetes (clase base y subtipos). Filtros por tipo y por rangos de
 * peso/volumen.
 */
public interface PaqueteRepository extends JpaRepository<Paquete, Long> {

	/** Por si necesitás navegar por Envío. */
	List<Paquete> findByEnvio_Id(Long envioId);

	/** Búsqueda exacta por código (usado al crear Envíos por referencia). */
	Optional<Paquete> findByCodigo(String codigo);

	/**
	 * Búsqueda de varios paquetes por código. Útil cuando se reciben varios códigos
	 * en EnvioCreateDTO.
	 */
	List<Paquete> findByCodigoIn(List<String> codigos);

	/**
	 * Búsqueda unificada: - tipo: subclase concreta (PaqueteFragil,
	 * PaqueteRefrigerado) o null para todos - pesoMin/pesoMax y volMin/volMax:
	 * rangos opcionales
	 *
	 * Si un parámetro es null, no se aplica ese filtro.
	 */
	@Query("""
			    SELECT p FROM Paquete p
			    WHERE (:pesoMin IS NULL OR p.pesoKg >= :pesoMin)
			      AND (:pesoMax IS NULL OR p.pesoKg <= :pesoMax)
			      AND (:volMin  IS NULL OR p.volumenDm3 >= :volMin)
			      AND (:volMax  IS NULL OR p.volumenDm3 <= :volMax)
			      AND (
			            :tipo IS NULL
			            OR (
			                  :tipo = 'FRAGIL' AND TYPE(p) = PaqueteFragil
			               )
			            OR (
			                  :tipo = 'REFRIGERADO' AND TYPE(p) = PaqueteRefrigerado
			               )
			          )
			""")
	List<Paquete> buscarFiltrado(@Param("tipo") String tipo, @Param("pesoMin") Double pesoMin,
			@Param("pesoMax") Double pesoMax, @Param("volMin") Double volMin, @Param("volMax") Double volMax);
}
