package ar.edu.unju.fi.logistica.enums;

import ar.edu.unju.fi.logistica.exception.EnvioException;

public enum EstadoEnvio {

    GENERADO {
        @Override public EstadoEnvio aAlmacen() { return EN_ALMACEN; }
        @Override public EstadoEnvio cancelar(String motivo) { return CANCELADO; }
    },

    EN_ALMACEN {
        @Override public EstadoEnvio aRuta() { return EN_RUTA; }
    },

    EN_RUTA {
        @Override public EstadoEnvio aEntregado(byte[] comprobante) {
            if (comprobante == null || comprobante.length == 0) {
                throw new EnvioException("Para ENTREGADO se requiere comprobante");
            }
            return ENTREGADO;
        }
    },

    ENTREGADO {
        @Override public EstadoEnvio devolver(String observacion) { return DEVUELTO; }
    },

    DEVUELTO,
    CANCELADO;

    /* Defaults: cuando la transición no aplica desde el estado actual */
    public EstadoEnvio aAlmacen() { throw new EnvioException("Transición no permitida"); }
    public EstadoEnvio aRuta() { throw new EnvioException("Transición no permitida"); }
    public EstadoEnvio aEntregado(byte[] comprobante) { throw new EnvioException("Transición no permitida"); }
    public EstadoEnvio cancelar(String motivo) { throw new EnvioException("Transición no permitida"); }
    public EstadoEnvio devolver(String observacion) { throw new EnvioException("Transición no permitida"); }
}
