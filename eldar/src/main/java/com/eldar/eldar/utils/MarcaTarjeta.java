package com.eldar.eldar.utils;

import java.time.LocalDate;

public enum MarcaTarjeta {
    VISA {
        @Override
        public double calcularTasa(LocalDate fecha) {
            double tasa = Utils.formatearAnio(fecha) / fecha.getMonthValue();
            return Utils.maximaTasa(tasa);
        }
    },
    NARA {
        @Override
        public double calcularTasa(LocalDate fecha) {
            double tasa = fecha.getDayOfMonth() * 0.5;
            return Utils.maximaTasa(tasa);
        }
    },
    AMEX {
        @Override
        public double calcularTasa(LocalDate fecha) {
            return fecha.getMonthValue() * 0.1;
        }
    };

    public abstract double calcularTasa(LocalDate fecha);
}
