package ar.edu.unju.fi.logistica.service.impl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ar.edu.unju.fi.logistica.domain.Envio;
import ar.edu.unju.fi.logistica.domain.PaqueteRefrigerado;
import ar.edu.unju.fi.logistica.domain.Ruta;
import ar.edu.unju.fi.logistica.domain.Vehiculo;
import ar.edu.unju.fi.logistica.dto.ruta.RutaCreateDTO;
import ar.edu.unju.fi.logistica.dto.ruta.RutaDTO;
import ar.edu.unju.fi.logistica.exception.RutaException;
import ar.edu.unju.fi.logistica.mapper.RutaMapper;
import ar.edu.unju.fi.logistica.repository.EnvioRepository;
import ar.edu.unju.fi.logistica.repository.RutaRepository;
import ar.edu.unju.fi.logistica.repository.VehiculoRepository;
import ar.edu.unju.fi.logistica.service.RutaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RutaServiceImpl implements RutaService {
	private static final String ERR_RUTA_NO_ENCONTRADA = "Ruta no encontrada";
	private static final String ERR_ENVIO_NO_ENCONTRADO = "Envío no encontrado";
	private static final String ERR_VEHICULO_NO_ENCONTRADO = "Vehículo no encontrado";

	private final RutaRepository rutaRepo;
	private final VehiculoRepository vehiculoRepo;
	private final EnvioRepository envioRepo;
	private final RutaMapper rutaMapper;

	@Override
	@Transactional
	public RutaDTO crear(RutaCreateDTO dto) {
		log.info("[Svc][Rutas] Crear ruta fecha={} vehiculoPatente={}", dto.getFecha(), dto.getVehiculoPatente());

		Vehiculo vehiculo = vehiculoRepo.findByPatenteIgnoreCase(dto.getVehiculoPatente())
				.orElseThrow(() -> new RutaException(ERR_VEHICULO_NO_ENCONTRADO));

		rutaRepo.findByFechaAndVehiculo_Id(dto.getFecha(), vehiculo.getId()).ifPresent(r -> {
			throw new RutaException("Ya existe una ruta para ese vehículo en esa fecha");
		});

		Ruta ruta = rutaMapper.toEntity(dto);
		ruta.setVehiculo(vehiculo);

		Ruta guardada = rutaRepo.save(ruta);
		log.info("[Svc][Rutas] Ruta creada id={} vehiculo={}", guardada.getId(), vehiculo.getPatente());

		return rutaMapper.toDTO(guardada);
	}

	@Override
	@Transactional
	public RutaDTO asignarEnvios(Long rutaId, List<Long> enviosIds) {
		if (enviosIds == null || enviosIds.isEmpty()) {
			throw new RutaException("Debe indicar al menos un envío para asignar");
		}

		Ruta ruta = obtenerRuta(rutaId);
		Vehiculo vehiculo = ruta.getVehiculo();

		for (Long envioId : enviosIds) {
			asignarEnvioIndividual(ruta, vehiculo, envioId);
		}

		log.info("[Svc][Rutas] Asignados {} envío(s) a ruta={}", enviosIds.size(), rutaId);
		return rutaMapper.toDTO(ruta);
	}

	@Override
	@Transactional
	public RutaDTO quitarEnvios(Long rutaId, List<Long> enviosIds, String observacion) {
		if (enviosIds == null || enviosIds.isEmpty()) {
			throw new RutaException("Debe indicar al menos un envío para quitar");
		}

		Ruta ruta = obtenerRuta(rutaId);

		for (Long envioId : enviosIds) {
			quitarEnvioIndividual(ruta, envioId, observacion);
		}

		log.info("[Svc][Rutas] Quitados {} envío(s) de ruta={} (obs={})", enviosIds.size(), rutaId, observacion);

		return rutaMapper.toDTO(ruta);
	}

	@Override
	public RutaDTO buscarPorId(Long id) {
		Ruta ruta = obtenerRuta(id);
		return rutaMapper.toDTO(ruta);
	}

	@Override
	public List<Long> listarEnviosIdsDeRutaEnFecha(Long rutaId, LocalDate fecha) {
		// validamos existencia de la ruta para dar mejor error
		obtenerRuta(rutaId);
		return rutaRepo.findEnviosIdsByRutaAndFecha(rutaId, fecha);
	}

	@Override
	public List<RutaDTO> listar(LocalDate fecha) {
		List<Ruta> rutas = (fecha == null) ? rutaRepo.findAll() : rutaRepo.findByFecha(fecha);

		log.debug("[Svc][Rutas] Listar rutas fecha={} → {} resultado(s)", fecha, rutas.size());

		return rutas.stream().map(rutaMapper::toDTO).toList();
	}

	/* ================== helpers privados ================== */

	private Ruta obtenerRuta(Long rutaId) {
		return rutaRepo.findById(rutaId).orElseThrow(() -> new RutaException(ERR_RUTA_NO_ENCONTRADA));
	}

	private void asignarEnvioIndividual(Ruta ruta, Vehiculo vehiculo, Long envioId) {
		Envio envio = envioRepo.findById(envioId)
				.orElseThrow(() -> new RutaException(ERR_ENVIO_NO_ENCONTRADO + ": " + envioId));

		if (envio.getRuta() != null && !envio.getRuta().getId().equals(ruta.getId())) {
			throw new RutaException("El envío " + envioId + " ya está asignado a otra ruta");
		}

		validarCompatibilidadYCapacidad(ruta, vehiculo, envio);

		// Flujo de estados: EN_ALMACEN → EN_RUTA
		envio.aRuta("Asignado a ruta " + vehiculo.getPatente());
		envio.setRuta(ruta);
		ruta.getEnvios().add(envio);

		log.debug("[Svc][Rutas] Envío {} asignado a ruta {}", envioId, ruta.getId());
	}

	private void quitarEnvioIndividual(Ruta ruta, Long envioId, String observacion) {
		Envio envio = envioRepo.findById(envioId)
				.orElseThrow(() -> new RutaException(ERR_ENVIO_NO_ENCONTRADO + ": " + envioId));

		if (!ruta.getEnvios().remove(envio)) {
			throw new RutaException("El envío " + envioId + " no pertenece a la ruta " + ruta.getId());
		}
		envio.setRuta(null);

		log.debug("[Svc][Rutas] Envío {} quitado de ruta {} (obs={})", envioId, ruta.getId(), observacion);
	}

	/**
	 * Valida que el vehículo soporte peso/volumen + condiciones de frío al agregar
	 * un envío a la ruta.
	 */
	private void validarCompatibilidadYCapacidad(Ruta ruta, Vehiculo v, Envio e) {
		BigDecimal pesoRuta = ruta.getEnvios().stream()
	            .flatMap(env -> env.getPaquetes().stream())
	            .map(p -> p.getPesoKg() != null ? p.getPesoKg() : BigDecimal.ZERO)
	            .reduce(BigDecimal.ZERO, BigDecimal::add);

	    BigDecimal volRuta = ruta.getEnvios().stream()
	            .flatMap(env -> env.getPaquetes().stream())
	            .map(p -> p.getVolumenDm3() != null ? p.getVolumenDm3() : BigDecimal.ZERO)
	            .reduce(BigDecimal.ZERO, BigDecimal::add);

	    BigDecimal pesoEnvio = e.getPaquetes().stream()
	            .map(p -> p.getPesoKg() != null ? p.getPesoKg() : BigDecimal.ZERO)
	            .reduce(BigDecimal.ZERO, BigDecimal::add);

	    BigDecimal volEnvio = e.getPaquetes().stream()
	            .map(p -> p.getVolumenDm3() != null ? p.getVolumenDm3() : BigDecimal.ZERO)
	            .reduce(BigDecimal.ZERO, BigDecimal::add);

		BigDecimal capPeso = v.getCapacidadPesoKg() != null ? v.getCapacidadPesoKg() : BigDecimal.ZERO;
		BigDecimal capVol = v.getCapacidadVolumenDm3() != null ? v.getCapacidadVolumenDm3() : BigDecimal.ZERO;

		if (pesoRuta.add(pesoEnvio).compareTo(capPeso) > 0 || volRuta.add(volEnvio).compareTo(capVol) > 0) {
			throw new RutaException("Se excede la capacidad del vehículo");
		}

		boolean requiereFrio = e.getPaquetes().stream().anyMatch(PaqueteRefrigerado.class::isInstance);

		if (requiereFrio) {
			if (!v.isRefrigerado()) {
				throw new RutaException("El envío requiere vehículo refrigerado");
			}

			BigDecimal min = v.getRangoTempMin() != null ? v.getRangoTempMin()
					: BigDecimal.valueOf(Double.NEGATIVE_INFINITY);

			BigDecimal max = v.getRangoTempMax() != null ? v.getRangoTempMax()
					: BigDecimal.valueOf(Double.POSITIVE_INFINITY);

			boolean ok = e.getPaquetes().stream().filter(PaqueteRefrigerado.class::isInstance)
					.map(PaqueteRefrigerado.class::cast).allMatch(pr -> {
						BigDecimal t = pr.getTemperaturaObjetivo();
						return t.compareTo(min) >= 0 && t.compareTo(max) <= 0;
					});

			if (!ok) {
				throw new RutaException("Temperatura objetivo fuera de rango del vehículo");
			}
		}
	}
}
