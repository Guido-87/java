package org.example.builders;

public class BuilderDirector {
    private BuilderCasa builderCasa;

    public BuilderDirector(BuilderCasa builderCasa) {
        this.builderCasa = builderCasa;
    }

    public void cambiarBuilder(BuilderCasa builderCasa) {
        this.builderCasa = builderCasa;
    }

    public void construirCasaDosPisosMaterial() {
        builderCasa.reiniciar();
        builderCasa.construirEstructura("Material");
        builderCasa.construirPisos(2);
        builderCasa.construirPileta(false);
        builderCasa.construirGarage(true);
        builderCasa.construirJardin(true);
        builderCasa.agregarExtra("Balcón");
    }

    public void construirCasaConPileta() {
        builderCasa.reiniciar();
        builderCasa.construirEstructura("Material");
        builderCasa.construirPisos(1);
        builderCasa.construirPileta(true);
        builderCasa.construirGarage(true);
        builderCasa.construirJardin(true);
        builderCasa.agregarExtra("Deck alrededor de Pileta");
    }

    public void construirCasaMaderaSimple() {
        builderCasa.reiniciar();
        builderCasa.construirEstructura("Madera");
        builderCasa.construirPisos(1);
        builderCasa.construirPileta(false);
        builderCasa.construirGarage(false);
        builderCasa.construirJardin(true);
    }
}
