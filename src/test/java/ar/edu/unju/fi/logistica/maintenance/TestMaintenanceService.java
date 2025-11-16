package ar.edu.unju.fi.logistica.maintenance;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("test")
public class TestMaintenanceService {

	private final DataSource dataSource;

	public TestMaintenanceService(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	public void clearDatabase() {
		try (Connection c = dataSource.getConnection(); Statement st = c.createStatement()) {

			// Autocommit evita “rollback-only” y commits implícitos conflictivos de
			// TRUNCATE
			c.setAutoCommit(true);

			st.execute("SET FOREIGN_KEY_CHECKS = 0");

			execQuiet(st, "TRUNCATE TABLE historial_envio");
			execQuiet(st, "TRUNCATE TABLE paquetes_fragiles");
			execQuiet(st, "TRUNCATE TABLE paquetes_refrigerados");
			execQuiet(st, "TRUNCATE TABLE paquetes");
			execQuiet(st, "TRUNCATE TABLE rutas_envios");
			execQuiet(st, "TRUNCATE TABLE envios");
			execQuiet(st, "TRUNCATE TABLE rutas");
			execQuiet(st, "TRUNCATE TABLE vehiculos");
			execQuiet(st, "TRUNCATE TABLE clientes");

			st.execute("ALTER TABLE paquetes  AUTO_INCREMENT = 1");
            st.execute("ALTER TABLE envios    AUTO_INCREMENT = 1");
            st.execute("ALTER TABLE rutas     AUTO_INCREMENT = 1");
            st.execute("ALTER TABLE vehiculos AUTO_INCREMENT = 1");
            st.execute("ALTER TABLE clientes  AUTO_INCREMENT = 1");

			st.execute("SET FOREIGN_KEY_CHECKS = 1");

		} catch (SQLException e) {
			// Si querés fallar el test cuando no se puede limpiar:
			throw new RuntimeException("No se pudo limpiar la BD de test", e);
		}
	}

	private void execQuiet(Statement st, String sql) {
		try {
			st.execute(sql);
		} catch (SQLException ignore) {
			/* tabla ausente, etc. */ }
	}
}
