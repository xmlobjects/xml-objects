package org.xmlobjects.serializer;

public class ObjectSerializeException extends Exception {
    private static final long serialVersionUID = -8749263875833215329L;

    public ObjectSerializeException() {
        super();
    }

    public ObjectSerializeException(String message) {
        super(message);
    }

    public ObjectSerializeException(Throwable cause) {
        super(cause);
    }

    public ObjectSerializeException(String message, Throwable cause) {
        super(message, cause);
    }
}
