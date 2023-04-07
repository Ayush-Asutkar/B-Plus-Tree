public class Main {
    public static void main(String[] args) {
        int order = 3;
        BPlusTree tree = new BPlusTree(order);
        tree.insert(21, 0);
        tree.insert(108, 31);
        tree.insert(56089, 3);
        tree.insert(234, 121);
        tree.insert(4325, -109);
//        tree.delete (108);
        System.out.println(tree.search(234));
        tree.insert(102, 39);
        tree.insert(65, -3);
//        tree.delete (102);
//        tree.delete (21);
        tree.insert(106, -4);
        tree.insert(23, 3);
        System.out.println(tree.search(23, 99));
        tree.insert(32, 1);
        tree.insert(220, 5);
//        tree.delete (234);
        System.out.println(tree.search(65));

        if (tree.search(220) != null) {
            System.out.println("Found");
        } else {
            System.out.println("Not found");
        }

//        DictionaryPair[] dp = new DictionaryPair[5];
//        dp[0] = new DictionaryPair(1, 100);
//        dp[1] = new DictionaryPair(3, 300);
//        dp[2] = new DictionaryPair(5, 500);
//        dp[3] = new DictionaryPair(4, 400);
//        dp[4] = new DictionaryPair(2, 200);
//        BPlusTree.sortDictionary(dp);
//
//        for(DictionaryPair dps: dp) {
//            System.out.println(dps.getKey() + "->" + dps.getValue());
//        }
    }
}