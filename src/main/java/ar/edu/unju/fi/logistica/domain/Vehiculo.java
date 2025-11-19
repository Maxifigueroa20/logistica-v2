package ar.edu.unju.fi.logistica.domain;

import java.math.BigDecimal;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
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
    @Column(name = "capacidad_peso_kg", precision = 7, scale = 2, nullable = false)
    private BigDecimal capacidadPesoKg;

    @Positive
    @Column(name = "capacidad_volumen_dm3", precision = 7, scale = 2, nullable = false)
    private BigDecimal capacidadVolumenDm3;

    @Column(name = "refrigerado", nullable = false)
    private boolean refrigerado;

    @Column(name = "rango_temp_min", precision = 5, scale = 2)
    private BigDecimal rangoTempMin;

    @Column(name = "rango_temp_max", precision = 5, scale = 2)
    private BigDecimal rangoTempMax;
}
