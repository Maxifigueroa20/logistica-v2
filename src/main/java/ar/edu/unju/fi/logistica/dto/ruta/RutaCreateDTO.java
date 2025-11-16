package ar.edu.unju.fi.logistica.dto.ruta;

import java.time.LocalDate;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

/**
 * DTO de entrada para crear una Ruta. Se permite opcionalmente enviar una lista
 * inicial de envíos a asignar.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Solicitud de creación de ruta diaria para un vehículo")
public class RutaCreateDTO {
	@NotNull
	@FutureOrPresent
	@Schema(example = "2025-11-20", description = "Fecha en la que el vehículo realizará la ruta")
	private LocalDate fecha;

	@NotBlank
	@Schema(example = "CC333CC", description = "Patente del vehículo ya registrado que se asignará a la ruta")
	private String vehiculoPatente;
}
