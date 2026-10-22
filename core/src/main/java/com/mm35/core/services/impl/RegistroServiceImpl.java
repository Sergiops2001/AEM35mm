package com.mm35.core.services.impl;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mm35.core.services.GroqService;
import com.mm35.core.services.RegistroService;
import com.mm35.core.services.ResultadoRegistro;
import org.apache.jackrabbit.api.security.user.*;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.Designate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.LoginException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

@Component(service = RegistroService.class, immediate = true)
public class RegistroServiceImpl implements RegistroService {

    private static final Logger LOG = LoggerFactory.getLogger(RegistroServiceImpl.class);

    private static final String SUBSERVICIO = "registro-usuarios";
    private static final String CARPETA_USUARIOS = "35mm";
    private static final String GRUPO_USUARIOS = "35mm-usuarios";

    // validacion datos del formulario
    // de 3 a 20 caracteres, minusculas, numeros o gión bajo
    private static final Pattern PATRON_USUARIO = Pattern.compile("^[a-z0-9_]{3,20}$");
    private static final int PASSWORD_MIN = 8;
    private static final int PASSWORD_MAX = 20;
    // ids que ya existen en AEM y que el service user no puede ver
    private static final Set<String> NOMBRES_RESERVADOS = Set.of("admin", "anonymous", "everyone", "administrators");


    @Reference
    private ResourceResolverFactory resolverFactory;



    @Override
    //Registro de usuario
    public ResultadoRegistro registrarUsuario(String usuario, String password) {
        // validar formato
        if (usuario == null || !PATRON_USUARIO.matcher(usuario).matches() || NOMBRES_RESERVADOS.contains(usuario)){
            return ResultadoRegistro.USUARIO_NO_VALIDO;
        }
        if (password == null || password.length() < PASSWORD_MIN || password.length() > PASSWORD_MAX){
            return ResultadoRegistro.PASSWORD_NO_VALIDA;
        }
        // abrir sesion como el usuario de servicio
        Map<String, Object> params = Map.of(ResourceResolverFactory.SUBSERVICE, SUBSERVICIO);
        try(ResourceResolver resolver = resolverFactory.getServiceResourceResolver(params)){
            Session session = resolver.adaptTo(Session.class);
            UserManager userManager = resolver.adaptTo(UserManager.class);
            if (session == null || userManager == null){
                LOG.error("No se pudo obtener la sesion o el UserManager del usuario de servicio");
                return ResultadoRegistro.ERROR;
            }
            // comprobar que no existe ya
            if(userManager.getAuthorizable(usuario) != null){
                return ResultadoRegistro.NO_DISPONIBLE;
            }
            // buscar el grupo
            Authorizable grupo = userManager.getAuthorizable(GRUPO_USUARIOS);
            if (grupo == null || !grupo.isGroup()){
                LOG.error("No existe el grupo {}: revisa el repoinit", GRUPO_USUARIOS);
                return ResultadoRegistro.ERROR;
            }
            // crear el usuario y meterlo en el grupo
            Principal principal = () -> usuario;
            User nuevoUsuario = userManager.createUser(usuario,password, principal, CARPETA_USUARIOS);
            ((Group) grupo).addMember(nuevoUsuario);
            // guardar
            session.save();
            LOG.info("Usuario registrado: {}", usuario);
            return ResultadoRegistro.CREADO;
            } catch (AuthorizableExistsException e) {
                return ResultadoRegistro.NO_DISPONIBLE;
            } catch (org.apache.sling.api.resource.LoginException e) {
                LOG.error("No se pudo abrir la sesion del subservicio '{}': {}", SUBSERVICIO, e.getMessage());
                return ResultadoRegistro.ERROR;
            } catch (RepositoryException e) {
                LOG.warn("No se pudo crear el usuario {}: {}", usuario, e.getMessage());
                return ResultadoRegistro.NO_DISPONIBLE;
            }
    }

}

