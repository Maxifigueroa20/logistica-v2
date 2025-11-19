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
@Inheritance(strategy = InheritanceType.JOINED)
@Table(name = "paquetes", indexes = {
    @Index(name = "ix_paquete_codigo", columnList = "codigo", unique = true),
    @Index(name = "ix_paquete_envio", columnList = "envio_id")
})
@DiscriminatorColumn(name = "tipo_paquete")
public abstract class Paquete {

	@Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected Long id;

    @NotBlank @Size(max = 40)
    @Column(name = "codigo", nullable = false, length = 40, unique = true)
    protected String codigo;

    @Positive
    @Column(name = "peso_kg", precision = 7, scale = 2, nullable = false)
    protected BigDecimal pesoKg;

    @Positive
    @Column(name = "volumen_dm3", precision = 7, scale = 2, nullable = false)
    protected BigDecimal volumenDm3;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "envio_id", nullable = true)
    protected Envio envio;
}
