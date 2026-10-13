package com.mm35.core.config;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.AttributeType;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

@ObjectClassDefinition(
    name = "35MM - TMDB API Config",
    description = "Configuración de la API de TMDB"
)
public @interface TMDBApiConfig {

    @AttributeDefinition(
        name = "apiBaseUrl",
        description = "URL base de la API de peliculas",
        type = AttributeType.STRING
    )
    String apiBaseUrl() default "https://api.themoviedb.org/3";

    @AttributeDefinition(
            name = "apiKey",
            description = "apiKey de la API de TMDB",
            type = AttributeType.STRING
    )
    String apiKey() default "";
}




