package DocumentClasses;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Random;

// holds and creates all decision trees
public class RandomForest {
    private final int numTrees;
    private final int maxFeatures;
    private final double entropyThreshold;
    private final int maxDepth;
    private final Random rand;
    private final ArrayList<DecisionTree> trees;

    public RandomForest(int numTrees, int maxFeatures, double entropyThreshold, int maxDepth, long seed) {
        this.numTrees = numTrees;
        this.maxFeatures = maxFeatures;
        this.entropyThreshold = entropyThreshold;
        this.maxDepth = maxDepth;
        this.rand = new Random(seed);
        this.trees = new ArrayList<>();
    }

    // bagging, returns a list of size numRows with the indices of random rows
    // note: numRows must be the size of the dataset
    public ArrayList<Integer> getRandomSamples(int numRows) {
        ArrayList<Integer> samples = new ArrayList<>(); // indices of rows
        for (int i = 0; i < numRows; i++) {
            int index = rand.nextInt(numRows);
            samples.add(index);
        }
        return samples;
    }


    // feature bagging, returns a subset of feature indices
    public ArrayList<Integer> getRandomAttributes(ArrayList<Integer> attributes) {
        ArrayList<Integer> shuffledAttributes = new ArrayList<>(attributes);
        Collections.shuffle(shuffledAttributes, rand); // shuffle to create randomness

        int numAttributes;
        if (maxFeatures > 0) {
            numAttributes = Math.min(maxFeatures, attributes.size());
        } else {
            // default: sqrt(d), at least 1
            numAttributes = Math.max(1, (int) Math.floor(Math.sqrt(attributes.size())));
        }

        return new ArrayList<>(shuffledAttributes.subList(0, numAttributes));
    }

    // training fit model
    public void fit(Matrix matrix) {
        ArrayList<Integer> allRows = matrix.findAllRows();
        ArrayList<Integer> allAttributes = matrix.findAllColumns(); // excludes labelIndex

        trees.clear();

        for (int t = 0; t < numTrees; t++) {
            // 1. bootstrap sample of rows
            ArrayList<Integer> sampleRows = getRandomSamples(allRows.size());

            // 2. build a DecisionTree on this sample
            DecisionTree tree = new DecisionTree(
                    maxDepth,
                    entropyThreshold,
                    matrix,
                    allAttributes,
                    sampleRows,
                    rand
            );

            trees.add(tree);
        }
    }

    //prediction using many trees for the majority vote
    //collective prediction from all trees
    public int predict(int[] instance) {
        HashMap<Integer, Integer> votes = new HashMap<>();

        for (DecisionTree tree : trees) {
            int pred = tree.predict(instance);
            votes.put(pred, votes.getOrDefault(pred, 0) + 1);
        }

        int bestClass = -1;
        int bestCount = -1;

        for (var entry : votes.entrySet()) {
            if (entry.getValue() > bestCount) {
                bestCount = entry.getValue();
                bestClass = entry.getKey();
            }
        }

        return bestClass;
    }

}


