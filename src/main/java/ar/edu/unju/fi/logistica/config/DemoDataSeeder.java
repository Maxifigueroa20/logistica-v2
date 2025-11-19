package ar.edu.unju.fi.logistica.config;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
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
@Profile("dev")
@ConditionalOnProperty(value = "app.demo", havingValue = "true")
public class DemoDataSeeder implements CommandLineRunner {

	private final ClienteRepository clienteRepository;
	private final VehiculoRepository vehiculoRepository;
	private final PaqueteRepository paqueteRepository;

	@Override
	@Transactional
	public void run(String... args) {
		log.info("[SEED] Ejecutando DemoDataSeeder (perfil=dev, app.demo=true)");
		log.info("[SEED] Iniciando carga de datos demo...");

		crearClientesDemo();
		crearVehiculosDemo();
		crearPaquetesDemo();

		log.info("[SEED] Carga demo finalizada. Clientes={}, Vehículos={}, Paquetes={}", clienteRepository.count(),
				vehiculoRepository.count(), paqueteRepository.count());
	}

	/* =================== CLIENTES =================== */

	private void crearClientesDemo() {
		log.info("[SEED][Clientes] Sembrando clientes demo (idempotente)");

		// 4 empresas
		guardarClienteSiNoExiste("EJESA SA", "30700000011", "388-400-0001", "contacto@ejesa.com", "Av. Principal 100",
				"4600");
		guardarClienteSiNoExiste("Telecom SA", "30700000022", "388-400-0002", "contacto@telecom.com",
				"Av. Siempre Viva 742", "4600");
		guardarClienteSiNoExiste("Correo Andino SA", "30700000033", "388-400-0003", "logistica@correoandino.com",
				"Ruta 9 Km 10", "4601");
		guardarClienteSiNoExiste("Distribuidora Norte SA", "30700000044", "388-400-0004", "ventas@disnorte.com",
				"Parque Industrial S/N", "4602");

		// 3 pymes
		guardarClienteSiNoExiste("Panadería San Juan", "23300000001", "388-500-0001", "sanjuan@panaderia.com",
				"San Martín 123", "4600");
		guardarClienteSiNoExiste("Kiosco La Esquina", "20300000002", "388-500-0002", "laesquina@kiosco.com",
				"Belgrano 456", "4600");
		guardarClienteSiNoExiste("Ferretería El Tornillo", "23300000003", "388-500-0003", "eltornillo@ferre.com",
				"Lavalle 789", "4600");

		// 5 personas
		guardarClienteSiNoExiste("Gabriel Flores", "30111111", "388-600-0001", "gabriel.flores@mail.com",
				"Barrio Centro 1", "4600");
		guardarClienteSiNoExiste("Maximiliano Figueroa", "30222222", "388-600-0002", "maxi.figueroa@mail.com",
				"Barrio Centro 2", "4600");
		guardarClienteSiNoExiste("Juan Rodriguez", "30333333", "388-600-0003", "juan.rodriguez@mail.com",
				"Barrio Centro 3", "4600");
		guardarClienteSiNoExiste("Lucía Pérez", "30444444", "388-600-0004", "lucia.perez@mail.com", "Barrio Sur 10",
				"4601");
		guardarClienteSiNoExiste("María Gomez", "30555555", "388-600-0005", "maria.gomez@mail.com", "Barrio Norte 20",
				"4602");
	}

	private void guardarClienteSiNoExiste(String nombre, String doc, String tel, String email, String direccion,
			String cp) {
		clienteRepository.findByDocumentoCuit(doc).ifPresentOrElse(existente -> {
			log.debug("[SEED][Clientes] {} existente.", doc);
		}, () -> {
			Cliente c = new Cliente();
			c.setNombreRazonSocial(nombre);
			c.setDocumentoCuit(doc);
			c.setTelefono(tel);
			c.setEmail(email);
			c.setDireccionPrincipal(direccion);
			c.setCodigoPostal(cp);
			clienteRepository.save(c);
			log.info("[SEED][Clientes] Creado cliente demo: {} ({})", nombre, doc);
		});
	}

	/* =================== VEHÍCULOS =================== */

	private void crearVehiculosDemo() {
		log.info("[SEED][Vehiculos] Sembrando vehículos demo (idempotente)");

		// 3 no refrigerados
		guardarVehiculoSiNoExiste("AA111AA", bd("1000.00"), bd("50.00"), false, null, null);
		guardarVehiculoSiNoExiste("BB222BB", bd("2000.00"), bd("80.00"), false, null, null);
		guardarVehiculoSiNoExiste("CC333CC", bd("1500.00"), bd("60.00"), false, null, null);

		// 2 refrigerados
		guardarVehiculoSiNoExiste("DD444DD", bd("1800.00"), bd("70.00"), true, -5.0, 5.0);
		guardarVehiculoSiNoExiste("EE555EE", bd("2200.00"), bd("90.00"), true, 2.0, 8.0);
	}

