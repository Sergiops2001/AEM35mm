package com.mm35.core.servlets;

import com.google.gson.JsonObject;
import com.mm35.core.services.GroqService;
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

/**
 * Servlet para el chat con la IA, el cual recibe una pregunta y devuelve un json con los resultados al front
 */
@Component(service = { Servlet.class })
@SlingServletResourceTypes(
        resourceTypes="35mm/components/chatIA",
        methods=HttpConstants.METHOD_POST,
        selectors="preguntar-chat-ia",// el nombre del selector que uso en el js
        extensions="json")
@ServiceDescription("Servlet para el chat con la IA")
public class ChatIAServlet extends SlingAllMethodsServlet {

    private static final long serialVersionUID = 1L;
    private static final int MAX_LONGITUD_PREGUNTA = 500; // tope de caracteres

    // inyectamos el servicio de movie con @Reference
    @Reference
    private GroqService groqService;

    @Override
    protected void doPost(final SlingHttpServletRequest req,
                         final SlingHttpServletResponse resp) throws ServletException, IOException {

        // obtenemos los parametros requeridos
        String pregunta = req.getParameter("pregunta");
        // configuracion de cabeceras
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        if (pregunta == null || pregunta.trim().isEmpty()){
            escribirError(resp, SlingHttpServletResponse.SC_BAD_REQUEST, "No se ha enviado ninguna pregunta");
            return;
        }
        if (pregunta.length() > MAX_LONGITUD_PREGUNTA){
            escribirError(resp, SlingHttpServletResponse.SC_BAD_REQUEST, "La pregunta es demasiado larga");
            return;
        }
        String respuestaIA = groqService.preguntarGroq(pregunta.trim());
        if (respuestaIA == null){
            escribirError(resp, SlingHttpServletResponse.SC_BAD_GATEWAY, "La IA no ha podido responder");
            return;
        }
        JsonObject json = new JsonObject();
        json.addProperty("respuesta", respuestaIA);
        resp.getWriter().write(json.toString());
    }
    // metodo auxiliar para escribir los errores, para no repetir codigo constantemente
    private void escribirError(SlingHttpServletResponse resp, int estado, String mensaje) throws IOException{
        resp.setStatus(estado);
        JsonObject json = new JsonObject();
        json.addProperty("error", mensaje);
        resp.getWriter().write(json.toString());
    }
}
