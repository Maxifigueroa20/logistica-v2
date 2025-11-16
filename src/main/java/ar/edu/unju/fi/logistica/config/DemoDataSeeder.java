package ar.edu.unju.fi.logistica.config;

import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import ar.edu.unju.fi.logistica.domain.Cliente;
import ar.edu.unju.fi.logistica.domain.PaqueteFragil;
import ar.edu.unju.fi.logistica.domain.PaqueteRefrigerado;
import ar.edu.unju.fi.logistica.domain.Vehiculo;
import ar.edu.unju.fi.logistica.enums.NivelFragilidad;
import ar.edu.unju.fi.logistica.repository.ClienteRepository;
import ar.edu.unju.fi.logistica.repository.PaqueteRepository;
import ar.edu.unju.fi.logistica.repository.VehiculoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(value = "app.demo", havingValue = "true")
public class DemoDataSeeder implements CommandLineRunner {

	private final ClienteRepository clienteRepository;
	private final VehiculoRepository vehiculoRepository;
	private final PaqueteRepository paqueteRepository;

	@Override
	@Transactional
	public void run(String... args) {
		if (yaHayDatos()) {
			log.info("[SEED] Datos existentes detectados, se omite carga demo.");
			return;
		}

		log.info("[SEED] Iniciando carga de datos demo...");

		crearClientesDemo();
		crearVehiculosDemo();
		crearPaquetesDemo();

		log.info("[SEED] Carga demo finalizada. Clientes={}, Vehículos={}, Paquetes={}", clienteRepository.count(),
				vehiculoRepository.count(), paqueteRepository.count());
	}

	private boolean yaHayDatos() {
		return clienteRepository.count() > 0 || vehiculoRepository.count() > 0 || paqueteRepository.count() > 0;
	}

	/* =================== CLIENTES =================== */

	private void crearClientesDemo() {
		log.info("[SEED][Clientes] Creando 12 clientes demo (empresas, pymes y personas)");

		// 4 empresas
		guardarCliente("EJESA SA", "30700000011", "388-400-0001", "contacto@ejesa.com", "Av. Principal 100", "4600");
		guardarCliente("Telecom SA", "30700000022", "388-400-0002", "contacto@telecom.com", "Av. Siempre Viva 742",
				"4600");
		guardarCliente("Correo Andino SA", "30700000033", "388-400-0003", "logistica@correoandino.com", "Ruta 9 Km 10",
				"4601");
		guardarCliente("Distribuidora Norte SA", "30700000044", "388-400-0004", "ventas@disnorte.com",
				"Parque Industrial S/N", "4602");

		// 3 pymes
		guardarCliente("Panadería San Juan", "23300000001", "388-500-0001", "sanjuan@panaderia.com", "San Martín 123",
				"4600");
		guardarCliente("Kiosco La Esquina", "20300000002", "388-500-0002", "laesquina@kiosco.com", "Belgrano 456",
				"4600");
		guardarCliente("Ferretería El Tornillo", "23300000003", "388-500-0003", "eltornillo@ferre.com", "Lavalle 789",
				"4600");

		// 5 personas
		guardarCliente("Gabriel Flores", "30111111", "388-600-0001", "gabriel.flores@mail.com", "Barrio Centro 1",
				"4600");
		guardarCliente("Maximiliano Figueroa", "30222222", "388-600-0002", "maxi.figueroa@mail.com", "Barrio Centro 2",
				"4600");
		guardarCliente("Juan Rodriguez", "30333333", "388-600-0003", "juan.rodriguez@mail.com", "Barrio Centro 3",
				"4600");
		guardarCliente("Lucía Pérez", "30444444", "388-600-0004", "lucia.perez@mail.com", "Barrio Sur 10", "4601");
		guardarCliente("María Gomez", "30555555", "388-600-0005", "maria.gomez@mail.com", "Barrio Norte 20", "4602");
	}

	private void guardarCliente(String nombre, String doc, String tel, String email, String direccion, String cp) {
		Cliente c = new Cliente();
		c.setNombreRazonSocial(nombre);
		c.setDocumentoCuit(doc);
		c.setTelefono(tel);
		c.setEmail(email);
		c.setDireccionPrincipal(direccion);
		c.setCodigoPostal(cp);
		clienteRepository.save(c);
	}

