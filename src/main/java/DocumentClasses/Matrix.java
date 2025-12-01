package DocumentClasses;

import java.util.*;
import java.io.*;

// represents data
public class Matrix {
    // a variable that is of type a two-dimensional array
    private final int[][] matrix;
    private final double lambda;
    private final int labelIndex;

    public Matrix(int[][] matrix, int labelIndex) {
        this.matrix = matrix;
        this.lambda = 1.0 / matrix.length;
        this.labelIndex = labelIndex;
    }

    // returns the number of rows in which the element at position attribute is equal to value.
    private int findFrequency(int attr, int value, ArrayList<Integer> rows) {
        int freq = 0;
        for (Integer row : rows) {
            if (matrix[row][attr] == value) {
                freq++;
            }
        }
        return freq;
    }

    // returns a HashSet of the different values for the specified attribute
    private HashSet<Integer> findDifferentValues(int attr, ArrayList<Integer> rows) {
        HashSet<Integer> differentValues = new HashSet<>();
        for (Integer row : rows) {
            differentValues.add(matrix[row][attr]);
        }
        return differentValues;
    }

    //  Returns an ArrayList of the rows where the value for the attribute is equal to value
    private ArrayList<Integer> findRows(int attr, int value, ArrayList<Integer> rows) {
        ArrayList<Integer> foundRows = new ArrayList<>();
        for (Integer row : rows) {
            if (matrix[row][attr] == value) {
                foundRows.add(row);
            }
        }
        return foundRows;
    }

    // returns log2 of input
    private double log2(double number) {
        return Math.log(number) / Math.log(2);
    }

    // finds the entropy of the dataset that consists of the specified rows
    private double findEntropy(ArrayList<Integer> rows) {
        if (rows.isEmpty()) {
            return 0.0;
        }

        double entropy = 0.0;
        HashSet<Integer> vals = findDifferentValues(labelIndex, rows);
        for (int v : vals) {
            int count = findFrequency(labelIndex, v, rows);
            double prob = (double) count / rows.size();
            if (prob != 0) {
                entropy -= prob * log2(prob);
            }
        }
        return entropy;
    }

    // weighted average entropy of the specified rows after it is partitioned on the attribute
    private double findEntropy(int attribute, ArrayList<Integer> rows) {
        if (rows.isEmpty()) {
            return 0.0;
        }
        double weightedEntropy = 0.0;
        HashSet<Integer> values = findDifferentValues(attribute, rows);
        for (int value : values) {
            ArrayList<Integer> matchingRows = findRows(attribute, value, rows);
            double entropy = findEntropy(matchingRows);
            weightedEntropy += ((double) matchingRows.size() / rows.size()) * entropy;
        }
        return weightedEntropy;
    }

    // finds the information gain of partitioning on the attribute. Considers only the specified rows.
    private double findGain(int attribute, ArrayList<Integer> rows)  {
        return findEntropy(rows) -  findEntropy(attribute, rows);
    }

    // returns the Information Gain Ratio, where we only look at the data defined by the set of rows, and we consider splitting on attribute.
    public double computeIGR(int attribute, ArrayList<Integer> rows) {
        if (rows.isEmpty()) {
            return 0.0;
        }
        double gain = findGain(attribute, rows);
        double denominator = 0.0;
        HashSet<Integer> values = findDifferentValues(attribute, rows);
        for (int value : values) {
            double prob = (double) findFrequency(attribute, value, rows) / rows.size();
            if (prob != 0) {
                denominator -= prob * log2(prob);
            }
        }
        if (denominator == 0) {
            return 0.0;
        }
        return gain / denominator;
    }

    // returns the most common category for the dataset that is the defined by the specified rows.
    public int findMostCommonValue(ArrayList<Integer> rows) {
        int maxCount = -1;
        int mostCommonClass = -1;
        HashSet<Integer> classes = findDifferentValues(labelIndex, rows);
        for (int c : classes) {
            int freq = findFrequency(labelIndex, c, rows);
            if (freq > maxCount) {
                maxCount = freq;
                mostCommonClass = c;
            }
        }
        return mostCommonClass;
    }

    // Splits the dataset that is defined by rows on the attribute.
    // Each element of the HashMap that is returned contains the value for the attribute and an ArrayList of rows that have this value.
    public HashMap<Integer, ArrayList<Integer>> split(int attribute, ArrayList<Integer> rows) {
        HashMap<Integer, ArrayList<Integer>> result = new HashMap<>();
        HashSet<Integer> values = findDifferentValues(attribute, rows);
        for (int value : values) {
            ArrayList<Integer> matchingRows = findRows(attribute, value, rows);
            result.put(value, matchingRows);
        }
        return result;
    }

    public boolean meetsThreshold(double threshold, ArrayList<Integer> rows) {
        return findEntropy(rows) <= threshold;
    }

    // return indices of all rows
    public ArrayList<Integer> findAllRows() {
        int numRows = matrix.length;
        ArrayList<Integer> rows = new ArrayList<>();
        for (int i = 0; i < numRows; i++) {
            rows.add(i);
        }
        return rows;
    }

    // return indices of all features
    public ArrayList<Integer> findAllColumns() {
        int numColumns = matrix[0].length;
        ArrayList<Integer> columns = new ArrayList<>();
        for (int i = 0; i < numColumns; i++) {
            columns.add(i);
        }
        columns.remove(labelIndex);
        return columns;
    }

}