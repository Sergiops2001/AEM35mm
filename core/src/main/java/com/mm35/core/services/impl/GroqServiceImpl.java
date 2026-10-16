package com.mm35.core.services.impl;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mm35.core.config.GroqApiConfig;
import com.mm35.core.services.GroqService;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.metatype.annotations.Designate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Map;

@Component(service = GroqService.class, immediate = true)
@Designate(ocd = GroqApiConfig.class)
public class GroqServiceImpl implements GroqService {

    private static final Logger LOG = LoggerFactory.getLogger(GroqServiceImpl.class);

    private String apiBaseGroqUrl;
    private String apiKeyGroq;
    private String modelGroq;

    private HttpClient httpClient;

    @Activate
    @Modified
    protected void activate(GroqApiConfig config) {
        this.apiBaseGroqUrl = config.apiBaseGroqUrl();
        this.apiKeyGroq = config.apiKeyGroq();
        this.modelGroq = config.modelGroq();
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(3))
                .build();
        LOG.info("GroqService activado con apiBaseGroqUrl: {}", apiBaseGroqUrl);
    }

    // Constante con el prompt para el modelo de la IA, aquí viene la definición de su compoortamiento
    private static final String SYSTEM_PROMPT = "Eres el asistente de cine de la web 35mm. Respondes siempre en español, "
            + "de forma breve y cercana. Solo hablas de películas: recomendaciones, actores, "
            + "directores y curiosidades. Si te preguntan por otro tema, indica con amabilidad "
            + "que solo puedes ayudar con cine."
            + " Responde en texto plano, sin Markdown, sin asteriscos y sin listas con simbolos.";

    @Override
    //Preguntar al chat de la IA de Groq
    public String preguntarGroq(String pregunta, List<Map<String, String>> historial) {
        if (pregunta == null || pregunta.trim().isEmpty()){
            return null;
        }
        if (apiKeyGroq == null || apiKeyGroq.isEmpty()){
            LOG.error("No hay apiKey de Groq configurada");
            return null;
        }
        String cuerpo = construirCuerpo(pregunta.trim(), historial);
        String respuestaJSON = llamarApiGroq(cuerpo);
        return parsearRespuesta(respuestaJSON);
    }

    // Construir cuerpo de la petición
    private String construirCuerpo(String pregunta, List<Map<String, String>> historial){

        JsonArray mensajes = new JsonArray();

        // Creamos el rol de sistema con las instrucciones del modelo
        JsonObject sistema = new JsonObject();
        sistema.addProperty("role", "system");
        sistema.addProperty("content", SYSTEM_PROMPT);
        mensajes.add(sistema);

        // añadimos el historial, si es que hay. Añadimos tanto los mensajes de user como los de assistant
        if (historial != null){
            for (Map<String, String> anterior : historial){
                JsonObject mensaje = new JsonObject();
                mensaje.addProperty("role", anterior.get("role"));
                mensaje.addProperty("content", anterior.get("content"));
                mensajes.add(mensaje);
            }
        }

        // creamos el rol de usuario con la pregunta actual
        JsonObject usuario = new JsonObject();
        usuario.addProperty("role", "user");
        usuario.addProperty("content", pregunta);
        mensajes.add(usuario);

        JsonObject cuerpo = new JsonObject();
        cuerpo.addProperty("model", modelGroq);
        cuerpo.add("messages", mensajes); // es la conversacion completa en forma de lista, cada mensaje lleva el role y el content
        cuerpo.addProperty("temperature", 0.7); // variedad en las respuetas a las preguntas para no responder siempre lo mismo
        cuerpo.addProperty("max_completion_tokens", 500); // para controlar la respuesta, que no sea muy larga
        return cuerpo.toString();
    }

    private String llamarApiGroq(String cuerpo){
        try{
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiBaseGroqUrl + "/chat/completions"))
                    .header("Authorization", "Bearer " + apiKeyGroq)
                    .header("Content-Type", "application/json")
                    .timeout(Duration.ofSeconds(20))
                    .POST(HttpRequest.BodyPublishers.ofString(cuerpo,
                                    StandardCharsets.UTF_8))
                    .build();
            HttpResponse<String> response = this.httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                return response.body();
            } else {
                LOG.error("Error al llamar a la API de Groq, status code: {}, respuesta: {}", response.statusCode(), response.body());
                return null;
            }
        } catch (Exception e) {
            LOG.error("Error al llamar a la API de Groq: {}", e.getMessage());
            return null;
        }
    }

    private String parsearRespuesta(String respuestaJson){
        if (respuestaJson == null){
            return null;
        }
        try {
            JsonObject json = JsonParser.parseString(respuestaJson).getAsJsonObject();
            JsonArray choices = json.getAsJsonArray("choices"); // choices es un array con varias opciones de respuesta
            if (choices == null || choices.isEmpty()){
                return null;
            }
            return choices.get(0).getAsJsonObject().getAsJsonObject("message").get("content").getAsString(); // cogemos la primera respuesta, y obtenemos el mensaje de la IA
        } catch (Exception e) {
            LOG.error("Error al leer la respuesta de Groq: {}", e.getMessage());
            return null;
        }
    }
}

