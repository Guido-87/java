package com.eldar.eldar.service;

import com.eldar.eldar.model.Tarjeta;

import java.util.List;

public interface TarjetaService {
    Tarjeta registrarTarjeta(Tarjeta tarjeta, String email) throws Exception;
    List<Tarjeta> obtenerTarjetasPorDni(String dni);
    Tarjeta obtenerTarjetaPorPan(String pan);
    void eliminarTarjeta(Long id);
}
