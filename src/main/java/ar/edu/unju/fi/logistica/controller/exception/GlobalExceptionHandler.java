package ar.edu.unju.fi.logistica.controller.exception;

import java.time.OffsetDateTime;
import java.util.stream.Collectors;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.ErrorResponseException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import ar.edu.unju.fi.logistica.controller.dto.MensajeError;
import ar.edu.unju.fi.logistica.exception.ClienteException;
import ar.edu.unju.fi.logistica.exception.EnvioException;
import ar.edu.unju.fi.logistica.exception.RutaException;
import ar.edu.unju.fi.logistica.exception.VehiculoException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

	private MensajeError body(String msg, HttpStatus st, HttpServletRequest req) {
		return new MensajeError(msg, req.getRequestURI(), st.value(), OffsetDateTime.now());
	}

	private static boolean isNotFound(String m) {
		return m != null && m.toLowerCase().contains("no encontrad");
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	ResponseEntity<MensajeError> beanValidation(MethodArgumentNotValidException ex, HttpServletRequest req) {
		String msg = ex.getBindingResult().getFieldErrors().stream()
				.map(fe -> fe.getField() + ": " + fe.getDefaultMessage()).distinct().collect(Collectors.joining("; "));
		return ResponseEntity.badRequest().body(body(msg, HttpStatus.BAD_REQUEST, req));
	}

	@ExceptionHandler(ConstraintViolationException.class)
	ResponseEntity<MensajeError> constraint(ConstraintViolationException ex, HttpServletRequest req) {
		String msg = ex.getConstraintViolations().stream().map(v -> v.getPropertyPath() + ": " + v.getMessage())
				.collect(Collectors.joining("; "));
		return ResponseEntity.badRequest().body(body(msg, HttpStatus.BAD_REQUEST, req));
	}

	@ExceptionHandler({ EnvioException.class, RutaException.class, ClienteException.class, VehiculoException.class })
	ResponseEntity<MensajeError> negocio(RuntimeException ex, HttpServletRequest req) {
		HttpStatus st = isNotFound(ex.getMessage()) ? HttpStatus.NOT_FOUND : HttpStatus.BAD_REQUEST;
		return ResponseEntity.status(st).body(body(ex.getMessage(), st, req));
	}

	@ExceptionHandler(DataIntegrityViolationException.class)
	ResponseEntity<MensajeError> conflict(DataIntegrityViolationException ex, HttpServletRequest req) {
		return ResponseEntity.status(HttpStatus.CONFLICT)
				.body(body("Conflicto de datos (duplicado o restricción)", HttpStatus.CONFLICT, req));
	}

	@ExceptionHandler({ ResponseStatusException.class, ErrorResponseException.class })
	ResponseEntity<MensajeError> responseStatus(Exception ex, HttpServletRequest req) {
		HttpStatus st;
		String msg;
		if (ex instanceof ResponseStatusException rse) {
			st = HttpStatus.valueOf(rse.getStatusCode().value());
			msg = rse.getReason();
		} else {
			var ere = (ErrorResponseException) ex;
			st = HttpStatus.valueOf(ere.getStatusCode().value());
			msg = ere.getBody() != null ? ere.getBody().getDetail() : ere.getMessage();
		}
		return ResponseEntity.status(st).body(body(msg, st, req));
	}

	@ExceptionHandler(Exception.class)
	ResponseEntity<MensajeError> fallback(Exception ex, HttpServletRequest req) {
		log.error("[API] 500", ex);
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(body("Error interno inesperado", HttpStatus.INTERNAL_SERVER_ERROR, req));
	}
}
