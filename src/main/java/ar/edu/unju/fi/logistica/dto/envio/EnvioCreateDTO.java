package ar.edu.unju.fi.logistica.dto.envio;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;

/**
 * DTO de entrada para registrar Envíos. Se envían los IDs de clientes
 * (remitente/destinatario) y la lista de paquetes.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Solicitud de creación de envío")
public class EnvioCreateDTO {
	@NotBlank
	@Schema(description = "Documento o CUIT del remitente ya registrado en el sistema.", example = "30700000022")
	private String remitenteDocumento;
	@NotBlank
	@Schema(description = "Documento o CUIT del destinatario ya registrado en el sistema.", example = "30222222")
	private String destinatarioDocumento;
	@NotBlank
	@Schema(description = "Dirección completa donde se entregará el envío.", example = "Av. Sarmiento 123")
	private String direccionEntrega;
	@NotBlank
	@Schema(description = "Código postal asociado a la dirección de entrega.", example = "4600")
	private String codigoPostal;
	@NotEmpty(message = "Debe incluir al menos un código de paquete")
	@Valid
	@Schema(description = "Listado de códigos de paquetes ya cargados en el sistema que formarán parte del envío.", example = "[\"PK-001\", \"PK-002\"]")
	private List<String> codigosPaquete;
}
