package ar.edu.unju.fi.logistica.service.impl;

import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ar.edu.unju.fi.logistica.domain.Cliente;
import ar.edu.unju.fi.logistica.domain.Envio;
import ar.edu.unju.fi.logistica.domain.Paquete;
import ar.edu.unju.fi.logistica.dto.envio.EnvioCreateDTO;
import ar.edu.unju.fi.logistica.dto.envio.EnvioDTO;
import ar.edu.unju.fi.logistica.dto.envio.EnvioHistorialDTO;
import ar.edu.unju.fi.logistica.enums.EstadoEnvio;
import ar.edu.unju.fi.logistica.exception.EnvioException;
import ar.edu.unju.fi.logistica.mapper.EnvioMapper;
import ar.edu.unju.fi.logistica.repository.ClienteRepository;
import ar.edu.unju.fi.logistica.repository.EnvioRepository;
import ar.edu.unju.fi.logistica.repository.PaqueteRepository;
import ar.edu.unju.fi.logistica.service.EnvioService;
import ar.edu.unju.fi.logistica.service.TrackingCodeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EnvioServiceImpl implements EnvioService {

	private static final String ERR_ENVIO_NO_ENCONTRADO = "Envío no encontrado: ";
	private static final int MAX_COMPROBANTE_BYTES = 16 * 1024 * 1024;

	private final EnvioRepository envioRepo;
	private final ClienteRepository clienteRepo;
	private final PaqueteRepository paqueteRepo;
	private final EnvioMapper envioMapper;
	private final TrackingCodeService tracking;

	@Override
	@Transactional
	public EnvioDTO crear(EnvioCreateDTO dto) {
		log.info("[Envio] Crear → remitenteDoc={}, destinatarioDoc={}, codPaquetes={}", dto.getRemitenteDocumento(),
				dto.getDestinatarioDocumento(), dto.getCodigosPaquete() != null ? dto.getCodigosPaquete().size() : 0);

		Cliente remitente = buscarClientePorDocumento(dto.getRemitenteDocumento(), "Remitente no encontrado");
		Cliente destinatario = buscarClientePorDocumento(dto.getDestinatarioDocumento(), "Destinatario no encontrado");

		validarRemitenteDestinatarioDistintos(remitente, destinatario);
		List<String> codigos = normalizarCodigos(dto.getCodigosPaquete());
		List<Paquete> paquetes = buscarPaquetesPorCodigo(codigos);

		Envio envio = construirEnvio(dto, remitente, destinatario, paquetes);
		var trackingData = tracking.generate();
		envio.setTrackingCodeHash(trackingData.hash());
		envio.registrarAlta("Alta de envío");

		envioRepo.save(envio);

		EnvioDTO out = envioMapper.toDTO(envio, trackingData.publicCode());
		log.info("[Envio] Creado id={} tracking={}", out.getId(), out.getTrackingCode());
		return out;
	}

	@Override
	@Transactional
	public EnvioDTO cancelar(Long envioId, String motivo) {
		log.info("[Envio] Cancelar → id={}, motivo='{}'", envioId, motivo);
		Envio envio = get(envioId);
		envio.cancelar(motivo); // GENERADO → CANCELADO (según enum EstadoEnvio)
		envioRepo.save(envio);
		return envioMapper.toDTO(envio, null);
	}

	@Override
	@Transactional
	public EnvioDTO marcarEntregado(Long envioId, byte[] comprobante) {
		log.info("[Envio] ENTREGAR → id={} (bytes={})", envioId, comprobante != null ? comprobante.length : -1);

		validarComprobante(comprobante);

		Envio envio = get(envioId);
		envio.aEntregado(comprobante, "Entregado con comprobante");
		envioRepo.save(envio);

		EnvioDTO dto = envioMapper.toDTO(envio, null);
		log.info("[Envio] Entregado id={} estado={} hasComprobante={}", dto.getId(), dto.getEstadoActual(),
				dto.isHasComprobante());
		return dto;
	}

	@Override
	@Transactional
	public EnvioDTO actualizarEstado(Long envioId, EstadoEnvio nuevoEstado, String observacion) {
		log.info("[Envio] Actualizar estado → id={}, nuevoEstado={}, obs='{}'", envioId, nuevoEstado, observacion);

		Envio envio = get(envioId);

		switch (nuevoEstado) {
		case EN_ALMACEN -> envio.aAlmacen(observacion);
		case EN_RUTA -> envio.aRuta(observacion);
		case DEVUELTO -> envio.devolver(observacion);
		case ENTREGADO -> throw new EnvioException("Use marcarEntregado() para ENTREGADO");
		case GENERADO -> throw new EnvioException("No se puede volver a GENERADO");
		case CANCELADO -> throw new EnvioException("Use cancelar() para CANCELADO");
		}

		envioRepo.save(envio);
		EnvioDTO dto = envioMapper.toDTO(envio, null);
		log.info("[Envio] Estado actualizado id={} estado={}", dto.getId(), dto.getEstadoActual());
		return dto;
	}

	@Override
	public EnvioDTO buscarPorId(Long id) {
		log.debug("[Envio] Buscar por id → {}", id);
		return envioMapper.toDTO(get(id), null);
	}

	@Override
	public List<EnvioDTO> buscarFiltrado(String remitenteDocumento, String destinatarioDocumento, EstadoEnvio estado) {
		String remDoc = normalize(remitenteDocumento);
		String desDoc = normalize(destinatarioDocumento);

		log.debug("[Envio] Buscar filtrado → remitenteDoc='{}', destinatarioDoc='{}', estado={}", remDoc, desDoc,
				estado);

		return envioRepo.findAll().stream()
				.filter(e -> remDoc == null || remDoc.equals(e.getRemitente().getDocumentoCuit()))
				.filter(e -> desDoc == null || desDoc.equals(e.getDestinatario().getDocumentoCuit()))
				.filter(e -> estado == null || e.getEstadoActual() == estado).map(e -> envioMapper.toDTO(e, null))
				.toList();
	}

	@Override
	public EnvioHistorialDTO buscarPorTracking(String publicCode) {
		log.info("[Envio] Buscar por tracking → '{}'", publicCode);
		byte[] hash = tracking.hashFromPublicCode(publicCode);

		Envio envio = envioRepo.findByTrackingCodeHashWithHistorial(hash)
				.orElseThrow(() -> new EnvioException("Envío no encontrado para tracking: " + publicCode));

		String normalized = publicCode != null ? publicCode.trim().toUpperCase() : null;
		EnvioHistorialDTO dto = envioMapper.toHistDTO(envio, normalized);

		log.info("[Envio] Encontrado por tracking → id={} estado={}", dto.getId(), dto.getEstadoActual());
		return dto;
	}

	// =========================================================
	// HELPERS PRIVADOS
	// =========================================================
	private Envio get(Long id) {
		return envioRepo.findById(id).orElseThrow(() -> new EnvioException(ERR_ENVIO_NO_ENCONTRADO + id));
	}

	private Cliente buscarClientePorDocumento(String documento, String mensajeError) {
		String doc = normalize(documento);
		if (doc == null) {
			throw new EnvioException("Documento/CUIT obligatorio");
		}
		return clienteRepo.findByDocumentoCuit(doc)
				.orElseThrow(() -> new EnvioException(mensajeError + " para documento " + doc));
	}

	private void validarRemitenteDestinatarioDistintos(Cliente remitente, Cliente destinatario) {
		if (Objects.equals(remitente.getId(), destinatario.getId())) {
			throw new EnvioException("Remitente y destinatario no pueden ser el mismo cliente");
		}
	}

	private List<String> normalizarCodigos(List<String> codigos) {
		if (codigos == null || codigos.isEmpty()) {
			throw new EnvioException("El envío debe contener al menos un código de paquete");
		}

		return codigos.stream().map(this::normalize).filter(c -> c != null).distinct().toList();
	}

	private List<Paquete> buscarPaquetesPorCodigo(List<String> codigos) {
		var paquetes = paqueteRepo.findByCodigoIn(codigos);
		if (paquetes.size() != codigos.size()) {
			// detectar cuáles no se encontraron
			var encontrados = paquetes.stream().map(Paquete::getCodigo).toList();
			var faltantes = codigos.stream().filter(c -> !encontrados.contains(c)).toList();
			throw new EnvioException("Paquetes no encontrados para códigos: " + faltantes);
		}

		paquetes.forEach(p -> {
			if (p.getEnvio() != null) {
				throw new EnvioException("El paquete ya está asignado a un envío: " + p.getCodigo());
			}
		});

		return paquetes;
	}

	private Envio construirEnvio(EnvioCreateDTO dto, Cliente remitente, Cliente destinatario, List<Paquete> paquetes) {

		Envio envio = envioMapper.toEntity(dto);
		envio.setRemitente(remitente);
		envio.setDestinatario(destinatario);
		envio.setEstadoActual(EstadoEnvio.GENERADO);

		paquetes.forEach(p -> p.setEnvio(envio));
		envio.setPaquetes(paquetes);

		return envio;
	}

	private void validarComprobante(byte[] comprobante) {
		if (comprobante == null || comprobante.length == 0) {
			throw new EnvioException("Se requiere comprobante (contenido binario)");
		}
		if (comprobante.length > MAX_COMPROBANTE_BYTES) {
			throw new EnvioException("El comprobante excede el tamaño máximo permitido (16MB)");
		}
	}

	private String normalize(String value) {
		if (value == null)
			return null;
		String t = value.trim();
		return t.isEmpty() ? null : t;
	}
}
