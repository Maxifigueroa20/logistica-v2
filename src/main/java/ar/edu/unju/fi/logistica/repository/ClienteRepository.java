package ar.edu.unju.fi.logistica.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import ar.edu.unju.fi.logistica.domain.Cliente;

/**
 * Acceso a clientes.
 * - Documento/CUIT único.
 */
public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    /** Busca por documento/CUIT (único, case-sensitive por lo general). */
    Optional<Cliente> findByDocumentoCuit(String documentoCuit);

    /** Verifica existencia de documento/CUIT (para validaciones de negocio). */
    boolean existsByDocumentoCuit(String documentoCuit);
    
    /**
     * Búsqueda "tipo autocomplete" por fragmento de documento/CUIT.
     * Útil para listados/filtrado en API (puede devolver varios clientes).
     */
    List<Cliente> findByDocumentoCuitContainingIgnoreCase(String documentoCuit);
}
