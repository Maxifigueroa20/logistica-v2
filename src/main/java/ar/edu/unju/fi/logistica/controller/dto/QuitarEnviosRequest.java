package ar.edu.unju.fi.logistica.controller.dto;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;

public record QuitarEnviosRequest(
        @NotEmpty
        @Schema(description = "IDs de envíos a quitar de la ruta", example = "[1, 2]")
        List<Long> enviosIds,
        @Schema(description = "Motivo u observación de la modificación de ruta", example = "Reprogramación de entrega")
        String observacion
) {}
