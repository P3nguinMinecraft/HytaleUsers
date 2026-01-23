package util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class FileManager {
	static {
		File dir = new File("data");
        if (!dir.exists()) {
            dir.mkdirs();
        }
	}
	
	public static void saveCheckpoint(String username) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(Config.CHECKPOINT_FILE))) {
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

    public static String readCheckpoint() {
        File file = new File(Config.CHECKPOINT_FILE);
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
    
    public static String readCookie() {
	    File file = new File(Config.COOKIE_FILE);
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
}
