package com.mt.mytutors.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Conversacion implements Serializable{
    private String id;
    private String nombre;
    private String tipo; // "individual" o "grupo"
    private String idTema;
    private List<String> participantes;
    private String ultimoMensaje;
    private String fechaUltimoMensaje;

    public Conversacion() {
    }

    public Conversacion(String nombre, String tipo){
        this.nombre = nombre;
        this.tipo = tipo;
    }
    public Conversacion(String id, String nombre, String tipo, String idTema, List<String> participantes) {
        this.id = id;
        this.nombre = nombre;
        this.tipo = tipo;
        this.idTema = idTema;
        this.participantes = participantes != null ? participantes : new ArrayList<>();;
    }

    // Getters and Setters

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

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getIdTema() {
        return idTema;
    }

    public void setIdTema(String idTema) {
        this.idTema = idTema;
    }

    public String getUltimoMensaje() {
        return ultimoMensaje;
    }

    public void setUltimoMensaje(String ultimoMensaje) {
        this.ultimoMensaje = ultimoMensaje;
    }

    public List<String> getParticipantes() {
        return participantes;
    }

    public void setParticipantes(List<String> participantes) {
        this.participantes = participantes;
    }

    public String getFechaUltimoMensaje() {
        return fechaUltimoMensaje;
    }

    public void setFechaUltimoMensaje(String fechaUltimoMensaje) {
        this.fechaUltimoMensaje = fechaUltimoMensaje;
    }

    public void agregarParticipante(String idUsuario){
        if(!participantes.contains(idUsuario)) {
            participantes.add(idUsuario);
        }
    }

    //verifica si un usuario es participante de la conversacion
    public boolean esParticipante(String idUsuario) {
        return participantes.contains(idUsuario);
    }

    //obtiene el id del otro participante en una conversacion individual
    public String obtenerOtroParticipante(String idUsuario) {
        for (String participante : participantes) {
            if (!participante.equals(idUsuario)) {
                return participante;
            }
        }
        return null; // Si no se encuentra otro participante
    }
}
