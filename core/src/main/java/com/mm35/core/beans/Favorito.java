package com.mm35.core.beans;

/**
* Bean de favorito, con id titulo y poster
*/

public class Favorito {
    private final String id;
    private final String titulo;
    private final String poster;


    public Favorito(String id, String titulo, String poster){
        this.id = id;
        this.titulo = titulo;
        this.poster = poster;
    }

    public String getId() {
        return id;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getPoster() {
        return poster;
    }
}




