package DocumentClasses;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

// single decision tree
public class DecisionTree {
    private final TreeNode root;
    private final int maxDepth;
    private final double threshold;

    public DecisionTree(int maxDepth, double threshold, Matrix matrix,
                        ArrayList<Integer> attributes, ArrayList<Integer> rows) {
        this.maxDepth = maxDepth;
        this.threshold = threshold;
        this.root = makeTree(matrix, attributes, rows, 0);
    }

    private TreeNode makeTree(Matrix matrix, ArrayList<Integer> attributes, ArrayList<Integer> rows, int level) {
        if (rows.isEmpty() || matrix.meetsThreshold(threshold, rows) || attributes.isEmpty() || level >= maxDepth) {
            int value = matrix.findMostCommonValue(rows);
            return new TreeNode(value);
        }

        int bestAttr = -1;
        double bestIGR = Double.NEGATIVE_INFINITY;
        for (int attr : attributes) {
            double igr = matrix.computeIGR(attr, rows);
            if (igr > bestIGR) {
                bestIGR = igr;
                bestAttr = attr;
            }
        }
        // no split
        if (bestAttr == -1 || bestIGR <= threshold) {
            int value = matrix.findMostCommonValue(rows);
            return new TreeNode(value);
        }

        // split
        TreeNode node = new TreeNode(bestAttr, new HashMap<>());

        // update possible attributes
        ArrayList<Integer> updatedAttributes = new ArrayList<>(attributes);
        updatedAttributes.remove(Integer.valueOf(bestAttr));

        HashMap<Integer, ArrayList<Integer>> splits = matrix.split(bestAttr, rows);
        for (var entry : splits.entrySet()) {
            int value = entry.getKey();
            ArrayList<Integer> childRows = entry.getValue();
            TreeNode child = makeTree(matrix, updatedAttributes, childRows, level + 1);
            node.children.put(value, child);
        }
        return node;
    }

    //predict feature to Decsion tree, single path though a single tree
    //giving one prediction
    public int predict(int[] instance) {
        TreeNode node = root;

        while (!node.isLeaf) {
            int attr = node.attributeIndex;
            int value = instance[attr];

            TreeNode child = node.children.get(value);

            if (child == null) {
                // unseen value at this node -> fallback to majority label stored at this node
                return node.prediction;
            }
            node = child;
        }
        return node.prediction;
    }


}
