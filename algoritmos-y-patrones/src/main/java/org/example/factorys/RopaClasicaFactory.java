package org.example.factorys;

import org.example.models.ropa.Jean;
import org.example.models.ropa.JeanSkinny;
import org.example.models.ropa.Remera;
import org.example.models.ropa.RemeraMangaCorta;

public class RopaClasicaFactory implements RopaFactory {
    @Override
    public Jean crearJean() {
        return new JeanSkinny();
    }

    @Override
    public Remera crearRemera() {
        return new RemeraMangaCorta();
    }
}
