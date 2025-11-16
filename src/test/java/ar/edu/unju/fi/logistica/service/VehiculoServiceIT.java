package ar.edu.unju.fi.logistica.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;

import ar.edu.unju.fi.logistica.dto.vehiculo.VehiculoCreateDTO;
import ar.edu.unju.fi.logistica.dto.vehiculo.VehiculoSearchCriteria;
import ar.edu.unju.fi.logistica.exception.VehiculoException;
import ar.edu.unju.fi.logistica.maintenance.TestMaintenanceService;
import ar.edu.unju.fi.logistica.support.ITBase;
import ar.edu.unju.fi.logistica.utils.TestDataFactory;

@Tag("service")
@DisplayName("VehiculoService – Creación y validaciones")
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
class VehiculoServiceIT extends ITBase {

	@Autowired	VehiculoService vehiculoService;
	@Autowired	TestMaintenanceService maintenance;

	private VehiculoCreateDTO vNoRefri;
	private VehiculoCreateDTO vRefri;
	private VehiculoCreateDTO vRefriInvalido;

	private VehiculoCreateDTO vParaBuscarPorPatente;
	private String patenteInexistente;

	private VehiculoSearchCriteria criteriaRefrigeradoCapacidad;

	@BeforeEach
	void prepareDtos() {
		vNoRefri = TestDataFactory.vehiculoNoRefri("AA111AA", 100, 200);
		vRefri = TestDataFactory.vehiculoRefri("BB222BB", 150, 250, -2, 8);
		vRefriInvalido = TestDataFactory.vehiculoRefri("CC333CC", 100, 200, 0, 0);
		vRefriInvalido.setRangoTempMin(null);
		vRefriInvalido.setRangoTempMax(null);
		vParaBuscarPorPatente = TestDataFactory.vehiculoNoRefri("DD444DD", 120, 220);
		patenteInexistente = "ZZZ999ZZZ";

		criteriaRefrigeradoCapacidad = TestDataFactory.vehiculoCriteriaRefrigerado(100.0, 200.0);
	}

	@AfterEach
	void cleanup() {
		maintenance.clearDatabase();
	}

	@Test
	@DisplayName("Crear vehículo no refrigerado correctamente")
	void crear_no_refrigerado() {
		var creado = vehiculoService.crear(vNoRefri);
		assertThat(creado.getId()).isNotNull();
		assertThat(creado.isRefrigerado()).isFalse();
	}

	@Test
	@DisplayName("Crear vehículo refrigerado correctamente")
	void crear_refrigerado() {
		var creado = vehiculoService.crear(vRefri);
		assertThat(creado.getId()).isNotNull();
		assertThat(creado.isRefrigerado()).isTrue();
		assertThat(creado.getRangoTempMin()).isEqualTo(-2);
		assertThat(creado.getRangoTempMax()).isEqualTo(8);
	}

	@Test
	@DisplayName("Refrigerado sin rangos válidos lanza VehiculoException")
	void crear_refrigerado_sin_rangos() {
		assertThatThrownBy(() -> vehiculoService.crear(vRefriInvalido)).isInstanceOf(VehiculoException.class);
	}

	@Test
	@DisplayName("Buscar vehículo por patente")
	void buscar_por_patente() {
		var creado = vehiculoService.crear(vParaBuscarPorPatente);
		var encontrado = vehiculoService.buscarPorPatente(creado.getPatente());

		assertThat(encontrado.getId()).isEqualTo(creado.getId());
		assertThat(encontrado.getPatente()).isEqualTo(creado.getPatente());
		assertThat(encontrado.isRefrigerado()).isEqualTo(creado.isRefrigerado());
	}

	@Test
	@DisplayName("Buscar por patente inexistente lanza VehiculoException")
	void buscar_por_patente_inexistente_lanza_VehiculoException() {
		assertThatThrownBy(() -> vehiculoService.buscarPorPatente(patenteInexistente))
				.isInstanceOf(VehiculoException.class);
	}

	@Test
	@DisplayName("Buscar vehículos refrigerados con capacidad mínima usando criteria de factory")
	void buscar_filtrado_con_criteria_factory() {
		// arrange
		vehiculoService.crear(vNoRefri); // no debería cumplir
		vehiculoService.crear(vRefri); // sí debería cumplir

		// act
		var filtrados = vehiculoService.buscar(criteriaRefrigeradoCapacidad);

		// assert
		assertThat(filtrados).isNotEmpty().allSatisfy(v -> {
			assertThat(v.isRefrigerado()).isTrue();
			assertThat(v.getCapacidadPesoKg()).isGreaterThanOrEqualTo(100.0);
			assertThat(v.getCapacidadVolumenDm3()).isGreaterThanOrEqualTo(200.0);
		});
	}
}
