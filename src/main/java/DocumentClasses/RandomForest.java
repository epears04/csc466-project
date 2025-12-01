package DocumentClasses;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

// holds and creates all decision trees
public class RandomForest {
    private final int numTrees;
    private final int maxFeatures;
    private final double entropyThreshold;
    private final int maxDepth;
    private final Random rand;

    public RandomForest(int numTrees, int maxFeatures, double entropyThreshold, int maxDepth, long seed) {
        this.numTrees = numTrees;
        this.maxFeatures = maxDepth;
        this.entropyThreshold = entropyThreshold;
        this.maxDepth = maxDepth;
        this.rand = new Random(seed);
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

        int numAttributes = (int) Math.floor(Math.sqrt(attributes.size())); // select sqrt(n) attributes to return
        return new ArrayList<>(shuffledAttributes.subList(0, numAttributes));
    }

}


