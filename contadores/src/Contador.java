import java.util.HashMap;

public class Contador {
    static int[] myArray = {1,2,1,3,3,1,2,1,5,1};

    // TODO: Agregar clave 4
    static StringBuilder contarNumeros() {
        HashMap<Integer, Integer> map = new HashMap<Integer, Integer>();
        for (int i : myArray) {
            if (map.containsKey(i)) {
                map.put(i, map.get(i) + 1);
            } else {
                map.put(i, 1);
            }
        }
        StringBuilder sb = new StringBuilder();
        map.forEach((k, v) -> sb.append(k)
                .append(": ")
                .append("*".repeat(v))
                .append("\n"));
        return sb;
    }

    static int[] contarOcurrencias() {
        HashMap<Integer, Integer> map2 = new HashMap<Integer, Integer>();
        for (int i : myArray) {
            if (i == myArray[i+1]) {
                if (map2.containsKey(i)) {
                    map2.put(i, map2.get(i) + 1);
                } else {
                    map2.put(i, 1);
                }
            }
        }
        final int[] results = {0, 0};
        map2.forEach((k, v) -> {
            if (k > results[0]) {
                results[0] = v;
                results[1] = k;
            }
        });
        return results;
    }
}
