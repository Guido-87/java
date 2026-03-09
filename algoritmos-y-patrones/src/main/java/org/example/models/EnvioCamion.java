package org.example.models;

public class EnvioCamion implements Envio {
    @Override
    public void enviarPaquete() {
        System.out.println("Enviando paquete mediante Camión");
    }
}
