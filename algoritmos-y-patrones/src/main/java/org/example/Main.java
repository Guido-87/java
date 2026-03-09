package org.example;

import java.util.Arrays;

public class Main {
    public static void main(String[] args) {
        int[][] matriz = new int[6][6];
        System.out.println(matriz.length);
        for (int i=0 ; i<matriz.length ; i++) {
            for (int j=0; j<matriz.length ; j++) {
                if (i == j) {
                    matriz[i][j] = 1;
                }
            }
        }
        System.out.println(Arrays.deepToString(matriz));
        System.out.println("-----");
        System.out.println("Módulo de Autenticación");
        Configuracion config1 = Configuracion.getInstancia();
        config1.mostrarMensaje();
        System.out.println("Módulo de Reportes");
        Configuracion config2 = Configuracion.getInstancia();
        config2.mostrarMensaje();
        System.out.println("Ambas referencias en memoria son iguales?");
        System.out.println(config1 == config2);
        System.out.println(config1);
        System.out.println(config2);
    }
}