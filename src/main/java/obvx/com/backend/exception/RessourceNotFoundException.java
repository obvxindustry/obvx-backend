package obvx.com.backend.exception;

public class RessourceNotFoundException extends RuntimeException{

    public RessourceNotFoundException(String message){
        super(message);
    }
}
