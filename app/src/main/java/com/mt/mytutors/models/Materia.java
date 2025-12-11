package com.mt.mytutors.models;

import java.io.Serializable;
public class Materia implements Serializable{
    private String id;
    private String nombre;
    private String descripcion;
    private String idFacultad;
    private String idCarrera;

    //constructor vacio
    public Materia() {
    }

    public Materia(String nombre, String descripcion, String idFacultad, String idCarrera) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.idFacultad = idFacultad;
        this.idCarrera = idCarrera;
    }

    public Materia(String nombre, String descripcion){
        this.nombre = nombre;
        this.descripcion = descripcion;
    }

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

    public String getIdFacultad() {
        return idFacultad;
    }

    public void setIdFacultad(String idFacultad) {
        this.idFacultad = idFacultad;
    }

    public String getIdCarrera() {
        return idCarrera;
    }

    public void setIdCarrera(String idCarrera) {
        this.idCarrera = idCarrera;
    }

    @Override
    public String toString() {
        return nombre;
    }
}
