package ar.edu.unju.fi.logistica.dto.paquete;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.*;
import lombok.experimental.SuperBuilder;

/** Base de entrada para crear paquetes. */
@Getter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = """
		Datos base para la creación de un paquete. Este objeto funciona como plantilla
		para los tipos concretos FRAGIL o REFRIGERADO.
		El campo `type` indica qué subtipo se debe procesar.
		""")
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type", visible = true)
@JsonSubTypes({ @JsonSubTypes.Type(value = PaqueteFragilCreateDTO.class, name = "FRAGIL"),
		@JsonSubTypes.Type(value = PaqueteRefrigeradoCreateDTO.class, name = "REFRIGERADO") })
public abstract class PaqueteCreateDTO {
	@NotBlank
	@Schema(example = "PK-025", description = "Código único del paquete. Previamente cargado en el sistema.")
	private String codigo;
	@Positive(message = "El peso debe ser mayor a 0")
	@Schema(description = "Peso del paquete expresado en kilogramos.", example = "2.5")
	private double pesoKg;
	@Positive(message = "El volumen debe ser mayor a 0")
	@Schema(description = "Volumen del paquete expresado en dm³", example = "5.0")
	private double volumenDm3;
}
