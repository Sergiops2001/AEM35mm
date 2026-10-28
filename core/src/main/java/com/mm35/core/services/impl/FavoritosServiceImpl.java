package com.mm35.core.services.impl;

import com.mm35.core.beans.DetallePelicula;
import com.mm35.core.beans.Favorito;
import com.mm35.core.services.FavoritosService;
import com.mm35.core.services.ResultadoFavorito;
import com.mm35.core.services.TMDBService;
import org.apache.jackrabbit.api.security.user.*;
import org.apache.sling.api.resource.*;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import java.util.*;
import java.util.regex.Pattern;

@Component(service = FavoritosService.class, immediate = true)
public class FavoritosServiceImpl implements FavoritosService {

    private static final Logger LOG = LoggerFactory.getLogger(FavoritosServiceImpl.class);

    private static final String SUBSERVICIO = "favoritos-usuario";
    private static final String CARPETA_FAVORITOS = "favoritos";
    private static final Pattern PATRON_ID = Pattern.compile("^\\d+"); // solo digitos

    @Reference
    private ResourceResolverFactory resolverFactory; // abrir y cerrar sesión

    @Reference
    private TMDBService tmdbService; // para pedir la info

    @Override
    public boolean esFavorito(String usuarioId, String peliculaId){
        if (!idValido(peliculaId)){
            return false;
        }
        try(ResourceResolver resolver = abrirResolver()){
            Resource usuario = recursoDelUsuario(resolver, usuarioId);
            return usuario != null && usuario.getChild(CARPETA_FAVORITOS + "/" + peliculaId) != null;
        }
        catch (LoginException | RepositoryException e){
            LOG.error("No se pudo comprobar el favorito {} de {}: {}", peliculaId, usuarioId, e.getMessage());
            return false;
        }
    }
    @Override
    public List<Favorito> obtenerFavoritos(String usuarioId){
        try(ResourceResolver resolver = abrirResolver()){
            Resource usuario = recursoDelUsuario(resolver, usuarioId);
            if (usuario == null){
                return null;
            }
            List<Favorito> listaFavoritos = new ArrayList<>();
            Resource carpetaFavoritos = usuario.getChild(CARPETA_FAVORITOS);
            if (carpetaFavoritos == null){
                LOG.debug("No hay favoritos");
                return listaFavoritos;
            }
            for (Resource hijo : carpetaFavoritos.getChildren()){
                ValueMap valoresHijo = hijo.getValueMap();
                String id = valoresHijo.get("id", String.class);
                String titulo = valoresHijo.get("titulo", String.class);
                String poster = valoresHijo.get("poster", String.class);
                listaFavoritos.add(new Favorito(id, titulo, poster));
            }
            Collections.reverse(listaFavoritos);
            return listaFavoritos;
        } catch (LoginException | RepositoryException e) {
            LOG.error("No se pudo obtener los favoritos de {}: {}",  usuarioId, e.getMessage());
            return null;
        }
    }

    @Override
    public ResultadoFavorito anadirFavorito(String usuarioId, String peliculaId){
        if (!idValido(peliculaId)) return ResultadoFavorito.PELICULA_NO_VALIDA;
        try(ResourceResolver resolver = abrirResolver()) {
            Resource usuario = recursoDelUsuario(resolver, usuarioId);
            if (usuario == null){
                return ResultadoFavorito.NO_AUTORIZADO;
            }
            // comprobar si existe la carpeta favoritos
            Resource carpetaFavoritos = usuario.getChild(CARPETA_FAVORITOS);
            // si no existe la creamos
            if (carpetaFavoritos == null){
                carpetaFavoritos = resolver.create(usuario, CARPETA_FAVORITOS, Map.of("jcr:primaryType", "nt:unstructured"));
            }
            if (carpetaFavoritos.getChild(peliculaId) != null) return ResultadoFavorito.OK;
            // llamar a TMDB
            DetallePelicula pelicula  = tmdbService.detallePelicula(peliculaId);
            if (pelicula == null) return ResultadoFavorito.PELICULA_NO_VALIDA;
            // propiedades del nodo
            HashMap<String, Object> propiedades = new HashMap<>();
            propiedades.put("jcr:primaryType", "nt:unstructured");
            propiedades.put("id", peliculaId);
            propiedades.put("titulo",pelicula.getTitulo());
            if (pelicula.getPoster() != null) {
                propiedades.put("poster", pelicula.getPoster());
            }
            propiedades.put("fecha", Calendar.getInstance());
            // crear el favorito con las propiedades
            resolver.create(carpetaFavoritos, peliculaId, propiedades);
            // commitear
            resolver.commit();
            return ResultadoFavorito.OK;
        } catch (LoginException | RepositoryException | PersistenceException e) {
            LOG.error("No se pudo añadir a favoritos la pelicula {} de {} : {}", peliculaId,  usuarioId, e.getMessage());
            return ResultadoFavorito.ERROR;
        }
    }

    @Override
    public ResultadoFavorito quitarFavorito(String usuarioId, String peliculaId){
        if (!idValido(peliculaId)) return ResultadoFavorito.PELICULA_NO_VALIDA;
        try(ResourceResolver resolver = abrirResolver()) {
            Resource usuario = recursoDelUsuario(resolver, usuarioId);
            if (usuario == null) {
                return ResultadoFavorito.NO_AUTORIZADO;
            }
            Resource peliculaAEliminar = usuario.getChild(CARPETA_FAVORITOS + "/" + peliculaId);
            if (peliculaAEliminar != null){
                resolver.delete(peliculaAEliminar);
                // commitear cambios
                resolver.commit();
            }
            return ResultadoFavorito.OK;
        } catch (LoginException | RepositoryException | PersistenceException e) {
            LOG.error("No se pudo eliminar de favoritos la pelicula {} de {} : {}", peliculaId,  usuarioId, e.getMessage());
            return ResultadoFavorito.ERROR;
        }
    }

    // metodos auxiliares
    // ver si el id es valido
    private boolean idValido(String id){
        if (id != null && !id.isEmpty() && PATRON_ID.matcher(id).matches()){
            return true;
        }
        else return false;
    }

    // obtener el resource resolver
    private ResourceResolver abrirResolver() throws LoginException {
        Map<String, Object> params = Map.of(ResourceResolverFactory.SUBSERVICE, SUBSERVICIO);
        return resolverFactory.getServiceResourceResolver(params);
    }

    // obtener los recursos del usuario
    private Resource recursoDelUsuario(ResourceResolver resolver, String usuarioId) throws RepositoryException {
        if (usuarioId == null) {
            return null;
        }
        UserManager userManager = resolver.adaptTo(UserManager.class);
        if (userManager == null) {
            return null;
        }
        // obtener usuario
        Authorizable usuario = userManager.getAuthorizable(usuarioId);
        if (usuario == null || usuario.isGroup()) {
            return null;
        }
        // devolver el recurso del path del user
        return resolver.getResource(usuario.getPath());
    }
}


