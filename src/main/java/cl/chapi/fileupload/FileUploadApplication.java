package cl.chapi.fileupload;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// Se marca esta clase como el punto principal de configuración de Spring Boot.
@SpringBootApplication
public class FileUploadApplication {
    // Se inicia la aplicación y el servidor web embebido.
    public static void main(String[] args) {
        SpringApplication.run(FileUploadApplication.class, args);
    }
}
