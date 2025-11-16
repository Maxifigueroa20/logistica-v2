package ar.edu.unju.fi.logistica.dto.paquete;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.SuperBuilder;

/** DTO de lectura para Paquete Refrigerado. */
@Getter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Detalle de un paquete refrigerado registrado")
public class PaqueteRefrigeradoDTO extends PaqueteDTO {
	@Schema(example = "4.0", description = "Temperatura objetivo (°C)")
	private double temperaturaObjetivo;
	@Schema(example = "2.0", description = "Temperatura mínima admisible (°C)")
	private double rangoMin;
	@Schema(example = "8.0", description = "Temperatura máxima admisible (°C)")
	private double rangoMax;
	@Schema(example = "2", description = "Máximo de horas tolerable fuera del sistema de refrigeración")
	private int horasMaxFueraFrio;
}
