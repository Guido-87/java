package com.eldar.eldar.repository;

import com.eldar.eldar.model.Tarjeta;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TarjetaRepository extends JpaRepository<Tarjeta, Long> {
    List<Tarjeta> findByPersonaDni(String dni);
    Tarjeta findByPan(String pan);
}
