package ar.edu.unju.fi.logistica.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ar.edu.unju.fi.logistica.domain.Paquete;
import ar.edu.unju.fi.logistica.dto.paquete.PaqueteCreateDTO;
import ar.edu.unju.fi.logistica.dto.paquete.PaqueteDTO;
import ar.edu.unju.fi.logistica.dto.paquete.PaqueteFragilCreateDTO;
import ar.edu.unju.fi.logistica.dto.paquete.PaqueteRefrigeradoCreateDTO;
import ar.edu.unju.fi.logistica.mapper.PaqueteMapper;
import ar.edu.unju.fi.logistica.repository.PaqueteRepository;
import ar.edu.unju.fi.logistica.service.PaqueteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaqueteServiceImpl implements PaqueteService {

	private final PaqueteRepository repo;
	private final PaqueteMapper mapper;

	@Override
	@Transactional
	public List<PaqueteDTO> crearLote(List<? extends PaqueteCreateDTO> dtos) {
		if (dtos == null || dtos.isEmpty()) {
			throw new IllegalArgumentException("Debe enviar al menos un paquete para crear");
		}

		log.info("[Svc][Paquetes] Crear lote de {} paquete(s)", dtos.size());

		List<Paquete> entidades = dtos.stream().map(this::mapToEntity).toList();

		var guardados = repo.saveAll(entidades);

		log.info("[Svc][Paquetes] Lote creado, {} paquete(s) persistidos", guardados.size());
		return guardados.stream().map(mapper::toDTO).toList();
	}

	@Override
	public List<PaqueteDTO> buscar(String tipo, Double pesoMin, Double pesoMax, Double volMin, Double volMax) {

		String tipoNormalizado = normalizeTipo(tipo);
		validarRango(pesoMin, pesoMax, "peso");
		validarRango(volMin, volMax, "volumen");

		log.debug("[Svc][Paquetes] Buscar → tipo={}, peso[{},{}], vol[{},{}]", tipoNormalizado, pesoMin, pesoMax,
				volMin, volMax);

		var entidades = repo.buscarFiltrado(tipoNormalizado, pesoMin, pesoMax, volMin, volMax);

		log.debug("[Svc][Paquetes] Resultado búsqueda → {} paquete(s)", entidades.size());
		return entidades.stream().map(mapper::toDTO).toList();
	}

	/* ================== helpers privados ================== */

	private Paquete mapToEntity(PaqueteCreateDTO dto) {
		if (dto instanceof PaqueteFragilCreateDTO pf) {
			return mapper.toEntity(pf);
		}
		if (dto instanceof PaqueteRefrigeradoCreateDTO pr) {
			return mapper.toEntity(pr);
		}
		throw new IllegalArgumentException("Tipo de PaqueteCreateDTO no soportado: " + dto.getClass());
	}

	private void validarRango(Double min, Double max, String label) {
		if (min != null && max != null && min > max) {
			throw new IllegalArgumentException("Rango de " + label + " inválido: mínimo mayor que máximo");
		}
	}

	/**
	 * Normaliza el tipo a "FRAGIL" o "REFRIGERADO". Si llega algo raro → null
	 * (equivale a "todos").
	 */
	private String normalizeTipo(String tipo) {
		if (tipo == null)
			return null;
		String t = tipo.trim().toUpperCase();
		if (t.isEmpty())
			return null;

		return switch (t) {
		case "FRAGIL", "FRÁGIL" -> "FRAGIL";
		case "REFRIGERADO", "REFRIG" -> "REFRIGERADO";
		default -> throw new IllegalArgumentException("Tipo de Paquete inválido.");
		};
	}
}
