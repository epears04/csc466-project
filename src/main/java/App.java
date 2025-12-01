import DocumentClasses.Matrix;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

// main class
public class App {
    private static final double THRESHOLD = 0.01;
    private static final int LABEL_INDEX = 4;
    // stores [data][attribute]
    private static int[][] data;
    private static Matrix matrix;
    public static void main(String[] args) {
        data = process("./files/data.txt");
        matrix = new Matrix(data, LABEL_INDEX);

        ArrayList<Integer> attributes = new ArrayList<>();
        for (int i = 0; i < data[0].length; i++) {
            if (i != LABEL_INDEX) {
                attributes.add(i);
            }
        }
        ArrayList<Integer> rows = new ArrayList<>();
        for (int i = 0; i < data.length; i++) {
            rows.add(i);
        }

        // printDecisionTree(data, attributes, rows, 0, 100);
    }

    public static int[][] process(String filename) {
        File file = new File(filename);
        List<int[]> rows = new ArrayList<>();

        try(Scanner reader = new Scanner(file)) {
            while(reader.hasNextLine()) {
                String line = reader.nextLine().trim();
                String[] tokens = line.split(",");
                int numCols = tokens.length;
                int[] row = new int[numCols];
                for (int i = 0; i < numCols; i++) {
                    int num = (int) Double.parseDouble(tokens[i]);
                    row[i] = num;
                }
                rows.add(row);
            }
        } catch (FileNotFoundException e) {
            System.err.println("File " + filename + " not found.");
        }

        // convert List<int[]> to int[][]
        int[][] matrix = new int[rows.size()][];
        for (int i = 0; i < rows.size(); i++) {
            matrix[i] = rows.get(i);
        }
        return matrix;
    }

}
