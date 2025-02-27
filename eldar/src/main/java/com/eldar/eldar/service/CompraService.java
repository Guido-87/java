package com.eldar.eldar.service;

import com.eldar.eldar.model.Compra;

public interface CompraService {
    void realizarCompra(Double monto, String detalle, String pan, String cvv) throws Exception;
    Compra obtenerDetalle(Long id);
}
