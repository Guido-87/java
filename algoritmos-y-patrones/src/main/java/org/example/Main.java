package org.example;

import org.example.builders.BuilderConcretoCasa;
import org.example.builders.BuilderDirector;
import org.example.creators.EnvioCamionCreator;
import org.example.creators.EnvioCorreoCreator;
import org.example.creators.EnvioCreator;
import org.example.creators.EnvioMotoCreator;
import org.example.factorys.RopaClasicaFactory;
import org.example.factorys.RopaEleganteFactory;
import org.example.factorys.RopaFactory;
import org.example.factorys.RopaUrbanaFactory;
import org.example.models.Casa;
import org.example.models.Mochila;
import org.example.models.Persona;
import org.example.prototypes.PedidoPizza;
import org.example.prototypes.PedidoPizzaEspecial;
import org.example.services.ServicioBD;
import org.example.services.ServicioCorreo;
import org.example.services.ServicioFacade;
import org.example.services.ServicioValidacion;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

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
        System.out.println("-----");

        Persona persona = new Persona("37350702", "Guido", "alvarezguido87@gmail.com");
        ServicioFacade sf = new ServicioFacade();
        sf.procesarPersona(persona);
        System.out.println("-----");

        ArrayList<String> lista = new ArrayList<String>();
        lista.add("Hola");
        lista.add("que");
        lista.add("tal");
        lista.add("estás?");
        Iterator<String> it = lista.iterator();
        while (it.hasNext()) {
            System.out.println(it.next());
        }
        System.out.println("-----");

        Mochila m = new Mochila();
        m.setZona1("Cuchara");
        m.setZona2("Bocadillo");
        m.setZona3("Agua");
        m.setZona4("Teléfono");
        Iterator<String> itm = m.iterator();
        while (itm.hasNext()) {
            System.out.println(itm.next());
        }
        for (String zona : m) {
            System.out.println(zona);
        }
        System.out.println("-----");

        RopaFactory factory = new RopaUrbanaFactory();
        TiendaRopa tienda = new TiendaRopa(factory);
        tienda.mostrarRopa();
        RopaFactory factory2 = new RopaClasicaFactory();
        tienda = new TiendaRopa(factory2);
        tienda.mostrarRopa();
        System.out.println("-----");

        BuilderConcretoCasa builder = new BuilderConcretoCasa();
        BuilderDirector director = new BuilderDirector(builder);
        director.construirCasaDosPisosMaterial();
        Casa casa1 = builder.obtenerResultado();
        System.out.println("Casa 1: " + casa1.toString());
        director.construirCasaConPileta();
        Casa casa2 = builder.obtenerResultado();
        System.out.println("Casa 2: " + casa2.toString());
        director.construirCasaMaderaSimple();
        Casa casa3 = builder.obtenerResultado();
        System.out.println("Casa 3: " + casa3.toString());

        // Sin director
        builder.reiniciar();
        builder.construirEstructura("Material");
        builder.construirPisos(5);
        builder.agregarExtra("Parrilla Grande");
        System.out.println("-----");

        PedidoPizza prototipoMuzza = new PedidoPizza("Grande", "Normal", List.of("Muzzarella", "Salsa de Tomate"));
        PedidoPizza muzzaAceitunas = prototipoMuzza.clonar();
        muzzaAceitunas.agregarIngrediente("Aceitunas");
        PedidoPizza muzzaJamon = prototipoMuzza.clonar();
        muzzaJamon.agregarIngrediente("Jamón");

        PedidoPizzaEspecial prototipoEspecial = new PedidoPizzaEspecial("Mediana", "Fina", List.of("Muzzarella", "Salsa"), true);
        PedidoPizzaEspecial especialRucula = prototipoEspecial.clonar();
        especialRucula.agregarIngrediente("Rúcula");
        System.out.println("Prototipo Base: " + prototipoMuzza);
        System.out.println("Variante con Aceitunas: " + muzzaAceitunas);
        System.out.println("Variante con Jamón: " + muzzaJamon);
        System.out.println("Prototipo Especial: " + prototipoEspecial);
        System.out.println("Especial Rúcula: " + especialRucula);
    }
}