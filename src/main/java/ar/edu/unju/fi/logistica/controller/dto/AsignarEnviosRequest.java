package ar.edu.unju.fi.logistica.controller.dto;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;

public record AsignarEnviosRequest(
        @NotEmpty
        @Schema(description = "IDs de envíos a asignar a la ruta", example = "[1, 2, 3]")
        List<Long> enviosIds
) {}
