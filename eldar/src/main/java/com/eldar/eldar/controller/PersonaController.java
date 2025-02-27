package com.eldar.eldar.controller;

import com.eldar.eldar.model.Persona;
import com.eldar.eldar.service.PersonaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/personas")
public class PersonaController {
    @Autowired
    private PersonaService personaService;

    @PostMapping
    public Persona crear(@RequestBody Persona persona) {
        return personaService.registrarPersona(persona);
    }

    @DeleteMapping("/{id}")
    public void eliminarPersona(@PathVariable Long id) {
        personaService.eliminarPersona(id);
    }
}
