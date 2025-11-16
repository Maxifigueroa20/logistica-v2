package ar.edu.unju.fi.logistica.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;

import ar.edu.unju.fi.logistica.dto.envio.EnvioCreateDTO;
import ar.edu.unju.fi.logistica.dto.historial.HistorialEstadoEnvioDTO;
import ar.edu.unju.fi.logistica.dto.paquete.PaqueteDTO;
import ar.edu.unju.fi.logistica.enums.EstadoEnvio;
import ar.edu.unju.fi.logistica.exception.EnvioException;
import ar.edu.unju.fi.logistica.maintenance.TestMaintenanceService;
import ar.edu.unju.fi.logistica.support.ITBase;
import ar.edu.unju.fi.logistica.utils.TestDataFactory;

@Tag("service")
@DisplayName("EnvioService – Flujo de creación, tracking y comprobante")
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
class EnvioServiceIT extends ITBase {

	@Autowired  ClienteService clienteService;
	@Autowired	EnvioService envioService;
	@Autowired	HistorialEstadoEnvioService historialService;
	@Autowired  PaqueteService paqueteService;
	@Autowired	TestMaintenanceService maintenance;

	private String remitenteDoc;
    private String destinatarioDoc;
	private EnvioCreateDTO envioSimple;
	private EnvioCreateDTO envioConFrio;

	@BeforeEach
	void prepareDtos() {
		var cRem = clienteService.crear(TestDataFactory.cliente("Rem", "20111111"));
		var cDes = clienteService.crear(TestDataFactory.cliente("Des", "20222222"));
		remitenteDoc = cRem.getDocumentoCuit();
        destinatarioDoc = cDes.getDocumentoCuit();
     // Paquetes para el envío simple (solo frágil)
        List<PaqueteDTO> paquetesSimple = paqueteService.crearLote(
                List.of(TestDataFactory.fragil(2, 5))
        );
        String codigoFragil = paquetesSimple.get(0).getCodigo();

        // Paquetes para el envío con frío (frágil + refrigerado)
        List<PaqueteDTO> paquetesConFrio = paqueteService.crearLote(
                List.of(
                        TestDataFactory.fragil(2, 5),
                        TestDataFactory.refri(1, 2, 4)
                )
        );
        String codigoFragil2 = paquetesConFrio.get(0).getCodigo();
        String codigoRefri   = paquetesConFrio.get(1).getCodigo();

        envioSimple = TestDataFactory.envio(remitenteDoc, destinatarioDoc, codigoFragil);
        envioConFrio = TestDataFactory.envio(remitenteDoc, destinatarioDoc,
                codigoFragil2, codigoRefri);
	}

	@AfterEach
	void cleanup() {
		maintenance.clearDatabase();
	}

	@Test
	@DisplayName("Crear envío inicia en GENERADO y devuelve tracking público ICG-...")
    void crear_envio_estado_generado_y_tracking_publico() {
        var creado = envioService.crear(envioSimple);
        assertThat(creado.getEstadoActual()).isEqualTo(EstadoEnvio.GENERADO);
        assertThat(creado.getTrackingCode()).isNotBlank();
        assertThat(creado.getTrackingCode()).startsWith("PGM-");
    }

	@Test
	@DisplayName("Envío con paquete refrigerado marca requiereFrio=true")
	void crear_envio_con_refrigerado() {
		var envio = envioService.crear(envioConFrio);
		assertThat(envio.isRequiereFrio()).isTrue();
	}
	
