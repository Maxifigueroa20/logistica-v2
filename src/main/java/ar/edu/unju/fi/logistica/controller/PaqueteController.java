package ar.edu.unju.fi.logistica.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import ar.edu.unju.fi.logistica.dto.paquete.PaqueteCreateDTO;
import ar.edu.unju.fi.logistica.dto.paquete.PaqueteDTO;
import ar.edu.unju.fi.logistica.service.PaqueteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/paquetes")
@Tag(name = "Paquetes", description = "Alta y consulta de paquetes frágiles y/o refrigerados.")
@RequiredArgsConstructor
public class PaqueteController {

	private final PaqueteService paqueteService;

	@Operation(summary = "Crear uno o varios paquetes", description = """
			Registra paquetes frágiles o refrigerados en el sistema.

			El cuerpo de la petición es un arreglo (lista) de objetos donde cada elemento indica su tipo:
			- type = "FRAGIL"        → se interpreta como PaqueteFragilCreateDTO
			- type = "REFRIGERADO"   → se interpreta como PaqueteRefrigeradoCreateDTO

			Cada paquete debe incluir:
			- un código único (por ejemplo, en los datos de demo se usan prefijos "PF-" para frágiles y "PR-" para refrigerados),
			- peso en kg,
			- volumen en dm³
			y, según el tipo, los campos adicionales correspondientes.

			Los códigos de paquete que se crean aquí luego se utilizan para armar envíos desde el endpoint de Envíos.
			""", requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true, description = "Lista de paquetes a registrar (frágiles o refrigerados).", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = PaqueteCreateDTO.class)))), responses = {
			@ApiResponse(responseCode = "200", description = "Paquetes creados correctamente", content = @Content(array = @ArraySchema(schema = @Schema(implementation = PaqueteDTO.class)))),
			@ApiResponse(responseCode = "400", description = "Datos inválidos en uno o más paquetes (códigos repetidos, pesos/volúmenes inválidos, etc.)", content = @Content) })
	@PostMapping
	public ResponseEntity<List<PaqueteDTO>> crearLote(@RequestBody List<PaqueteCreateDTO> dtos) {
		log.info("[API][Paquetes] POST /api/paquetes → cantidad={}", dtos != null ? dtos.size() : 0);

		var creados = paqueteService.crearLote(dtos);
		return ResponseEntity.ok(creados);
	}

	@Operation(summary = "Buscar paquetes con filtros opcionales", description = """
			Permite consultar los paquetes registrados aplicando filtros opcionales.

			Parámetros soportados:
			- tipo: FRAGIL | REFRIGERADO (si se omite, se devuelven ambos tipos).
			- pesoMin / pesoMax: rango de peso en kg (incluye los extremos).
			- volMin / volMax: rango de volumen en dm³ (incluye los extremos).

			Si no se envía ningún parámetro, devuelve todos los paquetes.
			""", responses = {
			@ApiResponse(responseCode = "200", description = "Listado de paquetes que cumplen los filtros indicados", content = @Content(array = @ArraySchema(schema = @Schema(implementation = PaqueteDTO.class)))),
			@ApiResponse(responseCode = "400", description = "Parámetros inconsistentes (por ejemplo, pesoMin mayor que pesoMax)", content = @Content) })
	@GetMapping
	public ResponseEntity<List<PaqueteDTO>> buscar(
			@Parameter(description = "Tipo de paquete (FRAGIL | REFRIGERADO). Si se omite, trae todos.", example = "FRAGIL") @RequestParam(required = false) String tipo,
			@Parameter(description = "Peso mínimo en kg (incluido).") @RequestParam(required = false) Double pesoMin,
			@Parameter(description = "Peso máximo en kg (incluido).") @RequestParam(required = false) Double pesoMax,
			@Parameter(description = "Volumen mínimo en dm3 (incluido).") @RequestParam(required = false) Double volMin,
			@Parameter(description = "Volumen máximo en dm3 (incluido).") @RequestParam(required = false) Double volMax) {

		log.info("[API][Paquetes] GET /api/paquetes → tipo={}, peso=[{},{}], vol=[{},{}]", tipo, pesoMin, pesoMax,
				volMin, volMax);

		var result = paqueteService.buscar(tipo, pesoMin, pesoMax, volMin, volMax);
		return ResponseEntity.ok(result);
	}
}
