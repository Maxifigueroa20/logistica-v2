package ar.edu.unju.fi.logistica.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Entity
@Table(name = "clientes", indexes = {
    @Index(name = "ix_cliente_doc", columnList = "documento_cuit", unique = true),
    @Index(name = "ix_cliente_nombre", columnList = "nombre_razon_social")
})public class Cliente {

	@Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank @Size(max = 150)
    @Column(name = "nombre_razon_social", nullable = false, length = 150)
    private String nombreRazonSocial;

    @NotBlank @Size(max = 20)
    @Column(name = "documento_cuit", nullable = false, length = 20, unique = true)
    private String documentoCuit;

    @Size(max = 30)
    @Column(name = "telefono", length = 30)
    private String telefono;

    @Email @Size(max = 120)
    @Column(name = "email", length = 120)
    private String email;

    @NotBlank @Size(max = 180)
    @Column(name = "direccion_principal", nullable = false, length = 180)
    private String direccionPrincipal;

    @NotBlank @Size(max = 12)
    @Column(name = "codigo_postal", nullable = false, length = 12)
    private String codigoPostal;
}
