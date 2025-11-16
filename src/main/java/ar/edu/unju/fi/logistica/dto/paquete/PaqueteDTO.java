package ar.edu.unju.fi.logistica.dto.paquete;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * DTO de lectura base de Paquete. Incluye un {@code trackingCode} generado
 * dinámicamente (no persistido).
 */
@Getter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = """
		Representa la información básica de un paquete registrado.
		El detalle específico (frágil o refrigerado) se obtiene en clases hijas.
		""")
public abstract class PaqueteDTO {
	@Schema(example = "10", description = "ID interno del paquete")
	private Long id;
	@Schema(example = "PK-001", description = "Código único del paquete")
	private String codigo;
	@Schema(example = "2.5", description = "Peso total del paquete en kilogramos")
	private double pesoKg;
	@Schema(example = "5.0", description = "Volumen total del paquete en dm³")
	private double volumenDm3;
	@Schema(example = "20", description = "ID del envío asociado. Null si aún no fue asignado.")
	private Long envioId;
}
