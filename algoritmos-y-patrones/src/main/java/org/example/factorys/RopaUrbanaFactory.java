package org.example.factorys;

import org.example.models.ropa.Jean;
import org.example.models.ropa.JeanCargo;
import org.example.models.ropa.Remera;
import org.example.models.ropa.RemeraOversize;

public class RopaUrbanaFactory implements RopaFactory {
    @Override
    public Jean crearJean() {
        return new JeanCargo();
    }

    @Override
    public Remera crearRemera() {
        return new RemeraOversize();
    }
}
