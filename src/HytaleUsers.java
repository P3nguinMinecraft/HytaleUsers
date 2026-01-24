
import java.io.IOException;

import feature.Analysis;
import feature.SearchManager;
import util.Config;
import util.IOManager;

public class HytaleUsers {
    public static void main(String[] args) {
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "--threads", "-t" -> {
                    if (i + 1 < args.length) Config.THREADS = Integer.parseInt(args[++i]);
                }
                case "--max", "-m" -> {
                    if (i + 1 < args.length) Config.MAX_LEN = Integer.parseInt(args[++i]);
                }
                case "--delay", "-d" -> {
                    if (i + 1 < args.length) Config.DELAY_MS = Long.parseLong(args[++i]);
                }
                case "--print", "-p" -> {
                    Config.PRINT_RESPONSE = true;
                }
                case "--check", "-c" -> {
					Config.RECHECK_EXISTING = true;
				}
                case "--analysis", "-a" -> {
					try {
						Analysis.alphabeticAnalysis();
						Analysis.alphaUnderscoreAnalysis();
					} catch (IOException e) {
						e.printStackTrace();
					}
					System.exit(0);
				}
                case "--help", "-h" -> {
                    printHelp();
                    System.exit(0);
                }
                default -> System.out.println("[WARN] Unknown argument: " + args[i]);
            }
        }

        Config.COOKIE = IOManager.readCookie();
        
        if (Config.COOKIE == null) {
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

        if (Config.THREADS * 1000 / Config.DELAY_MS > 3) {
            System.err.println("[ERROR] Too many requests per second. You will get rate limited by Cloudflare.");
            System.exit(0);
            return;
        }

        SearchManager.init();
    }
    
    private static void printHelp() {
        System.out.println("""
        HytaleUsers 1.1.0
        Small Java project that can be used to locate all available Hytale usernames
    
        Usage:
          java HytaleUsers [options]
    
        Options:
          -t, --threads <n>   Number of concurrent threads (default: 1)
          -m, --max <n>       Maximum username length to check (default: 3)
          -d, --delay <ms>    Delay between batches in milliseconds (default: 5000)
          -p, --print         Print raw API responses
          -c, --check         Re-check existing usernames in the output file
          -a, --analysis      Perform analysis on found usernames
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
}
