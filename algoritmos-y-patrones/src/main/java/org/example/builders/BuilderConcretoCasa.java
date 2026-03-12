package org.example.builders;

import org.example.models.Casa;

public class BuilderConcretoCasa implements BuilderCasa {
    private Casa resultado;

    @Override
    public void reiniciar() {
        this.resultado = new Casa();
    }

    @Override
    public void construirEstructura(String tipoEstructura) {
        resultado.setTipoEstructura(tipoEstructura);
    }

    @Override
    public void construirPisos(int cantidad) {
        resultado.setPisos(cantidad);
    }

    @Override
    public void construirPileta(boolean siONo) {
        resultado.setPileta(siONo);
    }

    @Override
    public void construirGarage(boolean siONo) {
        resultado.setGarage(siONo);
    }

    @Override
    public void construirJardin(boolean siONo) {
        resultado.setJardin(siONo);
    }

    @Override
    public void agregarExtra(String extra) {
        resultado.getExtras().add(extra);
    }

    public Casa obtenerResultado() {
        return resultado;
    }
}
