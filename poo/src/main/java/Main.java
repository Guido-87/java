import logica.Alumno;
import logica.Caja;

import java.util.*;

public class Main {

    enum Color {
        ROJO, AZUL, VERDE, AMARILLO, NARANJA, NEGRO, BLANCO
    }

    public static void main(String[] args) {
        List<Persona> lista = new ArrayList<Persona>();
        lista.add(new Persona(1, "Guido", 32));
        lista.add(new Persona(2, "Denu", 26));
        lista.add(new Persona(3, "Santi", 29));
        lista.add(new Persona(4, "Sebi", 28));
        System.out.println("-----For-----");
        for (int i=0; i<lista.size(); i++) {
            System.out.println("Prueba: " + lista.get(i).getNombre());
        }
        List<Persona> listaEnlazada = new LinkedList<Persona>();
        listaEnlazada.add(new Persona(1, "Guido", 32));
        listaEnlazada.add(new Persona(2, "Denu", 26));
        listaEnlazada.add(new Persona(3, "Santi", 29));
        listaEnlazada.add(new Persona(4, "Sebi", 28));
        listaEnlazada.add(0, new Persona(5, "Jorge", 32));
        lista.remove(1);
        String nombreBorrar = "Denu";
        for (Persona persona2 : listaEnlazada) {
            if (persona2.getNombre().equals(nombreBorrar)) {
                listaEnlazada.remove(persona2);
                break;
            }
        }
        System.out.println("-----ArrayList-----");
        for (Persona persona : lista) {
            System.out.println("Prueba: " + persona.getNombre());
        }
        System.out.println("-----Lista Enlazada-----");
        for (Persona persona : listaEnlazada) {
            System.out.println("Prueba: " + persona.getNombre());
        }
        System.out.println("-----Tamaño de las listas-----");
        System.out.println("ArrayList: " + lista.size());
        System.out.println("Lista Enlazada: " + listaEnlazada.size());
        System.out.println("-----Primer y último objeto Lista Enlazada-----");
        System.out.println("Primero: " + listaEnlazada.getFirst().toString());
        System.out.println("Último: " + listaEnlazada.getLast().toString());
        System.out.println("-----Borrando listas-----");
        lista.clear();
        listaEnlazada.clear();
        System.out.println("-----Listas vacías-----");
        System.out.println("ArrayList: " + lista.isEmpty());
        System.out.println("Lista Enlazada: " + listaEnlazada.isEmpty());

        Stack<Integer> pila = new Stack<Integer>();
        System.out.println("-----");
        System.out.println("Pila vacía: " + pila);
        System.out.println("Está vacía? " + pila.isEmpty());
        pila.push(1);
        pila.push(2);
        pila.push(3);
        pila.push(4);
        pila.push(5);
        for (Integer pilita : pila) {
            System.out.println(pilita);
        }
        System.out.println("Pila: " + pila);
        System.out.println("Pila vacía? " + pila.isEmpty());
        pila.pop(); // Elimina el último registro
        System.out.println("Está el 3? " + pila.search(3));
        System.out.println("Último agregado: " + pila.peek());

        System.out.println("-----");
        Map<Integer, String> mapaEmpleados = new HashMap<>();
        mapaEmpleados.put(1523, "Regulus de Leo");
        mapaEmpleados.put(1524, "Radamanthys de Wyvern");
        mapaEmpleados.put(1525, "Tenma de Pegaso");
        mapaEmpleados.put(1526, "Albafica de Piscis");
        mapaEmpleados.put(1527, "El Cid de Capricornio");
        boolean estaONo = mapaEmpleados.containsValue("Tenma de Pegaso");
        estaONo = mapaEmpleados.containsKey(1523);
        if (estaONo) {
            System.out.println("El valor buscado está");
        } else {
            System.out.println("El valor buscado no está");
        }
        mapaEmpleados.remove(1527);
        System.out.println(mapaEmpleados.values());
        System.out.println(mapaEmpleados.keySet());
        String nombre = mapaEmpleados.get(1524);
        System.out.println("El empleado buscado es: " + nombre);

        try {
            int resultado = 3 / 0;
        } catch (Exception e) {
            System.out.println("No se puede dividir por cero");
        }
        try {
            int edades[] = {15, 12, 23, 30};
            System.out.println("La edad de la posición 4 es: " + edades[4]);
        } catch (Exception e) {
            System.out.println("Intentaste acceder a un índice que no existe");
        }

        Auto auto = new Auto();
        auto.setId(1L);
        auto.setMarca("Renault");
        auto.setModelo("Duster");
        List<Propietario> listaPropietarios = new ArrayList<Propietario>();
        Propietario propietario1 = new Propietario();
        Propietario propietario2 = new Propietario();
        Propietario propietario3 = new Propietario();
        propietario1.setId(35L);
        propietario1.setNombre("Guido");
        propietario1.setApellido("Alvarez");
        propietario2.setId(23L);
        propietario2.setNombre("Denise");
        propietario2.setApellido("Valle");
        propietario3.setId(2L);
        propietario3.setNombre("Santiago");
        propietario3.setApellido("Rodriguez");
        listaPropietarios.add(propietario1);
        listaPropietarios.add(propietario2);
        listaPropietarios.add(propietario3);
        auto.setPropietarios(listaPropietarios);
        System.out.println("El auto " + auto.getMarca() + " " + auto.getModelo() +
                " tiene como propietarios a: " + auto.getPropietarios().toString());

        double num = 1.66;
        int numInt = (int) num;
        long numLong = (long) num;
        System.out.println("double: " + num);
        System.out.println("int: " + numInt);
        System.out.println("long: " + numLong);
        String cantidad = "15";
        String precio = "150.27";
        int cantEntero = Integer.parseInt(cantidad);
        double precioDouble = Double.parseDouble(precio);
        System.out.println("El valor total de la compra es: " + (cantEntero * precioDouble));
        int edad = 32;
        String edadString = String.valueOf(edad);
        String estaturaString = String.valueOf(num);
        System.out.println("Edad: " + edadString + " Estatura: " + estaturaString);

        Color color = Color.AZUL;
        System.out.println("El color es: " + color);
        for (Color c : Color.values()) {
            System.out.println("El color es: " +c);
        }
        System.out.println(color.name());
        System.out.println(color.ordinal());
        try {
            color = Color.valueOf("ASUL");
            System.out.println("Encontré: " + color);
        } catch (IllegalArgumentException e) {
            System.out.println("Ocurrió una excepción. El valor no es correcto");
        }

        Caja<String> cajaString = new Caja<>();
        cajaString.ponerAlgo("Cadena de texto");
        String contenido = cajaString.obtenerAlgo();
        System.out.println("El contenido es: " + contenido);
        Caja<Integer> cajaEnteros = new Caja<>();
        cajaEnteros.ponerAlgo(32);
        Integer numero = cajaEnteros.obtenerAlgo();
        System.out.println("El contenido es: " + numero);

        Alumno alumno1 = new Alumno();
        Alumno alumno2 = new Alumno(5, "Guido", "Alvarez");
        System.out.println("El id del alumno 2 es: " + alumno2.getId());
        System.out.println("El nombre es: " + alumno2.getNombre());
        System.out.println("El apellido es: " + alumno2.getApellido());
        alumno1.setId(8);
        alumno1.setNombre("Denise");
        alumno1.setApellido("Valle");
        System.out.println("-----");
        System.out.println("El id del alumno 1 es: " + alumno1.getId());
        System.out.println("El nombre es: " + alumno1.getNombre());
        System.out.println("El apellido es: " + alumno1.getApellido());
        System.out.println("-----");
        alumno2.setId(35);
        System.out.println("El id del alumno 2 es: " + alumno2.getId());
        System.out.println("El nombre es: " + alumno2.getNombre());
        System.out.println("El apellido es: " + alumno2.getApellido());

        Empleado empleado = new Empleado();
        Consultor consultor = new Consultor();
        Persona vector[] = new Persona[5];
        vector[0] = new Persona();
        vector[1] = empleado;
        vector[2] = consultor;
        vector[3] = new Jefe();
        Persona persona = new Persona();
        persona = consultor;
    }
}
