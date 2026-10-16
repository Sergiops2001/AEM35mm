// registrar el envio del formulario
$(document).ready(function () { // espera que el HTML este cargado
    const formulario = document.getElementById("chat-formulario");
    if(!formulario) return;
    formulario.addEventListener("submit", function (evento) {
        evento.preventDefault(); // evita lla recarga de la pagina
        enviarPregunta();
    });
});

const MAX_HISTORIAL = 10;
let historial = [];


// pintar mensaje
function agregarMensaje(texto, tipo) {
    const contenedor = document.getElementById("chat-mensajes");
    const mensaje = document.createElement("div");
    mensaje.className = "chat-mensaje chat-mensaje-" + tipo;
    mensaje.textContent = texto;
    contenedor.appendChild(mensaje);
    contenedor.scrollTop = contenedor.scrollHeight;
    requestAnimationFrame(function (){ // ejecuta este codigo antes del siguiente repintado de pantalla, 1º cargado opacity 0,2º cargado opacity 1 , pilla el cambio y ya si hace la transición
        requestAnimationFrame(function (){
            mensaje.classList.add("chat-mensaje-visible");
        })
    })
}

// crear contenedor mensaje pensando
function  crearPensando(){
    const contenedor = document.createElement("div");
    contenedor.className = "contenedor-pensando";
    contenedor.textContent = "Pensando...";
    document.getElementById("chat-mensajes").appendChild(contenedor);
}

// eliminar contenedor mensaje pensando
function eliminarPensando(){
    const pensando = document.querySelector(".contenedor-pensando");
    if (pensando){
        pensando.remove();
    }
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

    // tiempo en el que empieza la petición
    const tiempoInicio = Date.now();

    // llamar a crearPensando
    crearPensando();

    // PETICION
    $.ajax({
        url: servletResource + ".preguntar-chat-ia.json",
        type: "POST",
        data: { pregunta: pregunta,
                historial: JSON.stringify(historial)
        }
    }).done(function (response){
        procesarConTiempo(tiempoInicio, function (){
            eliminarPensando();
            agregarMensaje(response.respuesta, "ia");
            historial.push({ role: "user", content: pregunta });
            historial.push({ role: "assistant", content: response.respuesta });
            if (historial.length > MAX_HISTORIAL) {
                historial = historial.slice(-MAX_HISTORIAL);
            }
        })
    }).fail(function (xhr) {
        procesarConTiempo(tiempoInicio, function (){
            eliminarPensando();
            const mensaje = (xhr.responseJSON && xhr.responseJSON.error)
                ? xhr.responseJSON.error
                : "No se pudo contactar con el chat";
            agregarMensaje(mensaje, "error");
        })
    }).always(function () {
        procesarConTiempo(tiempoInicio, function (){
            boton.disabled = false;
            input.focus();
        })
    });
}

// funcion para procesar los tres segundos
function procesarConTiempo(tiempoInicio, callback){
    const tiempoTranscurrido = Date.now() - tiempoInicio;
    const tiempoRestante = Math.max(0, 3000 - tiempoTranscurrido); // establecemos 3 segundos

    setTimeout(callback, tiempoRestante);
}

