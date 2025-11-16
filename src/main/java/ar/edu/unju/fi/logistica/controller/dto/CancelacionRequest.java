package ar.edu.unju.fi.logistica.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record CancelacionRequest(@Schema(example = "Cliente solicitó cancelar el envío") String motivo) { }
