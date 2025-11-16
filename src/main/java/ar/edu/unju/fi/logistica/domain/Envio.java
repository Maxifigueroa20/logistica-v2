package ar.edu.unju.fi.logistica.domain;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import ar.edu.unju.fi.logistica.enums.EstadoEnvio;
import ar.edu.unju.fi.logistica.exception.EnvioException;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Entity
@Table(name = "envios", indexes = { @Index(name = "ix_envio_estado", columnList = "estado_actual"),
		@Index(name = "ix_envio_ruta", columnList = "ruta_id"),
		@Index(name = "ix_envio_remitente", columnList = "remitente_id"),
		@Index(name = "ix_envio_destinatario", columnList = "destinatario_id") })
public class Envio {

	@Id @GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name = "remitente_id", nullable = false)
	private Cliente remitente;

	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name = "destinatario_id", nullable = false)
	private Cliente destinatario;

	@NotBlank @Size(max = 180)
	@Column(name = "direccion_entrega", nullable = false, length = 180)
	private String direccionEntrega;

	@NotBlank @Size(max = 12)
	@Column(name = "codigo_postal", nullable = false, length = 12)
	private String codigoPostal;

	@NotNull
	@Enumerated(EnumType.STRING)
	@Column(name = "estado_actual", nullable = false, length = 20)
	private EstadoEnvio estadoActual;

	@Column(name = "tracking_hash", nullable = false, unique = true, updatable = false, columnDefinition = "BINARY(32)")
	private byte[] trackingCodeHash;

	@Lob
	@Basic(fetch = FetchType.LAZY)
	@Column(name = "comprobante_entrega", columnDefinition = "MEDIUMBLOB")
	private byte[] comprobanteEntrega;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "ruta_id")
	private Ruta ruta;

	@OneToMany(mappedBy = "envio", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<Paquete> paquetes = new ArrayList<>();

	@OneToMany(mappedBy = "envio", cascade = CascadeType.ALL, orphanRemoval = true)
	@OrderBy("fechaHora ASC")
	private List<HistorialEstadoEnvio> historiales = new ArrayList<>();

	/* ===================== API de dominio (transiciones) ===================== */

	/**
	 * Genera una entrada de historial inicial (sin cambiar estado). Úsalo al crear
	 * el envío.
	 */
	public void registrarAlta(String observacion) {
		if (estadoActual == null)
			throw new EnvioException("Estado inicial no establecido");
		registrarHistorial(estadoActual, estadoActual, observacion);
	}

	/** GENERADO -> EN_ALMACEN */
	public void aAlmacen(String observacion) {
		EstadoEnvio nuevo = this.estadoActual.aAlmacen();
		transicionar(nuevo, observacion);
	}

	/** EN_ALMACEN -> EN_RUTA */
	public void aRuta(String observacion) {
		EstadoEnvio nuevo = this.estadoActual.aRuta();
		transicionar(nuevo, observacion);
	}

	/** EN_RUTA -> ENTREGADO (requiere comprobante) */
	public void aEntregado(byte[] comprobante, String observacion) {
		EstadoEnvio nuevo = this.estadoActual.aEntregado(comprobante);
		this.comprobanteEntrega = comprobante;
		transicionar(nuevo, observacion);
	}

	/** GENERADO -> CANCELADO */
	public void cancelar(String motivo) {
		EstadoEnvio nuevo = this.estadoActual.cancelar(motivo);
		transicionar(nuevo, "Cancelado: " + (motivo != null ? motivo : "-"));
	}

	/** ENTREGADO -> DEVUELTO */
	public void devolver(String observacion) {
		EstadoEnvio nuevo = this.estadoActual.devolver(observacion);
		transicionar(nuevo, observacion);
	}

	/*
	 * ===================== Infraestructura de cambio + historial
	 * =====================
	 */

	private void transicionar(EstadoEnvio nuevo, String obs) {
		EstadoEnvio anterior = this.estadoActual;
		if (anterior == nuevo) {
			throw new EnvioException("El envío ya está en estado " + nuevo);
		}
		this.estadoActual = nuevo;
		registrarHistorial(anterior, nuevo, obs);
	}

	private void registrarHistorial(EstadoEnvio anterior, EstadoEnvio nuevo, String obs) {
		HistorialEstadoEnvio h = new HistorialEstadoEnvio();
		h.setEnvio(this);
		h.setEstadoAnterior(anterior != null ? anterior : nuevo);
		h.setEstadoNuevo(nuevo);
		h.setFechaHora(LocalDateTime.now());
		h.setObservacion(obs);
		this.historiales.add(h);
	}

	/* ===================== Helpers existentes ===================== */

	public void addPaquete(Paquete p) {
		paquetes.add(p);
		p.setEnvio(this);
	}

	public void removePaquete(Paquete p) {
		paquetes.remove(p);
		p.setEnvio(null);
	}

	public void addHistorial(HistorialEstadoEnvio h) {
		historiales.add(h);
		h.setEnvio(this);
	}

	public double getPesoTotal() {
		return paquetes.stream().mapToDouble(Paquete::getPesoKg).sum();
	}

	public double getVolumenTotal() {
		return paquetes.stream().mapToDouble(Paquete::getVolumenDm3).sum();
	}

	/** Derivado: true si hay al menos un paquete refrigerado */
	public boolean requiereFrio() {
		return paquetes.stream().anyMatch(PaqueteRefrigerado.class::isInstance);
	}

	@Transient
	public boolean hasComprobante() {
		return comprobanteEntrega != null && comprobanteEntrega.length > 0;
	}
}