	@Test
    @DisplayName("Buscar por tracking público devuelve el mismo envío")
    void buscar_por_tracking_detalle() {
        var creado = envioService.crear(envioSimple);
        var consultado = envioService.buscarPorTracking(creado.getTrackingCode());
        assertThat(consultado.getId()).isEqualTo(creado.getId());
        assertThat(consultado.getEstadoActual()).isEqualTo(EstadoEnvio.GENERADO);
        assertThat(consultado.getTrackingCode()).isEqualTo(creado.getTrackingCode().trim().toUpperCase());

        assertThat(consultado.getHistorial()).isNotNull();
        assertThat(consultado.getHistorial()).isNotEmpty();
        // El primero debe ser el evento de alta GENERADO
        assertThat(consultado.getHistorial().get(0).getEstadoNuevo()).isEqualTo(EstadoEnvio.GENERADO);
    }

	@Test
	@DisplayName("Marcar entregado sin comprobante (estando EN_RUTA) lanza EnvioException por comprobante")
	void marcar_entregado_sin_comprobante() {
		var e = envioService.crear(envioSimple);
		
		e = envioService.actualizarEstado(e.getId(), EstadoEnvio.EN_ALMACEN, "Ingreso");
        e = envioService.actualizarEstado(e.getId(), EstadoEnvio.EN_RUTA, "Despacho");
        
        final Long id = e.getId();

		assertThatThrownBy(() -> envioService.marcarEntregado(id, null)).isInstanceOf(EnvioException.class)
					.hasMessageContaining("comprobante");
	}
	
	@Test
    @DisplayName("Marcar ENTREGADO con comprobante binario persiste y cambia a ENTREGADO")
    void marcar_entregado_con_comprobante() {
        var e = envioService.crear(envioSimple);
        envioService.actualizarEstado(e.getId(), EstadoEnvio.EN_ALMACEN, "Ingreso");
        envioService.actualizarEstado(e.getId(), EstadoEnvio.EN_RUTA, "Despacho");

        byte[] bytes = new byte[] {1, 2, 3, 4, 5};
        var entregado = envioService.marcarEntregado(e.getId(), bytes);

        assertThat(entregado.getEstadoActual()).isEqualTo(EstadoEnvio.ENTREGADO);
        assertThat(entregado.isHasComprobante()).isTrue();
    }

	@Test
	@DisplayName("Flujo GENERADO→ALMACÉN→RUTA→ENTREGADO con historial correcto")
	void flujo_completo_generado_a_entregado() {
        var e = envioService.crear(envioSimple);

        envioService.actualizarEstado(e.getId(), EstadoEnvio.EN_ALMACEN, "Ingreso");
        envioService.actualizarEstado(e.getId(), EstadoEnvio.EN_RUTA, "Despacho");
        var entregado = envioService.marcarEntregado(e.getId(), new byte[] {9, 9, 9});

        assertThat(entregado.getEstadoActual()).isEqualTo(EstadoEnvio.ENTREGADO);

        var historial = historialService.listarPorEnvio(entregado.getId());
        assertThat(historial).hasSize(4); // GENERADO + 3 transiciones

        assertThat(historial)
                .extracting(HistorialEstadoEnvioDTO::getEstadoNuevo)
                .containsExactly(
                        EstadoEnvio.GENERADO,
                        EstadoEnvio.EN_ALMACEN,
                        EstadoEnvio.EN_RUTA,
                        EstadoEnvio.ENTREGADO
                );
    }

	@Test
	@DisplayName("No se permite retroceder estado (EN_ALMACEN → GENERADO)")
	void no_permite_retroceder_estado() {
		var e = envioService.crear(envioSimple);
	    e = envioService.actualizarEstado(e.getId(), EstadoEnvio.EN_ALMACEN, "Ingreso a almacén");
	    
	    final Long id = e.getId();

		assertThatThrownBy(() -> envioService.actualizarEstado(id, EstadoEnvio.GENERADO, "retroceso"))
				.isInstanceOf(EnvioException.class)
		        .hasMessageContaining("GENERADO");
	}
	
	@RepeatedTest(2)
    @DisplayName("Crear repetidamente no colisiona códigos únicos")
    void repetidos_no_colisionan_codigos() {
        var e = envioService.crear(envioSimple);
        assertThat(e.getId()).isNotNull();
    }
}
