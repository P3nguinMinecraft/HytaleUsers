package util;

public class Config {
    public static int MAX_LEN = 3;
    public static int THREADS = 1;
    public static long DELAY_MS = 5000;
    public static boolean PRINT_RESPONSE = false;
    public static String COOKIE = null;
    public static boolean RECHECK_EXISTING = false;

    public static final String API_BASE = "https://accounts.hytale.com/api/account/username-reservations/availability?username=";
    public static final int MIN_LEN = 3;

    public static final char[] CHARS = "abcdefghijklmnopqrstuvwxyz0123456789_".toCharArray();
    
}
