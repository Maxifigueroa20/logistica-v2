package ar.edu.unju.fi.logistica.domain;

import java.math.BigDecimal;

import jakarta.persistence.*;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Entity
@Table(name = "paquetes_refrigerados")
@PrimaryKeyJoinColumn(name = "id")
public class PaqueteRefrigerado extends Paquete {

    @Column(name = "temperatura_objetivo", precision = 5, scale = 2, nullable = false)
    private BigDecimal temperaturaObjetivo;

    @Column(name = "rango_min", precision = 5, scale = 2, nullable = false)
    private BigDecimal rangoMin;

    @Column(name = "rango_max", precision = 5, scale = 2, nullable = false)
    private BigDecimal rangoMax;

    @PositiveOrZero
    @Column(name = "horas_max_fuera_frio", nullable = false)
    private int horasMaxFueraFrio;
}