package ar.edu.unju.fi.logistica.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import java.time.LocalDate;
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
import ar.edu.unju.fi.logistica.dto.ruta.RutaCreateDTO;
import ar.edu.unju.fi.logistica.dto.ruta.RutaDTO;
import ar.edu.unju.fi.logistica.dto.vehiculo.VehiculoCreateDTO;
import ar.edu.unju.fi.logistica.enums.EstadoEnvio;
import ar.edu.unju.fi.logistica.exception.RutaException;
import ar.edu.unju.fi.logistica.maintenance.TestMaintenanceService;
import ar.edu.unju.fi.logistica.support.ITBase;
import ar.edu.unju.fi.logistica.utils.TestDataFactory;

@Tag("service")
@DisplayName("RutaService – Validaciones de capacidad y refrigeración")
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
class RutaServiceIT extends ITBase {

	@Autowired 	VehiculoService vehiculoService;
	@Autowired	RutaService rutaService;
	@Autowired	EnvioService envioService;
	@Autowired	ClienteService clienteService;
	@Autowired	PaqueteService paqueteService;
	@Autowired	TestMaintenanceService maintenance;

	private String remitenteDoc;
	private String destinatarioDoc;
	private VehiculoCreateDTO vehNoRefri;
	private VehiculoCreateDTO vehRefri;

	@BeforeEach
	void prepareDtos() {
		var cRem = clienteService.crear(TestDataFactory.cliente("Rem", "20999999"));
		var cDes = clienteService.crear(TestDataFactory.cliente("Des", "20888888"));
		remitenteDoc = cRem.getDocumentoCuit();
		destinatarioDoc = cDes.getDocumentoCuit();
		vehNoRefri = TestDataFactory.vehiculoNoRefri("AA000AA", 100, 200);
		vehRefri = TestDataFactory.vehiculoRefri("BB000BB", 200, 200, 0, 5);
	}

	@AfterEach
	void cleanup() {
		maintenance.clearDatabase();
	}

	private EnvioDTO crearEnvioFragil(double kg, double dm3) {
		List<PaqueteDTO> creados = paqueteService.crearLote(List.of(TestDataFactory.fragil(kg, dm3)));
		String codigo = creados.get(0).getCodigo();

		EnvioCreateDTO req = TestDataFactory.envio(remitenteDoc, destinatarioDoc, codigo);
		return envioService.crear(req);
	}

	private EnvioDTO crearEnvioRefrigerado(double kg, double dm3, double tObj) {
		List<PaqueteDTO> creados = paqueteService.crearLote(List.of(TestDataFactory.refri(kg, dm3, tObj)));
		String codigo = creados.get(0).getCodigo();

		EnvioCreateDTO req = TestDataFactory.envio(remitenteDoc, destinatarioDoc, codigo);
		return envioService.crear(req);
	}

	private RutaDTO crearRuta(VehiculoCreateDTO v) {
		var veh = vehiculoService.crear(v);
		var rc = RutaCreateDTO.builder().vehiculoPatente(veh.getPatente()).fecha(LocalDate.now()).build();
		return rutaService.crear(rc);
	}

	@Test
	@DisplayName("Asignación correcta dentro de capacidad")
	void asignacion_ok_dentro_capacidad() {
		var ruta = crearRuta(vehNoRefri);
		var e1 = crearEnvioFragil(20, 30);
		var e2 = crearEnvioFragil(40, 60);
		
		e1 = envioService.actualizarEstado(e1.getId(), EstadoEnvio.EN_ALMACEN, "Ingreso");
		e2 = envioService.actualizarEstado(e2.getId(), EstadoEnvio.EN_ALMACEN, "Ingreso");
		
		rutaService.asignarEnvios(ruta.getId(), List.of(e1.getId()));
		rutaService.asignarEnvios(ruta.getId(), List.of(e2.getId()));
		var actual = rutaService.buscarPorId(ruta.getId());
		assertThat(actual.getEnviosIds()).hasSize(2);
	}

	@Test
	@DisplayName("Exceso de peso bloquea asignación")
	void exceso_peso_bloquea() {
		var ruta = crearRuta(TestDataFactory.vehiculoNoRefri("AA123AA", 50, 500));
		var e1 = crearEnvioFragil(40, 10);
		e1 = envioService.actualizarEstado(e1.getId(), EstadoEnvio.EN_ALMACEN, "Ingreso");
		rutaService.asignarEnvios(ruta.getId(), List.of(e1.getId()));
		var e2 = crearEnvioFragil(20, 10);
		var e2Almacen = envioService.actualizarEstado(e2.getId(), EstadoEnvio.EN_ALMACEN, "Ingreso");
		Throwable thrown = catchThrowable(() -> rutaService.asignarEnvios(ruta.getId(), List.of(e2Almacen.getId())));
		assertThat(thrown).isInstanceOf(RutaException.class).hasMessageContaining("capacidad");
	}

	@Test
	@DisplayName("Envío refrigerado en vehículo no refrigerado bloquea")
	void refrigerado_en_vehiculo_no_refrigerado_bloquea() {
		var ruta = crearRuta(vehNoRefri);
		var e = crearEnvioRefrigerado(10, 10, 2);
		Throwable thrown = catchThrowable(() -> rutaService.asignarEnvios(ruta.getId(), List.of(e.getId())));
		assertThat(thrown).isInstanceOf(RutaException.class).hasMessageContaining("refriger");

	}

	@Test
	@DisplayName("Temperatura objetivo fuera de rango bloquea asignación")
	void temperatura_objetivo_fuera_de_rango_bloquea() {
		var ruta = crearRuta(vehRefri);
		var e = crearEnvioRefrigerado(5, 5, -5);
		Throwable thrown = catchThrowable(() -> rutaService.asignarEnvios(ruta.getId(), List.of(e.getId())));
		assertThat(thrown).isInstanceOf(RutaException.class).hasMessageContaining("Temperatura");
	}
}
