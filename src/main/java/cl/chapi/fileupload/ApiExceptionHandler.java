package cl.chapi.fileupload;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

// Se centralizan los errores de la API para devolver mensajes claros al JavaScript.
@RestControllerAdvice
public class ApiExceptionHandler {
    // Se convierten los errores controlados de la aplicación en respuestas JSON con su estado HTTP.
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, String>> handleStatus(ResponseStatusException exception) {
        return ResponseEntity.status(exception.getStatusCode())
                .body(Map.of("message", exception.getReason() == null
                        ? "Ocurrió un error" : exception.getReason()));
    }

    // Se informa específicamente cuando Spring rechaza una carga demasiado grande.
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<Map<String, String>> handleUploadSize() {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("message", "El tamaño máximo de cada archivo es 4 MB"));
    }
}
