package com.mm35.core.config;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.AttributeType;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

@ObjectClassDefinition(
    name = "35MM - Groq API Config",
    description = "Configuración de la API de Groq"
)
public @interface GroqApiConfig {

    @AttributeDefinition(
        name = "apiBaseGroqUrl",
        description = "URL base de la API de Groq",
        type = AttributeType.STRING
    )
    String apiBaseGroqUrl() default "https://api.groq.com/openai/v1";

    @AttributeDefinition(
            name = "apiKeyGroq",
            description = "apiKey de la API de Groq",
            type = AttributeType.STRING
    )
    String apiKeyGroq() default "";

    @AttributeDefinition(
            name = "modelGroq",
            description = "modelo de la API de Groq",
            type = AttributeType.STRING
    )
    String modelGroq() default "openai/gpt-oss-120b";
}




