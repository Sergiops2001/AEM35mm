package com.mm35.core.services;

import com.mm35.core.beans.Favorito;

import java.util.List;

/**
 * Favoritos de los usuarios de la web (los de /home/users/35mm)
 * El usuarioId debe salir de la sesion de la peticion, nunca de un parametro
 * Cualquier fallo se devuelve como resultado, sin lanzar excepciones
 */
public interface FavoritosService {

    /**
     * Favoritos del usuario, los mas recientes primero
     * Devuelve null si el usuario no es de la web (admin, autores...) o si hay un error,
     * y una lista vacia si es de la web pero no tiene favoritos
     */
    List<Favorito> obtenerFavoritos(String usuarioId);

    /**
     * true si la pelicula esta en los favoritos del usuario
     * Devuelve false tambien si el id no es valido, el usuario no es de la web o hay un error
     */
    boolean esFavorito(String usuarioId, String peliculaId);

    /**
     * Añade la pelicula a los favoritos del usuario. El titulo y el poster se sacan de TMDB
     * Si ya estaba, devuelve OK sin hacer nada (idempotente)
     */
    ResultadoFavorito anadirFavorito(String usuarioId, String peliculaId);

    /**
     * Quita la pelicula de los favoritos del usuario
     * Si no estaba, devuelve OK sin hacer nada (idempotente)
     */
    ResultadoFavorito quitarFavorito(String usuarioId, String peliculaId);
}

