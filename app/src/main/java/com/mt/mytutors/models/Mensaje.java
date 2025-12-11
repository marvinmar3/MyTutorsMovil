package com.mt.mytutors.models;

import java.io.Serializable;
public class Mensaje implements Serializable{
    private String id;
    private String idConversacion;
    private String idEmisor;
    private String contenido;
    private boolean leido;
    private String fechaEnvio;

    //campo desnormalizado para UI
    private String nombreEmisor;

    public Mensaje () {
    }

    public Mensaje(String idConversacion, String idEmisor, String contenido) {
        this(); // Llama al constructor vacío
        this.idConversacion = idConversacion;
        this.idEmisor = idEmisor;
        this.contenido = contenido;
    }

    public Mensaje(String id, String idConversacion, String idEmisor,
                   String contenido, boolean leido, String fechaEnvio) {
        this.id = id;
        this.idConversacion = idConversacion;
        this.idEmisor = idEmisor;
        this.contenido = contenido;
        this.leido = leido;
        this.fechaEnvio = fechaEnvio;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getIdConversacion() {
        return idConversacion;
    }

    public void setIdConversacion(String idConversacion) {
        this.idConversacion = idConversacion;
    }

    public String getIdEmisor() {
        return idEmisor;
    }

    public void setIdEmisor(String idEmisor) {
        this.idEmisor = idEmisor;
    }

    public String getContenido() {
        return contenido;
    }

    public void setContenido(String contenido) {
        this.contenido = contenido;
    }

    public boolean isLeido() {
        return leido;
    }

    public void setLeido(boolean leido) {
        this.leido = leido;
    }

    public String getFechaEnvio() {
        return fechaEnvio;
    }

    public void setFechaEnvio(String fechaEnvio) {
        this.fechaEnvio = fechaEnvio;
    }

    public String getNombreEmisor() {
        return nombreEmisor;
    }

    public void setNombreEmisor(String nombreEmisor) {
        this.nombreEmisor = nombreEmisor;
    }

    //verifica sue el mensaje fue enviado por el usuario actual
    public boolean esMio(String miId) {
        return miId != null && miId.equals(this.idEmisor);
    }
}

