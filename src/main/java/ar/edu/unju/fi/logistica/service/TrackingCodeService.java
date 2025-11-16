package ar.edu.unju.fi.logistica.service;

import java.util.Arrays;

import ar.edu.unju.fi.logistica.domain.Envio;

/**
 * Servicio para generar códigos de tracking no persistidos.
 */
public interface TrackingCodeService {
	/** Genera el código público (e.g., "ICG-XXXXX...") y su hash binario. */
    GeneratedTracking generate();

    /** Normaliza (trim+upper+remueve prefijo si aplica) y devuelve hash binario para búsqueda. */
    byte[] hashFromPublicCode(String publicCode);

    /** Si querés mostrar el código al consultar: (opcional) */
    default String recoverPublicCode(Envio envio) {
        // Si no se guarda en ningún lado el “plano”, normalmente NO se puede recuperar de un hash one-way.
        // Podés optar por no mostrarlo en GET o devolverlo sólo al crear.
        return null;
    }

    record GeneratedTracking(String publicCode, byte[] hash) {
        public GeneratedTracking {
            hash = (hash != null) ? hash.clone() : null; // copia defensiva al construir
        }
        @Override public byte[] hash() {                 // copia defensiva al exponer
            return (hash != null) ? hash.clone() : null;
        }
        @Override
        public boolean equals(Object o) {
            // ✅ Usamos el record pattern directo
            return (o instanceof GeneratedTracking(String otherCode, byte[] otherHash))
                && java.util.Objects.equals(publicCode, otherCode)
                && java.util.Arrays.equals(hash, otherHash);
        }
        @Override public int hashCode() {
            return 31 * java.util.Objects.hash(publicCode) + Arrays.hashCode(hash);
        }
        @Override public String toString() {
            return "GeneratedTracking{publicCode='%s', hash=%s}"
                    .formatted(publicCode, Arrays.toString(hash));
        }
    }
}
