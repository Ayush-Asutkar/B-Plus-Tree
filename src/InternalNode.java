//only holds keys
//does not hold dictionary pair
public class InternalNode extends Node {
    private final int maxDegree;
    private final int minDegree;
    private int degree;

    private InternalNode leftSibling;
    private InternalNode rightSibling;

    private Integer[] keys;

    private Node[] childPointers;

    public Integer[] getKeys() {
        return this.keys;
    }

    public int getDegree() {
        return this.degree;
    }

    public Node[] getChildPointers() {
        return this.childPointers;
    }

    public Node getChildPointers(int i) {
        return this.childPointers[i];
    }

    public InternalNode(int order, Integer[] keys) {
        this.maxDegree = order;
        this.minDegree = (int) Math.ceil(order / 2.0);
        this.degree = 0;
        this.keys = keys;
        this.childPointers = new Node[this.maxDegree + 1];
    }

    public InternalNode(int order, Integer[] keys, Node[] pointers) {
        this.maxDegree = order;
        this.minDegree = (int) Math.ceil(order / 2.0);
        this.degree = linearNullSearch(pointers);
        this.keys = keys;
        this.childPointers = pointers;
    }

    public void setDegree(int degree) {
        this.degree = degree;
    }

    public InternalNode getLeftSibling() {
        return leftSibling;
    }

    public void setLeftSibling(InternalNode leftSibling) {
        this.leftSibling = leftSibling;
    }

    public InternalNode getRightSibling() {
        return rightSibling;
    }

    public void setRightSibling(InternalNode rightSibling) {
        this.rightSibling = rightSibling;
    }

    public void appendChildPointer(Node pointer) {
        this.childPointers[this.degree] = pointer;
        this.degree++;
    }

    private int linearNullSearch(Node[] pointers) {
        for(int i=0; i < pointers.length; i++) {
            if (pointers[i] == null) {
                return i;
            }
        }

        return -1;
    }

    public int findIndexOfPointer(Node pointer) {
        for (int i=0; i < this.childPointers.length; i++) {
            if (childPointers[i] == pointer) {
                return i;
            }
        }

        return -1;
    }

    public void insertChildPointer(Node pointer, int index) {
        for (int i = this.degree - 1; i >= index; i--) {
            this.childPointers[i + 1] = this.childPointers[i];
        }
        this.childPointers[index] = pointer;
        this.degree++;
    }

    public void prependChildPointer(Node pointer) {
        for(int i = degree - 1; i >= 0; i--) {
            childPointers[i + 1] = childPointers[i];
        }
        this.childPointers[0] = pointer;
        this.degree++;
    }

    public boolean isOverFull() {
        return this.degree == this.maxDegree + 1;
    }

    public boolean isDeficient() {
        return this.degree < this.minDegree;
    }

    public boolean isLendable() {
        return this.degree > this.minDegree;
    }

    public boolean isMergeable() {
        return this.degree == this.minDegree;
    }

    public void removePointer(int index) {
        this.childPointers[index] = null;
        this.degree--;
    }

    public void removePointer(Node pointer) {
        for (int i = 0; i < this.childPointers.length; i++) {
            if (this.childPointers[i] == pointer) {
                this.childPointers[i] = null;
            }
        }
        this.degree--;
    }

    public void removeKey(int index) {
        this.keys[index] = null;
    }
}
