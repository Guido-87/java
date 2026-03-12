package org.example.builders;

public interface BuilderCasa {
    void reiniciar();
    void construirEstructura(String tipoEstructura);
    void construirPisos(int cantidad);
    void construirPileta(boolean siONo);
    void construirGarage(boolean siONo);
    void construirJardin(boolean siONo);
    void agregarExtra(String extra);
}
