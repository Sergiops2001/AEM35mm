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
import java.util.*;

import com.mm35.core.beans.DetallePelicula;


@Component(service = TMDBService.class, immediate = true)
@Designate(ocd = TMDBApiConfig.class)
public class TMDBServiceImpl implements TMDBService {

    private static final Logger LOG = LoggerFactory.getLogger(TMDBServiceImpl.class);

    private static final String IMAGE_BASE_URL = "https://image.tmdb.org/t/p/w500";

    private static final String IMAGE_BACKGROUND_URL = "https://image.tmdb.org/t/p/w1280";

    private static final String IMAGE_PROFILE_URL = "https://image.tmdb.org/t/p/w185";

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
    public DetallePelicula detallePelicula( String id){
        if (id == null || !id.trim().matches("\\d+")) {
            LOG.debug("Id de pelicula no valido: {}", id);
            return null;
        }
        String url = construirUrlDetalle(id.trim());
        String respuestaJson = llamarApiGenerica(url);
        if (respuestaJson == null) return null;
        try {
            JsonObject pelicula = JsonParser.parseString(respuestaJson).getAsJsonObject();
            return extraerCamposPeliculaDetalles(pelicula);
        } catch (Exception e) {
            LOG.error("No se pudo completar el detalle de la pelicula {}: {} " ,id, e.getMessage());
            return null;
        }
    }
    // construir url de detalle

    private String construirUrlDetalle (String id){
        String base = this.apiBaseUrl.endsWith("/") ? apiBaseUrl : this.apiBaseUrl + "/";
        return base + "movie/" + id + "?language=es-ES&append_to_response=credits";
    }

    // construir url imagen
    private String urlFoto(String rutaFoto){
        if (rutaFoto != null){
            return IMAGE_PROFILE_URL + rutaFoto;
        }
        return null;
    }

    // extraer campos del JsonObject de Detalle y devolver DetallePelicula relleno
    private DetallePelicula extraerCamposPeliculaDetalles(JsonObject pelicula ) {
        DetallePelicula detallesPelicula  = new DetallePelicula();
        detallesPelicula.setTitulo(leerTexto(pelicula, "title"));
        detallesPelicula.setLema(leerTexto(pelicula, "tagline"));
        detallesPelicula.setSinopsis(leerTexto(pelicula, "overview"));
        // fecha formateada
        String fecha = leerTexto(pelicula, "release_date");
        if (fecha != null && fecha.length() >= 4){
            detallesPelicula.setAnio(fecha.substring(0,4));
        }
        //duracion formateada
        Integer duracion = (leerNumeroEntero(pelicula, "runtime"));
        if (duracion != null && duracion > 0){
            int calculoDuracionHoras = duracion/60;
            int calculoDuracionMinutos = duracion %60;
            String duracionFormateada = calculoDuracionHoras + "h " + calculoDuracionMinutos + "m";
            detallesPelicula.setDuracion(duracionFormateada);
        }
        // total de votos
        detallesPelicula.setVotos(leerNumeroEntero(pelicula, "vote_count"));
        // puntuacion formateada
        Double puntuacion = (leerNumeroDecimal(pelicula, "vote_average"));
        if (puntuacion != null){
            detallesPelicula.setPuntuacion(String.format(new Locale("es"),"%.1f", puntuacion));
        }
        // imagenes
        // imagen de fondo
        String fondo = (leerTexto(pelicula, "backdrop_path"));
        if (fondo != null){
            detallesPelicula.setFondo(IMAGE_BACKGROUND_URL + fondo);
        }
        // imagen de poster
        String poster = (leerTexto(pelicula, "poster_path"));
        if (poster != null){
            detallesPelicula.setPoster(IMAGE_BASE_URL + poster);
        }
        // listas

        // generos(genres), un array dentro de pelicula, recorrer el array y añadir el nombre a la lista del DTO
        detallesPelicula.setGeneros(formatearGeneros(pelicula, "genres"));

        // para el resto necesito extraer  credits
        JsonObject credits = (leerObjeto(pelicula, "credits"));
        if (credits != null){
            detallesPelicula.setDirector(extraerDirector(credits, "crew"));
            detallesPelicula.setReparto(extraerReparto(credits, "cast"));
        }


        return detallesPelicula;
    }

