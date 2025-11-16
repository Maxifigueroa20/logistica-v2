package ar.edu.unju.fi.logistica.dto.paquete;

import ar.edu.unju.fi.logistica.enums.NivelFragilidad;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.SuperBuilder;

/** DTO de lectura para Paquete Frágil. */
@Getter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Detalle de un paquete frágil registrado")
public class PaqueteFragilDTO extends PaqueteDTO {
	@Schema(example = "ALTA", description = "Nivel de fragilidad")
	private NivelFragilidad nivelFragilidad;
	@Schema(example = "true", description = "Indica si cuenta con seguro adicional")
	private boolean seguroAdicional;
}
