package ar.edu.unju.fi.logistica.service;

import java.util.List;

import ar.edu.unju.fi.logistica.dto.vehiculo.VehiculoCreateDTO;
import ar.edu.unju.fi.logistica.dto.vehiculo.VehiculoDTO;
import ar.edu.unju.fi.logistica.dto.vehiculo.VehiculoSearchCriteria;

/**
 * Servicio para gestión de vehículos y consultas por capacidad/refrigeración.
 */
public interface VehiculoService {

	/** Crea un vehículo (refrigerado o no). */
	VehiculoDTO crear(VehiculoCreateDTO dto);

	/** Lista todos los vehículos. */
	List<VehiculoDTO> listar();

	/** Busca por patente (única). */
	VehiculoDTO buscarPorPatente(String patente);
	
	List<VehiculoDTO> buscarPorPatenteLike(String patente);

	/**
	 * Búsqueda unificada de vehículos con filtros opcionales.
	 *
	 * @param patente             Fragmento o patente completa (case-insensitive).
	 *                            Null/blank → no filtra.
	 * @param refrigerado         true/false para filtrar, null para ignorar.
	 * @param capacidadPesoMin    Peso mínimo soportado (nullable).
	 * @param capacidadPesoMax    Peso máximo soportado (nullable).
	 * @param capacidadVolumenMin Volumen mínimo soportado (nullable).
	 * @param capacidadVolumenMax Volumen máximo soportado (nullable).
	 * @param tempMin             Temperatura mínima requerida (nullable, solo
	 *                            aplica a refrigerados).
	 * @param tempMax             Temperatura máxima requerida (nullable, solo
	 *                            aplica a refrigerados).
	 *
	 *                            Sin parámetros (todos null/blank) → equivalente a
	 *                            listar().
	 */
	List<VehiculoDTO> buscar(VehiculoSearchCriteria criteria);
}
