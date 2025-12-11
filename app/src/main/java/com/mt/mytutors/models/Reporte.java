package com.mt.mytutors.models;

import java.io.Serializable;
public class Reporte implements Serializable{
    public static final String ESTADO_PENDIENTE = "PENDIENTE";
    public static final String ESTADO_REVISADO = "REVISADO";
    public static final String ESTADO_DESCARTADO = "DESCARTADO";

    private String id;
    private String motivo;
    private String descripcion;
    private String fechaCreado;
    private String estado;
    private String idEmisor;
    private String idTemaReportado;

    // Campos desnormalizados para UI
    private String nombreEmisor;
    private String nombreTema;

    // Constructor vacío requerido por Firebase
    public Reporte() {
        this.estado = ESTADO_PENDIENTE;
    }

    public Reporte(String motivo, String descripcion, String idEmisor, String idTemaReportado) {
        this();
        this.motivo = motivo;
        this.descripcion = descripcion;
        this.idEmisor = idEmisor;
        this.idTemaReportado = idTemaReportado;
    }

    public Reporte(String id, String motivo, String descripcion, String fechaCreado,
                   String estado, String idEmisor, String idTemaReportado) {
        this.id = id;
        this.motivo = motivo;
        this.descripcion = descripcion;
        this.fechaCreado = fechaCreado;
        this.estado = estado;
        this.idEmisor = idEmisor;
        this.idTemaReportado = idTemaReportado;
    }

    // Getters y Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getFechaCreado() {
        return fechaCreado;
    }

    public void setFechaCreado(String fechaCreado) {
        this.fechaCreado = fechaCreado;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getIdEmisor() {
        return idEmisor;
    }

    public void setIdEmisor(String idEmisor) {
        this.idEmisor = idEmisor;
    }

    public String getIdTemaReportado() {
        return idTemaReportado;
    }

    public void setIdTemaReportado(String idTemaReportado) {
        this.idTemaReportado = idTemaReportado;
    }

    public String getNombreEmisor() {
        return nombreEmisor;
    }

    public void setNombreEmisor(String nombreEmisor) {
        this.nombreEmisor = nombreEmisor;
    }

    public String getNombreTema() {
        return nombreTema;
    }

    public void setNombreTema(String nombreTema) {
        this.nombreTema = nombreTema;
    }

    /**
     * Verifica si el reporte está pendiente
     */
    public boolean isPendiente() {
        return ESTADO_PENDIENTE.equals(estado);
    }

    /**
     * Verifica si el reporte fue revisado
     */
    public boolean isRevisado() {
        return ESTADO_REVISADO.equals(estado);
    }

    /**
     * Verifica si el reporte fue descartado
     */
    public boolean isDescartado() {
        return ESTADO_DESCARTADO.equals(estado);
    }


}
