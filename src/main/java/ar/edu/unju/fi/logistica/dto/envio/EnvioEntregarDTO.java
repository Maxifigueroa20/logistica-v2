package ar.edu.unju.fi.logistica.dto.envio;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EnvioEntregarDTO {
	@NotBlank(message = "Debe adjuntar comprobante codificado en Base64")
	@Size(max = 16 * 1024 * 1024, message = "El comprobante Base64 no debe exceder un tamaño razonable")
	@Schema(description = "Contenido en Base64 del comprobante (PDF/JPG). "
			+ "El servicio se encargará de decodificarlo y validar el tamaño real.", example = "JVBERi0xLjMKJ....")
	private byte[] comprobante;
}
