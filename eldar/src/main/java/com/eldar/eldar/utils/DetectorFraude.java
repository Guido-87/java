package com.eldar.eldar.utils;

import java.util.Arrays;

public class DetectorFraude {
    public static double calcularMediana(int[] gastos) {
        Arrays.sort(gastos);
        int n = gastos.length;
        if (n % 2 == 1) {
            return gastos[n / 2];
        } else {
            return (gastos[n / 2 - 1] + gastos[n / 2]) / 2.0;
        }
    }

    public static int contarNotificacionesDeFraude(int[] gastos, int dias) {
        int notificaciones = 0;
        if (gastos.length < dias) {
            return 0;
        }
        for (int i = dias; i < gastos.length; i++) {
            int[] gastosPrevios = new int[dias];
            for (int j = 0; j < dias; j++) {
                gastosPrevios[j] = gastos[i - dias + j];
            }
            double mediana = calcularMediana(gastosPrevios);
            if (gastos[i] >= 2 * mediana) {
                notificaciones++;
            }
        }
        return notificaciones;
    }

    public static void main(String[] args) {
        int[] gastos = {10, 20, 30, 40, 50};
        int dias = 3;
        int notificaciones = contarNotificacionesDeFraude(gastos, dias);
        System.out.println("Cantidad de notificaciones de fraude: " + notificaciones);
    }
}