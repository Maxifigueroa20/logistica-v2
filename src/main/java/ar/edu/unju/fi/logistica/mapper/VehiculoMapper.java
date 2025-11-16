package ar.edu.unju.fi.logistica.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import ar.edu.unju.fi.logistica.domain.Vehiculo;
import ar.edu.unju.fi.logistica.dto.vehiculo.VehiculoCreateDTO;
import ar.edu.unju.fi.logistica.dto.vehiculo.VehiculoDTO;

@Mapper(config = MapperConfigBase.class)
public interface VehiculoMapper {

	VehiculoDTO toDTO(Vehiculo entity);

	@Mapping(target = "id", ignore = true)
	Vehiculo toEntity(VehiculoCreateDTO dto);
}
