public class Main {
    public static void main(String[] args) {
        // Resultados esperados: 5, 4, 0
        int[] input1 = new int[] {7,1,5,3,6,4};
        int[] input1b = new int[] {7,2,5,3,6,1,4};
        int[] input2 = new int[] {7,6,4,3,1};
        int max = getMax(input1b);
        System.out.println(max);
    }

    private static int getMax(int[] input) {
        int max = 0;
        for (int i = 0; i< input.length ; i++) {
            for (int j = i+1; j < input.length; j++) {
                if (input[j] > input[i]) {
                    if (max < input[j] - input[i]) {
                        max = input[j] - input[i];
                    }
                }
            }
        }
        return max;
    }
}