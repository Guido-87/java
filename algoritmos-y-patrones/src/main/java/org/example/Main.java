package org.example;

import org.example.creators.EnvioCamionCreator;
import org.example.creators.EnvioCorreoCreator;
import org.example.creators.EnvioCreator;
import org.example.creators.EnvioMotoCreator;

import java.io.IOException;
import java.util.Arrays;

public class Main {
    public static void main(String[] args) throws IOException {
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
        System.out.println(config1.getPropiedad("carpeta"));
        System.out.println(config1.getPropiedad("url"));
        System.out.println("-----");

        EnvioCreator envioCorreo = new EnvioCorreoCreator();
        envioCorreo.procesarEnvio();
        EnvioCreator envioMoto = new EnvioMotoCreator();
        envioMoto.procesarEnvio();
        EnvioCreator envioCamion = new EnvioCamionCreator();
        envioCamion.procesarEnvio();
    }
}