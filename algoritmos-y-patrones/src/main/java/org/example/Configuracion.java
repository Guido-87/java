package org.example;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class Configuracion {
    private static Configuracion instancia;
    private Properties properties;

    private Configuracion () throws IOException {
        System.out.println("Inicializando configuración en el sistema");
        properties = new Properties();
        properties.load(getClass().getClassLoader().getResourceAsStream("configuracion.properties"));
    }

    public static Configuracion getInstancia() throws IOException {
        if (instancia == null) {
            instancia = new Configuracion();
        }
        return instancia;
    }

    public void mostrarMensaje() {
        System.out.println("Configuración Activa ✅");
    }

    public String getPropiedad(String nombre) {
        return properties.getProperty(nombre);
    }
}
