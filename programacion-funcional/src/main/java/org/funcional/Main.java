package org.funcional;

import org.funcional.logica.Mensajero;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

public class Main {
    public static void main(String[] args) {
        Animal perro = new Animal () {
            @Override
            public void hacerSonido() {
                System.out.println("Guau!");
            }
        };
        perro.hacerSonido();

        new Vehiculo() {
            private int numPasajeros;
            public void manejar() {
                System.out.println("Estoy manejando");
            }
        }.manejar();

        Mensajero lambdaMsje = (nombre, edad) -> {
            System.out.println("Hola desde lambda " + nombre);
            System.out.println("Su edad es: " + edad);
        };
        lambdaMsje.emitirMensaje("Guido", 32);

        // Clases anónimas
        Operador suma = new Operador () {
            @Override
            public int operar(int num1, int num2) {
                return num1+num2;
            }
        };
        System.out.println("Suma anónima " + suma.operar(3,5));

        // Lambdas
        Operador suma2 = (num1,num2) -> num1+num2;
        System.out.println("Suma Lambda " + suma2.operar(4,5));

        // Referencia a un método estático
        Function<Integer, String> convertidor = String::valueOf;
        String resultado = convertidor.apply(35);
        System.out.println("El resultado es " + resultado);

        // Referencia a un método de instancia de un objeto
        Persona persona = new Persona();
        persona.nombre = "Guido";
        Runnable saludo = persona::saludar;
        saludo.run();

        // Referencia a un constructor
        BiFunction<String, Double, Persona> crearPersona = Persona::new;
        Persona persona2 = crearPersona.apply("Guido", 1.66);
        System.out.println("La persona es: " + persona2);

        // Referencia a un método de instancia de un objeto arbitrario
        List<Persona> personas = new ArrayList<>();
        personas.add(new Empleado());
        personas.add(new Gerente());
        personas.add(new Empleado());
        personas.add(new Gerente());
        personas.forEach(Persona::saludar);

        // Streams
        List<String> nombres = new ArrayList<>();
        nombres.add("Guido");
        nombres.add("Denu");
        nombres.add("Santi");
        nombres.stream().forEach(System.out::println);

        List<String> paises = Arrays.asList(
                "Argentina", "Alemania", "España", "Francia", "Italia",
                "Portugal", "México", "Chile", "Colombia", "Perú");
        paises.stream()
                .filter(pais -> pais.toLowerCase().contains("a"))
                .forEach(System.out::println);
    }
}