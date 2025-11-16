package ar.edu.unju.fi.logistica.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Entity
@Table(name = "paquetes_refrigerados")
@PrimaryKeyJoinColumn(name = "id")
public class PaqueteRefrigerado extends Paquete {

    @Column(name = "temperatura_objetivo", nullable = false)
    private double temperaturaObjetivo;

    @Column(name = "rango_min", nullable = false)
    private double rangoMin;

    @Column(name = "rango_max", nullable = false)
    private double rangoMax;

    @PositiveOrZero
    @Column(name = "horas_max_fuera_frio", nullable = false)
    private int horasMaxFueraFrio;
}