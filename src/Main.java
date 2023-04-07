import java.util.ArrayList;
import java.util.Collections;

public class Main {
    public static void main(String[] args) {
//        Scanner sc = new Scanner(System.in);
//
//        System.out.print("Order of b+ tree = ");
//        int order = sc.nextInt();
//
//        BPlusTree tree = new BPlusTree(order);
//
//        tree.insert(5, 33);
//        tree.insert(15, 21);
//        tree.insert(25, 31);
//        tree.insert(35, 41);
//        tree.insert(45, 10);

//        if (tree.search(15) != null) {
//            System.out.println("Found");
//        } else {
//            System.out.println("Not found");
//        }


        ArrayList<DictionaryPair> list = new ArrayList<>();
        list.add(new DictionaryPair(4, 5));
        list.add(new DictionaryPair(6, 5));
        list.add(new DictionaryPair(2, 5));
        list.add(new DictionaryPair(210, 5));
        list.add(new DictionaryPair(1, 5));
        Collections.sort(list);
        for(DictionaryPair dp: list) {
            System.out.println(dp.getKey());
        }
    }
}