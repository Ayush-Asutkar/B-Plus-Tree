import java.util.*;

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
        Node childNode = node.getChildPointers(i);
        if (childNode instanceof LeafNode) {
            return (LeafNode) childNode;
        } else {
            return findLeafNode((InternalNode) node.getChildPointers()[i], key);
        }
    }

    private int findIndexOfPointer(Node[] pointers, LeafNode node) {
        for(int i=0; i < pointers.length; i++) {
            if(pointers[i] == node) {
                return i;
            }
        }
        return -1;
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
        Arrays.sort(dictionaryPairs, new Comparator<DictionaryPair>() {
            @Override
            public int compare(DictionaryPair o1, DictionaryPair o2) {
                if ((o1 == null)  &&  (o2 ==  null)) {
                    return 0;
                } else if(o1 == null) {
                    return 1;
                } else if(o2 == null) {
                    return -1;
                } else {
                    return o1.compareTo(o2);
                }
            }
        });
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

    private void shiftDown(Node[] pointers, int amount) {
        Node[] newPointers = new Node[this.order + 1];
        for(int i = amount; i < pointers.length; i++) {
            newPointers[i - amount] = pointers[i];
        }
        pointers = newPointers;
    }

    private void handleDeficiency(InternalNode internalNode) {
        InternalNode sibling;
        InternalNode parent = internalNode.parent;

        //remedy deficient root node
        if(this.root == internalNode) {
            for(int i=0; i<internalNode.getChildPointers().length; i++) {
                if(internalNode.getChildPointers()[i] != null) {
                    if(internalNode.getChildPointers()[i] instanceof InternalNode) {
                        this.root = (InternalNode) internalNode.getChildPointers()[i];
                        this.root.parent = null;
                    } else if(internalNode.getChildPointers()[i] instanceof LeafNode) {
                        this.root = null;
                    }
                }
            }
        }

        // borrow
        else if((internalNode.getLeftSibling() != null)  &&  (internalNode.getLeftSibling().isLendable())) {
            sibling = internalNode.getLeftSibling();
        } else if((internalNode.getRightSibling() != null)  &&  (internalNode.getRightSibling().isLendable())) {
            sibling = internalNode.getRightSibling();

            //copy 1 key and pointer from sibling
            int borrowedKey = sibling.getKeys()[0];
            Node pointer = sibling.getChildPointers()[0];

            //copy root key and pointer into parent
            internalNode.getKeys()[internalNode.getDegree() - 1] = parent.getKeys()[0];
            internalNode.getChildPointers()[internalNode.getDegree()] = pointer;

            //copy borrowedKey into root
            parent.getKeys()[0] = borrowedKey;

            //delete key and pointer from sibling
            sibling.removePointer(0);
            Arrays.sort(sibling.getKeys());
            sibling.removePointer(0);
            shiftDown(internalNode.getChildPointers(), 1);
        }

        //Merge
        else if((internalNode.getLeftSibling() != null)  &&  (internalNode.getLeftSibling().isMergeable())) {

        } else if((internalNode.getRightSibling() != null)  &&  (internalNode.getRightSibling().isMergeable())) {
            sibling = internalNode.getRightSibling();

            //copy rightmost key in parent to beginning of sibling's keys and delete from parent
            sibling.getKeys()[sibling.getDegree() - 1] = parent.getKeys()[parent.getDegree() - 2];
            Arrays.sort(sibling.getKeys(), 0, sibling.getDegree());
            parent.getKeys()[parent.getDegree() - 2] = null;

            //copy internalNode's child pointer over to sibling's list of child pointers
            for(int i=0; i < internalNode.getChildPointers().length; i++) {
                if (internalNode.getChildPointers()[i] != null) {
                    sibling.prependChildPointer(internalNode.getChildPointers(i));
                    internalNode.getChildPointers(i).parent = sibling;
                    internalNode.removePointer(i);
                }
            }

            //delete child pointer from grandparent to deficient node
            parent.removePointer(internalNode);

            //remove left sibling
            sibling.setLeftSibling(internalNode.getLeftSibling());
        }

        //handle deficiency a level up if it exists
        if((parent != null)  &&  (parent.isDeficient())) {
            handleDeficiency(parent);
        }
    }

    public void printLevelOrder() {
        if(isEmpty()) {
            System.out.println("Tree is empty");
            return;
        }

        Queue<Node> q = new LinkedList<>();
        if(this.root != null) {
            q.add(this.root);
        } else {
            q.add(this.firstLeaf);
        }
        while(!q.isEmpty()) {
            Node tempNode = q.poll();
            if(tempNode instanceof InternalNode) {
                Integer[] keys = ((InternalNode) tempNode).getKeys();
                Node[] pointers = ((InternalNode) tempNode).getChildPointers();

                ArrayList<Integer> list = new ArrayList<>();
                for(Integer integer: keys) {
                    if(integer != null) {
                        list.add(integer);
                    }
                }
                System.out.print(list + " ");

                for(Node pt: pointers) {
                    if(pt != null) {
                        q.add(pt);
                    }
                }
            } else {
                DictionaryPair[] dictionaryPairs = ((LeafNode) tempNode).getDictionary();
                ArrayList<Integer> list = new ArrayList<>();
                for(DictionaryPair pair: dictionaryPairs) {
                    if(pair != null) {
                        list.add(pair.getKey());
                    }
                }
                System.out.print(list + " ");
            }
        }
        System.out.println("\n");
    }

    public boolean insert(int key, double value) {
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

        Double alreadyPresentVal = this.search(key);
        if(alreadyPresentVal != null) {
            return false;
        }

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

        return true;
    }

    public Double search(int key) {
        /*
         * Find the leaf node that holds the key, and then do a binary search in that leaf node
         */


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

    public ArrayList<Double> search(int lowerBound, int upperBound) {
        ArrayList<Double> result = new ArrayList<>();

        LeafNode currNode = this.firstLeaf;
        while(currNode != null) {
            DictionaryPair[] dictionaryPairs = currNode.getDictionary();
            for(DictionaryPair dp: dictionaryPairs) {
                if (dp == null) {
                    break;
                }

                if ((lowerBound <= dp.getKey())  &&  (dp.getKey() <= upperBound)) {
                    result.add(dp.getValue());
                }
            }

            currNode = currNode.getRightSibling();
        }

        return result;
    }

    public void delete(int key) {
        /* Before inserting an element into a B+ tree, following properties must be kept in mind:
         * -- A node can have a maximum of 'order' children
         * -- A node can contain a maximum of 'order' - 1 keys
         * -- A node should have a minimum of floor('order'/2) - 1 keys
         * -- A node (except root node) should contain a minimum of floor('order'/2) - 1 keys*/

        /* While deleting a key, take care of the keys present in the internal nodes as well,
         * because the values are redundant in a B+ tree.
         * Search the key to be deleted then follow the following steps:
         * Case - I :- The key to be deleted is present only at the leaf node not in the indexed
         * (or internal nodes). There are two cases for it
         * 1. There is more than the minimum number of keys is the node. Simply delete the key.
         *                      |25|
         *                   /        \
         *                /              \
         *           |15|                  |35 45|
         *         /   |                 /    |    \
         *      /      |               /      |       \
         *  |5| --> |15 20| --> |25 30| --> |35 40| --> |45 55|
         *
         * Deleting 40:
         *                      |25|
         *                   /        \
         *                /              \
         *           |15|                 |35 45|
         *         /   |                /    |    \
         *      /      |              /      |       \
         *  |5| --> |15 20| --> |25 30| --> |35| --> |45 55|
         *
         *
         * 2. There is an exact minimum number of keys in the node. Delete the key and borrow a key
         *    from the immediate sibling. Add the median key of the sibling node to the parent.
         *                      |25|
         *                   /        \
         *                /              \
         *           |15|                 |35 45|
         *         /   |                /    |    \
         *      /      |              /      |       \
         *  |5| --> |15 20| --> |25 30| --> |35| --> |45 55|
         *
         * Deleting 5:
         *                      |25|
         *                   /        \
         *                /              \
         *           |20|                 |35 45|
         *         /   |                /    |    \
         *      /      |              /      |       \
         *    |15| --> |20| --> |25 30| --> |35| --> |45 55|
         *
         *
         * Case - II:- The key to be deleted is present in the internal nodes as well. Then remove them
         * from the internal nodes as well. Following are the cases for this operation:
         * 1. If there is more than the minimum number of keys in the node, simply delete the key from
         *    the leaf node and delete the key from internal node as well. Fill the empty space in the
         *    internal node with the interior successor.
         *                      |25|
         *                   /        \
         *                /              \
         *           |20|                 |35 45|
         *         /   |                /    |    \
         *      /      |              /      |       \
         *    |15| --> |20| --> |25 30| --> |35| --> |45 55|
         *
         * Deleting 45:
         *                       |25|
         *                    /       \
         *                 /             \
         *           |20|                 |35 55|
         *         /   |                /    |    \
         *      /      |              /      |       \
         *    |15| --> |20| --> |25 30| --> |35| --> |55|
         *
         *
         * 2. If there is an exact minimum number of keys in the node, then delete the key and borrow
         *    a key from its immediate sibling (through the parent). Fill the empty space created in the
         *    index (internal node) with the borrowed key.
         *                       |25|
         *                    /       \
         *                 /             \
         *           |20|                 |35 55|
         *         /   |                /    |    \
         *      /      |              /      |       \
         *    |15| --> |20| --> |25 30| --> |35| --> |55|
         *
         * Deleting 35:
         *                     |25|
         *                  /       \
         *               /             \
         *           |20|              |30 55|
         *         /   |             /    |    \
         *      /      |           /      |       \
         *    |15| --> |20| --> |25| --> |30| --> |55|
         *
         * 3. This case is similar to Case - II (1) but here, empty space is generated above the immediate
         *    parent node. After deleting the key, merge the empty space with its sibling. Fill the empty
         *    space in the grandparent node with the inorder successor.
         *
         *                     |25|
         *                  /       \
         *               /             \
         *           |20|              |30 55|
         *         /   |             /    |    \
         *      /      |           /      |       \
         *    |15| --> |20| --> |25| --> |30| --> |55|
         *
         * Deleting 25:
         *                 |30|
         *               /      \
         *             /          \
         *         |20|            |55|
         *       /     \           /   \
         *      /       \         /     \
         *    |15| --> |20| --> |30| --> |55|
         *
         * Case - III:- In this case, the height of the tree gets shrunk.
         *
         *                 |30|
         *               /      \
         *             /          \
         *         |20|            |55|
         *       /     \           /   \
         *      /       \         /     \
         *    |15| --> |20| --> |30| --> |55|
         *
         * Deleting 55:
         *
         *           |20 30|
         *          /   |    \
         *        /     |      \
         *    |15| --> |20| --> |30|
         */

        int successor = Integer.MAX_VALUE;
        //find successor of key
        LeafNode node = (this.root == null) ? this.firstLeaf: findLeafNode(key);
        int ind = binarySearch(node.getDictionary(), node.getNumPairs(), key);
        if(ind >= 0) {
            //yes, we will delete it. We are just finding the successor
            DictionaryPair[] dps = node.getDictionary();

            boolean value = false;
            for(int i=ind+1; i<dps.length; i++) {
                if(dps[i] != null) {
                    value = true;
                    successor = dps[i].getKey();
                    break;
                }
            }
            if(!value) {
                node = node.getRightSibling();
                while(node != null) {
                    for(int i=0; i<node.getDictionary().length; i++) {
                        if(node.getDictionary()[i] != null) {
                            value = true;
                            successor = node.getDictionary()[i].getKey();
                            break;
                        }
                    }
                    if(value) {
                        break;
                    }

                    node = node.getRightSibling();
                }
            }
        }
//
//        System.out.println("successor = " + successor);

        if (isEmpty()) {
            System.out.println("Invalid Delete: The B+ tree is currently empty.");
        } else {


            // get leaf node and attempt to find index of key to delete
            LeafNode leafNode = (this.root == null) ? this.firstLeaf: findLeafNode(key);
            int dpIndex = binarySearch(leafNode.getDictionary(), leafNode.getNumPairs(), key);

            if(dpIndex < 0) {
                System.out.println("Invalid Delete: Key unable to be found");
            } else {
                //successfully delete the dictionary pair
                leafNode.delete(dpIndex);

                //check for deficiencies
                if(leafNode.isDeficient()) {
                    LeafNode sibling;
                    InternalNode parent = leafNode.parent;

                    //Borrow: First check the left sibling and then the right sibling
                    if((leafNode.getLeftSibling() != null)  &&
                            (leafNode.getLeftSibling().parent == leafNode.parent)  &&
                            (leafNode.getLeftSibling().isLendable())) {
                        sibling = leafNode.getLeftSibling();
                        DictionaryPair borrowedDP = sibling.getDictionary()[sibling.getNumPairs() - 1];

                        //Insert borrowed dictionary pair
                        leafNode.insert(borrowedDP);

                        //sort dictionary
                        sortDictionary(leafNode.getDictionary());

                        //delete dictionary pair from sibling
                        sibling.delete(sibling.getNumPairs() - 1);

                        //Update key in parent if necessary
                        int pointerIndex = findIndexOfPointer(parent.getChildPointers(), leafNode);
                        if(!(borrowedDP.getKey() >= parent.getKeys()[pointerIndex - 1])) {
                            parent.getKeys()[pointerIndex - 1] = leafNode.getDictionary()[0].getKey();
                        }
                    }  else if((leafNode.getRightSibling() != null)  &&
                                    (leafNode.getRightSibling().parent == leafNode.parent)  &&
                                    (leafNode.getRightSibling().isLendable())) {

                        sibling = leafNode.getRightSibling();
                        DictionaryPair borrowedDP = sibling.getDictionary()[0];

                        //Insert borrowed dictionary pair
                        leafNode.insert(borrowedDP);

                        //delete dictionary pair from sibling
                        sibling.delete(0);

                        //sort dictionary
                        sortDictionary(sibling.getDictionary());

                        //Update key in parent if necessary
                        int pointerIndex = findIndexOfPointer(parent.getChildPointers(), leafNode);
                        if (!(borrowedDP.getKey() < parent.getKeys()[pointerIndex])) {
                            parent.getKeys()[pointerIndex] = sibling.getDictionary()[0].getKey();
                        }
                    }
                    // Merge: First check the left sibling, then the right sibling
                    else if ((leafNode.getLeftSibling() != null)  &&
                                    (leafNode.getLeftSibling().parent == leafNode.parent)  &&
                                    (leafNode.getLeftSibling().isMergeable())) {

                        sibling = leafNode.getLeftSibling();
                        int pointerIndex = findIndexOfPointer(parent.getChildPointers(), leafNode);

                        //remove key and child pointer from parent
                        parent.removeKey(pointerIndex - 1);
                        parent.removePointer(leafNode);

                        //update sibling pointer
                        sibling.setRightSibling(leafNode.getRightSibling());

                        //check for deficiencies in parent
                        if (parent.isDeficient()) {
                            handleDeficiency(parent);
                        }
                    } else if ((leafNode.getRightSibling() != null)  &&
                                    (leafNode.getRightSibling().parent == leafNode.parent)  &&
                                    (leafNode.getRightSibling().isMergeable())) {

                        sibling = leafNode.getRightSibling();
                        int pointerIndex = findIndexOfPointer(parent.getChildPointers(), leafNode);

                        //remove key and child pointer from parent
                        parent.removeKey(pointerIndex);
                        parent.removePointer(pointerIndex);

                        //update sibling pointer
                        sibling.setLeftSibling(leafNode.getLeftSibling());
                        if(sibling.getLeftSibling() == null) {
                            this.firstLeaf = sibling;
                        }

                        if(parent.isDeficient()){
                            handleDeficiency(parent);
                        }
                    }
                } else if((this.root == null)  &&  (this.firstLeaf.getNumPairs() == 0)) {
                    //deleted dictionary pair was the only pair within the tree
                    //set first leaf as null
                    this.firstLeaf = null;
                } else {
                    sortDictionary(leafNode.getDictionary());
                }
            }
        }

        // check if key is present in the internal nodes
        if(!isEmpty()) {
            Queue<Node> q = new LinkedList<>();
            if(this.root != null) {
                q.add(this.root);
            } else {
                q.add(this.firstLeaf);
            }
            while(!q.isEmpty()) {
                Node tempNode = q.poll();
                if(tempNode instanceof InternalNode) {
                    Integer[] keys = ((InternalNode) tempNode).getKeys();
                    Node[] pointers = ((InternalNode) tempNode).getChildPointers();

                    for(int i=0; i<keys.length; i++) {
                        if((keys[i] != null) && (key == keys[i])) {
                            keys[i] = successor;
                            break;
                        }
                    }
                    for(Node pt: pointers) {
                        if(pt != null) {
                            q.add(pt);
                        }
                    }
                }
            }
        }
    }
}
