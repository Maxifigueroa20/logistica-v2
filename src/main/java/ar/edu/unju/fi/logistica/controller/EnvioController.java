package ar.edu.unju.fi.logistica.controller;

import java.net.URI;
import java.util.Base64;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import ar.edu.unju.fi.logistica.controller.dto.CambioEstadoRequest;
import ar.edu.unju.fi.logistica.controller.dto.CancelacionRequest;
import ar.edu.unju.fi.logistica.controller.dto.EntregaRequest;
import ar.edu.unju.fi.logistica.dto.envio.EnvioCreateDTO;
import ar.edu.unju.fi.logistica.dto.envio.EnvioDTO;
import ar.edu.unju.fi.logistica.dto.envio.EnvioHistorialDTO;
import ar.edu.unju.fi.logistica.dto.historial.HistorialEstadoEnvioDTO;
import ar.edu.unju.fi.logistica.enums.EstadoEnvio;
import ar.edu.unju.fi.logistica.service.EnvioService;
import ar.edu.unju.fi.logistica.service.HistorialEstadoEnvioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/envios")
@Tag(name = "Envíos", description = "Operaciones sobre envíos")
@RequiredArgsConstructor
public class EnvioController {

	private final EnvioService envioService;
	private final HistorialEstadoEnvioService historialService;

