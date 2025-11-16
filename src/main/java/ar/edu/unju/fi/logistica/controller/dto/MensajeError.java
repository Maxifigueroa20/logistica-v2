package ar.edu.unju.fi.logistica.controller.dto;

import java.time.OffsetDateTime;

public record MensajeError(String message, String path, int status, OffsetDateTime timestamp) { }
