package ar.edu.unju.fi.logistica.dto.historial;

import java.time.LocalDateTime;

import ar.edu.unju.fi.logistica.enums.EstadoEnvio;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HistorialEstadoEnvioCreateDTO {
	@NotNull private Long envioId;
	private EstadoEnvio estadoAnterior;
	@NotNull private EstadoEnvio estadoNuevo;
    private LocalDateTime fechaHora;
    private String observacion;
}
