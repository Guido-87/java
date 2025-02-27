package com.eldar.eldar.utils;

import java.time.LocalDate;

public class Utils {
    public static String FORMATO_FECHA = "dd-MM-yyyy";

    public static LocalDate formatearFecha(String fecha) {
        return LocalDate.parse(fecha, java.time.format.DateTimeFormatter.ofPattern(FORMATO_FECHA));
    }

    public static double formatearAnio(LocalDate fecha) {
        return fecha.getYear() % 100;
    }

    public static double maximaTasa(double tasa) {
        return Math.min(tasa, 5);
    }
}
