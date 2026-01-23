package feature;

import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class UsernameGenerator implements Iterator<String> {
    private final char[] chars;
    private final int length;
    private final int[] indices;
    private boolean done = false;

    public UsernameGenerator(char[] chars, int length) {
        this.chars = chars;
        this.length = length;
        this.indices = new int[length];
        Arrays.fill(indices, 0);
    }

    @Override
    public boolean hasNext() {
        return !done;
    }

    @Override
    public String next() {
        if (done) throw new NoSuchElementException();

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(chars[indices[i]]);
        }

        for (int pos = length - 1; pos >= 0; pos--) {
            if (indices[pos] < chars.length - 1) {
                indices[pos]++;
                break;
            } else {
                indices[pos] = 0;
                if (pos == 0) done = true;
            }
        }

        return sb.toString();
    }
}
