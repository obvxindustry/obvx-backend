package obvx.com.backend.exception;

public class RessourceAlreadyExistsException extends RuntimeException {

    public RessourceAlreadyExistsException(String message) {
        super(message);
    }
}