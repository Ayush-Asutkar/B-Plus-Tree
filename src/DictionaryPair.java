public class DictionaryPair implements Comparable<DictionaryPair> {

    private int key;
    private double value;

    public DictionaryPair(int key, int value) {
        this.key = key;
        this.value = value;
    }

    //helpful for sorting
    @Override
    public int compareTo(DictionaryPair o) {
        if (this.key == o.key) {
            return 0;
        } else if(this.key > o.key) {
            return 1;
        } else {
            return -1;
        }
    }
}
