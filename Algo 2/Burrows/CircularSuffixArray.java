import edu.princeton.cs.algs4.StdOut;

import java.util.Arrays;
import java.util.Comparator;

public class CircularSuffixArray {

    private final int length;
    private final Integer[] index;

    // circular suffix array of s
    public CircularSuffixArray(String s) {
        if (s == null) {
            throw new IllegalArgumentException();
        }
        length = s.length();
        index = new Integer[length];
        for (int i = 0; i < length; i++) {
            index[i] = i;
        }

        Comparator<Integer> cmp = (o1, o2) -> {
            int entry = o1;
            // skip equal characters
            while (s.charAt(o1) == s.charAt(o2)) {
                // compare next character, loop back when it goes over the last character
                o1 = (o1 + 1) % length;
                o2 = (o2 + 1) % length;
                // corner case: "AAA" and "AAA"
                // if come back to the entry, every character is the same
                if (o1 == entry) {
                    return 0;
                }
            }
            // sort according to the characters
            if (s.charAt(o1) < s.charAt(o2)) {
                return -1;
            }
            else {
                return 1;
            }
        };
        Arrays.sort(index, cmp);

    }

    // length of s
    public int length() {
        return length;
    }

    // returns index of ith sorted suffix
    public int index(int i) {
        if (i < 0 || i >= length) {
            throw new IllegalArgumentException();
        }
        return index[i];
    }

    // unit testing (required)
    public static void main(String[] args) {
        String original = "ABRACADABRA!";
        CircularSuffixArray csa = new CircularSuffixArray(original);
        for (int i = 0; i < csa.length(); i++) {
            StdOut.println(csa.index(i));
        }
    }
}
