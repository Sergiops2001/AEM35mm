package com.mm35.core.services;

/**
 * Posibles resultados al marcar pelicula como favorita o eliminarla
 */
public enum ResultadoFavorito {
    OK, // se ha hecho o estaba hecho
    NO_AUTORIZADO, // usuario no es de la web, 403
    PELICULA_NO_VALIDA, // el id esta mal o no existe en TMDB, 400
    ERROR // fallo algo del repositorio o server. 500
}
