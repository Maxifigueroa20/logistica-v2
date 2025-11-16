package ar.edu.unju.fi.logistica.service;

import java.util.List;

import ar.edu.unju.fi.logistica.dto.paquete.PaqueteCreateDTO;
import ar.edu.unju.fi.logistica.dto.paquete.PaqueteDTO;

/**
 * Servicio para gestión y consulta de paquetes (frágiles o refrigerados).
 */
public interface PaqueteService {

	/**
	 * Crea uno o varios paquetes (FRAGIL / REFRIGERADO) a partir de sus DTOs
	 * polimórficos.
	 */
	List<PaqueteDTO> crearLote(List<? extends PaqueteCreateDTO> dtos);

	/**
	 * Búsqueda unificada de paquetes.
	 *
	 * @param tipo    "FRAGIL", "REFRIGERADO" o null para todos
	 * @param pesoMin peso mínimo (nullable)
	 * @param pesoMax peso máximo (nullable)
	 * @param volMin  volumen mínimo (nullable)
	 * @param volMax  volumen máximo (nullable)
	 *
	 *                Si todos los parámetros son null → devuelve todos.
	 */
	List<PaqueteDTO> buscar(String tipo, Double pesoMin, Double pesoMax, Double volMin, Double volMax);
}
