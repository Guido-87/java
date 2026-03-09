package org.example.creators;

import org.example.models.Envio;
import org.example.models.EnvioCorreo;

public class EnvioCorreoCreator extends EnvioCreator {
    @Override
    protected Envio crearEnvio() {
        return new EnvioCorreo();
    }
}
