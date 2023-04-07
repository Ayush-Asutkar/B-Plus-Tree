# B+ Tree Implementation

## Introduction to B+ Tree
B+ Tree is an extension of B Tree which allows efficient insertion, 
deletion and search operations. In B Tree, keys and records both can be stored in the internal as well as leaf nodes.
Whereas, in B+ tree, records (data) can only be stored on the leaf nodes while internal nodes can only store the key values.
The leaf nodes of a B+ Tree are linked together in the form of a singly linked list to make the search queries more efficient.<br/>

### Definition of B+ Tree:
A B+ Tree of order m has these properties:
* The root is either a leaf or has at least two children.
* Each internal node, except for the root, has between ceil(m/2) and m children.
* Internal nodes do not store record, only store key values to guild the search.
* Each leaf node, has between ceil(m/2) and m keys and values.
* Leaf node store keys and records or pointers to records.
* All leaves are at the same level in the tree, so the tree is always height balanced.

## Search Algorithm:
* Do binary search on keys in current node.
    * When current node is a leaf node:
      * If search key is found, then return records.
      * If search is not found, then report an unsuccessful search.
    * When current node is an internal node:
      * If search key < key_o, then repeat the search process on the first branch of current node.
      * If search key >= key_last, then repeat the search process on the last branch of current node.
      * Otherwise, find the first key_i >= key, and repeat the search process on the (i+1) branch of current node.

## Insertion Algorithm:
* Perform a search to determine which leaf node the new key should go into.
* It the node is not full, insert the new key, done!
* Otherwise, split the leaf node:
  * Allocate a new leaf node and move half keys new node.
  * Insert the new leaf's smallest key into the parent node.
  * If the parent is full, split it too, repeat the split process above until a parent is found that need not split.
  * If the root splits, create a new root which has one key and two children.

## Deletion Algorithm:
* Perform a search to determine which leaf node contains the key.
* Remove the key from the leaf node. 
* If the leaf node is at least half-full, done!
* If the leaf node as L is less than half-full:
  * Try to borrow a key from sibling node as S (adjacent node with same parent)
    * If S is L's left sibling, then borrow S's last key, and replace their parent navigate key with this borrowed key value.
  * If you can not borrow a key from sibling node, then merge L and sibling S
    * After merged L and S, delete their parent navigate key and proper child pointer.
    * Repeat the borrow or merge operation on parent node, perhaps propagate to root node and decrease the height of the tree.