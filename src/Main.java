import java.util.ArrayList;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {

        System.out.println("\t**Welcome to DATABASE SERVER**\n");

        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter the order of B+ Tree: ");
        int order = scanner.nextInt();

        BPlusTree tree = new BPlusTree(order);

        boolean flag = true;
        do {
            System.out.println("\nPlease provide the queries with respective keys :");
            printOptions();

            int option = scanner.nextInt();
            switch (option) {
                case 1:
                    insertionMethod(tree, scanner);
                    break;

                case 2:
                    searchKeyMethod(tree, scanner);
                    break;

                case 3:
                    searchRangeMethod(tree, scanner);
                    break;

                case 4:
                    printTreeMethod(tree);
                    break;

                case 5:
                    deleteMethod(tree, scanner);
                    break;

                default:
                    flag = false;
                    break;
            }
        } while (flag);
    }

    private static void deleteMethod(BPlusTree tree, Scanner scanner) {
        System.out.print("Provide the key which is to be deleted: ");
        int key = scanner.nextInt();

        System.out.println("Deleting " + key);
        tree.delete(key);
    }

    private static void printTreeMethod(BPlusTree tree) {
        tree.printLevelOrder();
    }

    private static void searchRangeMethod(BPlusTree tree, Scanner scanner) {
        System.out.print("Provide the lower bound: ");
        int lowerBound = scanner.nextInt();

        System.out.print("Provide the upper bound: ");
        int upperBound = scanner.nextInt();

        ArrayList<Double> result = tree.search(lowerBound, upperBound);
        System.out.println("Result of " + lowerBound + " - " + upperBound + " = " + result);
    }

    private static void searchKeyMethod(BPlusTree tree, Scanner scanner) {
        System.out.print("Enter the key to be searched: ");
        int key = scanner.nextInt();
        Double value = tree.search(key);
        if (value == null) {
            System.out.println("The value does not exist in the tree");
        } else {
            System.out.println("The value is " + value + ", for the key " + key);
        }
    }

    private static void insertionMethod(BPlusTree tree, Scanner scanner) {
        System.out.print("Provide the key: ");
        int key = scanner.nextInt();

        System.out.print("Provide the value: ");
        double value = scanner.nextDouble();

        System.out.println("Inserting (" + key + ", " + value + ") pair...");
        boolean insertion = tree.insert(key, value);

        if(insertion) {
            System.out.println("Insertion successful!");
        } else {
            System.out.println("Primary key constraint violated. Entered value already exists.");
        }
    }

    private static void printOptions() {
        System.out.println("\tPress 1: Insertion" +
                "\n\tPress 2: Search a key" +
                "\n\tPress 3: Search on a range" +
                "\n\tPress 4: Print the tree in lever order traversal" +
                "\n\tPress 5: Delete key in the tree" +
                "\n\tPress 6: ABORT!");
    }
}