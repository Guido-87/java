package org.example.creators;

import org.example.models.Envio;
import org.example.models.EnvioMoto;

public class EnvioMotoCreator extends EnvioCreator {
    @Override
    protected Envio crearEnvio() {
        return new EnvioMoto();
    }
}
