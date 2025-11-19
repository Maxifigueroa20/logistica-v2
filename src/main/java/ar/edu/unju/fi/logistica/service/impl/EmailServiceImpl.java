package ar.edu.unju.fi.logistica.service.impl;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import ar.edu.unju.fi.logistica.domain.Envio;
import ar.edu.unju.fi.logistica.domain.HistorialEstadoEnvio;
import ar.edu.unju.fi.logistica.enums.EstadoEnvio;
import ar.edu.unju.fi.logistica.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementación "real" del servicio de email.
 * Usa Thymeleaf para renderizar plantillas HTML y se activa en todos los perfiles excepto 'test'.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Profile("!test")
public class EmailServiceImpl implements EmailService {

	private final JavaMailSender mailSender;
	private final TemplateEngine templateEngine;

	@Value("${app.mail.from:no-reply@logistica.local}")
	private String from;

	@Value("${app.tracking.base-url:https://example.com/seguimiento/}")
	private String trackingBaseUrl;

	private static final DateTimeFormatter FECHA_HORA_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

	@Override
	public void enviarEnvioRegistrado(Envio envio, String trackingCodePublic) {
		Set<String> destinatarios = obtenerDestinatarios(envio, false);
		if (destinatarios.isEmpty()) {
			log.info("[Email] Envío GENERADO id={} sin destinatarios con email, se omite envío", envio.getId());
			return;
		}

		Map<String, Object> data = Map.of("numeroEnvio", envio.getId(), "remitente",
				envio.getRemitente() != null ? nullSafe(envio.getRemitente().getNombreRazonSocial()) : "",
				"destinatario",
				envio.getDestinatario() != null ? nullSafe(envio.getDestinatario().getNombreRazonSocial()) : "",
				"direccion", nullSafe(envio.getDireccionEntrega()), "cp", nullSafe(envio.getCodigoPostal()), "tracking",
				trackingCodePublic, "trackingUrl", trackingBaseUrl + trackingCodePublic);

		String htmlBody = procesarTemplate("mail/envio-registrado", data);
		String subject = "[Logística] Envío registrado – Código " + trackingCodePublic;

		enviarCorreo(destinatarios, subject, htmlBody);
	}

	@Override
	public void enviarEnvioEntregado(Envio envio) {
		Set<String> destinatarios = obtenerDestinatarios(envio, false);
		if (destinatarios.isEmpty()) {
			log.info("[Email] Envío ENTREGADO id={} sin destinatarios con email, se omite envío", envio.getId());
			return;
		}

		LocalDateTime fechaEntrega = obtenerFechaEntrega(envio);
		String fechaEntregaStr = fechaEntrega != null ? fechaEntrega.format(FECHA_HORA_FMT) : "No disponible";

		String nombreDest = envio.getDestinatario() != null ? nullSafe(envio.getDestinatario().getNombreRazonSocial())
				: "cliente";

		Map<String, Object> data = Map.of("nombreDest", nombreDest, "numeroEnvio", envio.getId(), "direccion",
				nullSafe(envio.getDireccionEntrega()), "cp", nullSafe(envio.getCodigoPostal()), "fechaEntrega",
				fechaEntregaStr);

		String htmlBody = procesarTemplate("mail/envio-entregado", data);
		String subject = "[Logística] Envío entregado – N° " + envio.getId();

		enviarCorreo(destinatarios, subject, htmlBody);
	}

	// =================== Helpers internos ===================

	private String procesarTemplate(String templateName, Map<String, Object> variables) {
		Context context = new Context();
		context.setVariables(variables);
		return templateEngine.process(templateName, context);
	}

	private Set<String> obtenerDestinatarios(Envio envio, boolean soloDestinatario) {
		Set<String> destinatarios = new HashSet<>();

		if (!soloDestinatario && envio.getRemitente() != null) {
			String emailRem = envio.getRemitente().getEmail();
			if (emailRem != null && !emailRem.isBlank()) {
				destinatarios.add(emailRem.trim());
			}
		}

		if (envio.getDestinatario() != null) {
			String emailDest = envio.getDestinatario().getEmail();
			if (emailDest != null && !emailDest.isBlank()) {
				destinatarios.add(emailDest.trim());
			}
		}

		return destinatarios;
	}

	private LocalDateTime obtenerFechaEntrega(Envio envio) {
		return envio.getHistoriales().stream().filter(h -> h.getEstadoNuevo() == EstadoEnvio.ENTREGADO)
				.map(HistorialEstadoEnvio::getFechaHora).max(LocalDateTime::compareTo).orElse(null);
	}

	private void enviarCorreo(Set<String> destinatarios, String subject, String htmlBody) {
		for (String to : destinatarios) {
			try {
				MimeMessage message = mailSender.createMimeMessage();
				MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");
				helper.setFrom(from, "Logística UNJu - Grupo 02");
				helper.setTo(to);
				helper.setSubject(subject);
				helper.setText(htmlBody, true);

				mailSender.send(message);
                log.info("[Email] Enviado a {} con asunto '{}'", to, subject);
            } catch (MessagingException e) {
                log.error("[Email] Error enviando correo a {}: {}", to, e.getMessage(), e);
            } catch (Exception ex) {
                log.error("[Email] Error general enviando correo a {}: {}", to, ex.getMessage(), ex);
            }
		}
	}

	private String nullSafe(String value) {
		return value != null ? value : "";
	}
}
