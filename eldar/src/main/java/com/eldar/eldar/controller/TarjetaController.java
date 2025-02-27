package com.eldar.eldar.controller;

import com.eldar.eldar.model.Tarjeta;
import com.eldar.eldar.service.TarjetaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/tarjetas")
public class TarjetaController {
    @Autowired
    private TarjetaService tarjetaService;

    @PostMapping
    public Tarjeta crear(@RequestBody Tarjeta tarjeta, @RequestParam String email) throws Exception {
        return tarjetaService.registrarTarjeta(tarjeta, email);
    }

    @DeleteMapping("/{id}")
    public void eliminarTarjeta(@PathVariable Long id) {
        tarjetaService.eliminarTarjeta(id);
    }
}
