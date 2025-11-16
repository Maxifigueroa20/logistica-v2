package ar.edu.unju.fi.logistica.dto.ruta;

import java.time.LocalDate;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.SuperBuilder;

/** DTO de lectura de Ruta con totales agregados. */
@Getter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Detalle de ruta logística con envíos asociados")
public class RutaDTO {
	@Schema(example = "7", description = "ID interno de la ruta")
	private Long id;
	@Schema(example = "2025-11-10", description = "Fecha programada de la ruta")
	private LocalDate fecha;
	@Schema(example = "3", description = "ID interno del vehículo asignado a la ruta")
	private Long vehiculoId;
	@Schema(description = "Listado de IDs de los envíos asignados a esta ruta", example = "[10, 11, 15]")
	private List<Long> enviosIds;
}
