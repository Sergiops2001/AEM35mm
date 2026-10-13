package com.mm35.core.servlets;

import com.google.gson.Gson;
import com.mm35.core.services.TMDBService;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.HttpConstants;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.apache.sling.servlets.annotations.SlingServletResourceTypes;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.propertytypes.ServiceDescription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Servlet para el buscador de peliculas, el cual recibe un parametro y devuelve un json con los resultados al front
 */
@Component(service = { Servlet.class })
@SlingServletResourceTypes(
        resourceTypes="35mm/components/buscadorPelis",
        methods=HttpConstants.METHOD_GET,
        selectors="ejecutar-buscador-pelis",// el nombre del selector que uso en el js
        extensions="json")
@ServiceDescription("Servlet para el buscador de pelis")
public class BuscadorPelisServlet extends SlingSafeMethodsServlet {

    private static final long serialVersionUID = 1L;

    // inyectamos el servicio de movie con @Reference
    @Reference
    private TMDBService tmdbService;

    @Override
    protected void doGet(final SlingHttpServletRequest req,
                         final SlingHttpServletResponse resp) throws ServletException, IOException {

        // obtenemos los parametros requeridos
        String nombrePeli = req.getParameter("query");
        // configuracion de cabeceras
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        Gson gson = new Gson();

        if(nombrePeli != null && !nombrePeli.trim().isEmpty()){
            // obtenemos el servicio de TMDB para obtener la informacion de la peli buscada
            // llamamos al metodo del servicio para obtener la informacion de la pelicula
            List<Map<String, String>> pelisData = tmdbService.buscarPeliculas(nombrePeli.trim());
            // filtro para no mandar pelis sin poster
            pelisData.removeIf(peli -> !peli.containsKey("poster_path"));
            // con gson se convierte automaticamente el List<Map> en un JSON válido
            resp.getWriter().write(gson.toJson(pelisData));
        }
        else {
            // Manejo del caso donde no llega ningún parámetro
            resp.setStatus(SlingHttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\": \"No se proporcionaron parámetros de búsqueda\"}");
        }
    }
}
