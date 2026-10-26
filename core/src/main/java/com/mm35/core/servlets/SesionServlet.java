package com.mm35.core.servlets;


import com.google.gson.JsonObject;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.HttpConstants;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.apache.sling.servlets.annotations.SlingServletResourceTypes;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.propertytypes.ServiceDescription;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import java.io.IOException;

@Component(service = { Servlet.class })
@SlingServletResourceTypes(
        resourceTypes="35mm/components/header",
        methods=HttpConstants.METHOD_GET,
        selectors = "sesion",
        extensions = "json")
@ServiceDescription("Servlet que indica si hay un usuario con sesion iniciada")
public class SesionServlet extends SlingSafeMethodsServlet {

    private static final long serialVersionUID = 1L;
    private static final String USUARIO_ANONIMO = "anonymous";

    @Override
    protected void doGet(final SlingHttpServletRequest req,
            final SlingHttpServletResponse resp) throws ServletException, IOException {
        String usuario = req.getResourceResolver().getUserID();
        boolean autenticado = usuario != null && !USUARIO_ANONIMO.equals(usuario);

        // configuracion de cabeceras
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        // respuesta distinta para cada usuario, que nadie la guarde en cache
        resp.setHeader("Cache-Control", "no-store");

        JsonObject json = new JsonObject();
        json.addProperty("autenticado", autenticado);
        if (autenticado){
            json.addProperty("usuario", usuario);
        }
        resp.getWriter().write(json.toString());
    }
}
