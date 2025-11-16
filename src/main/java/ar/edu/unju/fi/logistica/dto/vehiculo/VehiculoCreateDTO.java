package ar.edu.unju.fi.logistica.dto.vehiculo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.*;

/**
 * DTO de entrada para alta de Vehículos. Nota: si {@code refrigerado = true},
 * validar en Service que {@code rangoTempMin/Max} no sean nulos.
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Alta de vehículo de transporte utilizado en las rutas")
public class VehiculoCreateDTO {
	@NotBlank
	@Schema(example = "AA123BB", description = "Patente única del vehículo")
	private String patente;
	@Positive
	@Schema(example = "5000", description = "Capacidad máxima de carga en kilogramos")
	private double capacidadPesoKg;
	@Positive
	@Schema(example = "15", description = "Capacidad máxima de volumen en metros cúbicos o dm³ equivalentes según el modelado")
	private double capacidadVolumenDm3;
	@Schema(example = "true", description = "Indica si el vehículo cuenta con sistema de refrigeración")
	private boolean refrigerado;
	@Schema(example = "-2.0", description = "Temperatura mínima que puede mantener el sistema de frío (si aplica)")
	private Double rangoTempMin;
	@Schema(example = "6.0", description = "Temperatura máxima que puede mantener el sistema de frío (si aplica)")
	private Double rangoTempMax;
}
