package org.example.models;

public class EnvioCorreo implements Envio {
    @Override
    public void enviarPaquete() {
        System.out.println("Enviando paquete mediante Correo Postal");
    }
}
