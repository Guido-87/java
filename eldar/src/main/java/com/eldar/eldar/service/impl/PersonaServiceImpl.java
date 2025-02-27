package com.eldar.eldar.service.impl;

import com.eldar.eldar.exception.AppException;
import com.eldar.eldar.model.Persona;
import com.eldar.eldar.repository.PersonaRepository;
import com.eldar.eldar.service.PersonaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PersonaServiceImpl implements PersonaService {

    @Autowired
    private PersonaRepository personaRepository;

    @Override
    public Persona registrarPersona(Persona persona) {
        if (personaRepository.findByDni(persona.getDni()) != null) {
            throw new AppException("Ya existe una persona con el DNI: " + persona.getDni());
        }
        return personaRepository.save(persona);
    }

    @Override
    public Persona obtenerPorDni(String dni) {
        Persona persona = personaRepository.findByDni(dni);
        if (persona == null) {
            throw new AppException("No se encontró una persona con el DNI: " + dni);
        }
        return persona;
    }

    @Override
    public Persona obtenerPorEmail(String email) {
        Persona persona = personaRepository.findByEmail(email);
        if (persona == null) {
            throw new AppException("No se encontró una persona con el email: " + email);
        }
        return persona;
    }

    @Override
    public void eliminarPersona(Long id) {
        personaRepository.deleteById(id);
    }
}