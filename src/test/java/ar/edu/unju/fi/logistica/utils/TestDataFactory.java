package ar.edu.unju.fi.logistica.utils;

import java.util.ArrayList;
import java.util.List;

import ar.edu.unju.fi.logistica.dto.cliente.ClienteCreateDTO;
import ar.edu.unju.fi.logistica.dto.envio.EnvioCreateDTO;
import ar.edu.unju.fi.logistica.dto.paquete.PaqueteFragilCreateDTO;
import ar.edu.unju.fi.logistica.dto.paquete.PaqueteRefrigeradoCreateDTO;
import ar.edu.unju.fi.logistica.dto.vehiculo.VehiculoCreateDTO;
import ar.edu.unju.fi.logistica.dto.vehiculo.VehiculoSearchCriteria;
import ar.edu.unju.fi.logistica.enums.NivelFragilidad;

/**
 * Factoría de DTOs para simplificar la creación de datos en tests.
 */
public final class TestDataFactory {

	private TestDataFactory() {
	}

	// * ==== helpers seguros para strings ==== */

	/** Convierte a [a-z0-9], reemplaza lo demás y provee fallback "user". */
	private static String slug(String s) {
		if (s == null)
			return "user";
		String out = s.toLowerCase().replaceAll("[^a-z0-9]", "");
		return out.isBlank() ? "user" : out;
	}

	/** Local-part de email: slug(nombre) + "-" + doc, acotado a 30 chars. */
	private static String emailLocal(String nombre, String doc) {
		String base = slug(nombre);
		String local = doc == null ? base : (base + "-" + doc);
		return local.length() > 30 ? local.substring(0, 30) : local;
	}

	/* ==== factories ==== */

	/** Cliente genérico (documento ≤20, email robusto). */
	public static ClienteCreateDTO cliente(String nombre, String docBase) {
		String doc = UniqueIds.doc20(docBase);
		String email = emailLocal(nombre, doc) + "@mail.com";

		return ClienteCreateDTO.builder().nombreRazonSocial(nombre).documentoCuit(doc).email(email).telefono("388-000")
				.direccionPrincipal("Calle 123").codigoPostal("4600").build();
	}

	/** Paquete frágil genérico. */
	public static PaqueteFragilCreateDTO fragil(double kg, double dm3) {
		return PaqueteFragilCreateDTO.builder().codigo(UniqueIds.code40("PF")).pesoKg(kg).volumenDm3(dm3)
				.nivelFragilidad(NivelFragilidad.MEDIA).seguroAdicional(false).build();
	}

	/** Paquete refrigerado genérico. */
	public static PaqueteRefrigeradoCreateDTO refri(double kg, double dm3, double tObj) {
		return PaqueteRefrigeradoCreateDTO.builder().codigo(UniqueIds.code40("PR")).pesoKg(kg).volumenDm3(dm3)
				.temperaturaObjetivo(tObj).rangoMin(-10d).rangoMax(10d).horasMaxFueraFrio(2).build();
	}

	/** Vehículo no refrigerado. */
	public static VehiculoCreateDTO vehiculoNoRefri(String basePatente, double kg, double dm3) {
		return VehiculoCreateDTO.builder().patente(UniqueIds.patente15(basePatente)).capacidadPesoKg(kg)
				.capacidadVolumenDm3(dm3).refrigerado(false).build();
	}

	/** Vehículo refrigerado. */
	public static VehiculoCreateDTO vehiculoRefri(String basePatente, double kg, double dm3, double tMin, double tMax) {
		return VehiculoCreateDTO.builder().patente(UniqueIds.patente15(basePatente)).capacidadPesoKg(kg)
				.capacidadVolumenDm3(dm3).refrigerado(true).rangoTempMin(tMin).rangoTempMax(tMax).build();
	}

	/** Envío genérico con lista de paquetes. */
	public static EnvioCreateDTO envio(String remitenteDocumento, String destinatarioDocumento,
			List<String> codigosPaquete) {

		List<String> codigos = new ArrayList<>(codigosPaquete);

		return EnvioCreateDTO.builder().remitenteDocumento(remitenteDocumento)
				.destinatarioDocumento(destinatarioDocumento).direccionEntrega("Belgrano 100").codigoPostal("4600")
				.codigosPaquete(codigos).build();
	}

	public static EnvioCreateDTO envio(String remitenteDocumento, String destinatarioDocumento,
			String... codigosPaquete) {
		return envio(remitenteDocumento, destinatarioDocumento, java.util.Arrays.asList(codigosPaquete));
	}

	public static VehiculoSearchCriteria vehiculoCriteriaRefrigerado(double capacidadPesoMin,
			double capacidadVolumenMin) {
		return VehiculoSearchCriteria.builder().refrigerado(true).capacidadPesoMin(capacidadPesoMin)
				.capacidadVolumenMin(capacidadVolumenMin).build();
	}
}
