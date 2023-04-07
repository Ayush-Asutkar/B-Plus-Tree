public class DictionaryPair implements Comparable<DictionaryPair> {

    private int key;
    private int value;

    public DictionaryPair(int key, int value) {
        this.key = key;
        this.value = value;
    }

    public int getKey() {
        return key;
    }

    public int getValue() {
        return value;
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
