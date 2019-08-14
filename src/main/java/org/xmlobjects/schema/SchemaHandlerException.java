package org.xmlobjects.schema;

public class SchemaHandlerException extends Exception {
    private static final long serialVersionUID = -5741051785363810322L;

    public SchemaHandlerException() {
        super();
    }

    public SchemaHandlerException(String message) {
        super(message);
    }

    public SchemaHandlerException(Throwable cause) {
        super(cause);
    }

    public SchemaHandlerException(String message, Throwable cause) {
        super(message, cause);
    }
}
