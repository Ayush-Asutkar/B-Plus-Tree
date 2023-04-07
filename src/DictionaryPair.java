public class DictionaryPair implements Comparable<DictionaryPair> {

    private int key;
    private double value;

    public DictionaryPair(int key, double value) {
        this.key = key;
        this.value = value;
    }

    public int getKey() {
        return key;
    }

    public double getValue() {
        return value;
    }

    //helpful for sorting
    @Override
    public int compareTo(DictionaryPair o) {
        return Integer.compare(this.key, o.key);
    }
}
