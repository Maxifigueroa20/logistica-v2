package ar.edu.unju.fi.logistica.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ar.edu.unju.fi.logistica.dto.cliente.ClienteCreateDTO;
import ar.edu.unju.fi.logistica.dto.cliente.ClienteDTO;
import ar.edu.unju.fi.logistica.dto.cliente.ClienteUpdateDTO;
import ar.edu.unju.fi.logistica.exception.ClienteException;
import ar.edu.unju.fi.logistica.mapper.ClienteMapper;
import ar.edu.unju.fi.logistica.repository.ClienteRepository;
import ar.edu.unju.fi.logistica.service.ClienteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ClienteServiceImpl implements ClienteService {

	private static final String ERR_CLIENTE_NO_ENCONTRADO = "Cliente no encontrado";

	private final ClienteRepository repo;
	private final ClienteMapper mapper;

	@Override
	@Transactional
	public ClienteDTO crear(ClienteCreateDTO dto) {
		log.info("[Svc][Clientes] Crear cliente documento={}", dto.getDocumentoCuit());

		validarDocumentoNoDuplicado(dto.getDocumentoCuit());

		var entity = mapper.toEntity(dto);
		var saved = repo.save(entity);

		log.info("[Svc][Clientes] Cliente creado id={} documento={}", saved.getId(), saved.getDocumentoCuit());
		return mapper.toDTO(saved);
	}

	@Override
	@Transactional
	public ClienteDTO actualizar(Long id, ClienteUpdateDTO dto) {
		log.info("[Svc][Clientes] Actualizar cliente id={}", id);

		var entity = repo.findById(id).orElseThrow(() -> new ClienteException(ERR_CLIENTE_NO_ENCONTRADO));

		mapper.update(entity, dto);
		var saved = repo.save(entity);

		log.info("[Svc][Clientes] Cliente actualizado id={}", saved.getId());
		return mapper.toDTO(saved);
	}

	@Override
	public ClienteDTO buscarPorId(Long id) {
		log.debug("[Svc][Clientes] Buscar por id={}", id);
		return repo.findById(id).map(mapper::toDTO).orElseThrow(() -> new ClienteException(ERR_CLIENTE_NO_ENCONTRADO));
	}

	@Override
	public List<ClienteDTO> listar() {
		log.debug("[Svc][Clientes] Listar todos los clientes");
		return repo.findAll().stream().map(mapper::toDTO).toList();
	}

	@Override
	public ClienteDTO buscarPorDocumento(String documentoCuit) {
		log.debug("[Svc][Clientes] Buscar documento EXACTO={}", documentoCuit);
		return repo.findByDocumentoCuit(documentoCuit).map(mapper::toDTO)
				.orElseThrow(() -> new ClienteException(ERR_CLIENTE_NO_ENCONTRADO));
	}

	@Override
	public List<ClienteDTO> buscarPorDocumentoLike(String documentoFragmento) {
		log.debug("[Svc][Clientes] Buscar por documento LIKE={}", documentoFragmento);

		return repo.findByDocumentoCuitContainingIgnoreCase(documentoFragmento).stream().map(mapper::toDTO).toList();
	}

	/* ================== helpers privados ================== */

	private void validarDocumentoNoDuplicado(String documentoCuit) {
		if (repo.existsByDocumentoCuit(documentoCuit)) {
			log.warn("[Svc][Clientes] Documento/CUIT duplicado documento={}", documentoCuit);
			throw new ClienteException("Documento/CUIT ya registrado");
		}
	}
}
