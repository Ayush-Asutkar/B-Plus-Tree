import java.util.Arrays;

// this holds the dictionary pair
// it does not have any children
// LeafNode has a minimum and maximum number of dictionary pairs it can hold
// this is specified by order, the max degree of the b+ tree
// and the LeafNode forms a doubly linked list
public class LeafNode extends Node {
    // maximum number of dictionary pairs LeafNode can hold
    private int maxNumPairs;

    // minimum number of dictionary pairs LeafNode can hold
    private int minNumPairs;

    // number of dictionary pairs it is holding right now
    private int numPairs;

    private LeafNode leftSibling;

    private LeafNode rightSibling;

    private DictionaryPair[] dictionary;

    public int getMaxNumPairs() {
        return maxNumPairs;
    }

    public int getNumPairs() {
        return numPairs;
    }

    public LeafNode getLeftSibling() {
        return leftSibling;
    }

    public LeafNode getRightSibling() {
        return rightSibling;
    }

    public DictionaryPair[] getDictionary() {
        return dictionary;
    }

    public void setLeftSibling(LeafNode leftSibling) {
        this.leftSibling = leftSibling;
    }

    public void setRightSibling(LeafNode rightSibling) {
        this.rightSibling = rightSibling;
    }

    public LeafNode(int order, DictionaryPair dictionaryPair) {
        this.maxNumPairs = order - 1;
        this.minNumPairs = (int) (Math.ceil(order / 2.0) - 1);
        this.dictionary = new DictionaryPair[order];
        this.numPairs = 0;
        this.insert(dictionaryPair);
    }

    public LeafNode(int order, DictionaryPair[] dictionaryPairs, InternalNode parent) {
        this.maxNumPairs = order - 1;
        this.minNumPairs = (int) (Math.ceil(order / 2.0) - 1);
        this.dictionary = dictionaryPairs;
        this.numPairs = linearNullSearch(dictionaryPairs);
        this.parent = parent;
    }

    private int linearNullSearch(DictionaryPair[] pointers) {
        for(int i=0; i < pointers.length; i++) {
            if (pointers[i] == null) {
                return i;
            }
        }

        return -1;
    }

    private boolean isFull() {
        return numPairs == maxNumPairs;
    }

    public boolean isDeficient() {
        return this.numPairs < this.minNumPairs;
    }

    public boolean isLendable() {
        return this.numPairs > this.minNumPairs;
    }

    public boolean isMergeable() {
        return this.numPairs == this.minNumPairs;
    }

    public boolean insert(DictionaryPair dictionaryPair) {
        if(this.isFull()) {
            return false;
        } else {
            // insert the dictionary pair
            this.dictionary[numPairs] = dictionaryPair;

            //increment numPairs
            this.numPairs++;

            //sort dictionary
            Arrays.sort(this.dictionary, 0, numPairs);

            return true;
        }
    }

    public void delete(int index) {
        //delete dictionary pair from leaf
        this.dictionary[index] = null;

        //decrement numPairs
        numPairs--;
    }

    public void increaseNumPair(int value) {
        this.numPairs += value;
    }
}
