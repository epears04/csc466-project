package DocumentClasses;

import java.util.ArrayList;
import java.util.HashMap;

// single decision tree
public class DecisionTree {
    private TreeNode root;
    private int maxDepth;
    private double threshold;

    public DecisionTree(int maxDepth, double threshold, Matrix matrix, ArrayList<Integer> attributes, ArrayList<Integer> rows) {
        this.root = makeTree(matrix, attributes, rows, 0);
        this.maxDepth = maxDepth;
        this.threshold = threshold;
    }

    public TreeNode getRoot() {
        return root;
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

}
