package ar.edu.unju.fi.logistica.utils;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Generadores centralizados de identificadores únicos para tests.
 * Garantizan unicidad y longitud máxima según las restricciones del modelo.
 */
public final class UniqueIds {

    private static final AtomicInteger SEQ = new AtomicInteger(1);

    private UniqueIds() {}

    /** CUIT o documento único (≤ 20 caracteres). */
    public static String doc20(String base) {
        String sanitized = base == null ? "" : base.replaceAll("-", "");
        String sfx = String.valueOf(SEQ.getAndIncrement());
        String out = sanitized + sfx;
        return out.length() > 20 ? out.substring(0, 20) : out;
    }

    /** Código único (≤ 40 caracteres). */
    public static String code40(String prefix) {
        String p = prefix == null ? "" : prefix;
        String raw = p + "-" + UUID.randomUUID().toString().replace("-", "");
        return raw.length() > 40 ? raw.substring(0, 40) : raw;
    }

    /** Patente única (≤ 15 caracteres). */
    public static String patente15(String base) {
        String b = base == null ? "" : base.replaceAll("[^A-Za-z0-9]", "");
        String out = b + "-" + SEQ.getAndIncrement();
        return out.length() > 15 ? out.substring(0, 15) : out;
    }
}
