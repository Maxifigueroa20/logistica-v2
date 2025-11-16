package ar.edu.unju.fi.logistica.controller;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import ar.edu.unju.fi.logistica.dto.vehiculo.VehiculoCreateDTO;
import ar.edu.unju.fi.logistica.dto.vehiculo.VehiculoDTO;
import ar.edu.unju.fi.logistica.dto.vehiculo.VehiculoSearchCriteria;
import ar.edu.unju.fi.logistica.service.VehiculoService;
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
@RequestMapping("/api/vehiculos")
@Tag(name = "Vehículos", description = "Alta y consultas de vehículos de reparto (capacidad y refrigeración).")
@RequiredArgsConstructor
public class VehiculoController {

	private final VehiculoService vehiculoService;

	@Operation(summary = "Crear vehículo", description = """
			Registra un nuevo vehículo de reparto.
			Si el vehículo es refrigerado, se debe informar el rango de temperatura que soporta.
			La patente no puede estar repetida.
			""", responses = {
			@ApiResponse(responseCode = "201", description = "Vehículo creado correctamente", content = @Content(schema = @Schema(implementation = VehiculoDTO.class))),
			@ApiResponse(responseCode = "400", description = "Datos inválidos (errores de validación en el cuerpo de la petición)", content = @Content),
			@ApiResponse(responseCode = "409", description = "Ya existe un vehículo con la misma patente", content = @Content) })
	@PostMapping
	public ResponseEntity<VehiculoDTO> crear(@Valid @RequestBody VehiculoCreateDTO dto) {
		log.info("[API][Vehiculos] POST /api/vehiculos → patente={}", dto.getPatente());
		VehiculoDTO creado = vehiculoService.crear(dto);
		return ResponseEntity.created(URI.create("/api/vehiculos/" + creado.getId())).body(creado);
	}

	@Operation(summary = "Listar vehículos (con búsqueda opcional por patente)", description = """
			Si no se envía el parámetro 'patente', devuelve todos los vehículos registrados.
			Si se envía, devuelve solo los vehículos cuya patente contenga el texto indicado
			(búsqueda parcial, no exacta). Útil para buscadores o autocompletado.
			""", responses = {
			@ApiResponse(responseCode = "200", description = "Listado de vehículos (completo o filtrado)", content = @Content(schema = @Schema(implementation = VehiculoDTO.class))) })
	@GetMapping
	public ResponseEntity<List<VehiculoDTO>> listar(
			@Parameter(example = "BB222BB", description = "Patente completa o parcial del vehículo. Si se omite, se devuelven todos.", required = false) @RequestParam(required = false) String patente) {
		log.info("[API][Vehiculos] GET /api/vehiculos → patente={}", patente);

		if (patente == null || patente.isBlank())
			return ResponseEntity.ok(vehiculoService.listar());

		return ResponseEntity.ok(vehiculoService.buscarPorPatenteLike(patente));
	}

	@Operation(summary = "Buscar vehículos con filtros avanzados", description = """
			Permite filtrar vehículos combinando distintos criterios.
			Todos los parámetros son opcionales:
			- refrigerado: true/false para indicar si el vehículo debe ser refrigerado.
			- capacidadPesoMin / capacidadVolumenMin: capacidad mínima requerida (peso en kg, volumen en dm3).
			- tempMin / tempMax: rango de temperatura requerido para envíos refrigerados.

			Si no se envía ningún parámetro, el resultado es equivalente a listar todos los vehículos.
			""", responses = {
			@ApiResponse(responseCode = "200", description = "Listado de vehículos que cumplen los filtros solicitados", content = @Content(schema = @Schema(implementation = VehiculoDTO.class))),
			@ApiResponse(responseCode = "400", description = "Parámetros inconsistentes (por ejemplo, tempMin mayor que tempMax)", content = @Content) })
	@GetMapping("/buscar")
	public ResponseEntity<List<VehiculoDTO>> buscar(
			@Parameter(description = "Indica si el vehículo debe ser refrigerado (true/false).") @RequestParam(required = false) Boolean refrigerado,
			@Parameter(description = "Peso mínimo soportado en kg.") @RequestParam(required = false) Double capacidadPesoMin,
			@Parameter(description = "Peso máximoo soportado en kg.") @RequestParam(required = false) Double capacidadPesoMax,
			@Parameter(description = "Volumen mínimo soportado en dm3.") @RequestParam(required = false) Double capacidadVolumenMin,
			@Parameter(description = "Volumen máximo soportado en dm3.") @RequestParam(required = false) Double capacidadVolumenMax,
			@Parameter(description = "Temperatura mínima requerida (solo aplica a vehículos refrigerados).") @RequestParam(required = false) Double tempMin,
			@Parameter(description = "Temperatura máxima requerida (solo aplica a vehículos refrigerados).") @RequestParam(required = false) Double tempMax) {

		// Armamos el criteria a partir de los query params
		VehiculoSearchCriteria criteria = VehiculoSearchCriteria.builder().refrigerado(refrigerado)
				.capacidadPesoMin(capacidadPesoMin).capacidadPesoMax(capacidadPesoMax)
				.capacidadVolumenMin(capacidadVolumenMin).capacidadVolumenMax(capacidadVolumenMax).tempMin(tempMin)
				.tempMax(tempMax).build();

		log.info("[API][Vehiculos] GET /api/vehiculos/buscar → {}", criteria);

		var result = vehiculoService.buscar(criteria);
		return ResponseEntity.ok(result);
	}
}
