package util;

import java.io.File;
import java.net.URISyntaxException;

public class Paths {
	
	public static final File DATA_DIR = folder("data");
    public static final File OUTPUT_FILE = file("data/available_usernames.txt");
    public static final File ALPHABETIC_FILE = file("data/alphabetic_usernames.txt");
    public static final File ALPHA_UNDERSCORE_FILE = file("data/alpha_underscore_usernames.txt");
    public static final File CHECKPOINT_FILE = file("data/checkpoint.txt");
    public static final File CHECKPOINT2_FILE = file("data/checkpoint2.txt");
    public static final File COOKIE_FILE = file("data/cookie.txt");
	
    public static File getJarDir() {
        try {
            return new File(
                Paths.class
                    .getProtectionDomain()
                    .getCodeSource()
                    .getLocation()
                    .toURI()
            ).getParentFile();
        } catch (URISyntaxException e) {
            throw new RuntimeException("Unable to resolve JAR directory", e);
        }
    }
    
    	public static File folder(String foldername) {
    		File f = new File(getJarDir(), foldername);
    		if (!f.exists()) {
				f.mkdirs();
			}
    		return f;
    	}
    
    public static File file(String filename) {
		File f = new File(getJarDir(), filename);
		if (!f.exists()) {
			try {
				f.createNewFile();
			} catch (Exception e) {
				e.printStackTrace();
			}
			return f;
		}
		else {
			return f;
		}
	}
}
