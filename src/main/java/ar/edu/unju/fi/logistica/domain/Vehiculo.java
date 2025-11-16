package ar.edu.unju.fi.logistica.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Entity
@Table(name = "vehiculos", indexes = {
    @Index(name = "ix_vehiculo_patente", columnList = "patente", unique = true)
})
public class Vehiculo {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank @Size(max = 15)
    @Column(name = "patente", nullable = false, length = 15, unique = true)
    private String patente;

    @Positive
    @Column(name = "capacidad_peso_kg", nullable = false)
    private double capacidadPesoKg;

    @Positive
    @Column(name = "capacidad_volumen_dm3", nullable = false)
    private double capacidadVolumenDm3;

    @Column(name = "refrigerado", nullable = false)
    private boolean refrigerado;

    @Column(name = "rango_temp_min")
    private Double rangoTempMin;

    @Column(name = "rango_temp_max")
    private Double rangoTempMax;
}
