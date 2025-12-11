package com.mt.mytutors.models;

import java.io.Serializable;
public class Facultad implements Serializable {
    private String id;
    private String nombre;

    public Facultad() {
    }

    public Facultad(String nombre) {
        this.nombre = nombre;
    }

    public Facultad(String id, String nombre) {
        this.id = id;
        this.nombre = nombre;
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

    @Override
    public String toString() {
        return nombre;
    }
}
