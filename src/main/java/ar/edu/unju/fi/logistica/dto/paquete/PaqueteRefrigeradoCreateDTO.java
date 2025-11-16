package ar.edu.unju.fi.logistica.dto.paquete;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.SuperBuilder;

/** Create DTO para Paquete Refrigerado. */
@Getter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(description = """
		Datos para registrar un paquete que requiere conservación térmica.
		Se deben enviar la temperatura objetivo y el rango permitido.
		""")
public class PaqueteRefrigeradoCreateDTO extends PaqueteCreateDTO {
	@NotNull
	@Schema(example = "4.0", description = "Temperatura objetivo del paquete (°C).")
	private double temperaturaObjetivo;
	@NotNull
	@Schema(example = "2.0", description = "Temperatura mínima permitida (°C).")
	private double rangoMin;
	@NotNull
	@Schema(example = "8.0", description = "Temperatura máxima permitida (°C).")
	private double rangoMax;
	@Schema(example = "2", description = "Cantidad máxima de horas que puede permanecer fuera de refrigeración.")
	private int horasMaxFueraFrio;
}
