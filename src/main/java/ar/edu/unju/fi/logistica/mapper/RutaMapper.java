package ar.edu.unju.fi.logistica.mapper;

import java.util.List;

import org.mapstruct.*;

import ar.edu.unju.fi.logistica.domain.Envio;
import ar.edu.unju.fi.logistica.domain.Ruta;
import ar.edu.unju.fi.logistica.dto.ruta.RutaCreateDTO;
import ar.edu.unju.fi.logistica.dto.ruta.RutaDTO;

@Mapper(config = MapperConfigBase.class)
public interface RutaMapper {

	@Mapping(target = "vehiculoId", source = "vehiculo.id")
	@Mapping(target = "enviosIds", expression = "java(mapEnviosIds(ruta.getEnvios()))")
	RutaDTO toDTO(Ruta ruta);

	@Mapping(target = "id", ignore = true)
	@Mapping(target = "vehiculo", ignore = true)
	@Mapping(target = "envios", ignore = true)
	Ruta toEntity(RutaCreateDTO dto);

	default List<Long> mapEnviosIds(List<Envio> envios) {
		if (envios == null || envios.isEmpty())
			return List.of();
		return envios.stream().map(Envio::getId).toList();
	}
}
