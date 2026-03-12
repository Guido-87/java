package org.example;

import org.example.factorys.RopaFactory;
import org.example.models.ropa.Jean;
import org.example.models.ropa.Remera;

public class TiendaRopa {
    private final RopaFactory factory;

    public TiendaRopa(RopaFactory factory) {
        this.factory = factory;
    }

    public void mostrarRopa() {
        Jean jean = factory.crearJean();
        Remera remera = factory.crearRemera();
        jean.descripcion();
        remera.descripcion();
    }
}
