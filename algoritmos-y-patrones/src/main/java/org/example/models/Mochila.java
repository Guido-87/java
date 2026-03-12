package org.example.models;

import java.util.Iterator;

public class Mochila implements Iterable<String> {
    private String zona1;
    private String zona2;
    private String zona3;
    private String zona4;
    private int posicion = 0;
    private int contador = 0;

    public Mochila() {
    }

    public String getZona1() {
        return zona1;
    }

    public void setZona1(String zona1) {
        this.zona1 = zona1;
        contador++;
    }

    public String getZona2() {
        return zona2;
    }

    public void setZona2(String zona2) {
        this.zona2 = zona2;
        contador++;
    }

    public String getZona3() {
        return zona3;
    }

    public void setZona3(String zona3) {
        this.zona3 = zona3;
        contador++;
    }

    public String getZona4() {
        return zona4;
    }

    public void setZona4(String zona4) {
        this.zona4 = zona4;
        contador++;
    }

    @Override
    public Iterator<String> iterator() {
        return new Iterator<String>() {
            @Override
            public boolean hasNext() {
                if (Mochila.this.posicion >= 4) {
                    posicion = 0;
                    return false;
                } else {
                    return true;
                }
            }

            @Override
            public String next() {
                if (Mochila.this.contador != 0) {
                    if (Mochila.this.posicion == 0 && Mochila.this.zona1 != null) {
                        Mochila.this.posicion++;
                        return zona1;
                    }
                    if (Mochila.this.posicion == 1 && Mochila.this.zona2 != null) {
                        Mochila.this.posicion++;
                        return zona2;
                    }
                    if (Mochila.this.posicion == 2 && Mochila.this.zona3 != null) {
                        Mochila.this.posicion++;
                        return zona3;
                    }
                    if (Mochila.this.posicion == 3 && Mochila.this.zona3 != null) {
                        Mochila.this.posicion = 4;
                        return zona4;
                    }
                }
                throw new IndexOutOfBoundsException();
            }
        };
    }
}
