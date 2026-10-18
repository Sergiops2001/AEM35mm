package com.mm35.core.services;

import java.util.List;
import java.util.Map;

public interface TMDBService {

    /**
     * Funciones para la API de TMDB
     */
    List<Map<String, String>> peliculasDestacadas();
    List<Map<String, String>> peliculasRecientes();
    List<Map<String, String>> peliculasValoradas();
    List<Map<String, String>> buscarPeliculas(String query);
    Map<String,String> detallePelicula( String id);
}

