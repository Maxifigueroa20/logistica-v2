package ar.edu.unju.fi.logistica.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;

import ar.edu.unju.fi.logistica.dto.cliente.ClienteCreateDTO;
import ar.edu.unju.fi.logistica.dto.cliente.ClienteDTO;
import ar.edu.unju.fi.logistica.dto.cliente.ClienteUpdateDTO;
import ar.edu.unju.fi.logistica.exception.ClienteException;
import ar.edu.unju.fi.logistica.maintenance.TestMaintenanceService;
import ar.edu.unju.fi.logistica.support.ITBase;
import ar.edu.unju.fi.logistica.utils.TestDataFactory;

@Tag("service")
@DisplayName("ClienteService – Creación, actualización y búsquedas")
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
class ClienteServiceIT extends ITBase {

	@Autowired	ClienteService clienteService;
	@Autowired	TestMaintenanceService maintenance;

	private ClienteCreateDTO cRem;
	private ClienteCreateDTO cDes;
	private ClienteUpdateDTO updDto;

	private Long inexistenteId;
	private String inexistenteDoc;

	@BeforeEach
	void prepareDtos() {
		cRem = TestDataFactory.cliente("Remitente SRL", "20111111");
		cDes = TestDataFactory.cliente("Destinatario SA", "20222222");

		updDto = ClienteUpdateDTO.builder().telefono("388-555-111").email("actualizado@mail.com")
				.direccionPrincipal("Siempre Viva 123").codigoPostal("4600").build();

		inexistenteId = 9_999_999L;
		inexistenteDoc = "00000000000000000000";
	}

	@AfterEach
	void cleanup() {
		maintenance.clearDatabase();
	}

	@Test
	@DisplayName("Crear cliente con DTO correctamente")
	void crear_conDTO_happyPath() {
		var creado = clienteService.crear(cRem);
		assertThat(creado.getId()).isNotNull();
		assertThat(creado.getNombreRazonSocial()).isEqualTo("Remitente SRL");

		String pref = cRem.getDocumentoCuit().replaceAll("-", "");
		assertThat(creado.getDocumentoCuit())
				.startsWith(pref.substring(0, Math.min(pref.length(), creado.getDireccionPrincipal().length())))
				.hasSizeLessThanOrEqualTo(20).matches("\\d+");
	}

	@Test
	@DisplayName("Actualizar cliente y buscar por documento")
	void actualizar_y_buscarPorDocumento() {
		var creado = clienteService.crear(cDes);

		var actualizado = clienteService.actualizar(creado.getId(), updDto);
		assertThat(actualizado.getTelefono()).isEqualTo("388-555-111");
		assertThat(actualizado.getEmail()).isEqualTo("actualizado@mail.com");
		assertThat(actualizado.getDireccionPrincipal()).isEqualTo("Siempre Viva 123");

		List<ClienteDTO> porDoc = clienteService.buscarPorDocumentoLike(creado.getDocumentoCuit());

		assertThat(porDoc).extracting(ClienteDTO::getId).contains(creado.getId());
	}

	@Test
	@DisplayName("Buscar cliente inexistente lanza ClienteException")
	void buscar_inexistente_lanza_ClienteException() {
		// Por id inexistente
		assertThatThrownBy(() -> clienteService.buscarPorId(inexistenteId)).isInstanceOf(ClienteException.class);

		// Por documento inexistente → ahora esperamos lista vacía
		var resultado = clienteService.buscarPorDocumentoLike(inexistenteDoc);
		assertThat(resultado).isEmpty();
	}
}
