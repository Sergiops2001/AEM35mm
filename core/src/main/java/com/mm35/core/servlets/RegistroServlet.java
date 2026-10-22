package com.mm35.core.servlets;

import com.google.gson.JsonObject;
import com.mm35.core.services.RegistroService;
import com.mm35.core.services.ResultadoRegistro;
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
import java.util.Locale;

@Component(service = { Servlet.class })
@SlingServletResourceTypes(
        resourceTypes = "35mm/components/registro",
        methods = HttpConstants.METHOD_POST,
        selectors = "registrar-usuario",
        extensions = "json")
@ServiceDescription("Servlet para registrar usuarios de la web")
public class RegistroServlet extends SlingAllMethodsServlet {

    private static final long serialVersionUID = 1L;

    @Reference
    private RegistroService registroService;

    @Override
    protected void doPost(final SlingHttpServletRequest req,
                          final SlingHttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        String usuario = req.getParameter("usuario");
        String password = req.getParameter("password");
        String password2 = req.getParameter("password2");

        if (estaVacio(usuario) || estaVacio(password) || estaVacio(password2)) {
            escribir(resp, SlingHttpServletResponse.SC_BAD_REQUEST, "error", "Rellena todos los campos");
            return;
        }
        if (!password.equals(password2)) {
            escribir(resp, SlingHttpServletResponse.SC_BAD_REQUEST, "error", "Las contraseñas no coinciden");
            return;
        }

        String usuarioNormalizado = usuario.trim().toLowerCase(Locale.ROOT);
        ResultadoRegistro resultado = registroService.registrarUsuario(usuarioNormalizado, password);

        switch (resultado) {
            case CREADO:
                escribir(resp, SlingHttpServletResponse.SC_OK, "mensaje", "Cuenta creada. Ya puedes iniciar sesión");
                break;
            case USUARIO_NO_VALIDO:
                escribir(resp, SlingHttpServletResponse.SC_BAD_REQUEST, "error",
                        "El usuario debe tener entre 3 y 20 caracteres: letras, números o _");
                break;
            case PASSWORD_NO_VALIDA:
                escribir(resp, SlingHttpServletResponse.SC_BAD_REQUEST, "error",
                        "La contraseña debe tener entre 8 y 64 caracteres");
                break;
            case NO_DISPONIBLE:
                escribir(resp, SlingHttpServletResponse.SC_CONFLICT, "error", "Ese nombre de usuario no está disponible");
                break;
            default:
                escribir(resp, SlingHttpServletResponse.SC_INTERNAL_SERVER_ERROR, "error",
                        "No se ha podido crear la cuenta. Inténtalo más tarde");
        }
    }

    private boolean estaVacio(String valor) {
        return valor == null || valor.trim().isEmpty();
    }

    private void escribir(SlingHttpServletResponse resp, int estado, String clave, String mensaje) throws IOException {
        resp.setStatus(estado);
        JsonObject json = new JsonObject();
        json.addProperty(clave, mensaje);
        resp.getWriter().write(json.toString());
    }
}
