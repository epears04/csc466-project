import DocumentClasses.Matrix;
import DocumentClasses.RandomForest;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

// main class
public class App {

    // label is the first column in your data (0 = normal, 1 = phishing)
    private static final int LABEL_INDEX = 0;

    // entropy threshold for stopping splits
    private static final double THRESHOLD = 0.01;

    public static void main(String[] args) {
        // Adjust these paths if needed based on where the CSVs actually live
        String trainPath = "data/train_inflated_binned.csv";
        String valPath   = "data/val_binned.csv";
        String testPath  = "data/test_binned.csv";

        // Load datasets
        HashMap<String, Integer> sourceMap = new HashMap<>();
        HashMap<String, Integer> tldMap = new HashMap<>();
        int[][] trainData = process(trainPath, sourceMap, tldMap);
        int[][] valData   = process(valPath, sourceMap, tldMap);
        int[][] testData  = process(testPath, sourceMap, tldMap);

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
        int numTrees = 20;   // start small increase later
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

        String[] baggingMethods = {"rows", "attributes", "both"};
        for (String method : baggingMethods) {
            System.out.println("Method: " + method);
            System.out.println("Training RandomForest on " + trainData.length + " rows...");
            forest.fit(trainMatrix, method);
            System.out.println("Training complete.");

            // Evaluate on training, validation, and test sets
//            double trainAcc = evaluate(forest, trainData);
//            double valAcc   = evaluate(forest, valData);
            double testAcc  = evaluate(forest, testData);

//            System.out.printf("Accuracy on TRAIN: %.2f%%%n", trainAcc * 100.0);
//            System.out.printf("Accuracy on VAL:   %.2f%%%n", valAcc * 100.0);
            System.out.printf("Accuracy on TEST:  %.2f%%%n", testAcc * 100.0);
        }
    }

    /**
     * Evaluate accuracy of the forest on a given dataset.
     */
    public static double evaluate(RandomForest forest, int[][] dataset) {
        int correct = 0;
        int[][] confusionMatrix = new int[2][2];
        for (int i = 0; i < dataset.length; i++) {
            int[] row = dataset[i];
            int label = row[LABEL_INDEX];
            int pred = forest.predict(row);
            confusionMatrix[label][pred]++;
            if (label == pred) {
                correct++;
            }
        }

        // print confusion matrix
        System.out.println("Confusion Matrix (rows = actual, cols = predicted)");
        System.out.println("               Predicted 0    Predicted 1");
        System.out.printf("Actual 0 (normal):   %8d      %8d%n",
                confusionMatrix[0][0], confusionMatrix[0][1]);
        System.out.printf("Actual 1 (phishing): %8d      %8d%n",
                confusionMatrix[1][0], confusionMatrix[1][1]);
        return correct / (double) dataset.length;
    }

    /**
     * Reads the CSV-like file and returns an int[][] matrix.
     * - Skips the header line.
     * - Reads numeric columns from the start of each row.
     * - Stops parsing a row when it hits the first non-numeric token (e.g., source, tld, url).
     * - Parses numeric columns and casts to int.
     */
    public static int[][] process(String filename, HashMap<String, Integer> sourceMap, HashMap<String, Integer> tldMap) {
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

                for (int i = 0; i < tokens.length; i++) {
                    String token = tokens[i].trim();
                    if (token.isEmpty()) {
                        continue;
                    }

                    try {
                        double value = Double.parseDouble(token);
                        numericValues.add((int) value);
                    } catch (NumberFormatException e) {
                        // map string features (source and tld) into classes
                        if (i == 36) {
                            if (sourceMap.containsKey(token)) {
                                int sourceVal = sourceMap.get(token);
                                numericValues.add(sourceVal);
                            } else {
                                int id = sourceMap.size() + 1;
                                sourceMap.put(token, id);
                                numericValues.add(id);
                            }
                        } else if (i == 37) {
                            if (tldMap.containsKey(token)) {
                                int tldVal = tldMap.get(token);
                                numericValues.add(tldVal);
                            } else {
                                int id = tldMap.size() + 1;
                                tldMap.put(token, id);
                                numericValues.add(id);
                            }
                        } else {
                            break;
                        }
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
