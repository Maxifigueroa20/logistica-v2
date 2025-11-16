package ar.edu.unju.fi.logistica.domain;

import ar.edu.unju.fi.logistica.enums.NivelFragilidad;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Entity
@Table(name = "paquetes_fragiles")
@PrimaryKeyJoinColumn(name = "id")
public class PaqueteFragil extends Paquete {

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "nivel_fragilidad", nullable = false, length = 10)
    private NivelFragilidad nivelFragilidad;

    @Column(name = "seguro_adicional", nullable = false)
    private boolean seguroAdicional;
}