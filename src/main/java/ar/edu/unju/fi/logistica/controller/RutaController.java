package ar.edu.unju.fi.logistica.controller;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import ar.edu.unju.fi.logistica.controller.dto.AsignarEnviosRequest;
import ar.edu.unju.fi.logistica.controller.dto.QuitarEnviosRequest;
import ar.edu.unju.fi.logistica.dto.envio.EnvioDTO;
import ar.edu.unju.fi.logistica.dto.ruta.RutaCreateDTO;
import ar.edu.unju.fi.logistica.dto.ruta.RutaDTO;
import ar.edu.unju.fi.logistica.service.EnvioService;
import ar.edu.unju.fi.logistica.service.RutaService;
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
@RequestMapping("/api/rutas")
@Tag(name = "Rutas", description = "Operaciones sobre rutas logísticas (asignación de envíos a vehículos por día).")
@RequiredArgsConstructor
public class RutaController {

	private final RutaService rutaService;
	private final EnvioService envioService;

	@Operation(summary = "Crear ruta", description = """
			Crea una ruta logística para una fecha y un vehículo (identificado por su patente).
			La ruta representa el recorrido que hará un vehículo en un día determinado.
			No es obligatorio asignar envíos en este momento: pueden agregarse más adelante.
			""", responses = {
			@ApiResponse(responseCode = "201", description = "Ruta creada correctamente", content = @Content(schema = @Schema(implementation = RutaDTO.class))),
			@ApiResponse(responseCode = "400", description = "Datos inválidos o fecha/patente mal informadas", content = @Content),
			@ApiResponse(responseCode = "409", description = "Ya existe una ruta para ese vehículo en la fecha indicada", content = @Content) })
	@PostMapping
	public ResponseEntity<RutaDTO> crear(@Valid @RequestBody RutaCreateDTO dto) {
		log.info("[API][Rutas] POST /api/rutas → fecha={}, vehiculoPatente={}", dto.getFecha(),
				dto.getVehiculoPatente());
		RutaDTO creado = rutaService.crear(dto);
		return ResponseEntity.created(URI.create("/api/rutas/" + creado.getId())).body(creado);
	}

	@Operation(summary = "Obtener una ruta por ID", description = "Devuelve toda la información de una ruta a partir de su identificador interno.")
	@ApiResponse(responseCode = "200", description = "Ruta encontrada", content = @Content(schema = @Schema(implementation = RutaDTO.class)))
	@GetMapping("/{id}")
	public ResponseEntity<RutaDTO> buscarPorId(@PathVariable Long id) {
		log.info("[API][Rutas] GET /api/rutas/{}", id);
		return ResponseEntity.ok(rutaService.buscarPorId(id));
	}

	@Operation(summary = "Listar rutas (con filtro opcional por fecha)", description = """
			Devuelve el listado de rutas registradas.
			Si se envía el parámetro 'fecha', solo devuelve las rutas correspondientes a ese día.
			Si no se envía, devuelve todas las rutas.
			""")
	@GetMapping
	public ResponseEntity<List<RutaDTO>> listar(
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @Parameter(description = "Fecha de la ruta a filtrar. Si se omite, se listan todas las rutas.", example = "2025-11-10") LocalDate fecha) {
		log.info("[API][Rutas] GET /api/rutas → fecha={}", fecha);
		return ResponseEntity.ok(rutaService.listar(fecha));
	}

	@Operation(summary = "Asignar uno o varios envíos a una ruta", description = """
			Asigna una colección de envíos a la ruta indicada.
			Antes de asignarlos se valida:
			- que el vehículo tenga capacidad suficiente de peso y volumen, y
			- que sea compatible con los paquetes refrigerados, en caso de que existan.
			Si alguna validación falla, la operación se rechaza.
			""")
	@PostMapping("/{rutaId}/envios")
	public ResponseEntity<RutaDTO> asignarEnvios(@PathVariable Long rutaId,
			@Valid @RequestBody AsignarEnviosRequest req) {
		log.info("[API][Rutas] POST /api/rutas/{}/envios → cantidadEnvios={}", rutaId,
				req.enviosIds() != null ? req.enviosIds().size() : 0);
		RutaDTO ruta = rutaService.asignarEnvios(rutaId, req.enviosIds());
		return ResponseEntity.ok(ruta);
	}

	@Operation(summary = "Quitar uno o varios envíos de una ruta", description = """
			Quita los envíos indicados de la ruta.
			Opcionalmente se puede enviar una observación para dejar registro del motivo
			(por ejemplo, reprogramación, error de carga, cancelación, etc.).
			""")
	@PostMapping("/{rutaId}/envios/quitar")
	public ResponseEntity<RutaDTO> quitarEnvios(@PathVariable Long rutaId,
			@Valid @RequestBody QuitarEnviosRequest req) {
		boolean obsPresent = (req.observacion() != null && !req.observacion().isBlank());
		log.info("[API][Rutas] POST /api/rutas/{}/envios/quitar → cantidadEnvios={}, obsPresent={}", rutaId,
				req.enviosIds() != null ? req.enviosIds().size() : 0, obsPresent);
		RutaDTO ruta = rutaService.quitarEnvios(rutaId, req.enviosIds(), req.observacion());
		return ResponseEntity.ok(ruta);
	}

	@Operation(summary = "Consultar envíos asignados a una ruta en una fecha", description = """
			Devuelve el detalle de los envíos asociados a la ruta indicada,
			pero solo si coinciden con la fecha solicitada.
			Si la ruta existe pero la fecha no coincide, se devuelve una lista vacía.
			""")
	@GetMapping("/{rutaId}/envios")
	public ResponseEntity<List<EnvioDTO>> enviosDeRutaEnFecha(@PathVariable Long rutaId,
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @Parameter(description = "Fecha de la ruta a consultar", example = "2025-11-20") LocalDate fecha) {
		log.info("[API][Rutas] GET /api/rutas/{}/envios?fecha={}", rutaId, fecha);
		var ids = rutaService.listarEnviosIdsDeRutaEnFecha(rutaId, fecha);
		var dtos = ids.stream().map(envioService::buscarPorId).toList();
		return ResponseEntity.ok(dtos);
	}
}
