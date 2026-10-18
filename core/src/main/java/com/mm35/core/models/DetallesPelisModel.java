package com.mm35.core.models;

import com.mm35.core.services.TMDBService;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.OSGiService;
import org.apache.sling.models.annotations.injectorspecific.Self;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;

import java.util.HashMap;
import java.util.Map;

@Model(adaptables =  SlingHttpServletRequest.class,
       defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)

public class DetallesPelisModel {

    private static final Logger LOG = LoggerFactory.getLogger(DetallesPelisModel.class);

    @Self
    private SlingHttpServletRequest request;


    @OSGiService
    private TMDBService tmdbService;

    private Map<String, String> pelicula = new HashMap<>();


    @PostConstruct
    protected void init() {
        if (tmdbService != null) {
            String id = request.getParameter("id");
            pelicula = tmdbService.detallePelicula(id);
        }
    }
    public boolean isEncontrada(){
        return pelicula.containsKey("title");
    }

    public String getTitulo(){
        return pelicula.get("title");
    }

    public String getPoster(){
        return pelicula.get("poster_path");
    }
}




