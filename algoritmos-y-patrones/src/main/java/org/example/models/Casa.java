package org.example.models;

import java.util.ArrayList;
import java.util.List;

public class Casa {
    private String tipoEstructura;
    private int pisos;
    private boolean pileta;
    private boolean garage;
    private boolean jardin;
    private List<String> extras = new ArrayList<>();

    public String getTipoEstructura() {
        return tipoEstructura;
    }

    public void setTipoEstructura(String tipoEstructura) {
        this.tipoEstructura = tipoEstructura;
    }

    public int getPisos() {
        return pisos;
    }

    public void setPisos(int pisos) {
        this.pisos = pisos;
    }

    public boolean isPileta() {
        return pileta;
    }

    public void setPileta(boolean pileta) {
        this.pileta = pileta;
    }

    public boolean isGarage() {
        return garage;
    }

    public void setGarage(boolean garage) {
        this.garage = garage;
    }

    public boolean isJardin() {
        return jardin;
    }

    public void setJardin(boolean jardin) {
        this.jardin = jardin;
    }

    public List<String> getExtras() {
        return extras;
    }

    public void setExtras(List<String> extras) {
        this.extras = extras;
    }

    @Override
    public String toString() {
        return "Casa{" +
                "tipoEstructura='" + tipoEstructura + '\'' +
                ", pisos=" + pisos +
                ", pileta=" + pileta +
                ", garage=" + garage +
                ", jardin=" + jardin +
                ", extras=" + extras +
                '}';
    }
}
