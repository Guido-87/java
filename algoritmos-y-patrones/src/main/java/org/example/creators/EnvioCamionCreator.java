package org.example.creators;

import org.example.models.Envio;
import org.example.models.EnvioCamion;

public class EnvioCamionCreator extends EnvioCreator {
    @Override
    protected Envio crearEnvio() {
        return new EnvioCamion();
    }
}
