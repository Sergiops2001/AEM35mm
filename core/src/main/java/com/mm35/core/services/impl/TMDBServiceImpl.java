package com.mm35.core.services.impl;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mm35.core.config.TMDBApiConfig;
import com.mm35.core.services.TMDBService;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.metatype.annotations.Designate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Component(service = TMDBService.class, immediate = true)
@Designate(ocd = TMDBApiConfig.class)
public class TMDBServiceImpl implements TMDBService {

    private static final Logger LOG = LoggerFactory.getLogger(TMDBServiceImpl.class);

    private static final String IMAGE_BASE_URL = "https://image.tmdb.org/t/p/w500";

    // Enum: cada categoría encapsula su propio path de TMDB.
    private enum TMDBCategoria {
        POPULARES("movie/popular"),
        RECIENTES("movie/now_playing"),
        MEJOR_VALORADAS("movie/top_rated"),
        BUSCAR_PELICULA("search/movie");

        private final String path;

        TMDBCategoria(String path) {
            this.path = path;
        }
    }

    private String apiBaseUrl;
    private String apiKey;

    private HttpClient httpClient;

    @Activate
    @Modified
    protected void activate(TMDBApiConfig config) {
        this.apiBaseUrl = config.apiBaseUrl();
        this.apiKey = config.apiKey();
        this.httpClient = HttpClient.newBuilder()
                        .connectTimeout(Duration.ofSeconds(3))
                                .build();
        LOG.info("TMDBService activado con apiBaseUrl: {}", apiBaseUrl);
    }

    //***PELICULAS INICIO***
    @Override
    //Peliculas destacadas
    public List<Map<String, String>> peliculasDestacadas() {
        return peliculasPorCategoria(TMDBCategoria.POPULARES);
    }
    @Override
    //Peliculas recientes
    public List<Map<String, String>> peliculasRecientes() {
        return peliculasPorCategoria(TMDBCategoria.RECIENTES);

    }
    @Override
    //Peliculas mejor valoradas
    public List<Map<String, String>> peliculasValoradas() {
        return peliculasPorCategoria(TMDBCategoria.MEJOR_VALORADAS);
    }

    // ***BUSQUEDA DE PELICULAS***
    @Override
    // buscar peliculas
    public List<Map<String, String>> buscarPeliculas(String query) {
        if (query == null || query.isEmpty()){
            LOG.debug("La busqueda está vacia, se devuelve una lista vacia");
            return new ArrayList<>();
        }
        String url = construirUrlBusqueda(query);
        String respuestaJson = llamarApiGenerica(url);
        return parsearPeliculas(respuestaJson);
    }

    private String construirUrlBusqueda(String query){
        String queryCodificada = URLEncoder.encode(query, StandardCharsets.UTF_8);
        String base = this.apiBaseUrl.endsWith("/") ? apiBaseUrl : this.apiBaseUrl + "/";
        return base + TMDBCategoria.BUSCAR_PELICULA.path + "?query=" + queryCodificada + "&language=es-ES&page=1";
    }

    // ***DETALLES DE PELICULAS***

    // detalles pelicula
    @Override
    public Map<String,String> detallePelicula( String id){
        if (id == null || !id.trim().matches("\\d+")) {
            LOG.debug("Id de pelicula no valido: {}", id);
            return new HashMap<>();
        }
        String url = construirUrlDetalle(id.trim());
        String respuestaJson = llamarApiGenerica(url);
        if (respuestaJson == null) return new HashMap<>();
        try {
            JsonObject pelicula = JsonParser.parseString(respuestaJson).getAsJsonObject();
            return extraerCamposPelicula(pelicula);
        } catch (Exception e) {
            LOG.error("No se pudo completar el detalle de la pelicula {}: {} " ,id, e.getMessage());
            return new HashMap<>();
        }
    }
    // construir url de detalle

    private String construirUrlDetalle (String id){
        String base = this.apiBaseUrl.endsWith("/") ? apiBaseUrl : this.apiBaseUrl + "/";
        return base + "movie/" + id + "?language=es-ES";
    }

    // funciones compartidas por varias

    private List<Map<String, String>> peliculasPorCategoria(TMDBCategoria categoria){
        String url = construirUrl(categoria);
        String respuestaJson = llamarApiGenerica(url);
        return parsearPeliculas(respuestaJson);
    }

    private String construirUrl(TMDBCategoria categoria){
        String base = this.apiBaseUrl.endsWith("/") ? apiBaseUrl : this.apiBaseUrl + "/";
        return base + categoria.path + "?language=es-ES&page=1";
    }

    private List<Map<String, String>> parsearPeliculas(String respuestaJson ) {
        List<Map<String, String>> listaPeliculas = new ArrayList<>();
        if (respuestaJson == null){
            LOG.debug("No se obtuvo respuesta de TMDB, se devuelve lista vacía");
            return  listaPeliculas;
        }
        try {
            JsonObject jsonRespuesta = JsonParser.parseString(respuestaJson).getAsJsonObject();
            if(!jsonRespuesta.has("results")){
                return listaPeliculas;
            }
            JsonArray results = jsonRespuesta.getAsJsonArray("results");
            for (int i = 0; i < results.size(); i++) {
                listaPeliculas.add(extraerCamposPelicula(results.get(i).getAsJsonObject()));
            }
        } catch (Exception e) {
            LOG.error("Error al parsear la respuesta de TMDB: {}", e.getMessage());
        }
        return listaPeliculas;
    }
    private Map<String, String> extraerCamposPelicula(JsonObject pelicula ) {
        Map<String, String> movieData = new HashMap<>();
        if (pelicula.has("title") && !pelicula.get("title").isJsonNull()){
            movieData.put("title", pelicula.get("title").getAsString());
        }
        if (pelicula.has("id") && !pelicula.get("id").isJsonNull()){
            movieData.put("id", pelicula.get("id").getAsString());
        }
        if (pelicula.has("poster_path") && !pelicula.get("poster_path").isJsonNull()){
            movieData.put("poster_path", IMAGE_BASE_URL +  pelicula.get("poster_path").getAsString());
        }
        return movieData;
    }

    // Metodo auxiliar para llamadas a la api
    private String llamarApiGenerica(String urlCompleta) {
        if (urlCompleta == null || urlCompleta.isEmpty()) {
            LOG.error("MovieService esta desactivado abortando la llamada a la API");
            return null;
        }
        LOG.debug("Ejecutando llamarApiGenerica() sobre apibaseUrl: {}", urlCompleta);
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(urlCompleta))
                    .header("Authorization", "Bearer " + apiKey)
                    .GET()
                    .timeout(Duration.ofSeconds(5))
                    .build();

            HttpResponse<String> response = this.httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                return response.body();
            } else {
                LOG.error("Error al llamar a la API de peliculas, status code: {}", response.statusCode());
                return null;
            }
        } catch (Exception e) {
            LOG.error("Error al construir la URL de la API de peliculas: {}", e.getMessage());
            return null;
        }
    }
    }
