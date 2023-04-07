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

        int index;

        for(int i=0; i < this.root.)
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

            this.firstLeaf = leafNode;
        } else {
            // find the leaf node to insert into
            LeafNode leafNode;
            if(this.root == null) {
                leafNode = this.firstLeaf;
            } else {
                leafNode = findLeafNode(key);
            }

            // TODO: 07-04-2023 Complete find leaf node and get back here
        }
    }
}
