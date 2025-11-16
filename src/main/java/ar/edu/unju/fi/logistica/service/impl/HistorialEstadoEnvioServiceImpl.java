package ar.edu.unju.fi.logistica.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ar.edu.unju.fi.logistica.dto.historial.HistorialEstadoEnvioDTO;
import ar.edu.unju.fi.logistica.mapper.HistorialEstadoEnvioMapper;
import ar.edu.unju.fi.logistica.repository.HistorialEstadoEnvioRepository;
import ar.edu.unju.fi.logistica.service.HistorialEstadoEnvioService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HistorialEstadoEnvioServiceImpl implements HistorialEstadoEnvioService {

    private final HistorialEstadoEnvioRepository repo;
    private final HistorialEstadoEnvioMapper mapper;

    @Override
    public List<HistorialEstadoEnvioDTO> listarPorEnvio(Long envioId) {
        return repo.findByEnvio_IdOrderByFechaHoraAsc(envioId)
                .stream().map(mapper::toDTO).toList();
    }
}
