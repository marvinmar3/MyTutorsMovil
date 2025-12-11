package com.mt.mytutors.models;

import java.io.Serializable;
public class SolicitudTutoria implements Serializable{
    private String id;
    private String idSolicitante;
    private String idTema;
    private boolean aceptada;
    private boolean respondida;
    private String fechaSolicitud;

    // Campos desnormalizados para UI
    private String nombreSolicitante;
    private String nombreTema;
    private String correoSolicitante;

    // Constructor vacío requerido por Firebase
    public SolicitudTutoria() {
        this.aceptada = false;
        this.respondida = false;
    }

    public SolicitudTutoria(String idSolicitante, String idTema) {
        this();
        this.idSolicitante = idSolicitante;
        this.idTema = idTema;
    }

    public SolicitudTutoria(String id, String idSolicitante, String idTema,
                            boolean aceptada, boolean respondida, String fechaSolicitud) {
        this.id = id;
        this.idSolicitante = idSolicitante;
        this.idTema = idTema;
        this.aceptada = aceptada;
        this.respondida = respondida;
        this.fechaSolicitud = fechaSolicitud;
    }

    // Getters y Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getIdSolicitante() {
        return idSolicitante;
    }

    public void setIdSolicitante(String idSolicitante) {
        this.idSolicitante = idSolicitante;
    }

    public String getIdTema() {
        return idTema;
    }

    public void setIdTema(String idTema) {
        this.idTema = idTema;
    }

    public boolean isAceptada() {
        return aceptada;
    }

    public void setAceptada(boolean aceptada) {
        this.aceptada = aceptada;
    }

    public boolean isRespondida() {
        return respondida;
    }

    public void setRespondida(boolean respondida) {
        this.respondida = respondida;
    }

    public String getFechaSolicitud() {
        return fechaSolicitud;
    }

    public void setFechaSolicitud(String fechaSolicitud) {
        this.fechaSolicitud = fechaSolicitud;
    }

    public String getNombreSolicitante() {
        return nombreSolicitante;
    }

    public void setNombreSolicitante(String nombreSolicitante) {
        this.nombreSolicitante = nombreSolicitante;
    }

    public String getNombreTema() {
        return nombreTema;
    }

    public void setNombreTema(String nombreTema) {
        this.nombreTema = nombreTema;
    }

    public String getCorreoSolicitante() {
        return correoSolicitante;
    }

    public void setCorreoSolicitante(String correoSolicitante) {
        this.correoSolicitante = correoSolicitante;
    }

    /**
     * Indica si la solicitud está pendiente de respuesta
     */
    public boolean isPendiente() {
        return !respondida;
    }

    /**
     * Obtiene el estado de la solicitud como texto
     */
    public String getEstadoTexto() {
        if (!respondida) {
            return "Pendiente";
        } else if (aceptada) {
            return "Aceptada";
        } else {
            return "Rechazada";
        }
    }
}
