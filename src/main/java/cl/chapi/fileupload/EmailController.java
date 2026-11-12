package cl.chapi.fileupload;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

// Se declara este componente como controlador REST para recibir solicitudes HTTP.
@RestController
@RequestMapping("/api")
@Validated
public class EmailController {
    // Se referencia el servicio que contiene la lógica de envío y validación de archivos.
    private final SmtpEmailService emailService;

    // Se recibe el servicio mediante inyección de dependencias de Spring.
    public EmailController(SmtpEmailService emailService) {
        this.emailService = emailService;
    }

    // Se expone POST /api/send y se limita el formato recibido a multipart/form-data.
    @PostMapping(value = "/send", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> send(
            // Se valida que el correo exista, no esté vacío y tenga formato válido.
            @RequestParam("email") @NotBlank @Email String email,
            // Se reciben todos los archivos seleccionados por el usuario.
            @RequestParam("files") MultipartFile[] files) {
        // Se delega el envío al servicio SMTP para mantener el controlador pequeño.
        emailService.send(email, files);
        // Se devuelve 200 OK cuando el correo se pudo enviar correctamente.
        return ResponseEntity.ok().build();
    }
}
