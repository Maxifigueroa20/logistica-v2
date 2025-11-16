package ar.edu.unju.fi.logistica.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import ar.edu.unju.fi.logistica.domain.Envio;
import ar.edu.unju.fi.logistica.enums.EstadoEnvio;

/**
 * Acceso a envíos. Búsquedas por remitente/destinatario/estado y por tracking.
 */
public interface EnvioRepository extends JpaRepository<Envio, Long> {

	@Query("""
			select e from Envio e
			where (:remitenteId    is null or e.remitente.id    = :remitenteId)
			  and (:destinatarioId is null or e.destinatario.id = :destinatarioId)
			  and (:estado        is null or e.estadoActual     = :estado)
			""")
	List<Envio> buscarFiltrado(@Param("remitenteId") Long remitenteId, @Param("destinatarioId") Long destinatarioId,
			@Param("estado") EstadoEnvio estado);

	/**
	 * Versión paginada del filtro unificado. Ideal para exponer en API con
	 * paginado.
	 */
	@Query("""
			select e from Envio e
			where (:remitenteId    is null or e.remitente.id    = :remitenteId)
			  and (:destinatarioId is null or e.destinatario.id = :destinatarioId)
			  and (:estado        is null or e.estadoActual     = :estado)
			""")
	Page<Envio> buscarFiltrado(@Param("remitenteId") Long remitenteId, @Param("destinatarioId") Long destinatarioId,
			@Param("estado") EstadoEnvio estado, Pageable pageable);

	/**
	 * Búsqueda por hash de tracking, incluyendo historial precargado. Usado para el
	 * endpoint de seguimiento (tracking público).
	 */
	@Query("""
			select e from Envio e
			left join fetch e.historiales h
			where e.trackingCodeHash = :hash
			""")
	Optional<Envio> findByTrackingCodeHashWithHistorial(@Param("hash") byte[] hash);
}
