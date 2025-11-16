package ar.edu.unju.fi.logistica.exception;

public class RutaException extends RuntimeException {
	private static final long serialVersionUID = 1L;
	public RutaException(String message) { super(message); }
    public RutaException(String message, Throwable cause) { super(message, cause); }
}
