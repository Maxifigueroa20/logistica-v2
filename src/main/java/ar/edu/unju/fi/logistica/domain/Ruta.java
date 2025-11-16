package ar.edu.unju.fi.logistica.domain;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Entity
@Table(
  name = "rutas",
  uniqueConstraints = @UniqueConstraint(name = "uk_ruta_fecha_vehiculo", columnNames = {"fecha","vehiculo_id"}),
  indexes = {
      @Index(name = "ix_ruta_fecha", columnList = "fecha"),
      @Index(name = "ix_ruta_vehiculo", columnList = "vehiculo_id")
  }
)
public class Ruta {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "fecha", nullable = false)
    private LocalDate fecha;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "vehiculo_id", nullable = false)
    private Vehiculo vehiculo;

    @OneToMany(mappedBy = "ruta")
    private List<Envio> envios = new ArrayList<>();
}
