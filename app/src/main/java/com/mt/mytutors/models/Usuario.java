package com.mt.mytutors.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
public class Usuario implements Serializable{
    private String id;
    private String nombre;
    private String correo;
    private String idFacultad;
    private String idCarrera;
    private String tipoUsuario; // alumno, profesor, etc.
    private String rolEnApp;    // tutor, tutorado
    private String rutaFoto;
    private boolean activo;
    private List<String> conversacionesIds;

    // Constructor vacío requerido por Firebase
    public Usuario() {
        this.activo = true;
        this.conversacionesIds = new ArrayList<>();
    }

    // Constructor con parámetros básicos
    public Usuario(String nombre, String correo) {
        this();
        this.nombre = nombre;
        this.correo = correo;
    }

    // Constructor completo
    public Usuario(String id, String nombre, String correo, String idFacultad,
                   String idCarrera, String tipoUsuario, String rolEnApp) {
        this();
        this.id = id;
        this.nombre = nombre;
        this.correo = correo;
        this.idFacultad = idFacultad;
        this.idCarrera = idCarrera;
        this.tipoUsuario = tipoUsuario;
        this.rolEnApp = rolEnApp;
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

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
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

    public String getTipoUsuario() {
        return tipoUsuario;
    }

    public void setTipoUsuario(String tipoUsuario) {
        this.tipoUsuario = tipoUsuario;
    }

    public String getRolEnApp() {
        return rolEnApp;
    }

    public void setRolEnApp(String rolEnApp) {
        this.rolEnApp = rolEnApp;
    }

    public String getRutaFoto() {
        return rutaFoto;
    }

    public void setRutaFoto(String rutaFoto) {
        this.rutaFoto = rutaFoto;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public List<String> getConversacionesIds() {
        return conversacionesIds;
    }

    public void setConversacionesIds(List<String> conversacionesIds) {
        this.conversacionesIds = conversacionesIds;
    }
}
