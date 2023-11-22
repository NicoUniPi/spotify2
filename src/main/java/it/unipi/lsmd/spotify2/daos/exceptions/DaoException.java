package it.unipi.lsmd.spotify2.daos.exceptions;

// Class declaration for DaoException, a custom exception for DAO operations
public class DaoException extends RuntimeException {

    // Constructor with a message for the exception
    public DaoException(String message) {
        super(message);
    }

    // Constructor with a message and a cause for the exception
    public DaoException(String message, Throwable cause) {
        super(message, cause);
    }
}
