package ar.edu.unju.fi.logistica.domain;

import java.time.LocalDateTime;

import ar.edu.unju.fi.logistica.enums.EstadoEnvio;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Entity
@Table(name = "historial_envio", indexes = {
    @Index(name = "ix_historial_envio", columnList = "envio_id,fecha_hora")
})
public class HistorialEstadoEnvio {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "envio_id", nullable = false)
    private Envio envio;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "estado_anterior", nullable = false, length = 20)
    private EstadoEnvio estadoAnterior;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "estado_nuevo", nullable = false, length = 20)
    private EstadoEnvio estadoNuevo;

    @NotNull
    @Column(name = "fecha_hora", nullable = false)
    private LocalDateTime fechaHora;

    @Size(max = 255)
    @Column(name = "observacion", length = 255)
    private String observacion;
}
