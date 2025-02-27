package com.eldar.eldar.service.impl;

import com.eldar.eldar.exception.AppException;
import com.eldar.eldar.model.Compra;
import com.eldar.eldar.model.Persona;
import com.eldar.eldar.model.Tarjeta;
import com.eldar.eldar.repository.CompraRepository;
import com.eldar.eldar.service.CompraService;
import com.eldar.eldar.service.TarjetaService;
import com.eldar.eldar.utils.EncryptUtil;
import com.eldar.eldar.utils.MarcaTarjeta;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class CompraServiceImpl implements CompraService {
    @Autowired
    private TarjetaService tarjetaService;

    @Autowired
    private JavaMailSender emailSender;

    @Autowired
    private CompraRepository compraRepository;

    @Override
    public void realizarCompra(Double monto, String detalle, String pan, String cvv) throws Exception {
        Tarjeta tarjeta = tarjetaService.obtenerTarjetaPorPan(pan);
        if (tarjeta != null && EncryptUtil.desencriptar(tarjeta.getCodigoSeguridad()).equals(cvv) && monto < 10000) {
            System.out.println("Compra realizada por " + monto + " con tarjeta " + pan);
            /* TODO: Revisar
            Persona persona = new Persona();
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(persona.getEmail());
            message.setSubject("Compra realizada");
            message.setText("Se ha realizado una compra de " + monto + " en detalle: " + detalle);
            emailSender.send(message);
             */
            Compra compra = new Compra();
            for (MarcaTarjeta c : MarcaTarjeta. values()) {
                if (c.name().equals(tarjeta.getMarca())) {
                    compra.setTasa(c.calcularTasa(LocalDate.now()));
                }
            }
            compra.setMarcaTarjeta(tarjeta.getMarca());
            compra.setMonto(monto);
            compraRepository.save(compra);
        }
    }

    @Override
    public Compra obtenerDetalle(Long id) {
        return compraRepository.findById(id).orElseThrow(() -> new AppException("Compra no encontrada"));
    }
}
