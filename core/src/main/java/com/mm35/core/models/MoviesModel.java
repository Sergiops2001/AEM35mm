package com.mm35.core.models;

import com.mm35.core.services.TMDBService;
import org.apache.commons.logging.Log;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.OSGiService;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.sling.models.annotations.Default;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Model(adaptables = { SlingHttpServletRequest.class,
        Resource.class }, defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)

public class MoviesModel {

    private static final Logger LOG = LoggerFactory.getLogger(MoviesModel.class);

    @ValueMapValue
    @Default(booleanValues = false)
    private boolean mostrarDestacadas;

    public boolean isMostrarDestacadas() {
        return mostrarDestacadas;
    }

    @ValueMapValue
    @Default(booleanValues = false)
    private boolean mostrarRecientes;

    public boolean isMostrarRecientes() {
        return mostrarRecientes;
    }

    @ValueMapValue
    @Default(booleanValues = false)
    private boolean mostrarValoradas;

    public boolean isMostrarValoradas() {
        return mostrarValoradas;
    }

    @OSGiService
    private TMDBService tmdbService;

    private List<Map<String, String>> peliculasDestacadas = new ArrayList<>();

    private List<Map<String, String>> peliculasValoradas = new ArrayList<>();

    private List<Map<String, String>> peliculasRecientes = new ArrayList<>();

    @PostConstruct
    protected void init() {
        try{
            peliculasDestacadas = tmdbService.peliculasDestacadas();
        }
        catch (Exception e){
            LOG.error("Error al obtener las peliculas destacadas: {}", e.getMessage());
        }
        try{
            peliculasValoradas = tmdbService.peliculasValoradas();
        }
        catch (Exception e){
            LOG.error("Error al obtener las peliculas destacadas: {}", e.getMessage());
        }
        try{
            peliculasRecientes = tmdbService.peliculasRecientes();
        }
        catch (Exception e){
            LOG.error("Error al obtener las peliculas destacadas: {}", e.getMessage());
        }
    }

    public List<Map<String, String>> getPeliculasDestacadas() {
        return peliculasDestacadas;
    }

    public List<Map<String, String>> getPeliculasValoradas() {
        return peliculasValoradas;
    }

    public List<Map<String, String>> getPeliculasRecientes() {
        return peliculasRecientes;
    }
}
