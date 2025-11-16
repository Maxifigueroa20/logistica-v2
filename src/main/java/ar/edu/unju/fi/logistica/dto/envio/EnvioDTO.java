package ar.edu.unju.fi.logistica.dto.envio;

import java.util.List;

import ar.edu.unju.fi.logistica.dto.paquete.PaqueteDTO;
import ar.edu.unju.fi.logistica.enums.EstadoEnvio;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.SuperBuilder;

/** DTO de lectura de Envío con totales y detalle de paquetes. */
@Getter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class EnvioDTO {
	@Schema(description = "Identificador único del envío.", example = "100")
	private Long id;
	@Schema(description = "ID interno del remitente asociado al envío.", example = "1")
	private Long remitenteId;

	@Schema(description = "ID interno del destinatario asociado al envío.", example = "2")
	private Long destinatarioId;

	@Schema(description = "Documento/CUIT del remitente.", example = "20123456789")
	private String remitenteDocumento;

	@Schema(description = "Documento/CUIT del destinatario.", example = "20987654321")
	private String destinatarioDocumento;
	@Schema(description = "Dirección exacta donde se debe entregar el envío.", example = "Av. Sarmiento 123")
	private String direccionEntrega;
	@Schema(description = "Código postal de la dirección de entrega.", example = "4600")
	private String codigoPostal;
	@Schema(description = "Estado actual del envío dentro del flujo logístico.", example = "EN_RUTA")
	private EstadoEnvio estadoActual;
	@Schema(hidden = true, description = "Comprobante de entrega almacenado como binario (PDF/JPG). "
			+ "No se expone directamente en las respuestas.")
	private byte[] comprobanteEntrega;
	@Schema(description = "ID interno de la ruta a la que está asignado el envío, si corresponde.", example = "5")
	private Long rutaId;
	@Schema(description = "Detalle de paquetes que componen el envío.")
	private List<PaqueteDTO> paquetes;

	@Schema(description = "Código público de seguimiento del envío (tracking).", example = "PGM-AB12CD34EF56")
	private String trackingCode;
	@Schema(description = "Indica si el envío requiere cadena de frío (si contiene al menos un paquete refrigerado).", example = "true")
	private boolean requiereFrio;
	@Schema(description = "Indica si el envío tiene un comprobante de entrega asociado.", example = "true")
	private boolean hasComprobante;
}
