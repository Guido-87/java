package com.eldar.eldar.service.impl;

import com.eldar.eldar.exception.AppException;
import com.eldar.eldar.model.Tarjeta;
import com.eldar.eldar.repository.TarjetaRepository;
import com.eldar.eldar.service.TarjetaService;
import com.eldar.eldar.utils.EncryptUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Random;

@Service
public class TarjetaServiceImpl implements TarjetaService {
    @Autowired
    private TarjetaRepository tarjetaRepository;

    @Autowired
    private JavaMailSender emailSender;

    @Override
    public Tarjeta registrarTarjeta(Tarjeta tarjeta, String email) throws Exception {
        EncryptUtil.generarClaveAES();
        tarjeta.setCodigoSeguridad(EncryptUtil.encriptar(new Random().toString()));
        tarjeta.setPan(EncryptUtil.encriptar(new Random().toString()));
        tarjetaRepository.save(tarjeta);
        /* TODO: Revisar
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Detalles de tu tarjeta");
        message.setText("CVV: " + tarjeta.getCodigoSeguridad() + "\nPAN: " + tarjeta.getPan());
        emailSender.send(message);
         */
        return tarjeta;
    }

    @Override
    public List<Tarjeta> obtenerTarjetasPorDni(String dni) {
        List<Tarjeta> tarjetas = tarjetaRepository.findByPersonaDni(dni);
        if (tarjetas.isEmpty()) {
            throw new AppException("No se encontraron tarjetas asociadas a este DNI.");
        }
        return tarjetas;
    }

    @Override
    public Tarjeta obtenerTarjetaPorPan(String pan) {
        Tarjeta tarjeta = tarjetaRepository.findByPan(pan);
        if (tarjeta == null) {
            throw new AppException("No se encontró una tarjeta con el PAN: " + pan);
        }
        return tarjeta;
    }

    @Override
    public void eliminarTarjeta(Long id) {
        tarjetaRepository.deleteById(id);
    }
}
