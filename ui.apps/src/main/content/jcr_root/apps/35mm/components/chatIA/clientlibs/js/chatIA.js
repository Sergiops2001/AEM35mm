// registrar el envio del formulario
$(document).ready(function () { // espera que el HTML este cargado
    const formulario = document.getElementById("chat-formulario");
    if(!formulario) return;
    formulario.addEventListener("submit", function (evento) {
        evento.preventDefault(); // evita lla recarga de la pagina
        enviarPregunta();
    });
});

// pintar mensaje
function agregarMensaje(texto, tipo) {
    const contenedor = document.getElementById("chat-mensajes");
    const mensaje = document.createElement("div");
    mensaje.className = "chat-mensaje chat-mensaje-" + tipo;
    mensaje.textContent = texto;
    contenedor.appendChild(mensaje);
    contenedor.scrollTop = contenedor.scrollHeight;
}

// enviar la pregunta

function enviarPregunta() {
    const input = document.getElementById("chatInput");
    const boton = document.getElementById("chatButton");
    const pregunta = input.value.trim();
    if (!pregunta) return;
    agregarMensaje(pregunta, "usuario");
    input.value = "";
    boton.disabled = true;

    const servletResource = $(".component-chatIA").data("current-resource");

    // PETICION
    $.ajax({
        url: servletResource + ".preguntar-chat-ia.json",
        type: "POST",
        data: { pregunta: pregunta}
    }).done(function (response){
        agregarMensaje(response.respuesta, "ia")
    }).fail(function (xhr) {
        const mensaje = (xhr.responseJSON && xhr.responseJSON.error)
            ? xhr.responseJSON.error
            : "No se pudo contactar con el chat";
        agregarMensaje(mensaje, "error");
    }).always(function () {
        boton.disabled = false;
        input.focus();
    });
}

