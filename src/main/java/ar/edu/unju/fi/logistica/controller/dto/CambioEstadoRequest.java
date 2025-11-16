package ar.edu.unju.fi.logistica.controller.dto;

import ar.edu.unju.fi.logistica.enums.EstadoEnvio;
import io.swagger.v3.oas.annotations.media.Schema;

public record CambioEstadoRequest(
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "EN_ALMACEN") EstadoEnvio nuevoEstado,
        @Schema(example = "Ingreso a depósito") String observacion) { }
