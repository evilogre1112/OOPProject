package com.oop.EXCEPTION;


public class PermissionException extends Exception {
    public PermissionException() {
        super("Permission denied");
    }
     public PermissionException(String mess) {
        super(mess);
    }
    public PermissionException(String message, Throwable cause) {
        super(message, cause);
    }
}