	/* =================== VEHÍCULOS =================== */

	private void crearVehiculosDemo() {
		log.info("[SEED][Vehiculos] Creando 5 vehículos demo");

		// 3 no refrigerados
		guardarVehiculo("AA111AA", 1000, 50, false, null, null);
		guardarVehiculo("BB222BB", 2000, 80, false, null, null);
		guardarVehiculo("CC333CC", 1500, 60, false, null, null);

		// 2 refrigerados
		guardarVehiculo("DD444DD", 1800, 70, true, -5.0, 5.0);
		guardarVehiculo("EE555EE", 2200, 90, true, 2.0, 8.0);
	}

	private void guardarVehiculo(String patente, double capPesoKg, double capVolumenDm3, boolean refrigerado,
			Double tMin, Double tMax) {
		Vehiculo v = new Vehiculo();
		v.setPatente(patente);
		v.setCapacidadPesoKg(capPesoKg);
		v.setCapacidadVolumenDm3(capVolumenDm3);
		v.setRefrigerado(refrigerado);
		v.setRangoTempMin(tMin);
		v.setRangoTempMax(tMax);
		vehiculoRepository.save(v);
	}

	/* =================== PAQUETES =================== */

	private void crearPaquetesDemo() {
		log.info("[SEED][Paquetes] Creando 24 paquetes demo (frágiles y refrigerados)");

		// 12 frágiles
		for (int i = 1; i <= 12; i++) {
			String codigo = "PF-" + String.format("%03d", i);
			double peso = 1.0 + i * 0.5; // entre ~1.5 y ~7.0 kg
			double volumen = 3.0 + i; // entre 4 y 15 dm3
			NivelFragilidad nivel = (i % 3 == 0) ? NivelFragilidad.ALTA
					: (i % 3 == 1) ? NivelFragilidad.MEDIA : NivelFragilidad.BAJA;
			boolean seguro = (i % 2 == 0);

			guardarPaqueteFragil(codigo, peso, volumen, nivel, seguro);
		}

		// 12 refrigerados
		List<Double> temps = List.of(2.0, 4.0, 5.0, 3.0, 1.0, 6.0, 7.0, 4.5, 2.5, 3.5, 5.5, 6.5);
		for (int i = 1; i <= 12; i++) {
			String codigo = "PR-" + String.format("%03d", i);
			double peso = 0.8 + i * 0.4; // entre ~1.2 y ~5.6 kg
			double volumen = 2.0 + i * 0.8; // entre ~2.8 y ~11.6 dm3
			double tObj = temps.get(i - 1);
			double rangoMin = tObj - 2.0;
			double rangoMax = tObj + 2.0;

			guardarPaqueteRefrigerado(codigo, peso, volumen, tObj, rangoMin, rangoMax, 4);
		}
	}

	private void guardarPaqueteFragil(String codigo, double pesoKg, double volumenDm3, NivelFragilidad nivel,
			boolean seguroAdicional) {
		PaqueteFragil p = new PaqueteFragil();
		p.setCodigo(codigo);
		p.setPesoKg(pesoKg);
		p.setVolumenDm3(volumenDm3);
		p.setEnvio(null); // sin envío asociado en demo
		p.setNivelFragilidad(nivel);
		p.setSeguroAdicional(seguroAdicional);
		paqueteRepository.save(p);
	}

	private void guardarPaqueteRefrigerado(String codigo, double pesoKg, double volumenDm3, double tObj,
			double rangoMin, double rangoMax, int horasMaxFueraFrio) {
		PaqueteRefrigerado p = new PaqueteRefrigerado();
		p.setCodigo(codigo);
		p.setPesoKg(pesoKg);
		p.setVolumenDm3(volumenDm3);
		p.setEnvio(null); // sin envío asociado en demo
		p.setTemperaturaObjetivo(tObj);
		p.setRangoMin(rangoMin);
		p.setRangoMax(rangoMax);
		p.setHorasMaxFueraFrio(horasMaxFueraFrio);
		paqueteRepository.save(p);
	}
}
