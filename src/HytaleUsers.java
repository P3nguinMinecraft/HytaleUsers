import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class HytaleUsers {
	
    // ================= CONFIG =================
    private static int MAX_LEN = 3;
    private static int THREADS = 1;
    private static long DELAY_MS = 5000;
    private static boolean PRINT_RESPONSE = false;

    private static final String API_BASE = "https://accounts.hytale.com/api/account/username-reservations/availability?username=";
    private static final int MIN_LEN = 3;
    private static final String OUTPUT_FILE = "data/available_usernames.txt";
    private static final String CHECKPOINT_FILE = "data/checkpoint.txt";
    private static final String COOKIE_FILE = "data/cookie.txt";

    private static final char[] CHARS = "abcdefghijklmnopqrstuvwxyz0123456789_".toCharArray();
    private static String COOKIE;
    // ==========================================

    @SuppressWarnings("CallToPrintStackTrace")
    public static void main(String[] args) {
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "--threads", "-t" -> {
                    if (i + 1 < args.length) THREADS = Integer.parseInt(args[++i]);
                }
                case "--max", "-m" -> {
                    if (i + 1 < args.length) MAX_LEN = Integer.parseInt(args[++i]);
                }
                case "--delay", "-d" -> {
                    if (i + 1 < args.length) DELAY_MS = Long.parseLong(args[++i]);
                }
                case "--print", "-p" -> {
                    PRINT_RESPONSE = true;
                }
                case "--help", "-h" -> {
                    printHelp();
                    System.exit(0);
                }
                default -> System.out.println("[WARN] Unknown argument: " + args[i]);
            }
        }

        String lastCheckpoint = readCheckpoint();
        if (COOKIE == null) {
            System.err.println("""
            [SETUP REQUIRED]
            A valid session cookie is required.

            1. Log in to Hytale.com (you must have an account already)
            2. Go to https://accounts.hytale.com/api/account/username-reservations/availability?username=aaa
            3. Open Chrome Dev Tools -> Network Tab
            4. Reload the page
            5. Find the availability request
            6. Scroll down to the cookie section and find the cookie:
            ory_kratos_session=XXXXXXXXXX;
            
            7. Place your cookie value in:
            data/cookie.txt
            
            Paste only the cookie value

            Correct:
            abcd1234efgh5678aslanb=

            Incorrect:
            ory_kratos_session=abcd1234efgh5678aslanb=;
            """);
            System.exit(0);
            return;
        }
        
        File dir = new File("data");
        if (!dir.exists()) {
            dir.mkdirs();
        }

        COOKIE = readCookie();

        if (THREADS * 1000 / DELAY_MS > 3) {
            System.err.println("[ERROR] Too many requests per second. You will get rate limited by Cloudflare.");
            System.exit(0);
            return;
        }

        ExecutorService executor = Executors.newFixedThreadPool(THREADS);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(OUTPUT_FILE, true))) {
            int checked = 0;
            int found = 0;
            long startTime = System.currentTimeMillis();
            boolean skip = lastCheckpoint != null;
        
            for (int len = MIN_LEN; len <= MAX_LEN; len++) {
                Iterator<String> generator = new UsernameGenerator(CHARS, len);
        
                List<String> batch = new ArrayList<>();
        
                while (generator.hasNext()) {
                    String username = generator.next();

                    if (skip) {
                        if (username.equals(lastCheckpoint)) skip = false;
                        continue;
                    }
                    
                    batch.add(username);

                    if (batch.size() >= THREADS || !generator.hasNext()) {
                        List<Future<Boolean>> futures = executor.invokeAll(
                            batch.stream().map(u -> (Callable<Boolean>) () -> checkUsername(u)).toList()
                        );

                        for (int i = 0; i < batch.size(); i++) {
                            Boolean available = null;
                            try {
                                available = futures.get(i).get();
                            } catch (InterruptedException | ExecutionException e) {
                                e.printStackTrace();
                            }

                            String u = batch.get(i);
                            checked++;

                            if (available != null && available == true) {
                                writer.write(u);
                                writer.newLine();
                                writer.flush();
                                found++;
                                System.out.println("[FOUND] " + u);
                            }
                            else if (available == null) {
                                System.err.println("[ERROR] API error for username: " + u);
                                for (Future<Boolean> f : futures) {
                                    f.cancel(true);
                                }
                                executor.shutdownNow();
                                System.exit(1);
                                return;
                            }
                        }

                        saveCheckpoint(batch.get(batch.size() - 1));

                        if (checked % 50 == 0) {
                            long elapsed = System.currentTimeMillis() - startTime;
                            double rate = checked / (elapsed / 1000.0);
                            System.out.println("Checked: " + checked + " | Found: " + found + " | Rate: " + String.format("%.2f", rate) + "/s");
                        }
                        batch.clear();
                        Thread.sleep(DELAY_MS);
                    }
                }
            }

            saveCheckpoint(null);
            executor.shutdown();
        
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    // ================= UTILS =================
    private static Boolean checkUsername(String username) {
        try {
            URI uri = new URI(API_BASE + username);
            HttpURLConnection conn = (HttpURLConnection) uri.toURL().openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("User-Agent",
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) " +
                "AppleWebKit/537.36 (KHTML, like Gecko) " +
                "Chrome/117.0.0.0 Safari/537.36");
            conn.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
            conn.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
            conn.setRequestProperty("Cookie", "ory_kratos_session=" + COOKIE);
                    
            int status = conn.getResponseCode();
            boolean available = status >= 200 && status < 300;
            String response = read(new InputStreamReader(available ? conn.getInputStream() : conn.getErrorStream()));

            if (response.equals("error code: 1015")) {
                System.out.println("[CF] Rate limited - ERROR 1015");
                return null;
            }
            if (PRINT_RESPONSE && !response.isEmpty()) {
                System.out.println(response);
            }
            return available;

        } 
        catch (IOException | URISyntaxException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static String read(InputStreamReader isr) throws IOException {
        BufferedReader reader = new BufferedReader(isr);
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            response.append(line);
        }
        reader.close();

        return response.toString();
    }

    @SuppressWarnings("CallToPrintStackTrace")
    private static void saveCheckpoint(String username) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(CHECKPOINT_FILE))) {
            if (username != null) {
                bw.write(username);
                bw.close();
            }
            else {
                bw.write("");
                bw.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("CallToPrintStackTrace")
    private static String readCheckpoint() {
        File file = new File(CHECKPOINT_FILE);
        if (!file.exists()) return null;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line = br.readLine();
            if (line != null && !line.isEmpty()) {
                System.out.println("[RESUME] Starting from last checkpoint: " + line);
                return line;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @SuppressWarnings("CallToPrintStackTrace")
    private static String readCookie() {
        File file = new File(COOKIE_FILE);
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line = br.readLine();
            if (line != null && !line.isEmpty()) {
                return line;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    private static void printHelp() {
        System.out.println("""
        HytaleUsers - Username availability checker
    
        Usage:
          java HytaleUsers [options]
    
        Options:
          -t, --threads <n>   Number of concurrent threads (default: 1)
          -m, --max <n>       Maximum username length to check (default: 3)
          -d, --delay <ms>    Delay between batches in milliseconds (default: 5000)
          -p, --print         Print raw API responses
          -h, --help          Show this help message and exit
    
        Notes:
          - A valid Cloudflare session cookie must be placed in:
              data/cookie.txt
    
          - Checkpoints are stored in:
              data/checkpoint.txt
    
          - Results are appended to:
              data/available_usernames.txt
    
          - Exits immediately on Cloudflare rate-limit (error 1015)
          - Automatically resumes from last checkpoint
        """);
    }    
    
    static class UsernameGenerator implements Iterator<String> {
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
}
