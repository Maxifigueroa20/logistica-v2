package ar.edu.unju.fi.logistica.exception;

public class VehiculoException extends RuntimeException{
	private static final long serialVersionUID = 1L;
	public VehiculoException(String message) { super(message); }
    public VehiculoException(String message, Throwable cause) { super(message, cause); }
}
