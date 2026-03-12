package org.example.factorys;

import org.example.models.ropa.Jean;
import org.example.models.ropa.JeanRecto;
import org.example.models.ropa.Remera;
import org.example.models.ropa.RemeraAlCuerpo;

public class RopaEleganteFactory implements RopaFactory {
    @Override
    public Jean crearJean() {
        return new JeanRecto();
    }

    @Override
    public Remera crearRemera() {
        return new RemeraAlCuerpo();
    }
}
