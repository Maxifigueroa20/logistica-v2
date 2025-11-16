package ar.edu.unju.fi.logistica.service.impl;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Locale;

import org.springframework.stereotype.Service;

import ar.edu.unju.fi.logistica.service.TrackingCodeService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class TrackingCodeServiceImpl implements TrackingCodeService {

	private static final String PREFIX = "PGM-";
	/** Alfabeto sin caracteres confusos (Crockford-like). */
	private static final char[] ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789".toCharArray();
	private static final int CODE_LEN = 14; // parte alfanumérica (sin prefijo)
	private static final SecureRandom RNG = new SecureRandom();

	@Override
	public GeneratedTracking generate() {
		String core = randomCore(CODE_LEN);
		String publicCode = PREFIX + core; // p.ej. ICG-A7K9X1P2Q8R5T3
		byte[] hash = sha256(publicCode); // hash binario 32 bytes
		return new GeneratedTracking(publicCode, hash);
	}

	@Override
	public byte[] hashFromPublicCode(String publicCode) {
		if (publicCode == null || publicCode.isBlank()) {
			throw new IllegalArgumentException("Tracking inválido");
		}
		String normalized = normalize(publicCode);
		return sha256(normalized);
	}

	private static String randomCore(int len) {
		char[] out = new char[len];
		for (int i = 0; i < len; i++) {
			out[i] = ALPHABET[RNG.nextInt(ALPHABET.length)];
		}
		return new String(out);
	}

	private static String normalize(String input) {
		String s = input.trim().toUpperCase(Locale.ROOT);
		// Si viene sin prefijo, lo agregamos; si viene con prefijo, lo respetamos.
		return s.startsWith(PREFIX) ? s : PREFIX + s;
	}

	private static byte[] sha256(String value) {
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			return md.digest(value.getBytes(StandardCharsets.UTF_8));
		} catch (Exception e) {
			throw new IllegalStateException("No se pudo calcular hash", e);
		}
	}
}
