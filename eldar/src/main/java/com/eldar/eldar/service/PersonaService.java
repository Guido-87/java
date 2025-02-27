package com.eldar.eldar.service;

import com.eldar.eldar.model.Persona;

public interface PersonaService {
    Persona registrarPersona(Persona persona);
    Persona obtenerPorDni(String dni);
    Persona obtenerPorEmail(String email);
    void eliminarPersona(Long id);
}
