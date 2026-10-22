// registrar el envio del formulario
$(document).ready(function () {
    const formulario = document.getElementById("registro-formulario");
    if (!formulario) return;
    formulario.addEventListener("submit", function (evento) {
        evento.preventDefault();
        registrarUsuario(formulario);
    });
});

// pintar el mensaje de resultado (tipo: "ok" o "error")
function mostrarMensajeRegistro(texto, tipo) {
    const mensaje = document.getElementById("registro-mensaje");
    if (!mensaje) return;
    mensaje.textContent = texto;
    mensaje.className = "registro-mensaje registro-mensaje-" + tipo;
}

function registrarUsuario(formulario) {
    if (formulario.password.value !== formulario.password2.value) {
        mostrarMensajeRegistro("Las contraseñas no coinciden", "error");
        return;
    }

    const boton = document.getElementById("registro-boton");
    const servletResource = $(".component-registro").data("current-resource");
    boton.disabled = true;

    $.ajax({
        url: servletResource + ".registrar-usuario.json",
        type: "POST",
        data: $(formulario).serialize()
    }).done(function (response) {
        mostrarMensajeRegistro(response.mensaje, "ok");
        formulario.reset();
        // tras un momento, al login
        setTimeout(function () {
            window.location.href = "/content/35mm/us/en/login.html";
        }, 1500);
    }).fail(function (xhr) {
        const mensaje = (xhr.responseJSON && xhr.responseJSON.error)
            ? xhr.responseJSON.error
            : "No se pudo conectar con el servidor";
        mostrarMensajeRegistro(mensaje, "error");
    }).always(function () {
        boton.disabled = false;
    });
}
