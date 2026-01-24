package util;

import java.io.*;
import java.util.ArrayList;
import java.util.Scanner;

public class IOManager {

    private static final File BASE_DIR;
    private static final File DATA_DIR;

    static {
        BASE_DIR = Paths.getJarDir();
        DATA_DIR = new File(BASE_DIR, "data");

        if (!DATA_DIR.exists()) {
            DATA_DIR.mkdirs();
        }
    }

    public static void saveCheckpoint(String username) {
    	File file = Config.RECHECK_EXISTING ? Paths.CHECKPOINT2_FILE : Paths.CHECKPOINT_FILE;
        try (BufferedWriter bw = new BufferedWriter(
                new FileWriter(file))) {

            bw.write(username != null ? username : "");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String readCheckpoint() {
    	File file = Config.RECHECK_EXISTING ? Paths.CHECKPOINT2_FILE : Paths.CHECKPOINT_FILE;
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
    
    public static void saveOutput(ArrayList<String> usernames) throws IOException {
        File tmp = new File(Paths.OUTPUT_FILE.getPath() + ".tmp");
        File out = Paths.OUTPUT_FILE;

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(tmp))) {
            for (String u : usernames) {
                bw.write(u);
                bw.newLine();
            }
            bw.flush();
        }

        if (out.exists()) out.delete();
        tmp.renameTo(out);
    }
    
    public static void appendOutput(String username) {
		try (BufferedWriter bw = new BufferedWriter(
				new FileWriter(Paths.OUTPUT_FILE, true))) {

			bw.write(username);
			bw.newLine();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
    
    public static ArrayList<String> readOutput() {
    	ArrayList<String> usernames = new ArrayList<>();
        if (!Paths.OUTPUT_FILE.exists()) return null;

        try (Scanner fileIn = new Scanner(Paths.OUTPUT_FILE)) {
            while (fileIn.hasNext()) {
            	String user = fileIn.next();
                usernames.add(user);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return usernames;
    }

    public static String readCookie() {
        File file = Paths.COOKIE_FILE;

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
            return (line != null && !line.isEmpty()) ? line : null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
