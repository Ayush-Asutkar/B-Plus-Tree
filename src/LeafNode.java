import java.util.ArrayList;
import java.util.Collections;

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

    private ArrayList<DictionaryPair> dictionary;

    private boolean isFull() {
        return numPairs == maxNumPairs;
    }


    public LeafNode(int order, DictionaryPair dictionaryPair) {
        this.maxNumPairs = order - 1;
        this.minNumPairs = (int) (Math.ceil(order / 2) - 1);
        this.dictionary = new ArrayList<>();
        this.numPairs = 0;
        this.insert(dictionaryPair);
    }

    public boolean insert(DictionaryPair dictionaryPair) {
        if(this.isFull()) {
            return false;
        } else {
            // insert the dictionary pair
            this.dictionary.add(dictionaryPair);

            //increment numPairs
            this.numPairs++;

            //sort dictionary
            Collections.sort(this.dictionary);

            return true;
        }
    }
}
