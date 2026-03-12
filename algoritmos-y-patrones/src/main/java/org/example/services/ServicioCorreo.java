package org.example.services;

public class ServicioCorreo {
    public void enviar(String correo, String mensaje) {
        System.out.println("Enviando a " + correo);
        System.out.println("El mensaje es: " + mensaje);
    }
}