	private void guardarVehiculoSiNoExiste(String patente, BigDecimal capPesoKg, BigDecimal capVolumenDm3,
			boolean refrigerado, Double tMin, Double tMax) {
		vehiculoRepository.findByPatenteIgnoreCase(patente).ifPresentOrElse(v -> {
		}, () -> {
			Vehiculo v = new Vehiculo();
			v.setPatente(patente);
			v.setCapacidadPesoKg(capPesoKg);
			v.setCapacidadVolumenDm3(capVolumenDm3);
			v.setRefrigerado(refrigerado);
			v.setRangoTempMin(bd(tMin, 2));
			v.setRangoTempMax(bd(tMax, 2));
			vehiculoRepository.save(v);
			log.info("[SEED][Vehiculos] Creado vehículo demo: {}", patente);
		});
	}

	/* =================== PAQUETES =================== */

	private void crearPaquetesDemo() {
		log.info("[SEED][Paquetes] Sembrando paquetes demo (frágiles y refrigerados, idempotente)");

		// 12 frágiles
		for (int i = 1; i <= 12; i++) {
			String codigo = "PF-" + String.format("%03d", i);
			BigDecimal peso = bd(1.0 + i * 0.5, 2); // entre ~1.5 y ~7.0 kg, redondeado
			BigDecimal volumen = bd(3.0 + i, 2); // entre 4 y 15 dm3
			NivelFragilidad nivel = (i % 3 == 0) ? NivelFragilidad.ALTA
					: (i % 3 == 1) ? NivelFragilidad.MEDIA : NivelFragilidad.BAJA;
			boolean seguro = (i % 2 == 0);

			guardarPaqueteFragilSiNoExiste(codigo, peso, volumen, nivel, seguro);
		}

		// 12 refrigerados
		List<Double> temps = List.of(2.0, 4.0, 5.0, 3.0, 1.0, 6.0, 7.0, 4.5, 2.5, 3.5, 5.5, 6.5);
		for (int i = 1; i <= 12; i++) {
			String codigo = "PR-" + String.format("%03d", i);
			BigDecimal peso = bd(0.8 + i * 0.4, 2); // entre ~1.2 y ~5.6 kg
			BigDecimal volumen = bd(2.0 + i * 0.8, 2); // entre ~2.8 y ~11.6 dm3
			double temp = temps.get(i - 1);
			BigDecimal tObj = bd(temp, 2);
			BigDecimal rangoMin = bd(temp - 2.0, 2);
			BigDecimal rangoMax = bd(temp + 2.0, 2);

			guardarPaqueteRefrigeradoSiNoExiste(codigo, peso, volumen, tObj, rangoMin, rangoMax, 4);
		}
	}

	private void guardarPaqueteFragilSiNoExiste(String codigo, BigDecimal pesoKg, BigDecimal volumenDm3,
			NivelFragilidad nivel, boolean seguroAdicional) {
		paqueteRepository.findByCodigo(codigo).ifPresentOrElse(p -> {
		}, () -> {
			PaqueteFragil p = new PaqueteFragil();
			p.setCodigo(codigo);
			p.setPesoKg(pesoKg);
			p.setVolumenDm3(volumenDm3);
			p.setEnvio(null); // sin envío asociado en demo
			p.setNivelFragilidad(nivel);
			p.setSeguroAdicional(seguroAdicional);
			paqueteRepository.save(p);
			log.info("[SEED][Paquetes] Creado paquete frágil demo: {}", codigo);
		});
	}

	private void guardarPaqueteRefrigeradoSiNoExiste(String codigo, BigDecimal pesoKg, BigDecimal volumenDm3,
			BigDecimal tObj, BigDecimal rangoMin, BigDecimal rangoMax, int horasMaxFueraFrio) {
		paqueteRepository.findByCodigo(codigo).ifPresentOrElse(existing -> {
		}, () -> {
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
			log.info("[SEED][Paquetes] Creado paquete refrigerado demo: {}", codigo);
		});
	}

	/* =================== Helpers =================== */

	private BigDecimal bd(String value) {
		return new BigDecimal(value);
	}

	private BigDecimal bd(double value, int scale) {
		return BigDecimal.valueOf(value).setScale(scale, java.math.RoundingMode.HALF_UP);
	}
}
