// Fade-in de las imagenes de la ficha: cada imagen aparece cuando termina de cargar.
// El script se incluye al final del HTL, asi que el HTML del componente ya existe
// cuando se ejecuta y no hace falta esperar a $(document).ready
(function () {
    "use strict";

    function marcarCargada(img) {
        img.classList.add("is-cargada");
    }

    document.querySelectorAll(".component-detallesPelis").forEach(function (componente) {
        // Activa el CSS que oculta las imagenes hasta que cargan (sin JS se ven siempre)
        componente.classList.add("js-fade");

        componente.querySelectorAll(".detalle-poster, .detalle-fondo-img").forEach(function (img) {
            // Si ya estaba cargada (por ejemplo, desde la cache del navegador), se muestra ya
            if (img.complete) {
                marcarCargada(img);
                return;
            }
            img.addEventListener("load", function () {
                marcarCargada(img);
            });
            // Si falla, tambien se muestra: mejor el icono de imagen rota que un hueco invisible
            img.addEventListener("error", function () {
                marcarCargada(img);
            });
        });
    });
})();
