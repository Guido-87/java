package org.example;

public class Configuracion {
    private static Configuracion instancia;

    private Configuracion () {
        System.out.println("Inicializando configuración en el sistema");
    }

    public static Configuracion getInstancia() {
        if (instancia == null) {
            instancia = new Configuracion();
        }
        return instancia;
    }

    public void mostrarMensaje() {
        System.out.println("Configuración Activa ✅");
    }
}
