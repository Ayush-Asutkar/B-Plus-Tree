//only holds keys
//does not hold dictionary pair
public class InternalNode extends Node {
    private int maxDegree;
    private int minDegree;
    private int degree;

    private InternalNode leftSibling;
    private InternalNode rightSibling;

    private Integer[] keys;

    private Node[] childPointers;

    public Integer[] getKeys() {
        return keys;
    }


}