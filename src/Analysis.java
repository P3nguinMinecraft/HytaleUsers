import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Scanner;

public class Analysis {
	private String OUTPUT_FILE;

	public Analysis(String OUTPUT_FILE) {
		this.OUTPUT_FILE = OUTPUT_FILE;
	}
	
	public void alphabeticAnalysis() throws FileNotFoundException {
		ArrayList<String> availableUsernames = readOutput();
		if (availableUsernames == null || availableUsernames.isEmpty()) {
			System.out.println("No available usernames found.");
			return;
		}

		PrintWriter alphabetic = new PrintWriter(new File("data/alphabetic_usernames.txt"));
		for (String username : availableUsernames) {
			if (isAlphabetic(username)) {
				System.out.println("[ALPHA] " + username);
				alphabetic.println(username);
			}
		}
		alphabetic.close();
	}
	
	public void alphaUnderscoreAnalysis() throws FileNotFoundException {
		ArrayList<String> availableUsernames = readOutput();
		if (availableUsernames == null || availableUsernames.isEmpty()) {
			System.out.println("No available usernames found.");
			return;
		}
		
		PrintWriter alphaunderscore = new PrintWriter(new File("data/alphaunderscore_usernames.txt"));
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
    
    private ArrayList<String> readOutput() {
    	ArrayList<String> usernames = new ArrayList<>();
        File file = new File(this.OUTPUT_FILE);
        if (!file.exists()) return null;

        try (Scanner fileIn = new Scanner(file)) {
            while (fileIn.hasNext()) {
            	String user = fileIn.next();
                usernames.add(user);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return usernames;
    }
}
