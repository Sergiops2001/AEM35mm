package com.mm35.core.servlets;


import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.mm35.core.beans.Favorito;
import com.mm35.core.services.FavoritosService;
import com.mm35.core.services.ResultadoFavorito;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.HttpConstants;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.apache.sling.servlets.annotations.SlingServletResourceTypes;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.propertytypes.ServiceDescription;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component(service = { Servlet.class })
@SlingServletResourceTypes(
        resourceTypes={"35mm/components/detallesPelis","35mm/components/misFavoritos"},
        methods={HttpConstants.METHOD_POST, HttpConstants.METHOD_GET},
        selectors = "favoritos",
        extensions = "json")
@ServiceDescription("Servlet para favoritos de usuario")
public class FavoritosServlet extends SlingAllMethodsServlet {

    private static final long serialVersionUID = 1L;
    private static final String USUARIO_ANONIMO = "anonymous";

    @Reference
    private FavoritosService favoritoService;



    @Override
    protected void doPost(final SlingHttpServletRequest req,
            final SlingHttpServletResponse resp) throws ServletException, IOException {
        prepararRespuesta(resp);
        String usuario = usuarioConectado(req);
        if (usuario == null) {
            escribirError(resp, SlingHttpServletResponse.SC_UNAUTHORIZED, "Inicia sesión para usar favoritos");
            return;
        }
        // leer accion e id
        String accion = req.getParameter("accion");
        String id = req.getParameter("id");

        ResultadoFavorito resultado;
        boolean favoritoDespues; // como queda el corazon si va bien

        if ("anadir".equals(accion)){
            resultado = favoritoService.anadirFavorito(usuario, id);
            favoritoDespues = true;
        }
        else if ("quitar".equals(accion)){
            resultado = favoritoService.quitarFavorito(usuario, id);
            favoritoDespues = false;
        }
        else {
            escribirError(resp, SlingHttpServletResponse.SC_BAD_REQUEST, "Error, no es posible realizar esa acción");
            return;
        }
        // aquí el switch
        switch (resultado){
            case OK:
                JsonObject json = new JsonObject();
                json.addProperty("favorito", favoritoDespues);
                resp.getWriter().write(json.toString());
                break;
            case PELICULA_NO_VALIDA:
                escribirError(resp, SlingHttpServletResponse.SC_BAD_REQUEST,
                        "Pelicula no valida");
                break;
            case NO_AUTORIZADO:
                escribirError(resp, SlingHttpServletResponse.SC_FORBIDDEN,
                        "Usuario no autorizado");
                break;
            default:
                escribirError(resp, SlingHttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                        "Error interno del servidor");
        }
    }
    @Override
    protected void doGet(final SlingHttpServletRequest req,
                          final SlingHttpServletResponse resp) throws ServletException, IOException {
        prepararRespuesta(resp);
        String usuario = usuarioConectado(req);
        if (usuario == null) {
            escribirError(resp, SlingHttpServletResponse.SC_UNAUTHORIZED, "Inicia sesión para usar favoritos");
            return;
        }
        String id = req.getParameter("id");
        JsonObject json = new JsonObject();
        // dos formas:
        // con id, para saber como pintar el corazon
        if (id != null){
            json.addProperty("favorito", favoritoService.esFavorito(usuario, id));
        }
        // sin id, lista de favoritos
        else {
            List<Favorito> listaFavoritos = favoritoService.obtenerFavoritos(usuario);
            if (listaFavoritos == null) {
                escribirError(resp, SlingHttpServletResponse.SC_FORBIDDEN, "Este usuario no puede tener favoritos");
                return;
            }
            Gson gson = new Gson();
            json.add("favoritos", gson.toJsonTree(listaFavoritos));
        }
        resp.getWriter().write(json.toString());
    }
    // metodos auxiliares
    // devuelve el id del usuario o null si es anónimo
    private String usuarioConectado(SlingHttpServletRequest req){
        if (req.getResourceResolver().getUserID() == null || req.getResourceResolver().getUserID().equals(USUARIO_ANONIMO) ) return null;
        return req.getResourceResolver().getUserID();
    }

    // pone las tres cabeceras de resp
    private void prepararRespuesta(SlingHttpServletResponse resp){
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        resp.setHeader("Cache-Control", "no-store");
    }

    // metodo auxiliar para escribir los errores, para no repetir codigo constantemente
    private void escribirError(SlingHttpServletResponse resp, int estado, String mensaje) throws IOException{
        resp.setStatus(estado);
        JsonObject json = new JsonObject();
        json.addProperty("error", mensaje);
        resp.getWriter().write(json.toString());
    }
}
