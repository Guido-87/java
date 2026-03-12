package org.example.prototypes;

import java.util.ArrayList;
import java.util.List;

public class PedidoPizza implements PrototipoPizza<PedidoPizza> {
    private String tamanio;
    private String tipoMasa;
    private List<String> ingredientes;

    public PedidoPizza(String tamanio, String tipoMasa, List<String> ingredientes) {
        this.tamanio = tamanio;
        this.tipoMasa = tipoMasa;
        this.ingredientes = new ArrayList<>(ingredientes);
    }

    public PedidoPizza(PedidoPizza prototipo) {
        this.tamanio = prototipo.tamanio;
        this.tipoMasa = prototipo.tipoMasa;
        this.ingredientes = new ArrayList<>(prototipo.ingredientes);
    }

    @Override
    public PedidoPizza clonar() {
        return new PedidoPizza(this);
    }

    public void agregarIngrediente(String ingrediente) {
        this.ingredientes.add(ingrediente);
    }

    @Override
    public String toString() {
        return "PedidoPizza{" +
                "tamanio='" + tamanio + '\'' +
                ", tipoMasa='" + tipoMasa + '\'' +
                ", ingredientes=" + ingredientes +
                '}';
    }
}
