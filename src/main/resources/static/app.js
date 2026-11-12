// Se obtienen los controles del formulario para poder validarlos y actualizarlos.
const form = document.querySelector('#upload-form');
const filesInput = document.querySelector('#files');
const error = document.querySelector('#error');
const submit = document.querySelector('#submit');
// Se convierten 4 MB a bytes para comparar el tamaño real de cada archivo.
const maxSize = 4 * 1024 * 1024;

// Se intercepta el envío normal del formulario para manejarlo con fetch sin recargar la página.
form.addEventListener('submit', async (event) => {
  event.preventDefault();
  error.textContent = '';
  // Se transforma la colección de archivos del navegador en un arreglo fácil de recorrer.
  const files = [...filesInput.files];

  // Se valida que el usuario haya elegido entre uno y dos archivos.
  if (files.length < 1 || files.length > 2) {
    error.textContent = 'Selecciona uno o dos archivos.';
    return;
  }
  // Se valida que ningún archivo esté vacío ni supere los 4 MB.
  if (files.some((file) => file.size === 0 || file.size > maxSize)) {
    error.textContent = 'Cada archivo debe pesar más de 0 y como máximo 4 MB.';
    return;
  }

  // Se bloquea el botón durante el envío para evitar solicitudes duplicadas.
  submit.disabled = true;
  submit.textContent = 'Enviando...';
  try {
    // Se envían correo y archivos juntos usando FormData y el endpoint del backend.
    const response = await fetch('/api/send', { method: 'POST', body: new FormData(form) });
    if (!response.ok) {
      const body = await response.json().catch(() => ({}));
      throw new Error(body.message || 'No se pudo enviar el correo.');
    }
    // Se redirige a la página de éxito cuando el backend responde correctamente.
    window.location.href = '/success.html';
  } catch (err) {
    // Se muestra el error y se habilita nuevamente el formulario para permitir otro intento.
    error.textContent = err.message;
    submit.disabled = false;
    submit.textContent = 'Enviar correo';
  }
});
