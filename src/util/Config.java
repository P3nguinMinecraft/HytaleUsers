package util;

public class Config {
    public static int MAX_LEN = 3;
    public static int THREADS = 1;
    public static long DELAY_MS = 5000;
    public static boolean PRINT_RESPONSE = false;
    public static String COOKIE = null;

    public static final String API_BASE = "https://accounts.hytale.com/api/account/username-reservations/availability?username=";
    public static final int MIN_LEN = 3;
    public static final String OUTPUT_FILE = "data/available_usernames.txt";
    public static final String CHECKPOINT_FILE = "data/checkpoint.txt";
    public static final String COOKIE_FILE = "data/cookie.txt";

    public static final char[] CHARS = "abcdefghijklmnopqrstuvwxyz0123456789_".toCharArray();
    
}
