package ar.edu.unju.fi.logistica.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;

import ar.edu.unju.fi.logistica.dto.envio.EnvioCreateDTO;
import ar.edu.unju.fi.logistica.dto.envio.EnvioDTO;
import ar.edu.unju.fi.logistica.dto.paquete.PaqueteDTO;
import ar.edu.unju.fi.logistica.enums.EstadoEnvio;
import ar.edu.unju.fi.logistica.maintenance.TestMaintenanceService;
import ar.edu.unju.fi.logistica.support.ITBase;
import ar.edu.unju.fi.logistica.utils.TestDataFactory;

@Tag("service")
@DisplayName("EnvioService – Búsqueda filtrada por remitente/destinatario/estado")
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
class EnvioServiceFilterIT extends ITBase {

	@Autowired	ClienteService clienteService;
	@Autowired	EnvioService envioService;
	@Autowired	PaqueteService paqueteService;
	@Autowired	TestMaintenanceService maintenance;

	private String rem1Doc;
	private String rem2Doc;
	private String des1Doc;
	private String des2Doc;

	private EnvioDTO e1; // rem1 - des1 - GENERADO
	private EnvioDTO e2; // rem1 - des2 - EN_ALMACEN
	private EnvioDTO e3; // rem2 - des1 - EN_RUTA
	private EnvioDTO e4; // rem2 - des2 - GENERADO

	@BeforeEach
	void setUp() {
		// Clientes
		var rem1 = clienteService.crear(TestDataFactory.cliente("Remitente 1", "20111111"));
		var rem2 = clienteService.crear(TestDataFactory.cliente("Remitente 2", "20222222"));
		var des1 = clienteService.crear(TestDataFactory.cliente("Destinatario 1", "20333333"));
		var des2 = clienteService.crear(TestDataFactory.cliente("Destinatario 2", "20444444"));

		rem1Doc = rem1.getDocumentoCuit();
		rem2Doc = rem2.getDocumentoCuit();
		des1Doc = des1.getDocumentoCuit();
		des2Doc = des2.getDocumentoCuit();

		// Un helper para crear paquetes + envío
		e1 = crearEnvio(rem1Doc, des1Doc); // GENERADO
		e2 = crearEnvio(rem1Doc, des2Doc); // luego EN_ALMACEN
		e3 = crearEnvio(rem2Doc, des1Doc); // luego EN_RUTA
		e4 = crearEnvio(rem2Doc, des2Doc); // GENERADO

		// Cambiar estados
		e2 = envioService.actualizarEstado(e2.getId(), EstadoEnvio.EN_ALMACEN, "Ingreso almacén");
		e3 = envioService.actualizarEstado(e3.getId(), EstadoEnvio.EN_ALMACEN, "Ingreso almacén");
		e3 = envioService.actualizarEstado(e3.getId(), EstadoEnvio.EN_RUTA, "Despacho");
	}

	@AfterEach
	void cleanup() {
		maintenance.clearDatabase();
	}

	private EnvioDTO crearEnvio(String remDoc, String desDoc) {
		// Creamos un paquete frágil simple y usamos su código
		List<PaqueteDTO> paquetes = paqueteService.crearLote(List.of(TestDataFactory.fragil(2, 5)));
		String codigo = paquetes.get(0).getCodigo();

		EnvioCreateDTO req = TestDataFactory.envio(remDoc, desDoc, codigo);
		return envioService.crear(req);
	}

	@Test
	@DisplayName("Filtrar solo por remitente devuelve sus envíos")
	void filtrar_por_remitente() {
		var resultado = envioService.buscarFiltrado(rem1Doc, null, null);

		assertThat(resultado).extracting(EnvioDTO::getId).containsExactlyInAnyOrder(e1.getId(), e2.getId());
	}

	@Test
	@DisplayName("Filtrar solo por destinatario devuelve sus envíos")
	void filtrar_por_destinatario() {
		var resultado = envioService.buscarFiltrado(null, des1Doc, null);

		assertThat(resultado).extracting(EnvioDTO::getId).containsExactlyInAnyOrder(e1.getId(), e3.getId());
	}

	@Test
	@DisplayName("Filtrar solo por estado GENERADO devuelve los envíos en ese estado")
	void filtrar_por_estado_generado() {
		var resultado = envioService.buscarFiltrado(null, null, EstadoEnvio.GENERADO);

		assertThat(resultado).extracting(EnvioDTO::getId).containsExactlyInAnyOrder(e1.getId(), e4.getId());
	}

	@Test
	@DisplayName("Filtrar por remitente + estado EN_ALMACEN devuelve justo el esperado")
	void filtrar_por_remitente_y_estado() {
		var resultado = envioService.buscarFiltrado(rem1Doc, null, EstadoEnvio.EN_ALMACEN);

		assertThat(resultado).extracting(EnvioDTO::getId).containsExactly(e2.getId());
	}

	@Test
	@DisplayName("Filtrar por remitente + destinatario + estado GENERADO devuelve un único envío")
	void filtrar_por_remitente_destinatario_y_estado() {
		var resultado = envioService.buscarFiltrado(rem2Doc, des2Doc, EstadoEnvio.GENERADO);

		assertThat(resultado).extracting(EnvioDTO::getId).containsExactly(e4.getId());
	}

	@Test
	@DisplayName("Filtros sin coincidencias devuelven lista vacía")
	void filtro_sin_resultados() {
		var resultado = envioService.buscarFiltrado("999999999999", null, null);
		assertThat(resultado).isEmpty();
	}

	@Test
	@DisplayName("Sin filtros (todo null) devuelve todos los envíos")
	void sin_filtros_devuelve_todos() {
		var resultado = envioService.buscarFiltrado(null, null, null);

		assertThat(resultado).extracting(EnvioDTO::getId).containsExactlyInAnyOrder(e1.getId(), e2.getId(), e3.getId(),
				e4.getId());
	}
}
