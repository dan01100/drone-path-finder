package uk.ac.ed.inf.aqmaps.movegenerator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A tree node for holding Moves
 */

public class TreeNode {

    private TreeNode parent;
    private List<TreeNode> children;
    private Move move;

    public TreeNode(TreeNode parent, Move move) {
        this.parent = parent;
        this.children = new ArrayList<TreeNode>();
        this.move = move;
    }

    public Move getMove() {
        return move;
    }

    public List<TreeNode> getChildren() {
        return children;
    }

    public TreeNode getParent() {
        return parent;
    }

    /**
     * Creates a TreeNode for every move and adds it to the children of this node
     * 
     * @param moves
     */
    public void addChildren(List<Move> moves) {

        for (var move : moves) {
            children.add(new TreeNode(this, move));
        }

    }

    /**
     * @return a list of Moves by iterating through treeNode's predecessors
     */
    public List<Move> getMoves() {

        var moves = new ArrayList<Move>();
        var treeNode = this;

        while (treeNode.getMove() != null) {
            moves.add(treeNode.getMove());
            treeNode = treeNode.parent;
        }

        Collections.reverse(moves);
        return moves;
    }

}
