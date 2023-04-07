import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        System.out.print("Order of b+ tree = ");
        int order = sc.nextInt();

        BPlusTree tree = new BPlusTree(order);

        tree.insert(5, 33);
        tree.insert(15, 21);
        tree.insert(25, 31);
        tree.insert(35, 41);
        tree.insert(45, 10);

        if (tree.search(15) != null) {
            System.out.println("Found");
        } else {
            System.out.println("Not found");
        }

    }
}