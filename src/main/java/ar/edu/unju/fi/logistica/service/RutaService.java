package ar.edu.unju.fi.logistica.service;

import java.time.LocalDate;
import java.util.List;

import ar.edu.unju.fi.logistica.dto.ruta.RutaCreateDTO;
import ar.edu.unju.fi.logistica.dto.ruta.RutaDTO;

/**
 * Servicio para gestión de rutas logísticas. Controla capacidad, refrigeración
 * y asignación de envíos.
 */
public interface RutaService {

	/** Crea una nueva ruta con vehículo asignado (sin envíos iniciales). */
	RutaDTO crear(RutaCreateDTO dto);

	/**
	 * Asigna uno o varios envíos a una ruta existente (valida capacidad y
	 * compatibilidad).
	 */
	RutaDTO asignarEnvios(Long rutaId, List<Long> enviosIds);

	/**
	 * Quita uno o varios envíos de la ruta.
	 *
	 * @param observacion texto opcional que puede loguearse o dejarse en historial
	 */
	RutaDTO quitarEnvios(Long rutaId, List<Long> enviosIds, String observacion);

	/**
	 * Devuelve los IDs de envíos de una ruta en una fecha dada. (Se puede usar
	 * luego para mapear a DTOs de Envío).
	 */
	List<Long> listarEnviosIdsDeRutaEnFecha(Long rutaId, LocalDate fecha);

	/** Busca una ruta por ID. */
	RutaDTO buscarPorId(Long id);

	/**
	 * Lista rutas, opcionalmente filtradas por fecha.
	 *
	 * @param fecha si es null, devuelve todas las rutas.
	 */
	List<RutaDTO> listar(LocalDate fecha);
}
