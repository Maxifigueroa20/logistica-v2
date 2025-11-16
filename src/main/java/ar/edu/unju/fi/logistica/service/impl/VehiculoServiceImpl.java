package ar.edu.unju.fi.logistica.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ar.edu.unju.fi.logistica.dto.vehiculo.VehiculoCreateDTO;
import ar.edu.unju.fi.logistica.dto.vehiculo.VehiculoDTO;
import ar.edu.unju.fi.logistica.dto.vehiculo.VehiculoSearchCriteria;
import ar.edu.unju.fi.logistica.exception.VehiculoException;
import ar.edu.unju.fi.logistica.mapper.VehiculoMapper;
import ar.edu.unju.fi.logistica.repository.VehiculoRepository;
import ar.edu.unju.fi.logistica.service.VehiculoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class VehiculoServiceImpl implements VehiculoService {

	private static final String ERR_VEHICULO_NO_ENCONTRADO = "Vehículo no encontrado";

	private final VehiculoRepository repo;
	private final VehiculoMapper mapper;

	@Override
	@Transactional
	public VehiculoDTO crear(VehiculoCreateDTO dto) {
		log.info("[Svc][Vehiculos] Crear vehículo patente={}", dto.getPatente());

		validarPatenteNoDuplicada(dto.getPatente());
		validarRefrigeradoConRango(dto);

		var entity = mapper.toEntity(dto);
		var saved = repo.save(entity);

		log.info("[Svc][Vehiculos] Vehículo creado id={} patente={}", saved.getId(), saved.getPatente());
		return mapper.toDTO(saved);
	}

	@Override
	public List<VehiculoDTO> listar() {
		log.debug("[Svc][Vehiculos] Listar todos los vehículos");
		return repo.findAll().stream().map(mapper::toDTO).toList();
	}

	@Override
	public VehiculoDTO buscarPorPatente(String patente) {
		log.debug("[Svc][Vehiculos] Buscar por patente={}", patente);
		return repo.findByPatenteIgnoreCase(patente).map(mapper::toDTO)
				.orElseThrow(() -> new VehiculoException(ERR_VEHICULO_NO_ENCONTRADO));
	}
	
	@Override
	public List<VehiculoDTO> buscarPorPatenteLike(String patente) {
		log.debug("[Svc][Vehiculos] Buscar por patente LIKE={}", patente);
		return repo.findByPatenteContainingIgnoreCase(patente).stream().map(mapper::toDTO).toList();
	}

	@Override
	public List<VehiculoDTO> buscar(VehiculoSearchCriteria c) {
		var pat = normalize(c.getPatente());

		log.debug(
				"[Svc][Vehiculos] Buscar filtros → patente='{}', refrigerado={}, capPeso[{}, {}], capVol[{}, {}], temp[{}, {}]",
				pat, c.getRefrigerado(), c.getCapacidadPesoMin(), c.getCapacidadPesoMax(), c.getCapacidadVolumenMin(),
				c.getCapacidadVolumenMax(), c.getTempMin(), c.getTempMax());

		boolean sinFiltros = pat == null && c.getRefrigerado() == null && c.getCapacidadPesoMin() == null
				&& c.getCapacidadPesoMax() == null && c.getCapacidadVolumenMin() == null
				&& c.getCapacidadVolumenMax() == null && c.getTempMin() == null && c.getTempMax() == null;

		if (sinFiltros) {
			log.debug("[Svc][Vehiculos] Sin filtros → listar()");
			return listar();
		}

		var resultados = repo.buscarFiltrado(pat, c.getRefrigerado(), c.getCapacidadPesoMin(), c.getCapacidadPesoMax(),
				c.getCapacidadVolumenMin(), c.getCapacidadVolumenMax(), c.getTempMin(), c.getTempMax());

		log.debug("[Svc][Vehiculos] Resultado búsqueda: {} items", resultados.size());

		return resultados.stream().map(mapper::toDTO).toList();
	}

	/* ================== helpers privados ================== */

	private void validarPatenteNoDuplicada(String patente) {
		repo.findByPatenteIgnoreCase(patente).ifPresent(v -> {
			log.warn("[Svc][Vehiculos] Patente duplicada patente={}", patente);
			throw new VehiculoException("Patente ya registrada");
		});
	}

	private void validarRefrigeradoConRango(VehiculoCreateDTO dto) {
		if (dto.isRefrigerado() && (dto.getRangoTempMin() == null || dto.getRangoTempMax() == null)) {
			log.warn("[Svc][Vehiculos] Vehículo refrigerado sin rango de temperatura definido");
			throw new VehiculoException("Vehículo refrigerado requiere rango de temperatura");
		}
	}

	private String normalize(String patente) {
		if (patente == null)
			return null;
		var trimmed = patente.trim();
		return trimmed.isEmpty() ? null : trimmed;
	}
}
