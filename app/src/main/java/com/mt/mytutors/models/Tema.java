package com.mt.mytutors.models;

import java.io.Serializable;
public class Tema implements Serializable{
    private String id;
    private String nombre;
    private String descripcion;
    private String rol;          // "tutor" o "tutorado"
    private String idMateria;
    private String idCreador;
    private String idTutor;

    // Campos adicionales para mostrar en la UI (desnormalizados)
    private String nombreCreador;
    private String nombreMateria;

    // Constructor vacío requerido por Firebase
    public Tema() {
    }

    public Tema(String nombre, String descripcion, String rol) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.rol = rol;
    }

    public Tema(String id, String nombre, String descripcion, String rol,
                String idMateria, String idCreador, String idTutor) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.rol = rol;
        this.idMateria = idMateria;
        this.idCreador = idCreador;
        this.idTutor = idTutor;
    }

    // Getters y Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }

    public String getIdMateria() {
        return idMateria;
    }

    public void setIdMateria(String idMateria) {
        this.idMateria = idMateria;
    }

    public String getIdCreador() {
        return idCreador;
    }

    public void setIdCreador(String idCreador) {
        this.idCreador = idCreador;
    }

    public String getIdTutor() {
        return idTutor;
    }

    public void setIdTutor(String idTutor) {
        this.idTutor = idTutor;
    }

    public String getNombreCreador() {
        return nombreCreador;
    }

    public void setNombreCreador(String nombreCreador) {
        this.nombreCreador = nombreCreador;
    }

    public String getNombreMateria() {
        return nombreMateria;
    }

    public void setNombreMateria(String nombreMateria) {
        this.nombreMateria = nombreMateria;
    }

    /**
     * Indica si el tema es una oferta de tutoría (rol = tutor)
     */
    public boolean esOferta() {
        return "tutor".equalsIgnoreCase(rol);
    }

    /**
     * Indica si el tema es una demanda de tutoría (rol = tutorado)
     */
    public boolean esDemanda() {
        return "tutorado".equalsIgnoreCase(rol);
    }
}