    // metodo auxiliar para formatear generos
    private List<String> formatearGeneros(JsonObject json, String valor){
        List<String> listaGeneros = new ArrayList<>(); // creacion de la lista para añadir en ella los generos
        if (json.has(valor)) { // si tiene el valor que buscamos (genre)
            JsonArray generosArray = json.getAsJsonArray(valor); // obtenemos ese valor como JsonArray
            for (int i = 0; i < generosArray.size(); i ++){
                JsonObject genero = generosArray.get(i).getAsJsonObject();
                String generoNombre = leerTexto(genero, "name");
                if (generoNombre != null){
                    listaGeneros.add(generoNombre);
                }
            }
        }
        //devolver la lista llena
        return listaGeneros;
    }
    // metodo auxiliar para extraer el DIRECTOR
    private DetallePelicula.Persona extraerDirector(JsonObject json, String valor){
        if (json.has(valor)) { // si tiene el valor que buscamos (crew)
            JsonArray crewArray = json.getAsJsonArray(valor); // obtenemos ese valor como JsonArray
            for (int i = 0; i < crewArray.size(); i ++){
                JsonObject trabajo = crewArray.get(i).getAsJsonObject();
                // con el valor name me valdria tanto para genero, crew y cast
                if ("Director".equals(leerTexto(trabajo, "job"))){
                    String nombre = leerTexto(trabajo, "name");
                    String imagenDirector = leerTexto(trabajo, "profile_path");
                    String imagenDirectorFormateada = urlFoto(imagenDirector);
                    String papel = "Director";
                    DetallePelicula.Persona persona = new DetallePelicula.Persona(nombre, papel, imagenDirectorFormateada);
                    return persona;
                }
            }
        }
        return null;
    }

    // metodo auxiliar para extraer el REPARTO
    private List<DetallePelicula.Persona> extraerReparto(JsonObject json, String valor){
        List<DetallePelicula.Persona> reparto = new ArrayList<>(); // aqui guardaremos el director
        if (json.has(valor)) { // si tiene el valor que buscamos (cast)
            JsonArray castArray = json.getAsJsonArray(valor); // obtenemos ese valor como JsonArray
            for (int i = 0; i < Math.min(castArray.size(), 5); i ++){
                JsonObject actor = castArray.get(i).getAsJsonObject();
                String nombre = leerTexto(actor, "name");
                String papel = leerTexto(actor, "character");
                String foto = leerTexto(actor,"profile_path");
                String fotoFormateada = urlFoto(foto);

                if (nombre!= null){
                    reparto.add(new DetallePelicula.Persona(nombre, papel,fotoFormateada));
                }
            }
        }
        //devolver la lista del reparto
        return reparto;
    }

    // metodo auxiliar para leer texto sin repetir has e isJsonNull
    private String leerTexto(JsonObject json ,String clave){
        if (json.has(clave) && !json.get(clave).isJsonNull()){
            return json.get(clave).getAsString();
        }
        return null;
    }
    // leer objeto
    private JsonObject leerObjeto(JsonObject json, String clave){
        if (json.has(clave) && json.get(clave).isJsonObject()){
            return json.getAsJsonObject(clave);
        }
        return null;
    }

    // metodo auxiliar para leer nummero entero sin repetir has e isJsonNull
    private Integer leerNumeroEntero(JsonObject json , String clave){
        if (json.has(clave) && !json.get(clave).isJsonNull()){
            return json.get(clave).getAsInt();
        }
        return null;
    }
    // metodo auxiliar para leer numero decimal sin repetir has e isJsonNull
    private Double leerNumeroDecimal(JsonObject json , String clave){
        if (json.has(clave) && !json.get(clave).isJsonNull()){
            return json.get(clave).getAsDouble();
        }
        return null;
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


    /*
     {
    "id": 550,
    "title": "El club de la lucha",
    "tagline": "Las reglas no existen...",
    "overview": "Un empleado de oficina insomne...",
    "release_date": "1999-10-15",
    "runtime": 139,
    "vote_average": 8.438,
    "vote_count": 30000,
    "poster_path": "/abc.jpg",
    "backdrop_path": "/xyz.jpg",
    "genres": [ {"id": 18, "name": "Drama"}, {"id": 53, "name": "Suspense"} ],
    "credits": {
      "cast": [ {"name": "Brad Pitt", "character": "Tyler Durden", "order": 0, "profile_path":
  "/..."}, ... ],
      "crew": [ {"name": "David Fincher", "job": "Director", ...}, ... ]
    }
  }


     */
