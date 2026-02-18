package org.funcional.logica;

public class Cliente {
    private Long nroCliente;
    private String nombre;
    private String apellido;
    private String direccion;
    private String telefono;

    public Cliente() {

    }

    public Cliente(Long nroCliente, String nombre, String apellido, String direccion, String telefono) {
        this.nroCliente = nroCliente;
        this.nombre = nombre;
        this.apellido = apellido;
        this.direccion = direccion;
        this.telefono = telefono;
    }

    public Long getNroCliente() {
        return nroCliente;
    }

    public void setNroCliente(Long nroCliente) {
        this.nroCliente = nroCliente;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }
}
