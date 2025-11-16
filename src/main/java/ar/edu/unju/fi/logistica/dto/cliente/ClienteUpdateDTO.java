package ar.edu.unju.fi.logistica.dto.cliente;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import lombok.*;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClienteUpdateDTO {
	@Pattern(regexp = "\\+?\\d{7,15}", message = "Formato de teléfono inválido.")
	@Schema(description = "Nuevo número de teléfono. Si no se envía, no se modifica.", example = "+543884009999")
	private String telefono;
	@Email(message = "Correo inválido")
	@Schema(description = "Nuevo correo electrónico. Opcional.", example = "nuevo_correo@example.com")
	private String email;
	@Schema(description = "Nueva dirección principal del cliente.", example = "Calle Belgrano 512")
	private String direccionPrincipal;
	@Schema(description = "Nuevo código postal asociado a la dirección.", example = "4605")
	private String codigoPostal;
}
