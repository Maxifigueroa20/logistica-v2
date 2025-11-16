package ar.edu.unju.fi.logistica.dto.historial;

import java.time.LocalDateTime;

import ar.edu.unju.fi.logistica.enums.EstadoEnvio;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.SuperBuilder;

/** DTO de lectura del historial de cambios de estado de un Envío. */
@Getter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class HistorialEstadoEnvioDTO {
	@Schema(description = "ID del registro del historial", example = "501")
	Long id;
	@Schema(description = "ID del envío asociado", example = "12")
	Long envioId;
	@Schema(description = "Estado anterior del envío", example = "EN_ALMACEN")
	EstadoEnvio estadoAnterior;
	@Schema(description = "Estado nuevo del envío", example = "EN_RUTA")
	EstadoEnvio estadoNuevo;
	@Schema(description = "Momento exacto en que ocurrió la transición de estado", example = "2025-11-10T09:15:00")
	LocalDateTime fechaHora;
	@Schema(description = "Observación o detalle informativo asociado al cambio de estado", example = "Salida del centro logístico")
	String observacion;
}
