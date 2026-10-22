function buscadorPelis() {
    // obtener el valor del input
    const input = document.getElementById("pelisInput");
    const nombreAEnviar = (input.value) ? input.value.trim() : "";
    if(!nombreAEnviar) return; // si no hay busqueda actual volver

    const servletResource = $(".component-registro").data("current-resource");

    var settings = {
        url: servletResource + ".ejecutar-buscador-pelis.json",
        type: "GET",
        data: {
            // parametros que espera el servlet
            query: nombreAEnviar
        },
    };
    $.ajax(settings).done(function (response) {
    const contenedorResultados = document.getElementById("resultadosPelis");
    contenedorResultados.innerHTML = ""; // Limpiar resultados anteriores
    if(response.length === 0){
        contenedorResultados.innerHTML = "No se encontraron peliculas para su busqueda"
    }
    else{
        response.forEach(peli => {
            contenedorResultados.appendChild(crearTarjetaPeli(peli));
        });
    }
    console.log("Success", response);
    }).fail((response) => {
        console.log("Error", response);
        const contenedorResultados = document.getElementById("resultadosPelis");
        contenedorResultados.innerHTML = "No se pudo realizar la busqueda"
    });
}
// funcion para montar los datos de un actor
function crearTarjetaPeli(peli) {
    const tarjeta = document.createElement("div");
    tarjeta.className = "resultado_peli";
    const link = document.createElement("a");
    link.className = "peli-link";
    link.href = "/content/35mm/us/en/detalles.html?id=" + peli.id;

    if (peli.poster_path){
        const img = document.createElement("img");
        img.className = "poster_peli";
        img.src = peli.poster_path;
        img.alt = peli.title || "";
        link.appendChild(img);
    }
    if (peli.title){
        const title = document.createElement("p");
        title.className = "movie_title";
        title.textContent = peli.title;
        link.appendChild(title);
    }
    tarjeta.appendChild(link);
    return tarjeta;
}

