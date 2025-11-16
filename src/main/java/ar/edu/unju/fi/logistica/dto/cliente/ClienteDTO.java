package ar.edu.unju.fi.logistica.dto.cliente;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

/** DTO de lectura de Cliente. */
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClienteDTO {
	@Schema(description = "Identificador único del cliente.", example = "10")
	private Long id;

	@Schema(description = "Nombre o razón social registrada.", example = "EJESA SA")
	private String nombreRazonSocial;

	@Schema(description = "Documento/CUIT del cliente.", example = "30700123456")
	private String documentoCuit;

	@Schema(description = "Teléfono de contacto.", example = "+543884001122")
	private String telefono;

	@Schema(description = "Correo electrónico de contacto.", example = "contacto@ejesa.com")
	private String email;

	@Schema(description = "Dirección principal declarada.", example = "Av. Sarmiento 123")
	private String direccionPrincipal;

	@Schema(description = "Código postal asociado a la dirección.", example = "4600")
	private String codigoPostal;
}
