package org.example.services;

import org.example.models.Persona;

public class ServicioFacade {
    private final ServicioCorreo sc = new ServicioCorreo();
    private final ServicioBD sbd = new ServicioBD();
    private final ServicioValidacion sv = new ServicioValidacion();

    public void procesarPersona(Persona persona) {
        if (sv.validar(persona.getDni())) {
            sbd.insertar(persona);
            sc.enviar(persona.getCorreo(), "Hola bienvenido a nuestra App");
        }
    }
}
