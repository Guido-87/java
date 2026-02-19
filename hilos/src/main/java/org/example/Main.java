package org.example;

import org.example.logica.Hilo;

public class Main {
    public static void main(String[] args) {
        Hilo proceso = new Hilo();
        proceso.start();
    }
}