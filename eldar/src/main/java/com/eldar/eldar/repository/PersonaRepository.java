package com.eldar.eldar.repository;

import com.eldar.eldar.model.Persona;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PersonaRepository extends JpaRepository<Persona, Long> {
    Persona findByDni(String dni);
    Persona findByEmail(String email);
}
