package obvx.com.backend.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
    }

    @Test
    @DisplayName("handleRessourceNotFound - Retourne 404 NOT_FOUND avec le message de l'exception")
    void handleRessourceNotFound_returnsNotFound() {
        RessourceNotFoundException ex = new RessourceNotFoundException("Category not found");

        ResponseEntity<ErrorReponse> response = exceptionHandler.handleRessourceNotFound(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(404);
        assertThat(response.getBody().getMessage()).isEqualTo("Category not found");
        assertThat(response.getBody().getTimestamp()).isNotNull();
    }

    @Test
    @DisplayName("handleValidationException - Retourne 400 BAD_REQUEST avec le premier message d'erreur")
    void handleValidationException_returnsBadRequestWithFieldErrorMessage() {
        Object target = new Object();
        BindingResult bindingResult = new BeanPropertyBindingResult(target, "categoryRequest");
        bindingResult.addError(new FieldError("categoryRequest", "name", "Name of category as required"));

        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(null, bindingResult);

        ResponseEntity<ErrorReponse> response = exceptionHandler.handleValidationException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(400);
        assertThat(response.getBody().getMessage()).isEqualTo("Name of category as required");
        assertThat(response.getBody().getTimestamp()).isNotNull();
    }

    @Test
    @DisplayName("handleValidationException - Retourne message par défaut si aucune erreur de champ")
    void handleValidationException_returnsDefaultMessageWhenNoFieldError() {
        Object target = new Object();
        BindingResult bindingResult = new BeanPropertyBindingResult(target, "categoryRequest");

        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(null, bindingResult);

        ResponseEntity<ErrorReponse> response = exceptionHandler.handleValidationException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(400);
        assertThat(response.getBody().getMessage()).isEqualTo("Données invalides");
        assertThat(response.getBody().getTimestamp()).isNotNull();
    }
}
