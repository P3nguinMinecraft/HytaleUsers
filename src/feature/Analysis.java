package feature;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import util.IOManager;
import util.Paths;

public class Analysis {
	public static void alphabeticAnalysis() throws FileNotFoundException {
		ArrayList<String> availableUsernames = IOManager.readOutput();
		if (availableUsernames == null || availableUsernames.isEmpty()) {
			System.out.println("No available usernames found.");
			return;
		}

		PrintWriter alphabetic = new PrintWriter(Paths.ALPHABETIC_FILE);
		for (String username : availableUsernames) {
			if (isAlphabetic(username)) {
				System.out.println("[ALPHA] " + username);
				alphabetic.println(username);
			}
		}
		alphabetic.close();
	}
	
	public static void alphaUnderscoreAnalysis() throws FileNotFoundException {
		ArrayList<String> availableUsernames = IOManager.readOutput();
		if (availableUsernames == null || availableUsernames.isEmpty()) {
			System.out.println("No available usernames found.");
			return;
		}
		
		PrintWriter alphaunderscore = new PrintWriter(Paths.ALPHA_UNDERSCORE_FILE);
		for (String username : availableUsernames) {
			if (isAlphaUnderscore(username)) {
				System.out.println("[ALPHAUNDERSCORE] " + username);
				alphaunderscore.println(username);
			}
		}
		alphaunderscore.close();
	}
    
    private static boolean isAlphabetic(String username) {
		for (char c : username.toCharArray()) {
			if (!Character.isAlphabetic(c)) {
				return false;
			}
		}
		return true;
    }
    
    private static boolean isAlphaUnderscore(String username) {
		for (char c : username.toCharArray()) {
			if (!Character.isAlphabetic(c) && c != '_') {
				return false;
			}
		}
		if (isAlphabetic(username)) {
			return false;
		}
		return true;
    }
}
