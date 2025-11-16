package ar.edu.unju.fi.logistica.exception;

public class EnvioException extends RuntimeException{
	private static final long serialVersionUID = 1L;
	public EnvioException(String message) { super(message); }
    public EnvioException(String message, Throwable cause) { super(message, cause); }
}
