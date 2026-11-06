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

// Favorito: el estado es personal, asi que no lo pinta el HTL sino este JS
// preguntando al FavoritosServlet (GET al cargar, POST al hacer clic)
(function () {
    "use strict";

    const RUTA_LOGIN = "/content/35mm/us/en/login.html";

    // lleva al login y, al entrar, vuelve a esta ficha (con su ?id=)
    function irAlLogin() {
        const aqui = window.location.pathname + window.location.search;
        window.location.href = RUTA_LOGIN + "?redirect=" + encodeURIComponent(aqui);
    }

    // pinta el boton segun sea favorita o no
    function pintarFavorito(boton, esFavorito) {
        boton.setAttribute("aria-pressed", esFavorito ? "true" : "false");
        boton.classList.toggle("is-favorito", esFavorito);
        boton.querySelector(".detalle-favorito-texto").textContent =
            esFavorito ? "En favoritos" : "Añadir a favoritos";
    }

    // sin pelicula no hay boton (el HTL no pinta la ficha)
    const boton = document.querySelector(".detalle-favorito");
    if (!boton) return;
    const url = boton.dataset.favoritosUrl;
    const peliculaId = boton.dataset.peliculaId;
    let anonimo = false;

    // Estado inicial: GET ?id=... -> {"favorito": true/false}
    $.ajax({ url: url, type: "GET", data: { id: peliculaId }, cache: false })
        .done(function (respuesta) {
            pintarFavorito(boton, respuesta.favorito);
            boton.hidden = false;
        })
        .fail(function (xhr) {
            if (xhr.status === 401) {
                // anonimo: se muestra el boton, y al pulsarlo va al login
                anonimo = true;
                pintarFavorito(boton, false);
                boton.hidden = false;
            }
            // 403 (admin, autores) u otros errores: el boton sigue oculto
        });

    // Clic: POST accion=anadir|quitar -> {"favorito": true/false}
    boton.addEventListener("click", function () {
        if (anonimo) {
            irAlLogin();
            return;
        }
        const esFavorito = boton.getAttribute("aria-pressed") === "true";
        // evita un segundo POST por doble clic mientras llega la respuesta
        boton.disabled = true;
        $.ajax({
            url: url,
            type: "POST",
            data: { accion: esFavorito ? "quitar" : "anadir", id: peliculaId }
        }).done(function (respuesta) {
            // se pinta lo que confirma el servidor, no lo que suponemos
            pintarFavorito(boton, respuesta.favorito);
        }).fail(function (xhr) {
            if (xhr.status === 401) {
                // la sesion caduco mientras estaba en la pagina
                irAlLogin();
            }
        }).always(function () {
            boton.disabled = false;
        });
    });
})();
