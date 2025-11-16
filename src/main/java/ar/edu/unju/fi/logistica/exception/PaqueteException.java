package ar.edu.unju.fi.logistica.exception;

public class PaqueteException extends RuntimeException {
	private static final long serialVersionUID = 1L;
	public PaqueteException(String message) { super(message); }
    public PaqueteException(String message, Throwable cause) { super(message, cause); }
}
