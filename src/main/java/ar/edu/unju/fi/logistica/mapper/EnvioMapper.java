package ar.edu.unju.fi.logistica.mapper;

import java.util.List;

import org.mapstruct.AfterMapping;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import ar.edu.unju.fi.logistica.domain.Envio;
import ar.edu.unju.fi.logistica.domain.PaqueteRefrigerado;
import ar.edu.unju.fi.logistica.dto.envio.EnvioCreateDTO;
import ar.edu.unju.fi.logistica.dto.envio.EnvioDTO;
import ar.edu.unju.fi.logistica.dto.envio.EnvioHistorialDTO;
import ar.edu.unju.fi.logistica.dto.historial.HistorialEstadoEnvioDTO;

@Mapper(config = MapperConfigBase.class)
public interface EnvioMapper {

	/* ====== ENTITY -> DTO (con tracking y requiereFrio) ====== */
	@Mapping(target = "remitenteId", source = "remitente.id")
	@Mapping(target = "destinatarioId", source = "destinatario.id")
	@Mapping(target = "remitenteDocumento", source = "remitente.documentoCuit")
    @Mapping(target = "destinatarioDocumento", source = "destinatario.documentoCuit")
	@Mapping(target = "rutaId", source = "ruta.id")
	@Mapping(target = "paquetes", ignore = true)
	@Mapping(target = "trackingCode", ignore = true)
	@Mapping(target = "requiereFrio", ignore = true)
	@Mapping(target = "hasComprobante", ignore = true)
	EnvioDTO toDTO(Envio entity, @Context String trackingCode);

	default EnvioDTO toDTO(Envio src) {
		return toDTO(src, null);
	}

	@AfterMapping
	default void afterToDTO(Envio src, @MappingTarget EnvioDTO.EnvioDTOBuilder<?, ?> dstBuilder,
			@Context String trackingCode) {
		boolean requiereFrio = src != null && src.getPaquetes() != null
				&& src.getPaquetes().stream().anyMatch(PaqueteRefrigerado.class::isInstance);

		boolean hasComprobante = src != null && src.getComprobanteEntrega() != null
				&& src.getComprobanteEntrega().length > 0;

		dstBuilder.trackingCode(trackingCode).requiereFrio(requiereFrio).hasComprobante(hasComprobante);
	}

	/* ====== ENTITY -> DTO (detalle con historial) ====== */
	@Mapping(target = "remitenteId", source = "remitente.id")
	@Mapping(target = "destinatarioId", source = "destinatario.id")
	@Mapping(target = "remitenteDocumento", source = "remitente.documentoCuit")
    @Mapping(target = "destinatarioDocumento", source = "destinatario.documentoCuit")
	@Mapping(target = "rutaId", source = "ruta.id")
	@Mapping(target = "paquetes", ignore = true)
	@Mapping(target = "trackingCode", ignore = true)
	@Mapping(target = "requiereFrio", ignore = true)
	@Mapping(target = "hasComprobante", ignore = true)
	@Mapping(target = "historial", expression = "java( mapHistorial(src) )")
	EnvioHistorialDTO toHistDTO(Envio src, @Context String trackingCode);

	default EnvioHistorialDTO toHistDTO(Envio src) {
		return toHistDTO(src, null);
	}

	/** Reutilizamos el mismo post-mapeo para setear flags y tracking. */
	@AfterMapping
	default void afterToHistDTO(Envio src, @MappingTarget EnvioHistorialDTO.EnvioHistorialDTOBuilder<?, ?> dstBuilder,
			@Context String trackingCode) {
		afterToDTO(src, dstBuilder, trackingCode);
	}

	/* ====== CREATE DTO -> ENTITY ====== */

	@Mapping(target = "id", ignore = true)
	@Mapping(target = "remitente", ignore = true)
	@Mapping(target = "destinatario", ignore = true)
	@Mapping(target = "estadoActual", ignore = true)
	@Mapping(target = "trackingCodeHash", ignore = true)
	@Mapping(target = "comprobanteEntrega", ignore = true)
	@Mapping(target = "ruta", ignore = true)
	@Mapping(target = "paquetes", ignore = true)
	@Mapping(target = "historiales", ignore = true)
	Envio toEntity(EnvioCreateDTO dto);

	/* ====== Helpers ====== */
	default List<Long> mapEnviosIds(List<ar.edu.unju.fi.logistica.domain.Envio> envios) {
		if (envios == null)
			return List.of();
		return envios.stream().map(Envio::getId).toList();
	}

	/**
	 * Convierte la colección de historiales del Envío a DTOs respetando LAZY (si ya
	 * está inicializada).
	 */
	default List<HistorialEstadoEnvioDTO> mapHistorial(Envio src) {
		if (src == null || src.getHistoriales() == null)
			return List.of();
		// Si preferís MapStruct puro, podés inyectar HistorialEstadoEnvioMapper y
		// llamar a su toDTOs(...)
		return src.getHistoriales().stream().map(h -> new HistorialEstadoEnvioDTO(h.getId(), h.getEnvio().getId(),
				h.getEstadoAnterior(), h.getEstadoNuevo(), h.getFechaHora(), h.getObservacion())).toList();
	}
}
