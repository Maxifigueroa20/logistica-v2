package ar.edu.unju.fi.logistica.dto.vehiculo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.SuperBuilder;

/** DTO de lectura de Vehículo. */
@Getter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Detalle de vehículo disponible para rutas de envío")
public class VehiculoDTO {
	@Schema(example = "3", description = "ID interno del vehículo")
	private Long id;
	@Schema(example = "AA123BB", description = "Patente del vehículo")
	private String patente;
	@Schema(example = "5000", description = "Capacidad máxima de carga en kilogramos")
	private double capacidadPesoKg;
	@Schema(example = "15", description = "Capacidad máxima de volumen en metros cúbicos o dm³ equivalentes")
	private double capacidadVolumenDm3;
	@Schema(example = "true", description = "Indica si el vehículo está preparado para transporte refrigerado")
	private boolean refrigerado;
	@Schema(example = "-2.0", description = "Temperatura mínima soportada por el equipo de frío (si refrigerado = true)")
	private Double rangoTempMin;
	@Schema(example = "6.0", description = "Temperatura máxima soportada por el equipo de frío (si refrigerado = true)")
	private Double rangoTempMax;
}
