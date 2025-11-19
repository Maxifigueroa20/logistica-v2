package ar.edu.unju.fi.logistica.service.impl;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import ar.edu.unju.fi.logistica.domain.Envio;
import ar.edu.unju.fi.logistica.service.EmailService;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementación "mock" de EmailService para el perfil 'test'.
 * No envía correos reales; solo hace log.
 */
@Slf4j
@Service
@Profile("test")
public class NoOpEmailService implements EmailService {

    @Override
    public void enviarEnvioRegistrado(Envio envio, String trackingCodePublic) {
        log.info("[Email-TEST] Simular envío de mail ENVÍO REGISTRADO id={} tracking={}",
                envio.getId(), trackingCodePublic);
    }

    @Override
    public void enviarEnvioEntregado(Envio envio) {
        log.info("[Email-TEST] Simular envío de mail ENVÍO ENTREGADO id={}", envio.getId());
    }
}
