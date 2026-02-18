package org.funcional;

import org.funcional.logica.Auto;
import org.funcional.logica.Cliente;
import org.funcional.logica.Mensajero;
import org.funcional.logica.Producto;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

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

        // Ejercicio integrador n°1
        List<Auto> listaAutos = Arrays.asList(
                new Auto("Volkswagen", "Amarok", 25000),
                new Auto("Volk" +
                        "wagen", "Taos", 32000),
                new Auto("Chevrolet", "Onix", 22000),
                new Auto("Chevrolet", "Tracker", 30000),
                new Auto("Fiat", "Cronos", 21000),
                new Auto("Fiat", "Pulse", 24000),
                new Auto("Toyota", "Corolla", 28000),
                new Auto("Toyota", "Yaris", 23000),
                new Auto("Renault", "Stepway", 20000),
                new Auto("Renault", "Duster", 27000),
                new Auto("Nissan", "Versa", 25000)
        );
        System.out.println("----------");
        System.out.println("Ordenado por precio de menor a mayor mediante sort");
        List<Auto> ordenPrecio = new ArrayList<Auto>(listaAutos);
        ordenPrecio.sort(Comparator.comparing(Auto::getCosto));
        ordenPrecio.forEach(System.out::println);
        System.out.println("----------");
        System.out.println("Ordenado por marca y luego por precio usando sorted()");
        listaAutos.stream()
                .sorted(Comparator
                        .comparing(Auto::getMarca)
                        .thenComparing(Auto::getCosto))
                .forEach(System.out::println);
        System.out.println("----------");
        System.out.println("Filtrando autos más baratos que 23000");
        listaAutos.stream()
                .filter(auto -> auto.getCosto() <= 23000)
                .forEach(System.out::println);
        System.out.println("----------");
        System.out.println("Filtrado autos Chevrolet y Volkswagen");
        listaAutos.stream()
                .filter(auto -> auto.getMarca().equals("Chevrolet") || auto.getMarca().equals("Volkswagen"))
                .forEach(System.out::println);
        System.out.println("----------");
        System.out.println("Filtrando autos cuyo modelo tiene una a");
        listaAutos.stream()
                .filter(auto -> auto.getModelo().toLowerCase().contains("a"))
                .forEach(System.out::println);
        System.out.println("----------");

        Optional<String> stringNull = Optional.ofNullable(null);
        if (stringNull.isEmpty()) {
            System.out.println("El valor es vacío");
        }
        if (stringNull == null) {
            System.out.println("El valor es null");
        }
        Optional<String> cadena = Optional.of("Guido");
        if (cadena.isEmpty()) {
            System.out.println("Es vacía");
        } else {
            System.out.println("Tiene un valor");
        }
        Optional<String> cadena2 = Optional.empty();
        if (cadena2.isEmpty()) {
            System.out.println("Es vacía");
        } else {
            System.out.println("Tiene un valor");
        }

        // Ejercicio integrador n°2
        List<Cliente> listaClientes = obtenerClientes();
        System.out.println("-----Bienvenido a Envíos Guido-----");
        System.out.println("Ingrese el número de cliente a enviar un paquete");
        Scanner teclado = new Scanner (System.in);
        // Long numero = teclado.nextLong();
        Optional<Cliente> cliente = buscarCliente(listaClientes, 1L);
        // Verificar cliente
        if (cliente.isPresent()) {
            Optional<String> direccion = obtenerDireccion(cliente);
            // Verificar la dirección
            if (direccion.isPresent()) {
                System.out.println("La dirección es: " + direccion.get());
            } else {
                System.out.println("Este cliente no tiene dirección");
            }
        } else {
            System.out.println("El cliente no existe");
        }

        // Collectors.toList
        List<String> nombresConA = nombres.stream()
                .filter(nombre -> nombre.contains("a"))
                .collect(Collectors.toList());
        System.out.println(nombresConA);

        // Collectors.toSet
        List<String> lenguajes = List.of("java", "python", "java", "PHP", "javascript");
        Set<String> sinRepetidos = lenguajes.stream()
                .map(String::toUpperCase)
                .collect(Collectors.toSet());
        System.out.println(sinRepetidos);

        // Collectors.joining()
        List<String> sgbds = List.of("MySQL", "Oracle", "PostgreSQL");
        String listaTecnologias = sgbds.stream()
                .map(String::toUpperCase)
                .collect(Collectors.joining(" | "));
        System.out.println(listaTecnologias);

        // Collectors.counting()
        Long cantidadConL = paises.stream()
                .filter(n -> n.startsWith("L"))
                .collect(Collectors.counting());
        System.out.println("Cantidad de paises con L: " + cantidadConL);

        // Collectors.partitioningBy()
        List<Integer> edades = List.of(13,15,20,21,35,33,32,48,57,12,10);
        Map<Boolean, List<Integer>> mayores = edades.stream()
                .collect(Collectors.partitioningBy(e-> e>=18));
        System.out.println(mayores);

        int[] numeros = {1,2,3,4,5,6,7,8,9,10};

        // Stream secuencial
        long tiempoInicio = System.nanoTime();
        Arrays.stream(numeros).forEach(n-> System.out.println("Secuencial " + n));
        long tiempoFin = System.nanoTime();
        System.out.println("Tiempo secuencial: " + (tiempoFin-tiempoInicio) + " nanosegundos");

        // Parallel Stream
        tiempoInicio = System.nanoTime();
        Arrays.stream(numeros).parallel().forEach(n-> System.out.println("Paralelo " + n));
        tiempoFin = System.nanoTime();
        System.out.println("Tiempo paralelo: " + (tiempoFin-tiempoInicio) + " nanosegundos");

        // Ejercicio integrador final
        List<Producto> productos = Arrays.asList(
                new Producto("Manzana", "frutas", 1, 2.5),
                new Producto("Leche", "lácteos", 2, 1.2),
                new Producto("Pan", "panadería", 5, 1.0),
                new Producto("Yogur", "lácteos", 3, 1.0),
                new Producto("Pera", "frutas", 4, 3.0),
                new Producto("Naranja", "frutas", 6, 2.0),
                new Producto("Queso", "lácteos", 2, 3.5),
                new Producto("Manteca", "lácteos", 1, 2.2),
                new Producto("Harina", "panadería", 3, 1.5),
                new Producto("Arroz", "alimentos", 4, 1.8),
                new Producto("Tomate", "frutas", 5, 1.2),
                new Producto("Lechuga", "verduras", 2, 0.9),
                new Producto("Pescado", "pescadería", 1, 4.0),
                new Producto("Pollo", "carnes", 3, 5.0),
                new Producto("Carne", "carnes", 2, 6.5)
        );

        // Filtrar productos de la categoría frutas
        List<Producto> frutas = productos.stream()
                .filter(p -> p.getCategoria().equals("frutas"))
                .collect(Collectors.toList());
        System.out.println("Productos que son frutas " + frutas);

        // Calcular el precio total de los productos lácteos
    }

    private static Optional<String> obtenerDireccion(Optional<Cliente> cliente) {
        return cliente
                .map(Cliente::getDireccion);
    }

    private static Optional<Cliente> buscarCliente(List<Cliente> listaClientes, Long numero) {
        return listaClientes.stream()
                .filter(cliente -> cliente.getNroCliente().equals(numero))
                .findFirst();
    }

    private static List<Cliente> obtenerClientes() {
        List<Cliente> clientes = Arrays.asList(
                new Cliente(1L, "Regulus", "Leo", null, "1111-1111"),
                new Cliente(2L, "Kagaho", "Bennu", "Av. Siempre Viva 742", "2222-2222"),
                new Cliente(3L, "Radamanthys", "Wyvern", "Mitre 100", "3333-3333"),
                new Cliente(4L, "Minos", "Griffon", "Belgrano 456", "4444-4444"),
                new Cliente(5L, "Aiacos", "Garuda", "San Martín 789", "5555-5555"),
                new Cliente(6L, "Alone", "Hades", null, "6666-6666"),
                new Cliente(7L, "Tenma", "Pegaso", "Corrientes 202", "7777-7777"),
                new Cliente(8L, "Hasgard", "Tauro", null, "8888-8888"),
                new Cliente(9L, "Albafica", "Piscis", "Seguí 404", "9999-9999"),
                new Cliente(10L, "Cid", "Capricornio", null, "1010-1010")
        );
        return clientes;
    }
}