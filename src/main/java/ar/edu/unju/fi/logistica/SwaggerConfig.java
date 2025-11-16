package ar.edu.unju.fi.logistica;

import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;

@Configuration
@OpenAPIDefinition(info = @Info(title = "Logística API - Grupo 02", version = "1.0", description = """
		API para gestión de clientes, vehículos, paquetes y envíos
		de una empresa de logística.

		Flujo típico de uso:

		1) Clientes
		   - Dar de alta remitentes y destinatarios.
		   - Endpoint principal: /api/clientes

		2) Paquetes
		   - Registrar paquetes frágiles y refrigerados.
		   - Convención de códigos en los datos de ejemplo:
		     • PF-...  → Paquetes frágiles (FRAGIL)
		     • PR-...  → Paquetes refrigerados (REFRIGERADO)
		   - Endpoint principal: /api/paquetes

		3) Envíos
		   - Crear un envío indicando:
		     • Documento/CUIT del remitente y destinatario,
		     • Lista de códigos de paquete (PF-..., PR-...) ya registrados.
		   - Al crear el envío, se genera un código de seguimiento público
		     (por ejemplo: ICG-AB12CD34EF56).
		   - Endpoint principal: /api/envios

		4) Estados del envío
		   - Flujo normal:
		     GENERADO → EN_ALMACEN → EN_RUTA → (ENTREGADO o DEVUELTO)
		   - Cambios de estado:
		     • Ingreso a almacén
		     • Salida a ruta
		     • Entrega con comprobante (Base64)
		     • Cancelación / Devolución

		5) Tracking para el cliente final
		   - A partir del código público (ej: ICG-AB12CD34EF56),
		     se puede consultar el detalle del envío y toda la traza
		     de cambios de estado.
		   - Endpoint: GET /api/envios/seguimiento?tracking=...
		""", contact = @Contact(name = "Equipo Grupo 02", email = "gefestudiante2009@gmail.com")))
public class SwaggerConfig {

}
