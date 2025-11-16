package ar.edu.unju.fi.logistica.controller;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import ar.edu.unju.fi.logistica.dto.cliente.ClienteCreateDTO;
import ar.edu.unju.fi.logistica.dto.cliente.ClienteDTO;
import ar.edu.unju.fi.logistica.dto.cliente.ClienteUpdateDTO;
import ar.edu.unju.fi.logistica.service.ClienteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/clientes")
@Tag(name = "Clientes", description = "Gestión de clientes (remitentes y destinatarios) y búsquedas por documento/CUIT.")
@RequiredArgsConstructor
public class ClienteController {

	private final ClienteService clienteService;

	@Operation(summary = "Crear cliente", description = """
			Registra un nuevo cliente (remitente o destinatario) en el sistema.
			El documento/CUIT debe ser único: si ya existe, la operación será rechazada.
			""", responses = {
			@ApiResponse(responseCode = "201", description = "Cliente creado correctamente", content = @Content(schema = @Schema(implementation = ClienteDTO.class))),
			@ApiResponse(responseCode = "400", description = "Datos inválidos (errores de validación en el cuerpo de la petición)", content = @Content),
			@ApiResponse(responseCode = "409", description = "Ya existe un cliente con el mismo documento/CUIT", content = @Content) })
	@PostMapping
	public ResponseEntity<ClienteDTO> crear(@Valid @RequestBody ClienteCreateDTO dto) {
		log.info("[API][Clientes] POST /api/clientes → nombre='{}', doc={}", dto.getNombreRazonSocial(),
				dto.getDocumentoCuit());
		ClienteDTO creado = clienteService.crear(dto);
		return ResponseEntity.created(URI.create("/api/clientes/" + creado.getId())).body(creado);
	}

	@Operation(summary = "Actualizar datos de contacto y dirección de un cliente", description = """
			Permite actualizar teléfono, email y domicilio de un cliente existente.
			El documento/CUIT no se modifica mediante este endpoint.
			""", responses = {
			@ApiResponse(responseCode = "200", description = "Cliente actualizado correctamente", content = @Content(schema = @Schema(implementation = ClienteDTO.class))),
			@ApiResponse(responseCode = "400", description = "Datos inválidos (errores de validación en el cuerpo de la petición)", content = @Content),
			@ApiResponse(responseCode = "404", description = "No se encontró un cliente con el identificador indicado", content = @Content) })
	@PutMapping("/{id}")
	public ResponseEntity<ClienteDTO> actualizar(@PathVariable Long id, @Valid @RequestBody ClienteUpdateDTO dto) {

		log.info("[API][Clientes] PUT /api/clientes/{}", id);
		return ResponseEntity.ok(clienteService.actualizar(id, dto));
	}

	@Operation(summary = "Obtener un cliente por su ID interno", description = "Devuelve todos los datos de un cliente a partir de su identificador interno.", responses = {
			@ApiResponse(responseCode = "200", description = "Cliente encontrado", content = @Content(schema = @Schema(implementation = ClienteDTO.class))),
			@ApiResponse(responseCode = "404", description = "No se encontró un cliente con el ID indicado", content = @Content) })
	@GetMapping("/{id}")
	public ResponseEntity<ClienteDTO> buscarPorId(@PathVariable Long id) {
		log.info("[API][Clientes] GET /api/clientes/{}", id);
		return ResponseEntity.ok(clienteService.buscarPorId(id));
	}

	@Operation(summary = "Listar clientes (con búsqueda opcional por documento/CUIT)", description = """
            Si no se envía el parámetro 'documento', devuelve el listado completo de clientes.
            Si se envía, devuelve solo los clientes cuyo documento/CUIT contenga el valor indicado.
            Esto permite usarlo como buscador/autocompletar en un formulario.
            """, responses = {
			@ApiResponse(responseCode = "200", description = "Listado de clientes (completo o filtrado)", content = @Content(schema = @Schema(implementation = ClienteDTO.class))) })
	@GetMapping
	public ResponseEntity<List<ClienteDTO>> listar(
			@Parameter(description = "Documento/CUIT completo o parcial. Ej: '307000', '2330', etc.", example = "30333333", required = false) @RequestParam(required = false) String documento) {

		log.info("[API][Clientes] GET /api/clientes → documento='{}'", documento);
		if (documento == null || documento.isBlank())
			return ResponseEntity.ok(clienteService.listar());

		return ResponseEntity.ok(clienteService.buscarPorDocumentoLike(documento));
	}
}
