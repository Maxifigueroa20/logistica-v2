package ar.edu.unju.fi.logistica.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import ar.edu.unju.fi.logistica.domain.HistorialEstadoEnvio;
import ar.edu.unju.fi.logistica.dto.historial.HistorialEstadoEnvioCreateDTO;
import ar.edu.unju.fi.logistica.dto.historial.HistorialEstadoEnvioDTO;

@Mapper(config = MapperConfigBase.class)
public interface HistorialEstadoEnvioMapper {

	@Mapping(target = "envioId", source = "envio.id")
	HistorialEstadoEnvioDTO toDTO(HistorialEstadoEnvio entity);

	List<HistorialEstadoEnvioDTO> toDTOs(List<HistorialEstadoEnvio> entities);

	/* Opcional (solo si querés backfill/testing directo): */
	@Mapping(target = "id", ignore = true)
	@Mapping(target = "envio", ignore = true)
	HistorialEstadoEnvio toEntity(HistorialEstadoEnvioCreateDTO dto);
}
