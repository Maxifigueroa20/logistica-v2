package ar.edu.unju.fi.logistica.dto.cliente;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;

/**
 * DTO de entrada para registrar/actualizar Clientes.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClienteCreateDTO {
	@NotBlank
	@Schema(description = "Nombre completo de la persona o razón social de la empresa.", example = "Mariano Márquez")
	private String nombreRazonSocial;
	@NotBlank
	@Schema(description = "Documento o CUIT del cliente. Debe ser único en la base de datos.", example = "20123456789")
	private String documentoCuit;
	@Pattern(regexp = "\\+?\\d{7,15}", message = "Formato de teléfono inválido. Use solo dígitos opcionalmente con '+' inicial.")
	@Schema(description = "Teléfono de contacto. Puede incluir prefijo internacional.", example = "+543884001123")
	private String telefono;
	@Email(message = "Correo inválido")
	@Schema(description = "Correo electrónico del cliente.", example = "cliente@example.com")
	private String email;
	@NotBlank
	@Schema(description = "Dirección principal del cliente. Se utiliza por defecto como punto de retiro o entrega.", example = "Av. Sarmiento 123")
	private String direccionPrincipal;
	@NotBlank
	@Schema(description = "Código postal asociado a la dirección principal.", example = "4600")
	private String codigoPostal;
}
