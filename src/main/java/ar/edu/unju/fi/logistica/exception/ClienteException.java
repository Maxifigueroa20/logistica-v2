package ar.edu.unju.fi.logistica.exception;

public class ClienteException extends RuntimeException{
	private static final long serialVersionUID = 1L;
	public ClienteException(String message) { super(message); }
    public ClienteException(String message, Throwable cause) { super(message, cause); }
}
