package ar.edu.unju.fi.logistica.mapper;

import org.mapstruct.*;

import ar.edu.unju.fi.logistica.domain.Cliente;
import ar.edu.unju.fi.logistica.dto.cliente.ClienteCreateDTO;
import ar.edu.unju.fi.logistica.dto.cliente.ClienteDTO;
import ar.edu.unju.fi.logistica.dto.cliente.ClienteUpdateDTO;

@Mapper(config = MapperConfigBase.class)
public interface ClienteMapper {

	ClienteDTO toDTO(Cliente entity);

	@Mapping(target = "id", ignore = true)
	Cliente toEntity(ClienteCreateDTO dto);

	@BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)

	@Mapping(target = "id", ignore = true)
	@Mapping(target = "nombreRazonSocial", ignore = true)
	@Mapping(target = "documentoCuit", ignore = true)
	void update(@MappingTarget Cliente entity, ClienteUpdateDTO dto);
}
