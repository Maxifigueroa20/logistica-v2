package ar.edu.unju.fi.logistica.service;

import java.util.List;

import ar.edu.unju.fi.logistica.dto.envio.EnvioCreateDTO;
import ar.edu.unju.fi.logistica.dto.envio.EnvioDTO;
import ar.edu.unju.fi.logistica.dto.envio.EnvioHistorialDTO;
import ar.edu.unju.fi.logistica.enums.EstadoEnvio;

/**
 * Servicio de gestión y seguimiento de envíos. Incluye registro, validación y
 * transición de estados.
 */
public interface EnvioService {

	/**
	 * Crea un nuevo envío. - Remitente y destinatario se buscan por documento/CUIT.
	 * - Los paquetes se referencian por su código (ya deben existir).
	 */
	EnvioDTO crear(EnvioCreateDTO dto);

	/** Cancela un envío (solo si está en estado GENERADO). */
	EnvioDTO cancelar(Long envioId, String motivo);

	/**
	 * Marca un envío como ENTREGADO (requiere comprobante binario). Aplica
	 * transición EN_RUTA -> ENTREGADO.
	 */
	EnvioDTO marcarEntregado(Long envioId, byte[] comprobante);

	/**
	 * Cambia el estado de un envío según el flujo permitido. Normalmente: GENERADO
	 * → EN_ALMACEN → EN_RUTA. Para ENTREGADO usar marcarEntregado().
	 */
	EnvioDTO actualizarEstado(Long envioId, EstadoEnvio nuevoEstado, String observacion);

	/** Busca un envío por ID. */
	EnvioDTO buscarPorId(Long id);

	/**
	 * Búsqueda unificada para el endpoint GET /envios con filtros opcionales.
	 *
	 * @param remitenteDocumento    documento/CUIT del remitente (nullable)
	 * @param destinatarioDocumento documento/CUIT del destinatario (nullable)
	 * @param estado                estado del envío (nullable)
	 */
	List<EnvioDTO> buscarFiltrado(String remitenteDocumento, String destinatarioDocumento, EstadoEnvio estado);

	/**
	 * Busca un envío por código de tracking público y devuelve el detalle +
	 * historial de cambios de estado.
	 */
	EnvioHistorialDTO buscarPorTracking(String publicCode);
}
