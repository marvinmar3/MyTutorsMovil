package com.mt.mytutors.models;

import java.io.Serializable;

public class Carrera implements Serializable{
    private String id;
    private String nombre;
    private String idFacultad;

    //constructor vacío
    public Carrera() {
    }

    public Carrera(String nombre){
        this.nombre = nombre;
    }

    public Carrera (String id, String nombre, String idFacultad) {
        this.id = id;
        this.nombre = nombre;
        this.idFacultad = idFacultad;
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

    public String getIdFacultad() {
        return idFacultad;
    }

    public void setIdFacultad(String idFacultad) {
        this.idFacultad = idFacultad;
    }

    @Override
    public String toString() {
        return nombre != null ? nombre: "Sin nombre";
    }
}
