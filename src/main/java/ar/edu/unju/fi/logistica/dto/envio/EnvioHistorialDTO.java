package ar.edu.unju.fi.logistica.dto.envio;

import java.util.List;

import ar.edu.unju.fi.logistica.dto.historial.HistorialEstadoEnvioDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class EnvioHistorialDTO extends EnvioDTO {
	@Schema(description = "Historial cronológico de cambios de estado del envío.")
	private List<HistorialEstadoEnvioDTO> historial;
}
