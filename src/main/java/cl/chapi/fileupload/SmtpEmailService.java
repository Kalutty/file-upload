package cl.chapi.fileupload;

import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.multipart.MultipartFile;

import java.util.Properties;

@Service
public class SmtpEmailService {
    // Se define el límite individual solicitado para cada archivo: 4 MB.
    private static final long MAX_FILE_SIZE = 4L * 1024 * 1024;
    // Se limita la cantidad total de archivos que puede recibir cada solicitud.
    private static final int MAX_FILES = 2;

    // Se almacenan los valores de configuración SMTP recibidos desde variables de entorno.
    private final String username;
    private final String password;
    private final String from;
    private final String subject;
    private final String host;
    private final int port;

    // Se carga la configuración sin escribir usuarios ni contraseñas directamente en el código.
    public SmtpEmailService(
            @Value("${app.smtp.username}") String username,
            @Value("${app.smtp.password}") String password,
            @Value("${app.smtp.from:${app.smtp.username}}") String from,
            @Value("${app.smtp.host:smtp.gmail.com}") String host,
            @Value("${app.smtp.port:587}") int port,
            @Value("${app.email.subject:Archivos recibidos}") String subject) {
        this.username = username;
        this.password = password;
        this.from = from;
        this.host = host;
        this.port = port;
        this.subject = subject;
    }

    // Se validan los archivos y se construye/envía el correo completo.
    public void send(String recipient, MultipartFile[] files) {
        validateFiles(files);
        try {
            // Se configura autenticación SMTP y STARTTLS para enviar de forma cifrada.
            Properties properties = new Properties();
            properties.put("mail.smtp.auth", "true");
            properties.put("mail.smtp.starttls.enable", "true");
            properties.put("mail.smtp.host", host);
            properties.put("mail.smtp.port", String.valueOf(port));

            // Se crea una sesión autenticada usando la cuenta SMTP configurada en AWS.
            Session session = Session.getInstance(properties, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(username, password);
                }
            });

            // Se prepara el mensaje, el remitente, el destinatario y el asunto.
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(from));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipient));
            message.setSubject(subject, "UTF-8");

            // Se arma un mensaje multipart para combinar texto y archivos adjuntos.
            MimeMultipart multipart = new MimeMultipart();
            MimeBodyPart text = new MimeBodyPart();
            text.setText("Se adjuntan los archivos enviados desde el formulario web.", "UTF-8");
            multipart.addBodyPart(text);

            // Se convierte cada archivo recibido en una parte adjunta del correo.
            for (MultipartFile file : files) {
                MimeBodyPart attachment = new MimeBodyPart();
                attachment.setContent(file.getBytes(), file.getContentType() == null
                        ? "application/octet-stream" : file.getContentType());
                attachment.setFileName(file.getOriginalFilename());
                multipart.addBodyPart(attachment);
            }
            // Se completa el mensaje MIME y se envía mediante el servidor SMTP.
            message.setContent(multipart);
            message.saveChanges();
            Transport.send(message);
        } catch (Exception exception) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "No fue posible enviar el correo por SMTP", exception);
        }
    }

    // Se refuerzan en el backend los límites que también se controlan visualmente en el navegador.
    private void validateFiles(MultipartFile[] files) {
        if (files == null || files.length == 0 || files.length > MAX_FILES) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Debes seleccionar entre uno y dos archivos");
        }
        for (MultipartFile file : files) {
            if (file.isEmpty() || file.getSize() > MAX_FILE_SIZE) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Cada archivo debe pesar más de 0 y como máximo 4 MB");
            }
        }
    }
}
