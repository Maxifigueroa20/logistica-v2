package ar.edu.unju.fi.logistica.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import ar.edu.unju.fi.logistica.domain.Paquete;
import ar.edu.unju.fi.logistica.domain.PaqueteFragil;
import ar.edu.unju.fi.logistica.domain.PaqueteRefrigerado;
import ar.edu.unju.fi.logistica.dto.paquete.PaqueteCreateDTO;
import ar.edu.unju.fi.logistica.dto.paquete.PaqueteDTO;
import ar.edu.unju.fi.logistica.dto.paquete.PaqueteFragilCreateDTO;
import ar.edu.unju.fi.logistica.dto.paquete.PaqueteFragilDTO;
import ar.edu.unju.fi.logistica.dto.paquete.PaqueteRefrigeradoCreateDTO;
import ar.edu.unju.fi.logistica.dto.paquete.PaqueteRefrigeradoDTO;

@Mapper(config = MapperConfigBase.class)
public interface PaqueteMapper {

	// ===== ENTITY -> DTO =====
	@Mapping(target = "envioId", source = "envio.id")
	PaqueteFragilDTO toDTO(PaqueteFragil entity);

	@Mapping(target = "envioId", source = "envio.id")
	PaqueteRefrigeradoDTO toDTO(PaqueteRefrigerado entity);

	// Wrapper polimórfico
	default PaqueteDTO toDTO(Paquete entity) {
		if (entity == null)
			return null;
		if (entity instanceof PaqueteFragil pf)
			return toDTO(pf);
		if (entity instanceof PaqueteRefrigerado pr)
			return toDTO(pr);
		throw new IllegalArgumentException("Tipo de Paquete no soportado: " + entity.getClass());
	}

	// Lista
	default List<PaqueteDTO> toDTOs(List<? extends Paquete> entities) {
		if (entities == null || entities.isEmpty()) {
            return List.of();
        }
		return entities.stream().map(this::toDTO).toList();
	}


	@Mapping(target = "id", ignore = true)
	@Mapping(target = "envio", ignore = true)
	PaqueteFragil toEntity(PaqueteFragilCreateDTO dto);

	@Mapping(target = "id", ignore = true)
	@Mapping(target = "envio", ignore = true)
	PaqueteRefrigerado toEntity(PaqueteRefrigeradoCreateDTO dto);

	default List<Paquete> toEntities(List<PaqueteCreateDTO> dtos) {
		if (dtos == null || dtos.isEmpty())
			return List.of();
		return dtos.stream().map(dto -> {
			if (dto instanceof PaqueteFragilCreateDTO pf)
				return toEntity(pf);
			if (dto instanceof PaqueteRefrigeradoCreateDTO pr)
				return toEntity(pr);
			throw new IllegalArgumentException("Tipo de PaqueteCreateDTO no soportado: " + dto.getClass());
		}).toList();
	}
}
