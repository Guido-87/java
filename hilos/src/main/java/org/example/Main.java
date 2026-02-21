package org.example;

import org.example.logica.Hilo;
import org.example.logica.HiloRunnable;

public class Main {
    public static void main(String[] args) {
        Hilo proceso = new Hilo();
        Thread proceso2 = new Thread(new HiloRunnable());
        proceso.start();
        proceso2.start();
    }
}