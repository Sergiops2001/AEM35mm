// muestra "Entrar / Crear cuenta" o "Hola, usuario / Salir" segun la sesion
$(document).ready(function () {
    const zona = document.querySelector(".usuario-header");
    if (!zona) return;

    $.ajax({
        url: zona.dataset.sesionUrl,
        type: "GET",
        cache: false,
    }).done(function (sesion) {
        if (sesion.autenticado){
            zona.querySelector(".usuario-nombre").textContent = sesion.usuario;
            zona.querySelector(".usuario-conectado").hidden = false;
        }
        else {
            // al entrar, volver a esta misma pagina
            const enlaceLogin = zona.querySelector(".usuario-enlace-login");
            const aqui = window.location.pathname + window.location.search;
            enlaceLogin.href = enlaceLogin.getAttribute("href") + "?redirect=" + encodeURIComponent(aqui);
            zona.querySelector(".usuario-anonimo").hidden = false;
        }
    })
});
