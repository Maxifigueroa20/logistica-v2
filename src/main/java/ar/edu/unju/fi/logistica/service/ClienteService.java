package ar.edu.unju.fi.logistica.service;

import java.util.List;

import ar.edu.unju.fi.logistica.dto.cliente.ClienteCreateDTO;
import ar.edu.unju.fi.logistica.dto.cliente.ClienteDTO;
import ar.edu.unju.fi.logistica.dto.cliente.ClienteUpdateDTO;

/**
 * Servicio para gestión de clientes (remitentes y destinatarios).
 */
public interface ClienteService {

	/** Crea un nuevo cliente. */
	ClienteDTO crear(ClienteCreateDTO dto);

	/** Actualiza datos de contacto o dirección. */
	ClienteDTO actualizar(Long id, ClienteUpdateDTO dto);

	/** Obtiene un cliente por su ID. */
	ClienteDTO buscarPorId(Long id);

	/** Lista todos los clientes. */
	List<ClienteDTO> listar();

	/**
	 * Busca un cliente por documento/CUIT de forma EXACTA. Usado para lógica de
	 * negocio donde el documento es único (por ejemplo al crear un Envío).
	 */
	ClienteDTO buscarPorDocumento(String documentoCuit);

	/**
	 * Busca clientes cuyo documento/CUIT contenga el fragmento indicado
	 * (case-insensitive). Útil para búsquedas tipo autocomplete en la API.
	 */
	List<ClienteDTO> buscarPorDocumentoLike(String fragmento);
}
