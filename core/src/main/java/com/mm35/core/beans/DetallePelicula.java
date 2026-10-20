package com.mm35.core.beans;

import java.util.ArrayList;
import java.util.List;

public class DetallePelicula {
    private String titulo;
    private String anio;
    private String sinopsis;
    private String lema;
    private String duracion;
    private String puntuacion;
    private Integer votos;
    private String poster;
    private String fondo;
    private List<String> generos = new ArrayList<>();
    private Persona director;
    private List<Persona> reparto = new ArrayList<>();

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String title) {
        this.titulo = title;
    }

    public List<Persona> getReparto() {
        return reparto;
    }

    public void setReparto(List<Persona> reparto) {
        this.reparto = reparto;
    }

    public Persona getDirector() {
        return director;
    }

    public void setDirector(Persona director) {
        this.director = director;
    }

    public List<String> getGeneros() {
        return generos;
    }

    public void setGeneros(List<String> generos) {
        this.generos = generos;
    }

    public String getFondo() {
        return fondo;
    }

    public void setFondo(String fondo) {
        this.fondo = fondo;
    }

    public String getPoster() {
        return poster;
    }

    public void setPoster(String poster) {
        this.poster = poster;
    }

    public Integer getVotos() {
        return votos;
    }

    public void setVotos(Integer votos) {
        this.votos = votos;
    }

    public String getPuntuacion() {
        return puntuacion;
    }

    public void setPuntuacion(String puntuacion) {
        this.puntuacion = puntuacion;
    }

    public String getDuracion() {
        return duracion;
    }

    public void setDuracion(String duracion) {
        this.duracion = duracion;
    }

    public String getLema() {
        return lema;
    }

    public void setLema(String lema) {
        this.lema = lema;
    }

    public String getSinopsis() {
        return sinopsis;
    }

    public void setSinopsis(String sinopsis) {
        this.sinopsis = sinopsis;
    }

    public String getAnio() {
        return anio;
    }

    public void setAnio(String anio) {
        this.anio = anio;
    }

    public static class Persona {
        private final String nombre;
        private final String papel;
        private final String foto;

        public Persona(String nombre, String papel, String foto){
            this.nombre = nombre;
            this.papel = papel;
            this.foto = foto;
        }
        public String getNombre(){return nombre;}
        public String getPapel(){return papel;}
        public String getFoto(){return foto;}
    }
}




