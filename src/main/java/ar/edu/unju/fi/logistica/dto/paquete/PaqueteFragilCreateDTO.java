package ar.edu.unju.fi.logistica.dto.paquete;

import ar.edu.unju.fi.logistica.enums.NivelFragilidad;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/** Create DTO para Paquete Frágil. */
@Data
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(description = """
		Datos para registrar un paquete frágil. Además del peso y volumen,
		se especifica su nivel de fragilidad y si requiere seguro adicional.
		""")
public class PaqueteFragilCreateDTO extends PaqueteCreateDTO {
	@NotNull
	@Schema(example = "BAJA", description = "Nivel de fragilidad del paquete (BAJA, MEDIA, ALTA).")
	private NivelFragilidad nivelFragilidad;
	@Schema(example = "false", description = "Indica si el paquete cuenta con seguro adicional.")
	private boolean seguroAdicional;
}
