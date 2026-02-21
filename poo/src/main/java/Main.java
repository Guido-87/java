import java.util.*;

public class Main {
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
        boolean estaONo = mapaEmpleados.containsValue("Tenma de Pegaso");
        estaONo = mapaEmpleados.containsKey(1523);
        if (estaONo) {
            System.out.println("El valor buscado está");
        } else {
            System.out.println("El valor buscado no está");
        }
    }
}
