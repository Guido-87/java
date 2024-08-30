import java.util.HashMap;

public class Main {
    public static void main(String[] args) {
        // Contar palabras
        String txt = "rojo verde rojo azul amarillo azul rojo naranja";
        String[] txtSplit = txt.split(" ");
        HashMap<String, Integer> count = new HashMap<String, Integer>();
        for (String s : txtSplit) {
            if (!count.containsKey(s)) {
                count.put(s, 1);
            } else {
                count.put(s, count.get(s) + 1);
            }
        }
        System.out.println(String.valueOf(count) + "\n");

        // Contar números y mostrar *
        StringBuilder sb = Contador.contarNumeros();
        System.out.println(sb);

        // Contar ocurrencias, mostrar cantidad y número
        // TODO: Corregir
        final int[] results = Contador.contarOcurrencias();
        System.out.println("Longest: " + results[0]);
        System.out.println("Number: " + results[1]);
    }
}