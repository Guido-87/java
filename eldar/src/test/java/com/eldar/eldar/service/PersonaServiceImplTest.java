package com.eldar.eldar.service;

import static org.mockito.Mockito.*;

import com.eldar.eldar.model.Persona;
import com.eldar.eldar.repository.PersonaRepository;
import com.eldar.eldar.service.impl.PersonaServiceImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class PersonaServiceImplTest {

    @Mock
    private PersonaRepository personaRepository;

    @InjectMocks
    private PersonaServiceImpl personaService;

    @Test
    public void testCrearPersona() {
        Persona persona = new Persona();
        persona.setNombre("Guido");
        persona.setEmail("alvarezguido87@gmail.com");
        when(personaRepository.save(persona)).thenReturn(persona);
        Persona result = personaService.registrarPersona(persona);
        Assertions.assertEquals("Guido", result.getNombre());
    }
}
