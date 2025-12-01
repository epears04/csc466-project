package DocumentClasses;

import java.util.HashMap;

// node of decision tree
public class TreeNode {
    public int attributeIndex; // attribute to split
    public HashMap<Integer, TreeNode> children = new HashMap<>();
    public boolean isLeaf;
    public int prediction;

    // leaf node
    public TreeNode(int prediction) {
        this.children = null;
        this.isLeaf = true;
        this.prediction = prediction;
    }

    // internal node
    public TreeNode(int attributeIndex, HashMap<Integer, TreeNode> children) {
        this.attributeIndex = attributeIndex;
        this.children = children;
        this.isLeaf = false;
    }
}