	@Operation(summary = "Crear un envío", description = """
			Crea un nuevo envío a partir de:

			- Un remitente y un destinatario ya registrados en el sistema, identificados por su documento/CUIT.
			- Una lista de códigos de paquete ya cargados previamente mediante el módulo de Paquetes.

			Por convención, en los datos de ejemplo:
			- Los paquetes frágiles tienen códigos que comienzan con "PF-" (por ejemplo: PF-001, PF-010).
			- Los paquetes refrigerados tienen códigos que comienzan con "PR-" (por ejemplo: PR-001, PR-005).

			El envío queda inicialmente en estado GENERADO y se le asigna un código de seguimiento público
			(por ejemplo: ICG-AB12CD34EF56) que luego puede utilizar el cliente final para consultar el estado.
			""", requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true, content = @Content(mediaType = "application/json", schema = @Schema(implementation = EnvioCreateDTO.class), examples = {
			@ExampleObject(name = "Envío solo con paquetes frágiles", value = """
					{
					  "remitenteDocumento": "30700000022",
					  "destinatarioDocumento": "30222222",
					  "direccionEntrega": "Av. Sarmiento 123",
					  "codigoPostal": "4600",
					  "codigosPaquete": ["PF-001", "PF-002"]
					}
					"""), @ExampleObject(name = "Envío mixto (frágil + refrigerado)", value = """
					{
					  "remitenteDocumento": "30700000022",
					  "destinatarioDocumento": "20388888",
					  "direccionEntrega": "Belgrano 450",
					  "codigoPostal": "4600",
					  "codigosPaquete": ["PF-003", "PR-021"]
					}
					""") })), responses = {
					@ApiResponse(responseCode = "201", description = "Envío creado correctamente", content = @Content(schema = @Schema(implementation = EnvioDTO.class))),
					@ApiResponse(responseCode = "400", description = "Datos inválidos (documentos inexistentes, códigos de paquete inexistentes, lista vacía, etc.)", content = @Content) })
	@PostMapping
	public ResponseEntity<EnvioDTO> crear(@Valid @RequestBody EnvioCreateDTO dto) {
		int cantCodigos = (dto.getCodigosPaquete() != null) ? dto.getCodigosPaquete().size() : 0;
		log.info("[API][Envios] POST /api/envios → remDoc={}, destDoc={}, codigosPaquete={}",
				dto.getRemitenteDocumento(), dto.getDestinatarioDocumento(), cantCodigos);

		EnvioDTO creado = envioService.crear(dto);
		return ResponseEntity.created(URI.create("/api/envios/" + creado.getId())).body(creado);
	}

	@Operation(summary = "Obtener un envío por ID", description = "Devuelve la información completa de un envío a partir de su identificador interno.")
	@ApiResponse(responseCode = "200", description = "Envío encontrado", content = @Content(schema = @Schema(implementation = EnvioDTO.class)))
	@GetMapping("/{id}")
	public ResponseEntity<EnvioDTO> buscarPorId(@PathVariable Long id) {
		log.info("[API][Envios] GET /api/envios/{}", id);
		return ResponseEntity.ok(envioService.buscarPorId(id));
	}

	@Operation(summary = "Buscar un envío por código de tracking (incluye historial)", description = """
			Permite consultar un envío a partir del código de seguimiento público
			que se entrega al cliente final (por ejemplo: ICG-AB12CD34EF56).

			La respuesta incluye tanto los datos del envío como toda la traza de cambios de estado
			(historial cronológico).
			""", parameters = @Parameter(name = "tracking", example = "ICG-AB12CD34EF56", description = "Código de seguimiento entregado al cliente"))
	@ApiResponse(responseCode = "200", description = "Envío encontrado para el código de tracking indicado", content = @Content(schema = @Schema(implementation = EnvioHistorialDTO.class)))
	@GetMapping(path = "/seguimiento", params = "tracking")
	public ResponseEntity<EnvioHistorialDTO> buscarPorTracking(@RequestParam String tracking) {
		log.info("[API][Envios] GET /api/envios/seguimiento?tracking={}", tracking);
		return ResponseEntity.ok(envioService.buscarPorTracking(tracking));
	}

	@Operation(summary = "Consulta unificada de envíos", description = """
			Permite listar envíos aplicando filtros opcionales sobre:

			- documento/CUIT del remitente
			- documento/CUIT del destinatario
			- estado actual del envío

			Si no se envía ningún parámetro, devuelve todos los envíos.
			""")
	@GetMapping
	public ResponseEntity<List<EnvioDTO>> buscarFiltrado(
			@RequestParam(required = false) @Parameter(description = "Documento/CUIT del remitente ya registrado", example = "20123456789") String remitenteDocumento,
			@RequestParam(required = false) @Parameter(description = "Documento/CUIT del destinatario ya registrado", example = "20987654321") String destinatarioDocumento,
			@RequestParam(required = false) @Parameter(description = "Estado del envío", example = "EN_RUTA") EstadoEnvio estado) {
		log.info("[API][Envios] GET /api/envios → remDoc={}, destDoc={}, estado={}", remitenteDocumento,
				destinatarioDocumento, estado);
		var lista = envioService.buscarFiltrado(remitenteDocumento, destinatarioDocumento, estado);
		return ResponseEntity.ok(lista);
	}

	@Operation(summary = "Actualizar estado de un envío (flujo normal, sin ENTREGADO ni CANCELADO)", description = """
			Cambia el estado del envío según el flujo permitido:
			- GENERADO → EN_ALMACEN
			- EN_ALMACEN → EN_RUTA
			- EN_RUTA → DEVUELTO

			Para ENTREGADO use /{id}/entrega.
			Para CANCELADO use /{id}/cancelar.
			""")
	@PatchMapping("/{id}/estado")
	public ResponseEntity<EnvioDTO> actualizarEstado(@PathVariable Long id,
			@Valid @RequestBody CambioEstadoRequest req) {
		log.info("[API][Envios] PATCH /api/envios/{}/estado → nuevoEstado={}, obsPresent={}", id, req.nuevoEstado(),
				(req.observacion() != null && !req.observacion().isBlank()));
		return ResponseEntity.ok(envioService.actualizarEstado(id, req.nuevoEstado(), req.observacion()));
	}

	@Operation(summary = "Registrar ingreso del envío a almacén", description = """
			Marca el envío como EN_ALMACEN a partir de su estado actual GENERADO.

			Uso típico:
			- Luego de crear el envío (estado GENERADO),
			  el operador registra el ingreso físico al almacén
			  mediante este endpoint.

			Si el envío no está en estado GENERADO, se devolverá un error
			de negocio (no se permite la transición).
			""")
	@PostMapping("/{id}/almacen")
	public ResponseEntity<EnvioDTO> ingresoAlmacen(@PathVariable Long id) {
		String observacion = "Ingreso a almacén";
		log.info("[API][Envios] POST /api/envios/{}/almacen → obs='{}'", id, observacion);

		var dto = envioService.actualizarEstado(id, EstadoEnvio.EN_ALMACEN, observacion);
		return ResponseEntity.ok(dto);
	}

	@Operation(summary = "Marcar entrega con comprobante (Base64 de PDF/JPG)", description = """
			Marca el envío como ENTREGADO y guarda el comprobante asociado.

			El comprobante se envía en formato Base64 (por ejemplo, una imagen JPG o un PDF),
			se decodifica en el servidor y se almacena de forma asociada al envío.
			""")
	@PostMapping("/{id}/entrega")
	public ResponseEntity<EnvioDTO> marcarEntregado(@PathVariable Long id, @Valid @RequestBody EntregaRequest req) {
		int length = (req.comprobanteBase64() != null) ? req.comprobanteBase64().length() : 0;
		log.info("[API][Envios] POST /api/envios/{}/entrega → base64Length={}", id, length);

		byte[] bytes = null;
		if (req.comprobanteBase64() != null && !req.comprobanteBase64().isBlank()) {
			try {
				bytes = Base64.getDecoder().decode(req.comprobanteBase64());
			} catch (IllegalArgumentException e) {
				throw new IllegalArgumentException("Comprobante Base64 inválido");
			}
		}

		return ResponseEntity.ok(envioService.marcarEntregado(id, bytes));
	}

	@Operation(summary = "Cancelar un envío", description = """
			Cancela un envío que todavía no ha sido entregado.
			Se puede enviar un motivo opcional que quedará registrado en el historial.
			""")
	@PostMapping("/{id}/cancelar")
	public ResponseEntity<EnvioDTO> cancelar(@PathVariable Long id,
			@RequestBody(required = false) CancelacionRequest req) {
		log.info("[API][Envios] POST /api/envios/{}/cancelar → motivoPresent={}", id,
				(req != null && req.motivo() != null && !req.motivo().isBlank()));
		String motivo = (req != null) ? req.motivo() : null;
		return ResponseEntity.ok(envioService.cancelar(id, motivo));
	}

	@Operation(summary = "Listar historial de un envío por ID", description = """
			Devuelve la traza completa de cambios de estado del envío, ordenada cronológicamente.
			Esto permite auditar el recorrido de un envío desde que se genera hasta su entrega o cancelación.
			""")
	@GetMapping("/{id}/historial")
	public ResponseEntity<List<HistorialEstadoEnvioDTO>> historial(@PathVariable Long id) {
		log.info("[API][Envios] GET /api/envios/{}/historial", id);
		return ResponseEntity.ok(historialService.listarPorEnvio(id));
	}
}
