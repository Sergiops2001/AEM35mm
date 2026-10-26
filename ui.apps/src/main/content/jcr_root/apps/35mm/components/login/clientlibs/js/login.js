// registrar el envio del formulario
$(document).ready(function () {
    const formulario = document.getElementById("login-formulario");
    if (!formulario) return;
    formulario.addEventListener("submit", function (evento) {
        evento.preventDefault();
        iniciarSesion(formulario);
    });
});

// pintar el mensaje de resultado (tipo: "ok" o "error")
function mostrarMensajeLogin(texto, tipo) {
    const mensaje = document.getElementById("login-mensaje");
    if (!mensaje) return;
    mensaje.textContent = texto;
    mensaje.className = "login-mensaje login-mensaje-" + tipo;
}

// funcion para el destino despues de login , solo se aceptan rutas de nuestra propia web
function destinoTrasLogin(){
    const destino = new URLSearchParams(window.location.search).get("redirect");
    if (destino && destino.startsWith("/") && !destino.startsWith("//") && !destino.startsWith("/\\")){
        return destino;
    }
    return "/content/35mm/us/en/example.html"
}


function iniciarSesion(formulario) {

    const usuario = formulario.j_username.value.trim().toLowerCase();
    const password = formulario.j_password.value;
    if ( !usuario || !password) {
        mostrarMensajeLogin("Introduce usuario y contraseña", "error");
        return;
    }

    const boton = document.getElementById("login-boton");
    const loginUrl = $(".component-login").data("login-url");
    boton.disabled = true;

    $.ajax({
        url: loginUrl,
        type: "POST",
        data: {
            j_username: usuario,
            j_password: password,
            j_validate: true,
            _charset_: "UTF-8"
        }
    }).done(function (response) {
        // la cookie login-token ya esta puesta, ahora navegar a la ventana indicada
        window.location.href = destinoTrasLogin();
    }).fail(function (xhr) {
        const mensaje = (xhr.status === 403)
            ? "Usuario o contraseña incorrectos"
            : "No se pudo iniciar sesión. Inténtalo más tarde";
        mostrarMensajeLogin(mensaje, "error");
        boton.disabled = false;
    });
}
