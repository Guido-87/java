package org.example.factorys;

import org.example.models.ropa.Jean;
import org.example.models.ropa.Remera;

public interface RopaFactory {
    Jean crearJean();
    Remera crearRemera();
}
