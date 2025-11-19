package ar.edu.unju.fi.logistica.service;

import ar.edu.unju.fi.logistica.domain.Envio;

/**
 * Servicio para el envío de notificaciones por correo electrónico relacionadas
 * con el ciclo de vida de un Envio.
 */
public interface EmailService {

	/**
	 * Notificación cuando se registra un envío (estado GENERADO).
	 * El trackingCodePublic es el código de seguimiento público que se genera
	 * únicamente al crear el envío.
	 */
	void enviarEnvioRegistrado(Envio envio, String trackingCodePublic);

	/**
	 * Notificación cuando el envío se marca como ENTREGADO.
	 * No incluye código de tracking (por diseño del dominio).
	 */
	void enviarEnvioEntregado(Envio envio);
}
