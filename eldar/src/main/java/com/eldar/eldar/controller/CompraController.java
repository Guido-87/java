package com.eldar.eldar.controller;

import com.eldar.eldar.model.Compra;
import com.eldar.eldar.service.CompraService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/compras")
public class CompraController {
    @Autowired
    private CompraService compraService;

    @PostMapping
    public void realizarCompra(@RequestParam Double monto, @RequestParam String detalle, @RequestParam String pan, @RequestParam String cvv) throws Exception {
        compraService.realizarCompra(monto, detalle, pan, cvv);
    }

    @GetMapping("/{id}")
    public Compra obtenerDetalle(@PathVariable Long id) {
        return compraService.obtenerDetalle(id);
    }
}
