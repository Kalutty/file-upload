# Envío de archivos por correo

Aplicación Spring Boot que recibe hasta dos archivos de 4 MB cada uno y los envía como adjuntos mediante SMTP.

## Requisitos locales

- Java 25 o superior
- Maven 3.9 o superior
- Una cuenta de correo con SMTP habilitado

## Ejecutar localmente

```powershell
$env:SMTP_HOST="smtp.gmail.com"
$env:SMTP_PORT="587"
$env:SMTP_USERNAME="tu-cuenta@gmail.com"
$env:SMTP_PASSWORD="tu-contraseña-de-aplicación"
$env:SMTP_FROM="tu-cuenta@gmail.com"
mvn spring-boot:run
```

Abre http://localhost:8080.

Para Gmail, `SMTP_PASSWORD` debe ser una contraseña de aplicación de 16 caracteres, no la contraseña normal de la cuenta. Google exige tener activada la verificación en dos pasos para crearla.

## Publicar en AWS (resumen paso a paso)

1. En **Elastic Beanstalk > Create application**, crea una aplicación nueva con plataforma **Java** y una rama compatible con el JDK usado por el proyecto. Usa el entorno **Web server**.
2. En **Configuration > Software > Environment properties** agrega `SMTP_HOST`, `SMTP_PORT`, `SMTP_USERNAME`, `SMTP_PASSWORD` y `SMTP_FROM`. Nunca subas la contraseña al repositorio.
3. Compila el archivo `.jar` y súbelo como aplicación. El archivo debe quedar en la raíz del paquete junto al `Procfile`:

   ```powershell
   mvn clean package
   New-Item -ItemType Directory deploy -Force
   Copy-Item target/file-upload-0.0.1-SNAPSHOT.jar deploy/
   Copy-Item Procfile deploy/
   Compress-Archive -Path deploy/* -DestinationPath file-upload.zip -Force
   ```

   En Elastic Beanstalk elige **Upload and deploy** y selecciona `file-upload.zip`.
4. Elastic Beanstalk entregará una URL pública `*.elasticbeanstalk.com`. Ábrela y prueba el formulario.
5. Para evitar cargos inesperados, configura un presupuesto/alerta de AWS Billing y elimina el entorno cuando termines las pruebas. La capa gratuita depende de la antigüedad y región de la cuenta; Elastic Beanstalk puede generar cargos por EC2, EBS o transferencia fuera de la cuota gratuita.

AWS solo alojará la aplicación; el envío lo realiza el proveedor SMTP configurado.
