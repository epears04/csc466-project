import DocumentClasses.Matrix;
import DocumentClasses.RandomForest;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

// main class
public class App {

    // label is the first column in your data (0 = normal, 1 = phishing)
    private static final int LABEL_INDEX = 0;

    // entropy threshold for stopping splits
    private static final double THRESHOLD = 0.01;

    public static void main(String[] args) {
        // Adjust these paths if needed based on where the CSVs actually live
        String trainPath = "csc466-project/src/main/java/DocumentClasses/train_filtered.csv";
        String valPath   = "csc466-project/src/main/java/DocumentClasses/val_filtered.csv";
        String testPath  = "csc466-project/src/main/java/DocumentClasses/test_filtered.csv";

        // Load datasets
        int[][] trainData = process(trainPath);
        int[][] valData   = process(valPath);
        int[][] testData  = process(testPath);

        // training data size
        int maxTrainRows = 100000;
        if (trainData.length > maxTrainRows) {
            int[][] small = new int[maxTrainRows][];
            for (int i = 0; i < maxTrainRows; i++) {
                small[i] = trainData[i];
            }
            trainData = small;
            System.out.println("Using only first " + maxTrainRows + " rows for TRAINING.");
        }

        System.out.println("Train size: " + trainData.length);
        System.out.println("Val size:   " + valData.length);
        System.out.println("Test size:  " + testData.length);

        // Wrap training set in Matrix (RandomForest.fit uses Matrix)
        Matrix trainMatrix = new Matrix(trainData, LABEL_INDEX);

        // Create and train the RandomForest
        int numTrees = 50;   // start small increase later
        int maxFeatures = 0; // 0 = use sqrt(#attributes) rule inside tree
        int maxDepth = 10;    // deeper trees => more complex model
        long seed = 42L;

        RandomForest forest = new RandomForest(
                numTrees,
                maxFeatures,
                THRESHOLD,
                maxDepth,
                seed
        );

        System.out.println("Training RandomForest on " + trainData.length + " rows...");
        forest.fit(trainMatrix);
        System.out.println("Training complete.");

        // Evaluate on training, validation, and test sets
        double trainAcc = evaluate(forest, trainData);
        double valAcc   = evaluate(forest, valData);
        double testAcc  = evaluate(forest, testData);

        System.out.printf("Accuracy on TRAIN: %.2f%%%n", trainAcc * 100.0);
        System.out.printf("Accuracy on VAL:   %.2f%%%n", valAcc * 100.0);
        System.out.printf("Accuracy on TEST:  %.2f%%%n", testAcc * 100.0);
    }

    /**
     * Evaluate accuracy of the forest on a given dataset.
     */
    public static double evaluate(RandomForest forest, int[][] dataset) {
        int correct = 0;
        for (int i = 0; i < dataset.length; i++) {
            int[] row = dataset[i];
            int label = row[LABEL_INDEX];
            int pred = forest.predict(row);
            if (label == pred) {
                correct++;
            }
        }
        return correct / (double) dataset.length;
    }

    /**
     * Reads the CSV-like file and returns an int[][] matrix.
     * - Skips the header line.
     * - Reads numeric columns from the start of each row.
     * - Stops parsing a row when it hits the first non-numeric token (e.g., source, tld, url).
     * - Parses numeric columns and casts to int.
     */
    public static int[][] process(String filename) {
        File file = new File(filename);
        List<int[]> rows = new ArrayList<>();

        try (Scanner reader = new Scanner(file)) {
            boolean isFirstLine = true;

            while (reader.hasNextLine()) {
                String line = reader.nextLine().trim();
                if (line.isEmpty()) {
                    continue; // skip blank lines
                }

                // Skip header row
                if (isFirstLine) {
                    isFirstLine = false;
                    continue;
                }

                String[] tokens = line.split(",");

                // Collect numeric tokens from the start of the row
                ArrayList<Integer> numericValues = new ArrayList<>();

                for (String token : tokens) {
                    token = token.trim();
                    if (token.isEmpty()) {
                        continue;
                    }

                    try {
                        double value = Double.parseDouble(token);
                        numericValues.add((int) value);
                    } catch (NumberFormatException e) {
                        // First non-numeric token: stop parsing this row
                        break;
                    }
                }

                if (!numericValues.isEmpty()) {
                    int[] row = new int[numericValues.size()];
                    for (int i = 0; i < numericValues.size(); i++) {
                        row[i] = numericValues.get(i);
                    }
                    rows.add(row);
                }
            }
        } catch (FileNotFoundException e) {
            System.err.println("File " + filename + " not found.");
            return null;
        }

        // convert List<int[]> to int[][]
        int[][] matrix = new int[rows.size()][];
        for (int i = 0; i < rows.size(); i++) {
            matrix[i] = rows.get(i);
        }

        System.out.println("Loaded " + matrix.length + " rows from " + filename +
                " with " + (matrix.length > 0 ? matrix[0].length : 0) + " numeric columns.");
        return matrix;
    }
}
