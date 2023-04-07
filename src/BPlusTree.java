import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class BPlusTree {
    int order; // order or fan-out of the b+ tree
    InternalNode root;
    LeafNode firstLeaf;

    public BPlusTree(int order) {
        this.order = order;
        this.root = null;
    }

    private boolean isEmpty() {
        return this.firstLeaf == null;
    }

    // Start at the root of the B+ tree and traverse down the tree via key comparisons
    private LeafNode findLeafNode(int key) {
        Integer[] keys = this.root.getKeys();

        int i;

        // find the next node on path to appropriate leaf node
        for(i=0; i < this.root.getDegree() - 1; i++) {
            if (key < keys[i]) {
                break;
            }
        }

        //return node if it is a leafNode object,
        //otherwise repeat the search function a level down
        Node child = this.root.getChildPointers(i);
        if (child instanceof LeafNode) {
            return (LeafNode) child;
        } else {
            return findLeafNode((InternalNode) child, key);
        }
    }

    private LeafNode findLeafNode(InternalNode node, int key) {
        Integer[] keys = this.root.getKeys();

        int i;

        // find the next node on path to appropriate leaf node
        for(i=0; i < node.getDegree() - 1; i++) {
            if (key < keys[i]) {
                break;
            }
        }

        //return node if it is a leafNode object,
        //otherwise repeat the search function a level down
        Node child = node.getChildPointers(i);
        if (child instanceof LeafNode) {
            return (LeafNode) child;
        } else {
            return findLeafNode((InternalNode) node.getChildPointers(i), key);
        }
    }

    //sorting the dictionaryPairs
    private void sortDictionary(DictionaryPair[] dictionaryPairs) {
        Arrays.sort(dictionaryPairs);
    }

    private int getMidPoint() {
        return (int) (Math.ceil((this.order + 1) / 2.0) - 1);
    }

    private ArrayList<DictionaryPair> splitDictionary(LeafNode leafNode, int split) {
        DictionaryPair[] dictionaryPairs = leafNode.getDictionary();

        //initialize two dictionaries that each hold half of the original dictionary values
        DictionaryPair[] halfDict = new DictionaryPair[this.order];

        // copy half of the values into halfDict
        for(int i = split; i < dictionaryPairs.length; i++) {
            halfDict[i - split] = dictionaryPairs[i];
            leafNode.delete(i);
        }

        return halfDict;
    }

    public void insert(int key, int value) {
        /* Before inserting an element into a B+ tree, following properties must be kept in mind:
        * -- The root has at least two children
        * -- Each node except root can have a maximum of 'order' children and at least 'order'/2 children
        * -- Each node can contain a maximum of 'order' - 1 keys and a minimum of floor('order'/2) - 1 keys*/

        /* The following steps are followed for inserting an element:
        * 1. Since every element is inserted into the leaf node, go to the appropriate leaf node.
        * 2. Insert the key into the leaf node
        *
        * Case I
        * 1. If the leaf is not full, insert the key into the leaf node in increasing order
        *
        * Case II
        * 1. If the leaf is full, insert the key into the leaf node in increasing order and balance the tree:
        * 2. Break the node at 'order'/2 position.
        * 3. Add the 'order'/2 th key to the parent node as well
        * 4. If the parent node is already full, follow the steps 2 to 3.
        *
        * Insertion example:
        * Insert 5:
        *          |5|
        *
        * Insert 15
        *         |5 15|
        *
        * Insert 25
        *                     |15|
        *                    /    \
        *  |5 15 25|  =>   |5| --> |15 25|
        *
        * Insert 35
        *    |15|                    |15      25|
        *   /    \                  /      |     \
        * |5| --> |15 25 35| =>  |5| --> |15| --> |25 35|
        *
        * Insert 45
        *        |15      25|
        *       /     |      \
        *    |5| --> |15| --> |25 35 45|
        *
        *        |15      25       35|
        *       /     |        |      \
        *    |5| --> |15| --> |25| --> |35 45|
        *
        *                 |25|
        *               /     \
        *             /         \
        *        |15|            |35|
        *      /     \          /     \
        *    |5| --> |15| --> |25| --> |35 45|
        *    */


        if (isEmpty()) {
            // the tree is empty
            // create a leaf node as first node in b plus tree
            LeafNode leafNode = new LeafNode(this.order, new DictionaryPair(key, value));

            //set as first leaf node
            this.firstLeaf = leafNode;
        } else {
            // find the leaf node to insert into
            LeafNode leafNode;
            if(this.root == null) {
                leafNode = this.firstLeaf;
            } else {
                leafNode = findLeafNode(key);
            }

            // insert into leaf node fails if node becomes overfull
            boolean insertCorrectly = leafNode.insert(new DictionaryPair(key, value));
            if(!insertCorrectly) {
                // sort all the dictionary pairs with the included pair to be inserted
                leafNode.getDictionary()[leafNode.getMaxNumPairs()] = new DictionaryPair(key, value);
                leafNode.increaseNumPair(1);

                //sort dictionary
                sortDictionary(leafNode.getDictionary());


                //split the sorted pairs into two halves
                int midPoint = getMidPoint();
                ArrayList<DictionaryPair> halfDict = splitDictionary(leafNode, midPoint);
                // TODO: 07-04-2023 Complete split dictionary method
            }
        }
    }
}
