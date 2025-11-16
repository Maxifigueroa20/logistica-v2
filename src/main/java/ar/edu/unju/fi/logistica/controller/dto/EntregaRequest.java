package ar.edu.unju.fi.logistica.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record EntregaRequest(
        @Schema(description = "Contenido del comprobante en Base64", example = "iVBORw0KGgoAAAANSUhEUgAA...") String comprobanteBase64) { }
