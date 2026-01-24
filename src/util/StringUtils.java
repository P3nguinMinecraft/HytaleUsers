package util;

import java.util.HashMap;
import java.util.Map;

public class StringUtils {
	
    private static final Map<Character, Integer> CHAR_ORDER = new HashMap<>();

    static {
        for (int i = 0; i < Config.CHARS.length; i++) {
            CHAR_ORDER.put(Config.CHARS[i], i);
        }
    }

    /** Compare two strings using the custom order defined in CHARS */
    public static int compare(String s1, String s2) {
        int len = Math.min(s1.length(), s2.length());

        for (int i = 0; i < len; i++) {
            char c1 = s1.charAt(i);
            char c2 = s2.charAt(i);

            int idx1 = CHAR_ORDER.getOrDefault(c1, -1);
            int idx2 = CHAR_ORDER.getOrDefault(c2, -1);

            if (idx1 != idx2) return idx1 - idx2;
        }

        return s1.length() - s2.length();
    }
}
