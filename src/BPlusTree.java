import java.util.Arrays;
import java.util.Comparator;

public class BPlusTree {
    private int order; // order or fan-out of the b+ tree
    private InternalNode root;
    private LeafNode firstLeaf;

    public BPlusTree(int order) {
        this.order = order;
        this.root = null;
    }

    private boolean isEmpty() {
        return this.firstLeaf == null;
    }

    private int getMidPoint() {
        return (int) Math.ceil((this.order + 1) / 2.0) - 1;
    }

    // perform standard binary search on a sorted DictionaryPair[]
    // and return the index. If not present, return negative value
    private int binarySearch(DictionaryPair[] dictionaryPairs, int numPairs, int key) {
        Comparator<DictionaryPair> comparator = new Comparator<DictionaryPair>() {
            @Override
            public int compare(DictionaryPair o1, DictionaryPair o2) {
                Integer a = Integer.valueOf(o1.getKey());
                Integer b = Integer.valueOf(o2.getKey());
                return a.compareTo(b);
            }
        };

        return Arrays.binarySearch(dictionaryPairs, 0, numPairs, new DictionaryPair(key, 0), comparator);
    }

    // Start at the root of the B+ tree and traverse down the tree via key comparisons
    private LeafNode findLeafNode(int key) {
        Integer[] keys = this.root.getKeys();
        int i;

        // find the next node on path to appropriate leaf node
        for (i = 0; i < this.root.getDegree() - 1; i++) {
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
        Integer[] keys = node.getKeys();
        int i;

        // find the next node on path to appropriate leaf node
        for (i = 0; i < node.getDegree() - 1; i++) {
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

    private int linearNullSearch(Node[] pointers) {
        for (int i = 0; i < pointers.length; i++) {
            if (pointers[i] == null) {
                return i;
            }
        }
        return -1;
    }

    //sorting the dictionaryPairs
    private void sortDictionary(DictionaryPair[] dictionaryPairs) {
        Arrays.sort(dictionaryPairs);
    }

    private DictionaryPair[] splitDictionary(LeafNode leafNode, int split) {
        DictionaryPair[] dictionaryPairs = leafNode.getDictionary();

        //initialize two dictionaries that each hold half of the original dictionary values
        DictionaryPair[] halfDict = new DictionaryPair[this.order];

        // copy half of the values into halfDict and update the original dictionaryPair
        for (int i = split; i < dictionaryPairs.length; i++) {
            halfDict[i - split] = dictionaryPairs[i];
            leafNode.delete(i);
        }

        return halfDict;
    }

    private void splitInternalNode(InternalNode internalNode) {
        //acquire parent
        InternalNode parent = internalNode.parent;

        //split keys and pointers in half
        int midPoint = this.getMidPoint();
        int newParentKey = internalNode.getKeys()[midPoint];
        Integer[] halfKeys = splitKeys(internalNode.getKeys(), midPoint);
        Node[] halfPointers = splitChildPointers(internalNode, midPoint);

        //change degree of original InternalNode interNode
        internalNode.setDegree(linearNullSearch(internalNode.getChildPointers()));

        //create new sibling internal node and add half of keys and pointers
        InternalNode sibling = new InternalNode(this.order, halfKeys, halfPointers);
        for (Node pointer: halfPointers) {
            if (pointer != null) {
                pointer.parent = sibling;
            }
        }

        //make internal nodes siblings of one another
        sibling.setRightSibling(internalNode.getRightSibling());
        if(sibling.getRightSibling() != null) {
            sibling.getRightSibling().setLeftSibling(sibling);
        }
        internalNode.setRightSibling(sibling);
        sibling.setLeftSibling(internalNode);

        if (parent == null) {
            //create new root node and add midpoint key and pointers
            Integer[] keys = new Integer[this.order];
            keys[0] = newParentKey;
            InternalNode newRoot = new InternalNode(this.order, keys);
            newRoot.appendChildPointer(internalNode);
            newRoot.appendChildPointer(sibling);
            this.root = newRoot;

            //add pointers from children to parent
            internalNode.parent = newRoot;
            sibling.parent = newRoot;
        } else {
            //add key to parent
            parent.getKeys()[parent.getDegree() - 1] = newParentKey;
            Arrays.sort(parent.getKeys(), 0, parent.getDegree());

            //set up pointer to new sibling
            int pointerIndex = parent.findIndexOfPointer(internalNode) + 1;
            parent.insertChildPointer(sibling, pointerIndex);
            sibling.parent = parent;
        }
    }

    private Integer[] splitKeys(Integer[] keys, int split) {
        Integer[] halfKeys = new Integer[this.order];

        //remove split-indexed value from keys
        keys[split] = null;

        //copy half of the values into halfKeys while updating the original keys
        for (int i = split + 1; i < keys.length; i++) {
            halfKeys[i - split - 1] = keys[i];
            keys[i] = null;
        }

        return halfKeys;
    }

    private Node[] splitChildPointers(InternalNode internalNode, int split) {
        Node[] pointers = internalNode.getChildPointers();
        Node[] halfPointers = new Node[this.order + 1];

        //copy half of the values into halfPointers while updating original keys
        for (int i = split + 1; i < pointers.length; i++) {
            halfPointers[i - split - 1] = pointers[i];
            internalNode.removePointer(i);
        }

        return halfPointers;
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
            if (this.root == null) {
                leafNode = this.firstLeaf;
            } else {
                leafNode = findLeafNode(key);
            }

            // insert into leaf node fails if node becomes overfull
            boolean insertCorrectly = leafNode.insert(new DictionaryPair(key, value));
            if (!insertCorrectly) {
                // sort all the dictionary pairs with the included pair to be inserted
                leafNode.getDictionary()[leafNode.getMaxNumPairs()] = new DictionaryPair(key, value);
                leafNode.increaseNumPair(1);

                //sort dictionary
                sortDictionary(leafNode.getDictionary());


                //split the sorted pairs into two halves
                int midPoint = getMidPoint();
                DictionaryPair[] halfDict = splitDictionary(leafNode, midPoint);

                if (leafNode.parent == null) {
                    //only 1 node in tree

                    // create internal node to serve as parent, use dictionary midpoint key
                    Integer[] parentKeys = new Integer[this.order];
                    parentKeys[0] = halfDict[0].getKey();
                    InternalNode parent = new InternalNode(this.order, parentKeys);
                    leafNode.parent = parent;
                    parent.appendChildPointer(leafNode);
                } else {
                    // add new key to parent for proper indexing
                    int newParentKey = halfDict[0].getKey();
                    leafNode.parent.getKeys()[leafNode.parent.getDegree() - 1] = newParentKey;
                    Arrays.sort(leafNode.parent.getKeys(), 0, leafNode.parent.getDegree());
                }

                // create new LeafNode that hold the other half
                LeafNode newLeafNode = new LeafNode(this.order, halfDict, leafNode.parent);

                //update child pointers of parent node
                int pointerIndex = leafNode.parent.findIndexOfPointer(leafNode) + 1;
                leafNode.parent.insertChildPointer(newLeafNode, pointerIndex);

                // make leaf nodes siblings of one another
                newLeafNode.setRightSibling(leafNode.getRightSibling());
                if (newLeafNode.getRightSibling() != null) {
                    newLeafNode.getRightSibling().setLeftSibling(newLeafNode);
                }
                leafNode.setRightSibling(newLeafNode);
                newLeafNode.setLeftSibling(leafNode);

                if (this.root == null) {
                    //set the root of B+ tree to be the parent
                    this.root = leafNode.parent;
                } else {
                    // if parent is overfull, repeat the process up the tree until no deficiencies are found

                    InternalNode internalNode = leafNode.parent;
                    while (internalNode != null) {
                        if (internalNode.isOverFull()) {
                            splitInternalNode(internalNode);
                        } else {
                            break;
                        }
                        internalNode = internalNode.parent;
                    }
                }
            }
        }
    }

    public Integer search(int key) {
        if (isEmpty()) {
            return null;
        }

        //find leaf node that holds the dictionary key
        LeafNode leafNode;
        if (this.root == null) {
            leafNode = this.firstLeaf;
        } else {
            leafNode = findLeafNode(key);
        }

        //perform binary search to find index of key within dictionary
        DictionaryPair[] dictionaryPairs = leafNode.getDictionary();
        int index = binarySearch(dictionaryPairs, leafNode.getNumPairs(), key);

        //if index negative, the key does not exist
        if (index < 0) {
            return null;
        }  else {
            return dictionaryPairs[index].getValue();
        }
    }
}
