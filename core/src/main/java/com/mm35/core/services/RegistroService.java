package com.mm35.core.services;

import java.util.List;
import java.util.Map;

public interface RegistroService {

    /**
     * Crea un usuario en la web y lo añade al grupo 35mm-usuarios
     * Cualquier fallo se devuelve como resultado, sin lanzar excepciones
     */
    ResultadoRegistro registrarUsuario(String usuario, String password);
}

