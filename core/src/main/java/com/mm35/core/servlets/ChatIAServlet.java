package com.mm35.core.servlets;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mm35.core.services.GroqService;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.HttpConstants;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.apache.sling.servlets.annotations.SlingServletResourceTypes;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.propertytypes.ServiceDescription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    private static final Logger LOG = LoggerFactory.getLogger(ChatIAServlet.class);

    private static final long serialVersionUID = 1L;
    private static final int MAX_LONGITUD_PREGUNTA = 500; // tope de caracteres
    private static final int MAX_MENSAJES_HISTORIAL = 10; // 5 mensajes y 5 respuestas
    private static final int MAX_LONGITUD_MENSAJE = 2000;

    // inyectamos el servicio de movie con @Reference
    @Reference
    private GroqService groqService;

    @Override
    protected void doPost(final SlingHttpServletRequest req,
                         final SlingHttpServletResponse resp) throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");

        // obtenemos los parametros requeridos
        String pregunta = req.getParameter("pregunta");
        List<Map<String, String>> historial = leerHistorial(req.getParameter("historial"));
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
        String respuestaIA = groqService.preguntarGroq(pregunta.trim(), historial);
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

    // metodo auxiliar paraa leer el historial, convierte el texto JSON del front en una lista validada; nunca lanza excepciones
    private List<Map<String, String>> leerHistorial(String historialJson){
        List<Map<String, String>> historial = new ArrayList<>();
        if (historialJson == null || historialJson.trim().isEmpty()){
            return historial;
        }
        try {
            JsonArray array = JsonParser.parseString(historialJson).getAsJsonArray();
            // empezamos por los ultimos mensajes, no por los primeros
            int inicio = Math.max(0, array.size() - MAX_MENSAJES_HISTORIAL);
            for (int i = inicio; i < array.size(); i ++){
                JsonObject mensaje = array.get(i).getAsJsonObject();
                String rol = mensaje.get("role").getAsString();
                String contenido = mensaje.get("content").getAsString();
                if (!"user".equals(rol) && !"assistant".equals(rol)){
                    continue; // no aceptamos roles "system" ni inventados
                }
                if (contenido.trim().isEmpty() || contenido.length() > MAX_LONGITUD_MENSAJE){
                    continue;
                }
                Map<String, String> valido = new HashMap<>();
                valido.put("role", rol);
                valido.put("content", contenido);
                historial.add(valido);
            }
        } catch (Exception e) {
            LOG.warn("Error al parsear el historial del chat: {}", e.getMessage());
            return new ArrayList<>();
        }
        return historial;
    }
}
